// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;

import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
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

        // Decrement the dose count in the cauldron
        System.out.println("Bottle used at: " + pos);
        System.out.println("Doses before decrement: " + CauldronStateTracker.getDoses(pos));
        CauldronStateTracker.decrementDoses(pos);

        // Update the visual fill level of the cauldron
        int dosesRemaining = CauldronStateTracker.getDoses(pos);
        int newLevel = Math.max(1, dosesRemaining); // 1-3 only

        BlockState newState = state.setValue(BrewCauldronBlock.LEVEL, newLevel);
        level.setBlock(pos, newState, 3);

        // Update the cauldron level and color
        int currentLevel = state.getValue(BrewCauldronBlock.LEVEL);
        int updatedLevel = currentLevel - 1;

        // If the level is above 0, update the level of the water until its depleted
        if (updatedLevel >= 0)
        {
            state = state.setValue(BrewCauldronBlock.LEVEL, updatedLevel);
            level.setBlock(pos, state, 3);

            if (updatedLevel == 0)
            {
                CauldronStateTracker.reset(pos);
                BlockState cleared = state
                    .setValue(BrewCauldronBlock.LEVEL, 0)
                    .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR);
                level.setBlock(pos, cleared, 3);
            }
        }
        else
        {
            // Drain completely and reset state/tint
            level.setBlock(pos, state
                .setValue(BrewCauldronBlock.LEVEL, 0)
                .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR), 3);

            // Reset the cauldron state and clear ingredients
            CauldronStateTracker.reset(pos);
            System.out.println("Cauldron at " + pos + " is now fully empty.");
        }

        // If doses reach zero, reset the cauldron state
        if (CauldronStateTracker.isEmpty(pos)) {
            CauldronStateTracker.reset(pos);;

            // Reset block to be empty visually as well
            BlockState cleared = state.setValue(BrewCauldronBlock.LEVEL, 0)
            .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR);
            level.setBlock(pos, cleared, 3);

            // For debug purposes:
            System.out.println("Cauldron at " + pos + " has been reset after 4 potions.");
        }

        // Cancel the event to prevent default behavior of double right-clicking
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // Replace glass bottle with the potion
        heldItem.shrink(1);
        if (!player.getInventory().add(result))
            player.drop(result, false);
    }
}
