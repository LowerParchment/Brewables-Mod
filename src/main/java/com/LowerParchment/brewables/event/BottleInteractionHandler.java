// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;

import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// PlayerInteractionHandler class to handle player bottle interactions with cauldrons
@Mod.EventBusSubscriber(modid = BrewablesMod.MODID)
public class BottleInteractionHandler
{
    private static final Map<BlockPos, Long> lastUseTime = new HashMap<>();
    private static final long LOCKOUT_MS = 100; // You can tune this

    // This method handles right-click interactions with blocks, specifically water cauldrons.
    @SubscribeEvent
    public static void onBottleUse(PlayerInteractEvent.RightClickBlock event)
    {
        // Log the event for debugging purposes
        System.out.println("onBottleUse triggered! Hand = " + event.getHand());

        // Ensure the interaction is with the main hand and not the off-hand
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos().immutable();

        // Prevent rapid double-triggering by adding a soft lockout per cauldron
        // position
        long now = System.currentTimeMillis();
        long lastUsed = lastUseTime.getOrDefault(pos, 0L);
        if (now - lastUsed < LOCKOUT_MS) {
            System.out.println("Skipping double-trigger at: " + pos);
            return;
        }
        lastUseTime.put(pos, now);

        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Only act if they're holding a glass bottle and clicked a water cauldron
        if (!heldItem.is(Items.GLASS_BOTTLE) || state.getBlock() != BrewablesMod.BREW_CAULDRON.get())
            return;

        // Check if the cauldron is ready for brewing
        var brewState = CauldronStateTracker.getState(pos);
        
        // Cauldron hasn't been stirred yet
        if (brewState != CauldronBrewState.BREW_READY && brewState != CauldronBrewState.FAILED)
            return;

        // Give the appropriate potion
        ItemStack result;

        // Witches Wart
        if (brewState == CauldronBrewState.FAILED)
            result = new ItemStack(BrewablesMod.WITCHS_WART.get());
        // Potion
        else
        {
            BrewRecipeRegistry.BrewResult brewResult = CauldronStateTracker.getResult(pos);
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

            // Set base potion type
            Item resultItem = isSplash ? Items.SPLASH_POTION : Items.POTION;
            result = new ItemStack(resultItem);
            PotionUtils.setPotion(result, finalPotion);
        }

        // Fetch updated blockstate to avoid stale overwrites
        BlockState currentState = level.getBlockState(pos);

        // Decrement the dose count in the cauldron
        System.out.println("Bottle used at: " + pos);
        System.out.println("Doses before decrement: " + CauldronStateTracker.getDoses(pos));
        CauldronStateTracker.decrementDoses(pos);

        // Get updated doses
        int dosesRemaining = CauldronStateTracker.getDoses(pos);
        int newLevel = Math.max(0, dosesRemaining); // 0-3 only

        // Always update block LEVEL immediately to reflect new doses
        BlockState updatedState = currentState
            .setValue(BrewCauldronBlock.LEVEL, newLevel)
            .setValue(BrewCauldronBlock.COLOR, currentState.getValue(BrewCauldronBlock.COLOR))
            .setValue(BrewCauldronBlock.BREW_STATE, currentState.getValue(BrewCauldronBlock.BREW_STATE));
        level.setBlock(pos, updatedState, 3);
        BrewablesMod.LOGGER.debug("[BLOCK UPDATE TRACE] LEVEL set to {} at {} in <method>", newLevel, pos);
        level.sendBlockUpdated(pos, currentState, updatedState, 3);

        System.out.println("[BLOCK UPDATE] LEVEL=" + newLevel);

        // If doses hit 0, reset state tracker and block
        if (dosesRemaining <= 0)
        {
            CauldronStateTracker.reset(pos);

            BlockState cleared = updatedState
                .setValue(BrewCauldronBlock.LEVEL, 0)
                .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR)
                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.EMPTY);
            level.setBlock(pos, cleared, 3);
            BrewablesMod.LOGGER.debug("[BLOCK UPDATE TRACE] LEVEL set to {} at {} in <method>", newLevel, pos);

            level.sendBlockUpdated(pos, updatedState, cleared, 3);
            level.scheduleTick(pos, cleared.getBlock(), 1);

            System.out.println("[FORCE RESET] Block at " + pos + " set to EMPTY after tracker reset.");
        }

        // Cancel the event to prevent default behavior of double right-clicking
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // Replace glass bottle with the potion
        heldItem.shrink(1);
        if (!player.getInventory().add(result))
            player.drop(result, false);

        BlockState finalState = level.getBlockState(pos);
        System.out.println("[DEBUG] Final LEVEL after bottle: " + finalState.getValue(BrewCauldronBlock.LEVEL));
    }
}
