package tkm.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class CharacterPanel extends JPanel {
    private JLabel character;
    private final String characterName;

    public CharacterPanel(String characterName) {
        this.characterName = characterName;
        this.initComponents(characterName);
        this.add(character);
    }

    private void initComponents(String characterName) {
        character = new JLabel(characterName);
    }
}
