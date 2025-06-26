// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import net.minecraft.util.StringRepresentable;

public enum BrewColorType implements StringRepresentable
{
    CLEAR("clear"),
    BROWN("brown"),
    BLUE("blue"),
    RED("red"),
    BLACK("black");

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
