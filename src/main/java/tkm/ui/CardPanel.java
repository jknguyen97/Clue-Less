
package tkm.ui;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author Mike
 */
public class CardPanel extends JPanel {
    
    private final String[] hand;
    private final ArrayList<JButton> cardButtons;
    //private JLabel character;
    
    public CardPanel(String[] playerHand) {
        for(String string : playerHand) {
            System.out.println(string);
        }
        this.hand = playerHand;
        cardButtons = new ArrayList();
        this.initComponents();
        //this.add(character);
    }
    
    private void initComponents() {
        //character = new JLabel("");
        this.setLayout(new GridLayout(3, 5));
        this.setBorder(BorderFactory.createTitledBorder("Your Cards"));
        
        for(String card : hand) {
            JButton button = new JButton(card);
            cardButtons.add(button);
            this.add(button);
        }
    }

    //public void setCharacter(String playerCharacter) {
    //    character.setText(playerCharacter);
    //}
}
