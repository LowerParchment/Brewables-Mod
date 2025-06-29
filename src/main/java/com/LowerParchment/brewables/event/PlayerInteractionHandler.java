// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.event;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.handler.BrewRecipeRegistry;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;

// Import dependencies
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

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
        System.out.println("RightClickBlock triggered!");
        Player player = event.getEntity();
        var level = event.getLevel();

        // Get the position of the block that was clicked
        BlockPos pos = event.getPos().immutable();
        BlockState state = level.getBlockState(pos);
        var block = state.getBlock();

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Allow refilling the cauldron with a water bucket
        if (heldItem.is(Items.WATER_BUCKET) && block == BrewablesMod.BREW_CAULDRON.get())
        {
            // If the cauldron is empty
            if (state.getValue(BrewCauldronBlock.LEVEL) == 0)
            {
                BrewablesMod.LOGGER.debug("Refill triggered at {}: LEVEL will be set to {}", pos, level);

                // Refill water level and reset tint
                level.setBlock(pos, state
                        .setValue(BrewCauldronBlock.LEVEL, 3)
                        .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR), 3);
                BrewablesMod.LOGGER.debug("[REFILL] LEVEL set to {} at {}", 3, pos);

                // Reset the brew states
                CauldronStateTracker.setState(pos, CauldronBrewState.EMPTY);
                CauldronStateTracker.setDoses(pos, 0);

                // Swap bucket for empty one
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                    player.getInventory().add(new ItemStack(Items.BUCKET));
                }

                player.displayClientMessage(Component.literal("You refilled the cauldron with water."), true);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        // Check if the player is holding a stirring rod and if the block is a water cauldron
        if (!heldItem.getItem().getDescriptionId().equals("item.brewables.stirring_rod"))
        {
            player.displayClientMessage(Component.literal("I need to stir this first."), true);
            return;
        }

        // Check if the clicked block isn't a water cauldron
        if (block != BrewablesMod.BREW_CAULDRON.get()) return;

        // Check if the cauldron is empty or not. If it has items, display them.
        CauldronBrewState brewState = CauldronStateTracker.getState(pos);
        List<ItemStack> ingredients = ingredientsByCauldron.getOrDefault(pos, new ArrayList<>());

        // The cauldron is full of potion, or witch's wart, but you tried to stir it anyway
        if (brewState == CauldronBrewState.BREW_READY || brewState == CauldronBrewState.FAILED)
        {
            
            player.displayClientMessage(Component.literal("This cauldron is full of potion. Use a bottle!"), true);
            return;
        }

        // The cauldron is empty and you tried to stir it
        if (ingredients.isEmpty())
        {
            player.displayClientMessage(Component.literal("Cauldron is empty."), true);
            return;
        }
        
        // Show ingredient list for feedback
        player.displayClientMessage(Component.literal("Ingredients in this cauldron:"), false);
        for (ItemStack stack : ingredients)
        {
            player.displayClientMessage(Component.literal("- " + stack.getCount() + "x " + stack.getDisplayName().getString()), false);
        } 

        // Determine the outcome based on the ingredients
        // You made a potion!
        Optional<BrewRecipeRegistry.BrewResult> result = BrewRecipeRegistry.match(ingredients);
        System.out.println("Matching ingredients at " + pos + ": " + ingredients);

        // match potion doses to cauldron fill level
        int currentLevel = state.getValue(BrewCauldronBlock.LEVEL);

        if (result.isPresent())
        {
            BrewRecipeRegistry.BrewResult brew = result.get();
            player.displayClientMessage(Component.literal("Successful brew! You made a potion."), true);

            // Brew is ready, so set the potion and its properties
            CauldronStateTracker.setState(pos, CauldronBrewState.BREW_READY);
            CauldronStateTracker.setDoses(pos, currentLevel);
            CauldronStateTracker.setResult(pos, brew);

            // Update the block state
            BlockState latest = level.getBlockState(pos);
            BlockState brewedState = latest
                .setValue(BrewCauldronBlock.LEVEL, currentLevel)
                .setValue(BrewCauldronBlock.COLOR, BrewRecipeRegistry.getColorForPotion(brew.basePotion()))
                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BREW_READY);
            level.setBlock(pos, brewedState, 3);
            level.sendBlockUpdated(pos, latest, brewedState, 3);
            BrewablesMod.LOGGER.debug("[BLOCK UPDATE TRACE] LEVEL set to {} at {} in <method>", currentLevel, pos);
        }
        // You brewed a Witch's Wart potion
        else
        {
            player.displayClientMessage(Component.literal("Witch’s Wart brewed. That can't be right..."), true);
            
            // Wart brew is ready, so set the potion and it's disgusting properties
            CauldronStateTracker.setState(pos, CauldronBrewState.FAILED);
            CauldronStateTracker.setDoses(pos, currentLevel);

            // Update the block state
            BlockState latest = level.getBlockState(pos);
            BlockState wartState = latest
                .setValue(BrewCauldronBlock.LEVEL, currentLevel)
                .setValue(BrewCauldronBlock.COLOR, BrewColorType.WART)
                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.FAILED);
            level.setBlock(pos, wartState, 3);
            level.sendBlockUpdated(pos, latest, wartState, 3);
            BrewablesMod.LOGGER.debug("[BLOCK UPDATE TRACE] LEVEL set to {} at {} in <method>", currentLevel, pos);
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
                BrewablesMod.LOGGER.info("Cleared ingredients from {}", pos);
            });
        }
    }
}
