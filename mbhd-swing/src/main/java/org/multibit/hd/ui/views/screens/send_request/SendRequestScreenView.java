package org.multibit.hd.ui.views.screens.send_request;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import org.multibit.commons.concurrent.SafeExecutors;
import org.multibit.hd.core.dto.BitcoinNetworkSummary;
import org.multibit.hd.core.dto.PaymentData;
import org.multibit.hd.core.dto.PaymentType;
import org.multibit.hd.core.events.BitcoinNetworkChangedEvent;
import org.multibit.hd.core.events.ExchangeRateChangedEvent;
import org.multibit.hd.core.events.SlowTransactionSeenEvent;
import org.multibit.hd.core.events.TransactionCreationEvent;
import org.multibit.hd.core.managers.InstallationManager;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.core.services.WalletService;
import org.multibit.hd.ui.MultiBitUI;
import org.multibit.hd.ui.events.view.WalletDetailChangedEvent;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.*;
import org.multibit.hd.ui.views.components.display_payments.DisplayPaymentsModel;
import org.multibit.hd.ui.views.components.display_payments.DisplayPaymentsView;
import org.multibit.hd.ui.views.screens.AbstractScreenView;
import org.multibit.hd.ui.views.screens.Screen;
import org.multibit.hd.ui.views.themes.Themes;
import org.multibit.hd.ui.views.wizards.Wizards;
import org.multibit.hd.ui.views.wizards.send_bitcoin.SendBitcoinParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * <p>View to provide the following to application:</p>
 * <ul>
 * <li>Provision of components and layout for the wallet detail display</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class SendRequestScreenView extends AbstractScreenView<SendRequestScreenModel> {

  private static final Logger log = LoggerFactory.getLogger(SendRequestScreenView.class);

  private JButton sendBitcoin;
  private JButton requestBitcoin;
  private JButton stakeBlackcoin;

  private ModelAndView<DisplayPaymentsModel, DisplayPaymentsView> displaySendingPaymentsMaV;

  private ModelAndView<DisplayPaymentsModel, DisplayPaymentsView> displayRequestedPaymentsMaV;

  /**
   * Handles update operations
   */
  private static final ExecutorService executorService = SafeExecutors.newSingleThreadExecutor("send-request-update-service");

  /**
   * @param panelModel The model backing this panel view
   * @param screen     The screen to filter events from components
   * @param title      The key to the main title of this panel view
   */
  public SendRequestScreenView(SendRequestScreenModel panelModel, Screen screen, MessageKey title) {
    super(panelModel, screen, title);
  }

  @Override
  public void newScreenModel() {

  }

  @Override
  public JPanel initialiseScreenViewPanel() {

    MigLayout layout = new MigLayout(
            Panels.migXYDetailLayout(),
            "[]10[10]10[]", // Column constraints
            "1[]20[]10[]" // Row constraints
    );

    JPanel contentPanel = Panels.newPanel(layout);

    Action showSendBitcoinWizardAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        SendBitcoinParameter parameter = new SendBitcoinParameter(null, null);

        Panels.showLightBox(Wizards.newSendBitcoinWizard(parameter).getWizardScreenHolder());
      }
    };

    Action showRequestBitcoinWizardAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newRequestBitcoinWizard().getWizardScreenHolder());
      }
    };
    
    Action stakeBlackCoinWizardAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {

          Panels.showLightBox(Wizards.newStakeBitcoinWizard().getWizardScreenHolder());
        }
      };

    sendBitcoin = Buttons.newSendBitcoinWizardButton(showSendBitcoinWizardAction);

    requestBitcoin = Buttons.newRequestBitcoinWizardButton(showRequestBitcoinWizardAction);
    
    stakeBlackcoin = Buttons.newStakeBlackcoinWizardButton(stakeBlackCoinWizardAction);

    if (InstallationManager.unrestricted) {
      // Start with enabled buttons and use Bitcoin network status to modify
      sendBitcoin.setEnabled(true);
      requestBitcoin.setEnabled(true);
      stakeBlackcoin.setEnabled(true);
    } else {
      // Start with disabled button and use Bitcoin network status to modify
      sendBitcoin.setEnabled(false);
      requestBitcoin.setEnabled(false);
      stakeBlackcoin.setEnabled(false);
    }

    // Initialise panel with a blank list of today's sending payments
    List<PaymentData> todaysSendingPayments = Lists.newArrayList();
    displaySendingPaymentsMaV = Components.newDisplayPaymentsMaV(getScreen().name() + "_SENDING");
    displaySendingPaymentsMaV.getModel().setValue(todaysSendingPayments);

    JScrollPane sendingPaymentsScrollPane = new JScrollPane(displaySendingPaymentsMaV.getView().newComponentPanel(),
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sendingPaymentsScrollPane.setBackground(Themes.currentTheme.detailPanelBackground());
    sendingPaymentsScrollPane.getViewport().setBackground(Themes.currentTheme.detailPanelBackground());
    sendingPaymentsScrollPane.setOpaque(true);
    sendingPaymentsScrollPane.setBorder(BorderFactory.createEmptyBorder());

    // Ensure we maintain the overall theme
    ScrollBarUIDecorator.apply(sendingPaymentsScrollPane, true);

    // Initialise panel with a blank list of today's requested payments
    List<PaymentData> todaysRequestedPayments = Lists.newArrayList(); //walletService.subsetPaymentsAndSort(allPayments, PaymentType.RECEIVING);
    displayRequestedPaymentsMaV = Components.newDisplayPaymentsMaV(getScreen().name() + "_REQUESTED");
    displayRequestedPaymentsMaV.getModel().setValue(todaysRequestedPayments);

    JScrollPane requestingPaymentsScrollPane = new JScrollPane(displayRequestedPaymentsMaV.getView().newComponentPanel(),
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    requestingPaymentsScrollPane.getViewport().setBackground(Themes.currentTheme.detailPanelBackground());
    requestingPaymentsScrollPane.setOpaque(true);
    requestingPaymentsScrollPane.setBorder(BorderFactory.createEmptyBorder());

    // Ensure we maintain the overall theme
    ScrollBarUIDecorator.apply(requestingPaymentsScrollPane, true);

    contentPanel.add(Labels.newBlankLabel(), "width 47%,pushx");
    contentPanel.add(Labels.newBlankLabel(), "width 6%,pushx");
    contentPanel.add(Labels.newBlankLabel(), "width 47%,pushx,wrap");

    contentPanel.add(sendBitcoin, MultiBitUI.LARGE_BUTTON_MIG + ",align center");
    contentPanel.add(stakeBlackcoin, MultiBitUI.LARGE_BUTTON_MIG + ",align center");
    contentPanel.add(Panels.newVerticalDashedSeparator(), "growy,spany 2");
    contentPanel.add(requestBitcoin, MultiBitUI.LARGE_BUTTON_MIG + ",align center,wrap");
    
    contentPanel.add(sendingPaymentsScrollPane, "grow,push");
    contentPanel.add(requestingPaymentsScrollPane, "grow,push,wrap");

    return contentPanel;
  }

  @Override
  public boolean beforeShow() {
    // Ensure the send / request buttons are kept up to date
    Optional<BitcoinNetworkChangedEvent> changedEvent = CoreServices.getApplicationEventService().getLatestBitcoinNetworkChangedEvent();
    if (changedEvent.isPresent()) {
      updateSendRequestButtons(changedEvent.get());
    }
    return true;
  }

  @Override
  public void afterShow() {
    update();
  }

  /**
   * @param event The "Bitcoin network changed" event - one per block downloaded during synchronization
   */
  @Subscribe
  public void onBitcoinNetworkChangeEvent(final BitcoinNetworkChangedEvent event) {

    if (!isInitialised()) {
      return;
    }

    log.trace("Received 'Bitcoin network changed' event: {}", event.getSummary());

    Preconditions.checkNotNull(event, "'event' must be present");
    Preconditions.checkNotNull(event.getSummary(), "'summary' must be present");

    BitcoinNetworkSummary summary = event.getSummary();

    Preconditions.checkNotNull(summary.getSeverity(), "'severity' must be present");

    // Keep the UI response to a minimum due to the volume of these events
    updateSendRequestButtons(event);

  }

  @Subscribe
  public void onExchangeRateChangedEvent(ExchangeRateChangedEvent exchangeRateChangedEvent) {
    update();
  }

  @Subscribe
  public void onSlowTransactionSeenEvent(SlowTransactionSeenEvent slowTransactionSeenEvent) {
    update();
  }

  @Subscribe
  public void onTransactionCreationEvent(TransactionCreationEvent transactionCreationEvent) {
    update();
  }

  /**
   * Update the payments when a walletDetailsChangedEvent occurs
   */
  @Subscribe
  public void onWalletDetailChangedEvent(WalletDetailChangedEvent walletDetailChangedEvent) {
    log.trace("Wallet detail has changed");
    update();
  }

  private void update() {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        updateInternal();
      }
    });
  }


  private void updateInternal() {
    if (!isInitialised()) {
      return;
    }

    // Ensure the buttons are kept up to date
    Optional<BitcoinNetworkChangedEvent> changedEvent = CoreServices.getApplicationEventService().getLatestBitcoinNetworkChangedEvent();
    if (changedEvent.isPresent()) {
      updateSendRequestButtons(changedEvent.get());
    }

    if (!CoreServices.getCurrentWalletService().isPresent()) {
      throw new IllegalStateException("SendRequestScreenView is showing without a current wallet service");
    }
    WalletService walletService = CoreServices.getCurrentWalletService().get();

    log.trace("Updating the payment data set - expensive");
    final Set<PaymentData> allPayments = walletService.getPaymentDataSet();

    // Find the 'Sending' transactions for today
    final List<PaymentData> todaysSendingPayments = walletService.subsetPaymentsAndSort(allPayments, PaymentType.SENDING);

    // Find the 'Requested' events for today
    final List<PaymentData> todaysRequestedPayments = walletService.subsetPaymentsAndSort(allPayments, PaymentType.RECEIVING);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        displaySendingPaymentsMaV.getModel().setValue(todaysSendingPayments);
        displaySendingPaymentsMaV.getView().createView();
        displaySendingPaymentsMaV.getView().updateView();

        displayRequestedPaymentsMaV.getModel().setValue(todaysRequestedPayments);
        displayRequestedPaymentsMaV.getView().createView();
        displayRequestedPaymentsMaV.getView().updateView();
      }
    });
  }

  private void updateSendRequestButtons(BitcoinNetworkChangedEvent event) {
    boolean newEnabled;
    boolean canChange = true;

    // NOTE: Both send and request are disabled when the network is not available
    // because it is possible that a second wallet is generating transactions using
    // addresses that this one has not displayed yet. This would lead to the same
    // address being used twice.
    switch (event.getSummary().getSeverity()) {
      case RED:
        // Enable on RED only if unrestricted (allows FEST tests without a network)
        newEnabled = InstallationManager.unrestricted;
        break;
      case AMBER:
        // Enable on AMBER only if unrestricted
        newEnabled = InstallationManager.unrestricted;
        break;
      case GREEN:
        // Enable on GREEN
        newEnabled = true;
        break;
      case PINK:
      case EMPTY:
        // Maintain the status quo
        newEnabled = sendBitcoin.isEnabled();
        canChange = false;
        break;
      default:
        // Unknown status
        throw new IllegalStateException("Unknown event severity " + event.getSummary().getSeverity());
    }

    if (canChange) {
      final boolean finalNewEnabled = newEnabled;

      // If button is not enabled and the newEnabled is false don't do anything
      if (requestBitcoin.isEnabled() || newEnabled) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            sendBitcoin.setEnabled(finalNewEnabled);
            requestBitcoin.setEnabled(finalNewEnabled);
            stakeBlackcoin.setEnabled(finalNewEnabled);
          }
        });
      }
    }
  }
}
