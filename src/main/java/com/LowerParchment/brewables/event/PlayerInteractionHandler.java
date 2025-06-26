// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;
import com.LowerParchment.brewables.BrewablesMod;

// Import dependencies
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// PlayerInteractionHandler class to handle player interactions with cauldrons
@Mod.EventBusSubscriber(modid = BrewablesMod.MODID)
public class PlayerInteractionHandler
{
    // Shared map reference
    public static Map<BlockPos, List<ItemStack>> ingredientsByCauldron =
        ItemInCauldronHandler.getIngredientsMap();

    // This method handles right-click interactions with blocks, specifically water cauldrons.
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        System.out.println("🎯 RightClickBlock triggered!");
        Player player = event.getEntity();
        var level = event.getLevel();

        // use immutable() to ensure thread safety
        BlockPos pos = BlockPos.containing(event.getHitVec().getLocation()).immutable();

        // DEBUG REMOVE LATER
        var state = event.getLevel().getBlockState(pos);
        var block = state.getBlock();
        System.out.println("🔍 Block at " + pos + ": " + block.getDescriptionId());
        // DEBUG REMOVE LATER

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Check if the player is holding a stirring rod and if the block is a water cauldron
        if (!heldItem.getItem().getDescriptionId().equals("item.brewables.stirring_rod")) {
            System.out.println("⚠️ Not holding stirring rod. Held: " + heldItem.getDescriptionId());
            return;
        }

        // Check if the clicked block is a water cauldron
        if (block != Blocks.WATER_CAULDRON) {
            return;
        }

        // DEBUG REMOVE LATER
        System.out.println("🧪 Checking cauldron at " + pos);
        System.out.println("📦 Known tracked cauldrons: " + ingredientsByCauldron.keySet());
        // DEBUG REMOVE LATER

        // Check if the cauldron is empty or not. If it has items, display them.
        List<ItemStack> ingredients = ingredientsByCauldron.getOrDefault(pos, new ArrayList<>());
        if (ingredients.isEmpty())
        {
            player.displayClientMessage(Component.literal("Cauldron is empty."), true);
            return;
        }
        
        // Show ingredient list for feedback
        player.displayClientMessage(Component.literal("🍲 Ingredients in this cauldron:"), false);
        for (ItemStack stack : ingredients)
        {
            player.displayClientMessage(Component.literal("- " + stack.getCount() + "x " + stack.getDisplayName().getString()), false);
        }

        // DEBUG REMOVE LATER
        System.out.println("🧪 Ingredient count at " + pos + ": " + ingredients.size());
        // DEBUG REMOVE LATER
        

        // Check for specific ingredients to determine the outcome of the brewing
        boolean hasNetherWart = false;
        boolean hasBlazePowder = false;

        // Loop through the ingredients to check for Nether Wart and Blaze Powder
        for (ItemStack stack : ingredients) {
            if (stack.is(Items.NETHER_WART)) hasNetherWart = true;
            if (stack.is(Items.BLAZE_POWDER)) hasBlazePowder = true;
        }

        //DEBUG
        System.out.println("🧪 Has Nether Wart? " + hasNetherWart);
        System.out.println("🔥 Has Blaze Powder? " + hasBlazePowder);
        //DEBUG

        // Determine the outcome based on the ingredients
        // If both Nether Wart and Blaze Powder are present, brew a custom potion
        if (hasNetherWart && hasBlazePowder)
        {
            player.displayClientMessage(Component.literal("✨ Successful brew! You made a custom potion."), true);

            // Spawn potion
            ItemStack potion = new ItemStack(net.minecraft.world.item.Items.POTION);
            ItemEntity potionEntity = new ItemEntity(
                level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                potion
            );
            potionEntity.setDeltaMovement(0, 0.2, 0);
            level.addFreshEntity(potionEntity);
        }
        // You brewed a Witch's Wart potion
        else
        {
            player.displayClientMessage(Component.literal("💀 Witch’s Wart brewed. That can't be right..."), true);

            // Spawn Witch’s Wart (placeholder: fermented spider eye)
            ItemStack wart = new ItemStack(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE);
            ItemEntity wartEntity = new ItemEntity(
                level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                wart
            );
            level.addFreshEntity(wartEntity);
        }

        // Cancel the event to prevent default interaction behavior
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // Clear ingredients from the cauldron
        ingredientsByCauldron.remove(pos);
    }
}
