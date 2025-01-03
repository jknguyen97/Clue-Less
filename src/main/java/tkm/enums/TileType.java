
package tkm.enums;

import java.awt.Color;

/**
 * @file TileType.java
 * @date 11/8/2024
 * @author Mike
 * 
 * This enum type specifies what each tile number represents in out tile map for
 * Clue-less.
 */

public enum TileType {
    BLANK(Color.BLACK),
    HALLWAY(),
    STARTING_SQUARE(Color.LIGHT_GRAY),
    STUDY(),
    HALL(),
    LOUNGE(),
    LIBRARY(),
    BILLIARD_ROOM(),
    DINNING_ROOM(),
    CONSERVATORY(),
    BALLROOM(),
    KITCHEN();
    
    private final Color color;
    
    TileType() {
        this.color = new Color(245, 222, 179); //Brown
    }
    
    TileType(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
}
