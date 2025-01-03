
package tkm.enums;

/**
 * @file RoomType.java
 * @date 10/25/2024
 * @author Mike
 * 
 * This enum type represents all available rooms for Clue-less
 */

public enum RoomType {
    STUDY("Study", true), 
    HALL("Hall", false), 
    LOUNGE("Lounge", true), 
    LIBRARY("Library", false), 
    BILLIARD_ROOM("Billiard Room", false), 
    DINING_ROOM("Dining Room", false), 
    CONSERVATORY("Conservatory", true), 
    BALLROOM("Ballroom", false), 
    KITCHEN("Kitchen", true);
    
    private final String name;
    private final boolean secretPassageway;
    
    RoomType(String name, boolean secretPassageway) {
        this.name = name;
        this.secretPassageway = secretPassageway;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean hasSecretPassageWay() {
        return this.secretPassageway;
    }
}