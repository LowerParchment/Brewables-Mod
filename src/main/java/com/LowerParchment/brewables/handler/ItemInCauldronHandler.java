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
    public static void clearIngredients(BlockPos pos)
    {
        ingredientsByCauldron.remove(pos.immutable());
    }

    // This method is called on every world tick to check for items in water cauldrons.
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        var level = event.level;
        if (level.isClientSide) return;

        // Check all item entities in the world
        for (var player : level.players())
        {
            BlockPos playerPos = player.blockPosition();
            AABB searchBox = new AABB(playerPos).inflate(6); // 6-block radius
        
            // Find item entities within the search box
            // This will check for items within a 6-block radius of the player.
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox))
            {
                BlockPos cauldronPos = BlockPos.containing(item.position().x, item.position().y - 0.1, item.position().z).immutable();
                BrewablesMod.LOGGER.debug("Item landed over BlockPos: " + cauldronPos);
                BlockState state = level.getBlockState(cauldronPos);

                // Make sure that the cauldron has water in it
                if (!state.hasProperty(BrewCauldronBlock.LEVEL) || state.getValue(BrewCauldronBlock.LEVEL) == 0)
                {
                    BrewablesMod.LOGGER.debug("Cauldron at " + cauldronPos + " is dry - ignoring thrown item.");
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
                    if (itemStack.getItem() == Items.NETHER_WART && currentState == CauldronBrewState.EMPTY)
                    {
                        BrewablesMod.LOGGER.debug(
                                "[ITEM HANDLER] Nether Wart triggered block update at {} with LEVEL={}", cauldronPos,
                                state.getValue(BrewCauldronBlock.LEVEL));

                        // Set cauldron state to BASE_READY
                        CauldronStateTracker.setState(cauldronPos, CauldronBrewState.BASE_READY);

                        // Visually set it to brown for readiness
                        BlockState newState = state
                            .setValue(BrewCauldronBlock.COLOR, BrewColorType.BROWN)
                            .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.BASE_READY);
                        level.setBlock(cauldronPos, newState, 3);
                        level.sendBlockUpdated(cauldronPos, state, newState, 3);
                        BrewablesMod.LOGGER.debug("[BLOCK UPDATE TRACE] LEVEL set to {} at {} in <method>", level, cauldronPos);

                        // Set the doses and discard the nether wart item
                        CauldronStateTracker.setDoses(cauldronPos, state.getValue(BrewCauldronBlock.LEVEL));
                        item.discard();
                        continue;
                    }

                    // For all other items, only accept if we're already in BASE_READY state
                    if (currentState != CauldronBrewState.BASE_READY)
                    {
                        System.out.println("Cauldron not ready — item ignored: " + item.getItem().getDisplayName().getString());
                        continue;
                    }

                    // Add the item to the cauldron's ingredient list
                    List<ItemStack> ingredientList = ingredientsByCauldron.computeIfAbsent(cauldronPos, k -> new ArrayList<>());
                    ingredientList.add(item.getItem().copy());

                    // Log added ingredient
                    System.out.println("Added " + itemStack.getCount() + "x " + itemStack.getDisplayName().getString() + " to cauldron at " + cauldronPos);

                    // Discard the item entity
                    item.discard();
                }
            }
        }
    }
}