// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import net.minecraft.util.StringRepresentable;

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

    private final String name;

    BrewColorType(String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
