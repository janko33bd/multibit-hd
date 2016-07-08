package org.multibit.hd.ui.views.wizards.stake_blackcoins;

import org.multibit.hd.ui.views.components.enter_password.EnterPasswordModel;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelModel;

public class StakeBlackcoinDisplayPaymentRequestPanelModel extends AbstractWizardPanelModel {

  private final EnterPasswordModel passwordModel;

  public StakeBlackcoinDisplayPaymentRequestPanelModel(String panelName, EnterPasswordModel passwordModel) {
    super(panelName);

    this.passwordModel = passwordModel;

  }

  /**
   * @return The "enter credentials" model
   */
  public EnterPasswordModel getPasswordModel() {
    return passwordModel;
  }

}
