
package tkm.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Mike
 */
public class TitlePanel extends JPanel {
    
    // SCREEN SETTINGS
    final private int originalTileSize = 10; // 16x16 tile
    final private int scale = 3;  // scale up * 3

    final private int tileSize = originalTileSize * scale; 
    final private int maxScreenCol = 20;
    final private int maxScreenRow = 20;
    final private int screenWidth = tileSize * maxScreenCol; 
    final private int screenHeight = tileSize * maxScreenRow;
    
    //private final ImageIcon image;
    private final JLabel title;
    private final JLabel authors;
    
    public TitlePanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        
        title = new JLabel("Clue-Less");
        //image = new ImageIcon();
        authors = new JLabel("Chandler Cook, Derek Osborne, Guenevere Logan, "
                + "Justin Nguyen, Stephen Snelling ");
        
        title.setFont(new Font("SansSerif", Font.BOLD, 60));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        authors.setFont(new Font("SansSerif", Font.BOLD, 10));
        authors.setForeground(Color.LIGHT_GRAY);
        authors.setHorizontalAlignment(SwingConstants.CENTER);
        
        this.add(title);
        //this.add(image);
        this.add(authors);      
    }
    
}
