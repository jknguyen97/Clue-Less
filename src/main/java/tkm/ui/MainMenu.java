package tkm.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenu extends JPanel{
    
    private JLabel menu;
    private JButton hostGame;
    private JButton joinGame;
    private JButton exitGame;
    
    // Constructor
    public MainMenu() {
        this.initComponents();
        
        this.setLayout(new GridLayout(0, 1, 5, 5));
        
        JPanel menuLabelPanel = new JPanel(new BorderLayout());
        menuLabelPanel.add(menu, BorderLayout.CENTER);
        
        this.add(menuLabelPanel);
        this.add(hostGame);
        this.add(joinGame);
        this.add(exitGame);
    }
    
    private void initComponents() {
        menu = new JLabel("Main Menu");
        menu.setHorizontalAlignment(SwingConstants.CENTER);
        menu.setVerticalAlignment(SwingConstants.CENTER);
        hostGame = new JButton("Host Game");
        joinGame = new JButton("Join Game");
        exitGame = new JButton("ExitGame");
    }
    
    public JButton getHostGameButton() {
        return hostGame;
    }
    
    public JButton getJoinGameButton() {
        return joinGame;
    }
    
    public JButton getExitGameButton() {
        return exitGame;
    }

}
