package tkm.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import tkm.enums.TileType;
import tkm.gamelogic.GamePiece;

/**
 * @file GamePanel.java
 * @date 10/31/2024
 * @author Chandler
 * @edited Mike Snelling - 10/31/2024
 * 
 * This class is a JPanel that visual represents the current state of the board
 * for a Clue-Less game. 
 */

public class GamePanel extends JPanel{

    // SCREEN SETTINGS
    final private int originalTileSize = 10; // 16x16 tile
    final private int scale = 3;  // scale up * 3

    final private int tileSize = originalTileSize * scale; // 30X30 tile
    final private int maxScreenCol = 20;
    final private int maxScreenRow = 20;
    final private int screenWidth = tileSize * maxScreenCol; // 600 pixels
    final private int screenHeight = tileSize * maxScreenRow; // 600 pixels
    
    private int[][] tileMap;
    private ArrayList<GamePiece> pieces;
    
    //private final Map<Point, Component> roomMap;
    
    /**
     * TO DO
     * 
     * Need to implement a way to track the room panels for retrieval later
     * Could use a Map or some other mechanism
     */

    
    public GamePanel(int[][] tileMap, ArrayList<GamePiece> pieces) {

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        
        /*
        TEMPORARY for Presentation
        */
//        this.setLayout(new BorderLayout());
//        JLabel titleLabel = new JLabel("Clue-Less");
//        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 60));
//        titleLabel.setForeground(Color.WHITE);
//        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
//        this.add(titleLabel, BorderLayout.CENTER);

        //roomMap = new HashMap<>();

        this.tileMap = tileMap;
        this.pieces = pieces;
        
    } //end of Constructor
    
    public void setGamePieces(ArrayList<GamePiece> pieces) {
        this.pieces = pieces;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //int[][] tileMap = gameBoard.getTileMap();
        
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                int tile = tileMap[i][j];
                
                switch (tile) {
                    case 0 -> {
                        g.setColor(TileType.BLANK.getColor());
                    }
                    case 2 -> {
                        g.setColor(TileType.STARTING_SQUARE.getColor());
                    }
                    case 1, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> {
                        g.setColor(TileType.STUDY.getColor());
                    }
                    default -> System.out.println("There was a problem drawing the tile map.");
                }
                
                // Draw the tile
                g.fillRect(j * tileSize, i * tileSize, tileSize, tileSize);
                g.setColor(Color.BLACK);
                g.drawRect(j * tileSize, i * tileSize, tileSize, tileSize);
            }
        }
        
        this.drawRoomLabels(g);
        this.drawGamePieces(g);
    }
    
    // Helper method to draw labels of each room
    private void drawRoomLabels(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Study", 30, 20);     // Adjust coordinates to place above Study
        g.drawString("Hall", 250, 20);
        g.drawString("Lounge", 450, 20);
        g.drawString("Library", 30, 230);
        g.drawString("Billiard Room", 200, 230);
        g.drawString("Dining Room", 410, 230);
        g.drawString("Conservatory", 30, 590);
        g.drawString("Ballroom", 250, 590);
        g.drawString("Kitchen", 450, 590);
    }
    
    private void drawGamePieces(Graphics g) {
        for(GamePiece piece : pieces) {
            piece.draw(g);
        }
    }
    
}