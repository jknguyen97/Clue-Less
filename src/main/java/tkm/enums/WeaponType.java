
package tkm.enums;

/**
 * @file WeaponType.java
 * @date 10/25/2024
 * @author Mike
 * This enum type represents the weapons available in Clue-Less
 */

public enum WeaponType {
    KNIFE("Knife"),
    CANDLESTICK("Candlestick"),
    REVOLVER("Revolver"),
    ROPE("Rope"),
    LEAD_PIPE("Lead Pipe"),
    WRENCH("Wrench");

    private final String name;

    WeaponType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
