package org.multibit.hd.ui.views.wizards.stake_blackcoins;

import org.multibit.hd.ui.views.wizards.AbstractHardwareWalletWizardModel;

public class StakeBlackcoinWizardModel  extends AbstractHardwareWalletWizardModel<StakeBlackcoinState> {

	private StakeBlackcoinDisplayPaymentRequestPanelModel stakePanelModel;

	public StakeBlackcoinWizardModel(StakeBlackcoinState state) {
		super(state);
		
	}
	
	/**
	   * @return The credentials the user entered
	   */
	  public String getPassword() {
	    return stakePanelModel.getPasswordModel().getValue();
	  }
	
	void setStakePanelModel(StakeBlackcoinDisplayPaymentRequestPanelModel stakePanelModel) {
	    this.stakePanelModel = stakePanelModel;
	  }

}
