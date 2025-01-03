
package tkm.enums;

import java.awt.Color;

/**
 * @file CharacterType.java
 * @date 10/25/2024
 * @author Mike
 * 
 * This enum type represents the playable characters in Clue-less.
 */

public enum CharacterType {
    MISS_SCARLET("Miss Scarlet", Color.RED, 13, 2), 
    KERNEL_MUSTARD("Kernel Mustard", Color.YELLOW, 18, 6),
    MRS_WHITE("Mrs. White", Color.WHITE, 13, 18), 
    MR_GREEN("Mr. Green", Color.GREEN, 6, 18), 
    MRS_PEACOCK("Mrs. Peacock", Color.BLUE, 2, 13), 
    PROFESSOR_PLUM("Professor Plum", Color.MAGENTA, 2, 6);
    
    private final String name;
    private final Color color;
    private final int x, y;         // starting coordinates on the tilemap
    
    CharacterType(String name, Color color, int x, int y) {
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Color getColor() {
        return this.color;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
}