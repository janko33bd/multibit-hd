package org.multibit.hd.ui.views.wizards.stake_blackcoins;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.bitcoinj.core.Wallet;
import org.multibit.hd.core.dto.WalletSummary;
import org.multibit.hd.core.managers.WalletManager;
import org.multibit.hd.core.services.BitcoinNetworkService;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.Buttons;
import org.multibit.hd.ui.views.components.Components;
import org.multibit.hd.ui.views.components.ModelAndView;
import org.multibit.hd.ui.views.components.Panels;
import org.multibit.hd.ui.views.components.enter_password.EnterPasswordModel;
import org.multibit.hd.ui.views.components.enter_password.EnterPasswordView;
import org.multibit.hd.ui.views.components.panels.PanelDecorator;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.fonts.CryptoCoinsIcon;
import org.multibit.hd.ui.views.themes.NimbusDecorator;
import org.multibit.hd.ui.views.wizards.AbstractWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import net.miginfocom.swing.MigLayout;

public class StakeBlackcoinDisplayPaymentRequestPanelView extends AbstractWizardPanelView<StakeBlackcoinWizardModel, StakeBlackcoinDisplayPaymentRequestPanelModel> {
	
	private static final Logger log = LoggerFactory.getLogger(StakeBlackcoinDisplayPaymentRequestPanelView.class);
	private final BitcoinNetworkService bitcoinNetworkService;
	private ModelAndView<EnterPasswordModel, EnterPasswordView> enterPasswordMaV;
	private StakeBlackcoinDisplayPaymentRequestPanelModel panelModel;
	private JPanel newComponentPanel;

	public StakeBlackcoinDisplayPaymentRequestPanelView(AbstractWizard<StakeBlackcoinWizardModel> wizard,
			String panelName) {
		super(wizard, panelName, AwesomeIcon.ROCKET, MessageKey.DISPLAY_STAKE_BLACKCOIN_TITLE);
		bitcoinNetworkService = CoreServices.getOrCreateBitcoinNetworkService();
	}

	@Override
	public void newPanelModel() {
		// Require a reference for the model
	    enterPasswordMaV = Components.newEnterPasswordMaV(getPanelName());
	    
	 // Configure the panel model
	    panelModel = new StakeBlackcoinDisplayPaymentRequestPanelModel(
	      getPanelName(),
	      enterPasswordMaV.getModel()
	    );
	    setPanelModel(panelModel);
	    getWizardModel().setStakePanelModel(panelModel);
	    // Bind it to the wizard model
	    registerComponents(enterPasswordMaV);
	}

	@Override
	public void initialiseContent(JPanel contentPanel) {
		
		newComponentPanel = enterPasswordMaV.getView().newComponentPanel();
		
		contentPanel.add(newComponentPanel, "align right,wrap");
		
		Action stake = stakeAction();
		JButton stakeButton = Buttons.newButton(stake, MessageKey.STAKE, MessageKey.STAKE_TOOLTIP, null);		
		contentPanel.add(stakeButton, "align center");
	}

	private Action stakeAction() {

	    return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WalletSummary currentWalletSummary = WalletManager.INSTANCE.getCurrentWalletSummary().get();
				String enteredPassword = getPanelModel().get().getPasswordModel().getValue();
				if(enteredPassword.equals(currentWalletSummary.getWalletPassword().getPassword())){
					Wallet wallet = currentWalletSummary.getWallet();
					if(!wallet.calculateAllSpendCandidates().isEmpty() 
							&& !Strings.isNullOrEmpty(getPanelModel().get().getPasswordModel().getValue())){
						
						log.debug(String.valueOf(newComponentPanel.getComponentCount()));
						Object source = event.getSource();
		                if (source instanceof JButton) {
		                    NimbusDecorator.applyThemeColor(Color.GREEN,(JButton)source);
		                }
		                enterPasswordMaV.getView().setEnabled(false);
		                if (!bitcoinNetworkService.isStaking()) {
							bitcoinNetworkService.startStaking(enteredPassword);
						}
					}
				}else{
					enterPasswordMaV.getView().incorrectPassword();
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
