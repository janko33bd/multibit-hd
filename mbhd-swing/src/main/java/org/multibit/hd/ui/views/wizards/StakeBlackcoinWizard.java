package org.multibit.hd.ui.views.wizards;

import java.util.Map;

import org.multibit.hd.ui.views.wizards.request_bitcoin.RequestBitcoinEnterDetailsPanelView;
import org.multibit.hd.ui.views.wizards.request_bitcoin.RequestBitcoinState;
import org.multibit.hd.ui.views.wizards.send_bitcoin.SendBitcoinDisplayPaymentRequestPanelView;
import org.multibit.hd.ui.views.wizards.send_bitcoin.SendBitcoinState;
import org.multibit.hd.ui.views.wizards.stake_blackcoins.StakeBlackcoinDisplayPaymentRequestPanelView;

import com.google.common.base.Optional;

public class StakeBlackcoinWizard extends AbstractHardwareWalletWizard<StakeBlackcoinWizardModel>{

	

	public StakeBlackcoinWizard(StakeBlackcoinWizardModel stakeBlackcoinWizardModel) {
		super(stakeBlackcoinWizardModel, false, Optional.absent());
	}

	@Override
	protected void populateWizardViewMap(Map<String, AbstractWizardPanelView> wizardViewMap) {
		wizardViewMap.put(
				StakeBlackcoinState.START.name(),
			      new StakeBlackcoinDisplayPaymentRequestPanelView(this, SendBitcoinState.SEND_DISPLAY_PAYMENT_REQUEST.name()));
		
		
		
	}

}
