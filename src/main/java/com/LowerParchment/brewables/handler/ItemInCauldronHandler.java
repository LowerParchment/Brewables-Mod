// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.handler;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.CauldronStateTracker;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// In this code, we check for item entities in the world and see if they are in a water cauldron.
@Mod.EventBusSubscriber (modid = BrewablesMod.MODID)
public class ItemInCauldronHandler
{

    // A map to store the ingredients in cauldrons by their positions.
    private static final Map<BlockPos, List<ItemStack>> ingredientsByCauldron = new HashMap<>();

    // Make the map accessible to other classes.
    public static Map<BlockPos, List<ItemStack>> getIngredientsMap()
    {
        return ingredientsByCauldron;
    }
    
    // This method is called to clear the ingredients in a cauldron at a specific position.
    public static void clearIngredients(BlockPos pos) {
        ingredientsByCauldron.remove(pos.immutable());
    }
    // This method is called on every world tick to check for items in water cauldrons.
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        
        var level = event.level;
        if (level.isClientSide) return; // Only run on server side

        // Check all item entities in the world
        for (var player : event.level.players())
        {
            BlockPos playerPos = player.blockPosition();
            AABB searchBox = new AABB(playerPos).inflate(6); // 6-block radius
        
            // Find item entities within the search box
            // This will check for items within a 6-block radius of the player.
            for (ItemEntity item : event.level.getEntitiesOfClass(ItemEntity.class, searchBox))
            {
                BlockPos cauldronPos = BlockPos.containing(item.position().x, item.position().y - 0.1, item.position().z).immutable();
                System.out.println("📍 Item landed over BlockPos: " + cauldronPos);
                BlockState state = level.getBlockState(cauldronPos);
        
                // Check to see if the item is in a water cauldron
                if (state.getBlock() == BrewablesMod.BREW_CAULDRON.get())
                {
                    // Get the current state of the cauldron at this position and check if it is
                    // ready for new ingredients
                    CauldronBrewState currentState = CauldronStateTracker.getState(cauldronPos);
                    if (currentState == CauldronBrewState.BREW_READY || currentState == CauldronBrewState.FAILED)
                    {
                        return; // Don't accept new ingredients after brewing is done
                    }

                    // Log for debugging
                    System.out.println("Adding to ingredientsByCauldron at: " + cauldronPos);
                    System.out.println("Current ingredient map keys:");

                    // Print out the keys in the ingredientsByCauldron map for debugging
                    for (BlockPos key : ingredientsByCauldron.keySet())
                    {
                        System.out.println("- " + key);
                    }

                    // Log the item entity found near the player for debugging
                    System.out.println("🧪 Item near player in water cauldron: " + item.getItem().getDisplayName().getString());

                    // Add the item to the cauldron's ingredient list
                    ingredientsByCauldron.putIfAbsent(cauldronPos, new ArrayList<>());
                    ingredientsByCauldron.get(cauldronPos).add(item.getItem().copy());

                    // Log the ingredient added to the cauldron
                    System.out.println("📝 Ingredient added to cauldron at " + cauldronPos + ": " +
                        item.getItem().getDisplayName().getString());
                    
                    // Optional debugging output
                    System.out.println("🍲 Current ingredients in " + cauldronPos + ":");
                    for (ItemStack stack : ingredientsByCauldron.get(cauldronPos))
                    {
                        System.out.println("- " + stack.getCount() + "x " + stack.getDisplayName().getString());
                    }

                    // Discard the item entity
                    item.discard();
                }
            }
        }
    }
}