package org.multibit.hd.core.network;

import com.google.common.base.Optional;
import org.bitcoinj.core.*;
import org.multibit.hd.core.dto.BitcoinNetworkSummary;
import org.multibit.hd.core.dto.WalletSummary;
import org.multibit.hd.core.events.CoreEvents;
import org.multibit.hd.core.events.TransactionSeenEvent;
import org.multibit.hd.core.managers.WalletManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MultiBitPeerEventListener implements PeerEventListener {

  private static final Logger log = LoggerFactory.getLogger(MultiBitPeerEventListener.class);

  private int originalBlocksLeft = -1;
  private int lastPercent = 0;

  private int numberOfConnectedPeers = 0;

  private boolean isDownloading = false;

  public MultiBitPeerEventListener() {
  }

  @Override
  public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
    // Do nothing - this is a list of potential peers to connect to, not actually connected peers
  }

  @Override
  public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
    if (blocksLeft > originalBlocksLeft) {
      originalBlocksLeft = blocksLeft;
    }

    //log.debug("Number of blocks left: {}, originalBlocksLeft: {}", blocksLeft, originalBlocksLeft);

    if (blocksLeft < 0 || originalBlocksLeft <= 0) {
      isDownloading = false;
      return;
    }

    isDownloading = blocksLeft > 0;

    double pct = 100.0 - (100.0 * (blocksLeft / (double) originalBlocksLeft));
    if ((int) pct != lastPercent) {
      if (block != null) {
        progress(pct, blocksLeft, new Date(block.getTimeSeconds() * 1000));
      }
      lastPercent = (int) pct;

      // Fire the download percentage when it changes
      CoreEvents.fireBitcoinNetworkChangedEvent(BitcoinNetworkSummary.newChainDownloadProgress(lastPercent, blocksLeft));
    }

    if (blocksLeft == 0) {
      doneDownload();
    }
  }

  @Override
  public void onChainDownloadStarted(Peer peer, int blocksLeft) {
    log.trace("Chain download started with number of blocks left = {}", blocksLeft);

    isDownloading = blocksLeft > 0;

    startDownload(blocksLeft);
    // Only mark this the first time, because this method can be called more than once during a chain download
    // if we switch peers during it.
    if (originalBlocksLeft == -1) {
      originalBlocksLeft = blocksLeft;
    } else {
      log.info("Chain download switched to {}", peer);
    }

    CoreEvents.fireBitcoinNetworkChangedEvent(BitcoinNetworkSummary.newChainDownloadProgress(lastPercent, blocksLeft));

    if (blocksLeft == 0) {
      doneDownload();
    }
  }

  @Override
  public void onPeerConnected(Peer peer, int peerCount) {
    log.trace("(connect) Number of peers = " + peerCount + ", lastPercent = " + lastPercent);

    numberOfConnectedPeers = peerCount;

    CoreEvents.fireBitcoinNetworkChangedEvent(
            BitcoinNetworkSummary.newNetworkPeerCount(numberOfConnectedPeers));
  }

  @Override
  public void onPeerDisconnected(Peer peer, int peerCount) {
    log.trace("(disconnect) Number of peers = " + peerCount);
    if (peerCount == numberOfConnectedPeers) {
      // Don't fire an event - not useful
      return;
    }
    numberOfConnectedPeers = peerCount;

    CoreEvents.fireBitcoinNetworkChangedEvent(
      BitcoinNetworkSummary.newNetworkPeerCount(numberOfConnectedPeers));
  }

  @Override
  public Message onPreMessageReceived(Peer peer, Message message) {
    return message;
  }

  @Override
  public void onTransaction(Peer peer, Transaction transaction) {
    // See if the transaction is relevant and adding them as pending if so.
    if (transaction != null) {
      Optional<WalletSummary> currentWalletSummary = WalletManager.INSTANCE.getCurrentWalletSummary();
      if (currentWalletSummary.isPresent() && currentWalletSummary.get() != null) {
        Wallet currentWallet = currentWalletSummary.get().getWallet();
        if (currentWallet != null) {
          try {
            if (currentWallet.isTransactionRelevant(transaction)) {
              log.debug("Relevant transaction {} has been seen by peer {}", transaction.getHashAsString(), peer.getAddress());

              if (!(transaction.isTimeLocked() && transaction.getConfidence().getSource() != TransactionConfidence.Source.SELF)) {
                Sha256Hash transactionHash = transaction.getHash();
                if (currentWallet.getTransaction(transactionHash) == null) {
                  int transactionIdentityHashCode = System.identityHashCode(transaction);

                  log.debug(
                    "MultiBitHD adding a new pending transaction for the wallet '{}'\n{}",
                    currentWalletSummary.get().getWalletId(),
                    transaction.toString()
                  );

                  try {
                    // If this transaction is zero confirmations, add it to the wallet
                    if (transaction.isPending()) {
                      currentWallet.receivePending(transaction, null);

                      // Emit an event so that GUI elements can update as required
                      Coin value = transaction.getValue(currentWallet);
                      TransactionSeenEvent transactionSeenEvent = new TransactionSeenEvent(transaction, value);

                      // Check this is the first time we have seen this transaction

                      // Check the transaction in the wallet is the same object we put in
                      // If it is different then some other thread put this tx in so this one is not the first.
                      // (Probably this tx was reported by another peer)
                      Transaction transactionInWallet = currentWallet.getTransaction(transactionHash);
                      if (transactionInWallet != null) {
                        int transactionInWalletIdentityHashCode = System.identityHashCode(transactionInWallet);
                        if (transactionIdentityHashCode == transactionInWalletIdentityHashCode) {
                          // This is the first time we have seen this transaction
                          transactionSeenEvent.setFirstAppearanceInWallet(true);
                          CoreEvents.fireTransactionSeenEvent(transactionSeenEvent);
                          log.debug("Firing transaction seen event {}", transactionSeenEvent);
                        }
                      }
                    }
                  } catch (IllegalStateException e) {
                    log.warn("Illegal state receiving pending transaction", e);
                    // Carry on regardless to give user confidence that something happened
                  }
                }
              }
            }
          } catch (ScriptException se) {
            // Cannot understand this transaction - carry on
          }
        }
      }
    }
  }

  @Override
  public List<Message> getData(Peer peer, GetDataMessage m) {
    return null;
  }

  /**
   * Called when download progress is made.
   *
   * @param pct  the percentage of chain downloaded, estimated
   * @param date the date of the last block downloaded
   */
  protected void progress(double pct, int blocksSoFar, Date date) {

    // Logging this information in production is not necessary
    log.trace(
      "Chain download {}% done with {} blocks to go, block date {}",
      (int) pct,
      blocksSoFar,
      DateFormat.getDateTimeInstance().format(date)
    );
  }

  /**
   * Called when download is initiated.
   *
   * @param blocks the number of blocks to download, estimated
   */
  protected void startDownload(int blocks) {
    isDownloading = blocks > 0;

    log.info("Started download with {} blocks to download", blocks);
    CoreEvents.fireBitcoinNetworkChangedEvent(BitcoinNetworkSummary.newChainDownloadStarted());

  }

  /**
   * Called when we are done downloading the block chain.
   */
  protected void doneDownload() {
    log.info("Download of block chain complete");
    isDownloading = false;

    // Fire that we have completed the sync
    lastPercent = 100;
    originalBlocksLeft = -1; // Clear for next sync
    CoreEvents.fireBitcoinNetworkChangedEvent(BitcoinNetworkSummary.newChainDownloadProgress(100, 0));

    // Used to indicate sync has finished
    CoreEvents.fireBitcoinNetworkChangedEvent(BitcoinNetworkSummary.newChainDownloadCompleted());

    // Then fire the number of connected peers
    CoreEvents.fireBitcoinNetworkChangedEvent(
      BitcoinNetworkSummary.newNetworkPeerCount(numberOfConnectedPeers));
  }

  public boolean isDownloading() {
    return isDownloading;
  }
}

