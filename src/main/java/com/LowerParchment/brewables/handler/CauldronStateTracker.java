// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.handler;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry.BrewResult;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

// This class tracks the state of cauldrons in the game, including their brew states and remaining doses.
public class CauldronStateTracker
{
    private static final Map<BlockPos, CauldronBrewState> brewStates = new HashMap<>();
    private static final Map<BlockPos, Integer> doseCounts = new HashMap<>();

    // Get the current state of a cauldron
    public static CauldronBrewState getState(BlockPos pos)
    {
        pos = pos.immutable();
        return brewStates.getOrDefault(pos, CauldronBrewState.EMPTY);
    }

    // Set the state of a cauldron
    public static void setState(BlockPos pos, CauldronBrewState state)
    {
        brewStates.put(pos.immutable(), state);
    }

    // Remove tracking for a cauldron (e.g., when drained or broken)
    public static void reset(BlockPos pos)
    {
        pos = pos.immutable();
        brewStates.put(pos, CauldronBrewState.EMPTY);
        doseCounts.remove(pos);
        brewResults.remove(pos);
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

    // Decrement doses by one (min 0)
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

    // Record to hold the result of a brewing process
    private static final Map<BlockPos, BrewResult> brewResults = new HashMap<>();

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

    // Clear the result of a brewing process for a cauldron
    public static void clearResult(BlockPos pos)
    {
        brewResults.remove(pos);
    }
}

