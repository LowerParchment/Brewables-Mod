// Import user defined dependencies
package com.LowerParchment.brewables.block;

// Import Minecraft dependencies
import net.minecraft.util.StringRepresentable;

// Enum for representing cauldron brew color states.
// Used in blockstates to control cauldron tinting and texture selection.
public enum BrewColorType implements StringRepresentable
{
    CLEAR("clear"),
    BROWN("brown"),
    SWIFTNESS("swiftness"),
    SLOWNESS("slowness"),
    LEAPING("leaping"),
    STRENGTH("strength"),
    HEALING("healing"),
    HARMING("harming"),
    POISON("poison"),
    REGENERATION("regeneration"),
    FIRE_RESISTANCE("fire_resistance"),
    WATER_BREATHING("water_breathing"),
    NIGHT_VISION("night_vision"),
    INVISIBILITY("invisibility"),
    TURTLE_MASTER("turtle_master"),
    SLOW_FALLING("slow_falling"),
    WEAKNESS("weakness"),
    WART("wart");

    // Name of the brew color type used for serialization.
    private final String name;

    // Associates each enum constant with its string representation.
    BrewColorType(String name)
    {
        this.name = name;
    }

    // Serialized name used for JSON and blockstate matching.
    @Override
    public String getSerializedName()
    {
        return name;
    }

    // Converts the enum to its string representation.
    @Override
    public String toString()
    {
        return name;
    }
}
