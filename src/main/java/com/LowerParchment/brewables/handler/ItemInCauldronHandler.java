// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.handler;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry.BrewResult;
import com.LowerParchment.brewables.handler.CauldronStateTracker;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
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
                System.out.println("Item landed over BlockPos: " + cauldronPos);
                BlockState state = level.getBlockState(cauldronPos);

                // Make sure that the cauldron has water in it
                if (!state.hasProperty(BrewCauldronBlock.LEVEL) || state.getValue(BrewCauldronBlock.LEVEL) == 0)
                {
                    System.out.println("Cauldron at " + cauldronPos + " is dry — ignoring thrown item.");
                    continue;
                }
        
                // Check to see if the item is in a water cauldron
                if (state.getBlock() == BrewablesMod.BREW_CAULDRON.get())
                {
                    // Get the current state of the cauldron at this position and check if it is
                    // ready for new ingredients
                    CauldronBrewState currentState = CauldronStateTracker.getState(cauldronPos);
                    ItemStack itemStack = item.getItem();

                    // Special case: Nether Wart can move the state from EMPTY → BASE_READY
                    if (itemStack.getItem() == Items.NETHER_WART)
                    {
                        // If the player inserted nethwart, check if the cauldron is empty
                        if (currentState == CauldronBrewState.EMPTY)
                        {
                            // Set cauldron state to BASE_READY
                            CauldronStateTracker.setState(cauldronPos, CauldronBrewState.BASE_READY);
                            System.out.println("Cauldron at " + cauldronPos + " is now BASE_READY with Nether Wart!");

                            // Visually set it to brown for readiness
                            BlockState newState = state
                                .setValue(BrewCauldronBlock.LEVEL, state.getValue(BrewCauldronBlock.LEVEL)) // preserve level
                                .setValue(BrewCauldronBlock.COLOR, BrewColorType.BROWN)
                                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BASE_READY);
                            level.setBlock(cauldronPos, newState, 3);

                            // Ensure doses match cauldron's fill level when starting nether wart brew
                            int startingLevel = state.getValue(BrewCauldronBlock.LEVEL);
                            CauldronStateTracker.setDoses(cauldronPos, startingLevel);

                            // Discard the Nether Wart item
                            item.discard();
                            continue;
                        }
                        else
                        {
                            System.out.println("Nether Wart rejected — cauldron not empty.");
                        
                            // If the cauldron is not empty, discard the Nether Wart
                            item.discard();
                            continue;
                        }
                    }

                    // For all other items, only accept if we're already in BASE_READY state
                    if (currentState != CauldronBrewState.BASE_READY)
                    {
                        System.out.println("Cauldron not ready — item ignored: " + item.getItem().getDisplayName().getString());
                        continue;
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

                    // // Begin the brewing process if the cauldron is filled with water, but not yet ready for brewing
                    // if (itemStack.getItem() == Items.NETHER_WART)
                    // {
                    //     // If the item is Nether Wart, check if the cauldron is empty
                    //     if (currentState == CauldronBrewState.EMPTY)
                    //     {
                    //         // Set cauldron state to BASE_READY
                    //         CauldronStateTracker.setState(cauldronPos, CauldronBrewState.BASE_READY);

                    //         // Log update
                    //         System.out.println("Cauldron at " + cauldronPos + " is now BASE_READY with Nether Wart!");

                    //         // Optional: Force block update if needed for tinting logic
                    //         level.sendBlockUpdated(cauldronPos, state, state, 3);

                    //         // Discard the Nether Wart item
                    //         item.discard();

                    //         // Skip the rest of the ingredient handling logic
                    //         continue;
                    //     }
                    //     else
                    //     {
                    //         System.out.println("Nether Wart rejected — cauldron not empty.");
                    //         item.discard();
                    //         continue;
                    //     }
                    // }

                    // Safely get or create the ingredient list
                    List<ItemStack> ingredientList = ingredientsByCauldron.computeIfAbsent(cauldronPos, k -> new ArrayList<>());

                    // Add the item
                    ingredientList.add(item.getItem().copy());

                    // Log contents safely
                    System.out.println("Current ingredients in " + cauldronPos + ":");
                    for (ItemStack stack : ingredientList)
                    {
                        System.out.println("- " + stack.getCount() + "x " + stack.getDisplayName().getString());
                    }

                    // After adding the new item, check for a matching recipe
                    Optional<BrewResult> match = BrewRecipeRegistry.match(ingredientList);

                    // If a matching recipe is found, update the cauldron
                    if (match.isPresent())
                    {
                        // Get the result of the brewing match and its color
                        BrewResult result = match.get();
                        BrewColorType color = BrewRecipeRegistry.getColorForPotion(result.basePotion());

                        // Update cauldron blockstate: set color + BREW_READY
                        BlockState newState = state
                            .setValue(BrewCauldronBlock.LEVEL, 3) // start with full doses
                            .setValue(BrewCauldronBlock.COLOR, color)
                            .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BREW_READY);
                        level.setBlock(cauldronPos, newState, 3);

                        // Update state tracker
                        CauldronStateTracker.setState(cauldronPos, CauldronBrewState.BREW_READY);
                        CauldronStateTracker.setResult(cauldronPos, result);
                        CauldronStateTracker.setDoses(cauldronPos, 3);

                        // Clear ingredients to reset for next brew
                        clearIngredients(cauldronPos);

                        // Feedback for audio and visual effects
                        level.playSound(null, cauldronPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                                        cauldronPos.getX() + 0.5, cauldronPos.getY() + 1.0, cauldronPos.getZ() + 0.5,
                                        0.0, 0.1, 0.0);

                        // Log the successful brew match
                        System.out.println("Brew started at " + cauldronPos + " with potion: " + result.basePotion() + " color: " + color);
                    }
                    else
                    {
                        // You made a witch's wart potion!
                        BlockState failedState = state
                            .setValue(BrewCauldronBlock.LEVEL, 3) // or current level
                            .setValue(BrewCauldronBlock.COLOR, BrewColorType.WART)
                            .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.FAILED);
                        level.setBlock(cauldronPos, failedState, 3);

                        // Update the state tracker
                        CauldronStateTracker.setState(cauldronPos, CauldronBrewState.FAILED);
                        CauldronStateTracker.setDoses(cauldronPos, 3);
                        clearIngredients(cauldronPos);
                    }

                    // Log the ingredient added to the cauldron
                    System.out.println("Ingredient added to cauldron at " + cauldronPos + ": " +
                        item.getItem().getDisplayName().getString());
                    
                    // Optional debugging output
                    System.out.println("Current ingredients in " + cauldronPos + ":");
                    for (ItemStack stack : ingredientList)
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