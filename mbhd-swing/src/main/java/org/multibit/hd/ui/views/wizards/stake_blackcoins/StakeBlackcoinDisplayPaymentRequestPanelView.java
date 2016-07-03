package org.multibit.hd.ui.views.wizards.stake_blackcoins;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.bitcoinj.core.Wallet;
import org.blackcoinj.pos.Staker;
import org.multibit.hd.core.managers.WalletManager;
import org.multibit.hd.core.services.BitcoinNetworkService;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.Buttons;
import org.multibit.hd.ui.views.components.panels.PanelDecorator;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.themes.NimbusDecorator;
import org.multibit.hd.ui.views.wizards.AbstractWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;
import org.multibit.hd.ui.views.wizards.StakeBlackcoinWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class StakeBlackcoinDisplayPaymentRequestPanelView extends AbstractWizardPanelView {
	private static final Logger log = LoggerFactory.getLogger(StakeBlackcoinDisplayPaymentRequestPanelView.class);
	private final BitcoinNetworkService bitcoinNetworkService;

	public StakeBlackcoinDisplayPaymentRequestPanelView(StakeBlackcoinWizard stakeBlackcoinWizard, String panelName) {
		super(stakeBlackcoinWizard, panelName, AwesomeIcon.ROCKET, MessageKey.DISPLAY_STAKE_BLACKCOIN_TITLE);
		bitcoinNetworkService = CoreServices.getOrCreateBitcoinNetworkService();
	}

	@Override
	public void newPanelModel() {
		// no model for now

	}

	@Override
	public void initialiseContent(JPanel contentPanel) {
		
		Action stake = stakeAction();
		JButton stakeButton = Buttons.newButton(stake, MessageKey.STAKE, MessageKey.STAKE_TOOLTIP, null);		
		contentPanel.add(stakeButton, "align center");
	}

	private Action stakeAction() {

	    return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				              
				Wallet wallet = WalletManager.INSTANCE.getCurrentWalletSummary().get().getWallet();
				if(!wallet.calculateAllSpendCandidates().isEmpty()){
					Object source = event.getSource();
					
	                if (source instanceof JButton) {
	                    NimbusDecorator.applyThemeColor(Color.GREEN,(JButton)source);
	                }
					
	                if (!bitcoinNetworkService.isStaking()) {
						bitcoinNetworkService.startStaking();
					}
				}
			}
	    	
	    };

	}

	@Override
	protected void initialiseButtons(AbstractWizard wizard) {
		PanelDecorator.addFinish(this, wizard);
		
	}
	
	@Override
	public boolean beforeHide(boolean isExitCancel) {
		if (bitcoinNetworkService.isStaking()) {
			log.info("stopping");
			BitcoinNetworkService bitcoinNetworkService = CoreServices.getOrCreateBitcoinNetworkService();
			bitcoinNetworkService.stopStaking();
			log.info("stopped");
		}
		
	    if (isExitCancel) {
	    	
	    }
		return true;
	}
	@Override
	public void updateFromComponentModels(Optional componentModel) {
		// TODO Auto-generated method stub

	}

}
