// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry.BrewResult;

// Java Utilities imports
import java.util.*;

// Minecraft Forge imports
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;

// Player interaction handler for right-click events on blocks, specifically Brewables mod cauldrons
@Mod.EventBusSubscriber(modid = BrewablesMod.MODID)
public class RightClickHandler
{
    // DEBUG
    static {
        BrewablesMod.LOGGER.info("[RightClickHandler] Class loaded — if you see this, Forge registered your handler.");
    }

    // Class available declarations
    private static final Map<BlockPos, Long> lastUseTime = new HashMap<>();
    private static final long LOCKOUT_MS = 100;

    // Shared map reference for ingredients in cauldrons
    public static final Map<BlockPos, List<ItemStack>> ingredientsByCauldron = ItemInCauldronHandler.getIngredientsMap();

    // Handle all possible right click interactions
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        // Guard: Ensure the interaction is with the main hand and not the off-hand
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos().immutable();
        BlockState state = level.getBlockState(pos);
        var block = state.getBlock();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        // Determine the action based on the held item
        String actionForSwitchCase;
        if (heldItem.is(Items.WATER_BUCKET))    // Water Bucket
        {
            actionForSwitchCase = "REFILL";
        }
        else if (heldItem.is(Items.GLASS_BOTTLE))   // Glass Bottle
        {
            actionForSwitchCase = "BOTTLE";
        }
        else if (heldItem.getItem().getDescriptionId().equals("item.brewables.stirring_rod"))   // Stirring Rod
        {
            actionForSwitchCase = "STIRRING_ROD";
        }
        else
        {
            actionForSwitchCase = "NONE";
        }

        // Guard: Don't do anything if the right click isn't on a Brew Cauldron Block
        if (block != BrewablesMod.BREW_CAULDRON.get()) return;

        // Switch case to handle different actions based on the held item
        switch (actionForSwitchCase)
        {
            // Handle refilling the cauldron with a water bucket
            case "REFILL":
            {
                // If the cauldron is empty
                if (state.getValue(BrewCauldronBlock.LEVEL) == 0)
                {
                    BrewablesMod.LOGGER.debug("Refill triggered at {}: LEVEL will be set to 3", pos);

                    // Refill water level and reset tint. Then update the block data.
                    BlockState newState = state
                            .setValue(BrewCauldronBlock.LEVEL, 3)
                            .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR);
                    level.setBlock(pos, newState, 3);
                    level.sendBlockUpdated(pos, state, newState, 3);
                    BrewablesMod.LOGGER.debug("[REFILL] Cauldron refilled at {}: LEVEL=3, COLOR=CLEAR", pos);

                    // Reset the brew states
                    CauldronStateTracker.setState(pos, CauldronBrewState.EMPTY);
                    CauldronStateTracker.setDoses(pos, 0);

                    // Swap held water bucket for an empty one since the player poured it into the cauldron
                    if (!player.isCreative())
                    {
                        // Remove the water bucket from the player's inventory
                        heldItem.shrink(1);

                        // Give the player an empty bucket
                        player.getInventory().add(new ItemStack(Items.BUCKET));
                    }

                    // Display a message to the player confirming the refill
                    player.displayClientMessage(Component.literal("[Water Bucket] You refilled the cauldron with water."), true);

                    // Cancel the event to prevent default behavior of double right-clicking
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
                else
                {
                    // Prevent vanilla behavior replacing the block
                    player.displayClientMessage(Component.literal("[Water Bucket] The cauldron is already filled."), true);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }

                // Case Finished
                break;
            }

            // Handle filling a bottle from the cauldron
            case "BOTTLE":
            {
                // Get cauldron brew state early
                CauldronBrewState brewState = CauldronStateTracker.getState(pos);

                // Early exit: skip if cauldron isn't BREW_READY or FAILED (i.e., EMPTY or in-progress)
                if (brewState != CauldronBrewState.BREW_READY && brewState != CauldronBrewState.FAILED)
                {
                    BrewablesMod.LOGGER.debug("[BOTTLE] Skipping: cauldron not in BREW_READY or FAILED state at {}", pos);
                    break;
                }

                // Prevent rapid double-triggering by adding a soft lockout per cauldron position
                long now = System.currentTimeMillis();
                long lastUsed = lastUseTime.getOrDefault(pos, 0L);
                if (now - lastUsed < LOCKOUT_MS) {
                    System.out.println("[BOTTLE] Skipping double-trigger at: " + pos);
                    return;
                }
                lastUseTime.put(pos, now);

                // Determine the correct potion, or witch's wart to fill the bottle with
                ItemStack result;
                if (brewState == CauldronBrewState.FAILED)  // Witch's Wart
                {
                    result = new ItemStack(BrewablesMod.WITCHS_WART.get());
                }
                else    // Potion
                {
                    // Retrieve the saved BrewResult from the tracker (originally matched during stirring)
                    BrewResult brewResult = CauldronStateTracker.getResult(pos);
                    Potion base = brewResult.basePotion();
                    boolean isStrong = brewResult.useGlowstone();
                    boolean isLong = brewResult.useRedstone();
                    boolean isSplash = brewResult.useGunpowder();
                    Potion finalPotion = base;

                    // Apply potency/duration modifiers for redstone, glowstone, and gunpowder
                    if (isStrong)
                    {
                        if (Potions.HEALING.equals(base)) finalPotion = Potions.STRONG_HEALING;
                        else if (Potions.POISON.equals(base)) finalPotion = Potions.STRONG_POISON;
                        else if (Potions.REGENERATION.equals(base)) finalPotion = Potions.STRONG_REGENERATION;
                        else if (Potions.STRENGTH.equals(base)) finalPotion = Potions.STRONG_STRENGTH;
                        else if (Potions.HARMING.equals(base)) finalPotion = Potions.STRONG_HARMING;
                        else if (Potions.SLOWNESS.equals(base)) finalPotion = Potions.STRONG_SLOWNESS;
                    }
                    else if (isLong)
                    {
                        if (Potions.SWIFTNESS.equals(base)) finalPotion = Potions.LONG_SWIFTNESS;
                        else if (Potions.LEAPING.equals(base)) finalPotion = Potions.LONG_LEAPING;
                        else if (Potions.FIRE_RESISTANCE.equals(base)) finalPotion = Potions.LONG_FIRE_RESISTANCE;
                        else if (Potions.WATER_BREATHING.equals(base)) finalPotion = Potions.LONG_WATER_BREATHING;
                        else if (Potions.NIGHT_VISION.equals(base)) finalPotion = Potions.LONG_NIGHT_VISION;
                        else if (Potions.REGENERATION.equals(base)) finalPotion = Potions.LONG_REGENERATION;
                        else if (Potions.SLOWNESS.equals(base)) finalPotion = Potions.LONG_SLOWNESS;
                        else if (Potions.POISON.equals(base)) finalPotion = Potions.LONG_POISON;
                        else if (Potions.WEAKNESS.equals(base)) finalPotion = Potions.LONG_WEAKNESS;
                        else if (Potions.TURTLE_MASTER.equals(base)) finalPotion = Potions.LONG_TURTLE_MASTER;
                    }

                    // Create the potion item stack based on the final potion properties
                    Item resultItem = isSplash ? Items.SPLASH_POTION : Items.POTION;
                    result = new ItemStack(resultItem);
                    PotionUtils.setPotion(result, finalPotion);
                }

                // Decrement the dose count in the cauldron
                BrewablesMod.LOGGER.debug("[BOTTLE] Bottle used at {}", pos);
                BrewablesMod.LOGGER.debug("[BOTTLE] Doses before decrement: {}", CauldronStateTracker.getDoses(pos));
                CauldronStateTracker.decrementDoses(pos);

                // Update the cauldron's visual water LEVEL after taking a dose
                int dosesRemaining = CauldronStateTracker.getDoses(pos);
                int newLevel = Math.max(0, dosesRemaining);
                BlockState updatedState = state
                    .setValue(BrewCauldronBlock.LEVEL, newLevel)
                    .setValue(BrewCauldronBlock.COLOR, state.getValue(BrewCauldronBlock.COLOR))
                    .setValue(BrewCauldronBlock.BREW_STATE, state.getValue(BrewCauldronBlock.BREW_STATE));
                
                // Update the cauldron block data
                BrewablesMod.LOGGER.debug("[BOTTLE] Triggered before that one spot. Level here is: {} at {}", newLevel, pos);
                level.setBlock(pos, updatedState, 3);
                level.sendBlockUpdated(pos, state, updatedState, 3);
                BrewablesMod.LOGGER.debug("[BOTTLE] Triggered after that one spot. Level here is: {} at {}", newLevel, pos);

                // If doses hit 0, reset the cauldron
                if (dosesRemaining == 0)
                {
                    CauldronStateTracker.reset(pos);
                    ItemInCauldronHandler.clearIngredients(pos);

                    // Force chunk + block state reset: replace with air first
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                    // Re-place fresh cauldron with correct empty state
                    BlockState cleared = BrewablesMod.BREW_CAULDRON.get().defaultBlockState()
                        .setValue(BrewCauldronBlock.LEVEL, 0)
                        .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR)
                        .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.EMPTY);
                    level.setBlock(pos, cleared, 3);

                    level.sendBlockUpdated(pos, Blocks.AIR.defaultBlockState(), cleared, 3);
                    level.markAndNotifyBlock(pos, level.getChunkAt(pos), Blocks.AIR.defaultBlockState(), cleared, 3, 512);

                    BrewablesMod.LOGGER.debug("[BOTTLE] Cauldron force-reset by air-swap at {}", pos);
                }

                // Replace glass bottle with the resulting potion
                heldItem.shrink(1);
                if (!player.getInventory().add(result))
                    player.drop(result, false);
                BrewablesMod.LOGGER.debug("[BOTTLE] Final LEVEL after bottle: {}", level.getBlockState(pos).getValue(BrewCauldronBlock.LEVEL));

                // Cancel the event
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                // Case Finished
                break;
            }
            // Handle stirring rod interactions
            case "STIRRING_ROD":
            {
                // Check the current cauldron state and any ingredients present
                CauldronBrewState brewState = CauldronStateTracker.getState(pos);
                List<ItemStack> ingredients = ingredientsByCauldron.getOrDefault(pos, new ArrayList<>());

                // The cauldron is empty and you tried to stir it
                if (ingredients.isEmpty())
                {
                    player.displayClientMessage(Component.literal("I need to add ingredients first..."), true);
                    break;
                }

                // Show the ingredients that were in the cauldron post stirring
                if (brewState == CauldronBrewState.BREW_READY || brewState == CauldronBrewState.FAILED)
                {
                    player.displayClientMessage(Component.literal("Ingredients in this cauldron:"), false);
                    for (ItemStack stack : ingredients) {
                        player.displayClientMessage(
                                Component.literal("- " + stack.getCount() + "x " + stack.getDisplayName().getString()),
                                false);
                    }
                }

                // Attempt to match the ingredients to a valid brew recipe
                Optional<BrewRecipeRegistry.BrewResult> result = BrewRecipeRegistry.match(ingredients);
                BrewablesMod.LOGGER.debug("[STIR] Matching ingredients at {}: {}", pos, ingredients);

                // Fetch the current level of the water in the cauldron
                int dosesRemaining = CauldronStateTracker.getDoses(pos);
                int updatedLevel = Math.max(0, dosesRemaining);
                if (updatedLevel == 0)
                    updatedLevel = state.getValue(BrewCauldronBlock.LEVEL);

                // Determine what kind of brew was made
                if (result.isPresent())     // YOU MADE A POTION!
                {
                    // Successfully brewed a potion: set its properties and notify the player
                    BrewRecipeRegistry.BrewResult brew = result.get();
                    CauldronStateTracker.setState(pos, CauldronBrewState.BREW_READY);
                    CauldronStateTracker.setDoses(pos, updatedLevel);
                    CauldronStateTracker.setResult(pos, brew);
                    player.displayClientMessage(Component.literal("Successful brew! You made a potion."), true);

                    // Update the block state
                    BlockState brewedState = state
                        .setValue(BrewCauldronBlock.LEVEL, 
                                    updatedLevel)
                        .setValue(BrewCauldronBlock.COLOR, BrewRecipeRegistry.getColorForPotion(brew.basePotion()))
                        .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BREW_READY);
                    level.setBlock(pos, brewedState, 3);
                    level.sendBlockUpdated(pos, state, brewedState, 3);
                    BrewablesMod.LOGGER.debug("[STIR] LEVEL set to {} at {} with successful brew", updatedLevel, pos);

                    // EARLY EXIT HERE: prevents the handler from continuing and incorrectly
                    // firing the "already brewed" message during the same stir click
                    break;
                }

                // The cauldron is full of potion, or witch's wart, but you tried to stir it anyway
                if (brewState == CauldronBrewState.BREW_READY || brewState == CauldronBrewState.FAILED)
                {
                    player.displayClientMessage(Component.literal("This cauldron is full of potion. Use a bottle!"),
                            true);
                    break;
                }

                else    // YOU MADE A WITCH'S WART!
                {
                    player.displayClientMessage(Component.literal("Witch’s Wart brewed. That can't be right..."), true);
                    
                    // Wart brew is ready, so set it's disgusting properties
                    CauldronStateTracker.setState(pos, CauldronBrewState.FAILED);
                    CauldronStateTracker.setDoses(pos, updatedLevel);

                    // Update the block state
                    BlockState wartState = state
                        .setValue(BrewCauldronBlock.LEVEL, 
                                    updatedLevel)
                        .setValue(BrewCauldronBlock.COLOR, BrewColorType.WART)
                        .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.FAILED);
                    level.setBlock(pos, wartState, 3);
                    level.sendBlockUpdated(pos, state, wartState, 3);
                    BrewablesMod.LOGGER.debug("[STIR] LEVEL set to {} at {} with failed brew", updatedLevel, pos);
                }

                // Cancel the event to prevent default interaction behavior
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                // Clear ingredients from the cauldron after a tick has passed, otherwise it will be cleared immediately and the player won't see the potion spawn.
                if (!level.isClientSide)
                {
                    level.getServer().execute(() ->
                    {
                        ingredientsByCauldron.remove(pos);
                        BrewablesMod.LOGGER.info("[STIR] Cleared ingredients from {}", pos);
                    });
                }

                // Case Finished
                break;
            }
            // Any other possible interaction
            default:
            {
                break;
            }
        }
    }
}