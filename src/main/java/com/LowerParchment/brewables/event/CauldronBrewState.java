// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;

import net.minecraft.util.StringRepresentable;

public enum CauldronBrewState implements StringRepresentable
{
    EMPTY("empty"),
    BASE_READY("base_ready"),
    BREW_READY("brew_ready"),
    FAILED("failed");

    private final String name;

    CauldronBrewState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}