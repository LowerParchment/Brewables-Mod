// Import user defined dependencies
package com.LowerParchment.brewables.event;

// Import Minecraft dependencies
import net.minecraft.util.StringRepresentable;

public enum CauldronBrewState implements StringRepresentable
{
    EMPTY("empty"),                 // No water or potion present
    BASE_READY("base_ready"),       // Base ingredient added (e.g., Nether Wart equivalent)
    BREW_READY("brew_ready"),       // Valid brew with effects is ready
    FAILED("failed");               // Invalid recipe produced Witch's Wart

    private final String name;

    CauldronBrewState(String name)
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