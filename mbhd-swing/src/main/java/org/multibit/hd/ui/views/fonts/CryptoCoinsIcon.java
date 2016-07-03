package org.multibit.hd.ui.views.fonts;

/**
 * <p>Enum to provide the following to UI:</p>
 * <ul>
 * <li>Provision of easy references to Font Awesome Iconography</li>
 * </ul>
 *
 * @since 0.0.1
 */
public enum CryptoCoinsIcon {

  // Copy and paste the output from FontAwesomeTools below here

  BLACKCOIN('\ue909')

  // End of enum
  ;

  private final Character character;

  private CryptoCoinsIcon(Character character) {
    this.character = character;
  }

  public Character getChar() {
    return character;
  }

  @Override
  public String toString() {
    return character.toString();
  }





}