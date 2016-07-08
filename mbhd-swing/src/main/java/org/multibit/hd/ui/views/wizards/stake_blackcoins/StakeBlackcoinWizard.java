package org.multibit.hd.ui.views.wizards.stake_blackcoins;

import java.util.Map;

import org.multibit.hd.ui.views.wizards.AbstractHardwareWalletWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;
import com.google.common.base.Optional;

public class StakeBlackcoinWizard  extends AbstractHardwareWalletWizard<StakeBlackcoinWizardModel>{

	public StakeBlackcoinWizard(StakeBlackcoinWizardModel wizardModel) {
		super(wizardModel, false, Optional.absent());
		
	}

	private StakeBlackcoinDisplayPaymentRequestPanelModel stakePanelModel;

	/**
	   * @return The credentials the user entered
	   */
	  public String getPassword() {
	    return stakePanelModel.getPasswordModel().getValue();
	  }
	  
	  void setConfirmPanelModel(StakeBlackcoinDisplayPaymentRequestPanelModel stakePanelModel) {
		    this.stakePanelModel = stakePanelModel;
		  }

	@Override
	protected void populateWizardViewMap(Map<String, AbstractWizardPanelView> wizardViewMap) {
		wizardViewMap.put(
			      StakeBlackcoinState.START_STAKING.name(),
			      new StakeBlackcoinDisplayPaymentRequestPanelView(this, StakeBlackcoinState.START_STAKING.name()));
		
	}
	

}
