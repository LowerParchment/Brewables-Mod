// Import user defined dependencies
package com.LowerParchment.brewables.event;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;
import com.LowerParchment.brewables.item.WitchsWartItem;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry.BrewResult;

// Import Minecraft, Forge, and Java dependencies
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// Player interaction handler for right-click events on blocks, specifically Brewables mod cauldrons
@Mod.EventBusSubscriber(modid = BrewablesMod.MODID)
public class RightClickHandler
{
    // Shared data for interaction tracking and ingredient memory
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

        // Ignore client-side event entirely
        if (level.isClientSide) return;

        // Detect context-specific cauldron interaction based on held item
        String actionForSwitchCase;
        if (heldItem.is(Items.WATER_BUCKET))
        {
            actionForSwitchCase = "REFILL";
        }
        else if (heldItem.is(Items.GLASS_BOTTLE))
        {
            actionForSwitchCase = "BOTTLE";
        }
        else if (heldItem.getItem().getDescriptionId().equals("item.brewables.stirring_rod"))
        {
            actionForSwitchCase = "STIRRING_ROD";
        }
        else actionForSwitchCase = "NONE";

        // Guard: Only proceed if the block is our custom brew cauldron
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
                    // Refill water level and reset tint. Then update the block data.
                    BlockState newState = state
                            .setValue(BrewCauldronBlock.LEVEL, 3)
                            .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR);
                    level.setBlock(pos, newState, 3);
                    level.sendBlockUpdated(pos, state, newState, 3);

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

                    // Cancel the event to prevent vanilla right-clicking behavior (e.g. water bucket placing)
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
                // Play the bottle fill sound effect
                level.playSound(
                        null,
                        pos,
                        SoundEvents.BOTTLE_FILL,
                        SoundSource.BLOCKS,
                        1.2F,
                        0.9F + level.random.nextFloat() * 0.1F);

                CauldronBrewState brewState = CauldronStateTracker.getState(pos);

                // Early exit if cauldron isn't BREW_READY or FAILED
                if (brewState != CauldronBrewState.BREW_READY && brewState != CauldronBrewState.FAILED) break;

                // Prevent rapid double-triggering by adding soft lockout
                long now = System.currentTimeMillis();
                long lastUsed = lastUseTime.getOrDefault(pos, 0L);
                if (now - lastUsed < LOCKOUT_MS) return;
                lastUseTime.put(pos, now);

                // Determine the correct potion or witch's wart
                ItemStack result;
                if (brewState == CauldronBrewState.FAILED)
                {
                    BrewResult wartResult = CauldronStateTracker.getResult(pos);
                    boolean isSplash = wartResult.useGunpowder();
                    boolean isLingering = wartResult.useDragonBreath();
                    result = WitchsWartItem.getWitchsWartStack(isSplash, isLingering);
                }
                else
                {
                    BrewResult brewResult = CauldronStateTracker.getResult(pos);
                    Potion base = brewResult.basePotion();
                    boolean isStrong = brewResult.useGlowstone();
                    boolean isLong = brewResult.useRedstone();
                    boolean isSplash = brewResult.useGunpowder();
                    boolean isLingering = brewResult.useDragonBreath();
                    Potion finalPotion = base;

                    // Apply potency/duration modifiers
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

                    // Create the potion stack based on the final potion and modifiers
                    result = BrewRecipeRegistry.createPotionStack(finalPotion, isSplash, isLingering);
                }

                // Check if the player has at least one glass bottle in their inventory
                if (!heldItem.isEmpty())
                {
                    // Give 1 potion
                    ItemStack potionCopy = result.copy();
                    if (!player.getInventory().add(potionCopy))
                    {
                        player.drop(potionCopy, false);
                    }
                    heldItem.shrink(1);

                    // Decrement the cauldron's level/doses
                    int currentLevel = state.getValue(BrewCauldronBlock.LEVEL);
                    int newLevel = currentLevel - 1;
                    if (newLevel > 0)
                    {
                        // Update block state to show reduced level
                        BlockState newState = state.setValue(BrewCauldronBlock.LEVEL, newLevel);
                        level.setBlock(pos, newState, Block.UPDATE_ALL);
                        level.sendBlockUpdated(pos, state, newState, Block.UPDATE_ALL);

                        // Set the new dose level in the tracker
                        CauldronStateTracker.setDoses(pos, newLevel);
                    }
                    else
                    {
                        // Reset cauldron when last dose is taken
                        BlockState newState = BrewablesMod.BREW_CAULDRON.get().defaultBlockState()
                                .setValue(BrewCauldronBlock.LEVEL, 0)
                                .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR)
                                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.EMPTY);
                        level.setBlock(pos, newState, Block.UPDATE_ALL);
                        level.sendBlockUpdated(pos, state, newState, Block.UPDATE_ALL);

                        // Reset the brew state tracker and clear ingredients
                        CauldronStateTracker.reset(pos);
                        ItemInCauldronHandler.clearIngredients(pos);
                    }

                    // Cancel the event
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                else
                {
                    // If the player has no glass bottles, show a message
                    player.displayClientMessage(Component.literal("You need at least 1 glass bottle to retrieve a dose."), true);
                }

                // Case Finished
                break;
            }

            // Handle stirring rod interactions
            case "STIRRING_ROD":
            {
                // Check the current cauldron state and any ingredients present
                CauldronBrewState brewState = CauldronStateTracker.getState(pos);
                List<ItemStack> ingredients = ingredientsByCauldron.getOrDefault(pos, new ArrayList<>());

                // Check for a campfire block below the cauldron
                BlockPos belowPos = pos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if (!(belowState.getBlock() instanceof CampfireBlock) || !belowState.getValue(CampfireBlock.LIT))
                {
                    if (!level.isClientSide())
                    {
                        player.displayClientMessage(Component.literal("I'll need a campfire to boil the water."), true);
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    break;
                }

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

                // Fetch the current level of the water in the cauldron
                int updatedLevel = 3;

                // Determine what kind of brew was made
                if (result.isPresent()) // YOU MADE A POTION!
                {
                    // Play the sound effect for successful brewing
                    level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.1F, 0.7F + level.random.nextFloat() * 0.2F);
                    level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.0F);

                    // Spawn a particle effect for visual feedback
                    if (level instanceof ServerLevel serverLevel)
                    {
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5,
                                pos.getY() + 0.9,
                                pos.getZ() + 0.5,
                                15,
                                0.4, 0.2, 0.4,
                                0.05);
                    }

                    // Successfully brewed a potion: set its properties and notify the player
                    BrewResult data = result.get();
                    Potion finalPotion = BrewRecipeRegistry.applyModifiers(
                        data.basePotion(),
                        data.useGlowstone(),
                        data.useRedstone()
                    );
                    CauldronStateTracker.setState(pos, CauldronBrewState.BREW_READY);
                    CauldronStateTracker.setDoses(pos, updatedLevel);
                    CauldronStateTracker.setResult(pos,
                    new BrewRecipeRegistry.BrewResult(finalPotion, data.useGlowstone(), data.useRedstone(), data.useGunpowder(), data.useDragonBreath()));
                    player.displayClientMessage(Component.literal("Successful brew! You made a potion."), true);

                    // Update the block state
                    BlockState brewedState = state
                            .setValue(BrewCauldronBlock.LEVEL,
                                    updatedLevel)
                            .setValue(BrewCauldronBlock.COLOR, BrewRecipeRegistry.getColorForPotion(finalPotion))
                            .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BREW_READY);
                    level.setBlock(pos, brewedState, 3);
                    level.sendBlockUpdated(pos, state, brewedState, 3);

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

                else // YOU MADE A WITCH'S WART!
                {
                    // Play the sound effect for successful brewing
                    level.playSound(
                            null,
                            pos,
                            SoundEvents.ENCHANTMENT_TABLE_USE,
                            SoundSource.BLOCKS,
                            1.0F,
                            0.8F + level.random.nextFloat() * 0.2F);

                    // Spawn a particle effect for visual feedback
                    if (level instanceof ServerLevel serverLevel)
                    {
                        serverLevel.sendParticles(
                                ParticleTypes.WITCH,
                                pos.getX() + 0.5,
                                pos.getY() + 0.9,
                                pos.getZ() + 0.5,
                                15,
                                0.4, 0.2, 0.4,
                                0.05);
                    }

                    // Inform the player that they brewed a Witch's Wart
                    player.displayClientMessage(Component.literal("Witch’s Wart brewed. That can't be right..."), true);

                    // Wart brew is ready, so set it's disgusting properties
                    CauldronStateTracker.setState(pos, CauldronBrewState.FAILED);
                    CauldronStateTracker.setDoses(pos, updatedLevel);

                    // Store whether or not the cauldron contains gunpowder or dragon's breath on a Witch's Wart brew
                    boolean hasGunpowder = ItemInCauldronHandler.contains(pos, Items.GUNPOWDER);
                    boolean hasDragonBreath = ItemInCauldronHandler.contains(pos, Items.DRAGON_BREATH);

                    // Set the result for the cauldron state tracker
                    CauldronStateTracker.setResult(pos, new BrewResult(Potions.WATER, false, false, hasGunpowder, hasDragonBreath));

                    // Update the block state to reflect the failed brew
                    BlockState wartState = state
                            .setValue(BrewCauldronBlock.LEVEL,
                                    updatedLevel)
                            .setValue(BrewCauldronBlock.COLOR, BrewColorType.WART)
                            .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.FAILED);
                    level.setBlock(pos, wartState, 3);
                    level.sendBlockUpdated(pos, state, wartState, 3);
                }

                // Cancel the event to prevent default interaction behavior
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                // Clear ingredients from the cauldron after a tick has passed, otherwise it
                // will be cleared immediately and the player won't see the potion spawn.
                if (!level.isClientSide)
                {
                    level.getServer().execute(() -> {
                        ingredientsByCauldron.remove(pos);
                    });
                }

                // Case Finished
                break;
            }
            // Any other possible interaction in the switch case
            default: break;
        }
    }
}