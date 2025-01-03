
package tkm.ui;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @file StartGamePanel.java
 * @date 11/11/2024
 * @author Mike
 * 
 * This class is a panel that allows the host to start the game. It shows how
 * many connected players there are.
 */

public class StartGamePanel extends JPanel {
    private JButton startGame;
    private JLabel players;
    private JLabel waiting;
    private int currentPlayerCount;
    private final boolean HOST;
    
    public StartGamePanel(boolean host) {
        this.HOST = host;
        currentPlayerCount = 0;
        this.initComponents();
        
        this.setLayout(new GridLayout(0, 1, 5, 5));
        
        this.add(startGame);
        this.add(players);
        this.add(waiting);
        
        startGame.setEnabled(HOST);
    }
    
    private void initComponents() { 
        startGame = new JButton("Start Game");
        players = new JLabel(currentPlayerCount + " Players have joined!");
        this.centerLabel(players);
        waiting = new JLabel("Waiting on host to start the game.");
        this.centerLabel(waiting);
    }
    
    private void centerLabel(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
    }
    
    public JButton getStartGameButton() {
        return startGame;
    }
    
    public JLabel getPlayerLabel() {
        return players;
    }
    
    public void updatePlayerCount(int currentPlayerCount) {
        this.currentPlayerCount = currentPlayerCount;
        players.setText(currentPlayerCount + " Players have joined!");
        
        this.revalidate();
        this.repaint();
    }
}
