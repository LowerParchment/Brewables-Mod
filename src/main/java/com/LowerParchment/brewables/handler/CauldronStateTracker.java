// Import user defined dependencies
package com.LowerParchment.brewables.handler;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry.BrewResult;

// Import Minecraft and Java dependencies
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

/// Tracks cauldron states including:
// - Brew phase (empty, base_ready, brew_ready, failed)
// - Dose count (remaining potions extractable)
// - BrewResult metadata (potion output + modifiers)
public class CauldronStateTracker
{
    private static final Map<BlockPos, CauldronBrewState> brewStates = new HashMap<>();
    private static final Map<BlockPos, Integer> doseCounts = new HashMap<>();
    private static final Map<BlockPos, BrewResult> brewResults = new HashMap<>();

    // Get the current state of a cauldron
    public static CauldronBrewState getState(BlockPos pos)
    {
        pos = pos.immutable();
        return brewStates.getOrDefault(pos, CauldronBrewState.EMPTY);
    }

    // Returns full map of all tracked cauldron brew states (for debugging or sync)
    public static Map<BlockPos, CauldronBrewState> getAllTrackedStates()
    {
        return brewStates;
    }

    // Set the state of a cauldron
    public static void setState(BlockPos pos, CauldronBrewState state)
    {
        System.out.println("[TRACKER SET] Setting state of " + pos + " to " + state);
        brewStates.put(pos.immutable(), state);
    }

    // Removes all tracking info for a cauldron (called when broken or fully emptied)
    public static void reset(BlockPos pos)
    {
        BlockPos immutablePos = pos.immutable();
        brewStates.remove(immutablePos);
        doseCounts.remove(immutablePos);
        brewResults.remove(immutablePos);
        ItemInCauldronHandler.clearIngredients(pos);
        System.out.println("Cauldron reset at " + pos);
    }

    // Set how many doses remain in a cauldron
    public static void setDoses(BlockPos pos, int doses)
    {
        doseCounts.put(pos.immutable(), doses);
    }

    // Get remaining dose count
    public static int getDoses(BlockPos pos)
    {
        pos = pos.immutable();
        return doseCounts.getOrDefault(pos, 0);
    }

    // Decreases potion dose count by 1, clamps at 0
    public static void decrementDoses(BlockPos pos)
    {
        pos = pos.immutable();
        int before = getDoses(pos);
        int after = Math.max(0, before - 1);
        System.out.println("decrementDoses called at " + pos + " | Before: " + before + " → After: " + after);
        doseCounts.put(pos, after);
    }

    // Check if the cauldron is depleted
    public static boolean isEmpty(BlockPos pos)
    {
        pos = pos.immutable();
        return getDoses(pos) <= 0;
    }

    //Set the result of a brewing process for a cauldron
    public static void setResult(BlockPos pos, BrewResult result)
    {
        brewResults.put(pos.immutable(), result);
    }

    // Get the result of a brewing process for a cauldron
    public static BrewResult getResult(BlockPos pos)
    {
        pos = pos.immutable();
        return brewResults.get(pos);
    }

    // Removes stored BrewResult without touching dose or state
    public static void clearResult(BlockPos pos)
    {
        brewResults.remove(pos);
    }
}

