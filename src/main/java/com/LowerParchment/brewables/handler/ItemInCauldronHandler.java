// Import user defined dependencies
package com.LowerParchment.brewables.handler;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.block.BrewColorType;
import com.LowerParchment.brewables.event.CauldronBrewState;

// Import Minecraft, Forge, and Java dependencies
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import java.util.*;

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

    // Called every server tick to:
    // - Check all item entities near players
    // - Detect items in water-filled custom cauldrons
    // - Handle ingredient dropping logic
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
                if (!item.isAlive()) continue;
            
                BlockPos cauldronPos = BlockPos.containing(item.position().x, item.position().y - 0.1, item.position().z).immutable();
                BlockState state = level.getBlockState(cauldronPos);
            
                // Skip non-cauldrons immediately
                if (state.getBlock() != BrewablesMod.BREW_CAULDRON.get())
                {
                    BrewablesMod.LOGGER.debug("[ITEM HANDLER] Skipping non-cauldrons at {}: block={}", cauldronPos,
                        BuiltInRegistries.BLOCK.getKey(state.getBlock()));
                    continue;
                }

                // Make sure that the cauldron has water in it
                if (!state.hasProperty(BrewCauldronBlock.LEVEL) || state.getValue(BrewCauldronBlock.LEVEL) == 0)
                {
                    ResourceLocation blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                    BrewablesMod.LOGGER.debug("[ITEM HANDLER] DRY CHECK at {}: block={}, LEVEL property? {}, LEVEL={}",
                        cauldronPos,
                        blockName,
                        state.hasProperty(BrewCauldronBlock.LEVEL),
                        state.hasProperty(BrewCauldronBlock.LEVEL) ? state.getValue(BrewCauldronBlock.LEVEL) : "N/A"
                    );
                    continue;
                }
        
                // Get the current state of the cauldron at this position and check if it is ready for new ingredients
                CauldronBrewState currentState = CauldronStateTracker.getState(cauldronPos);
                ItemStack itemStack = item.getItem();

                // NEW EARLY EXIT: if the cauldron state is EMPTY, and LEVEL=0, skip entirely
                if (currentState == CauldronBrewState.EMPTY && state.getValue(BrewCauldronBlock.LEVEL) == 0)
                {
                    continue;
                }

                // Cleanup: if cauldron state is EMPTY but ingredients still exist, clear them
                if (currentState == CauldronBrewState.EMPTY && ingredientsByCauldron.containsKey(cauldronPos))
                {
                    BrewablesMod.LOGGER.debug("[ITEM HANDLER] Clearing stale ingredients at {} since cauldron reset to EMPTY.", cauldronPos);
                    clearIngredients(cauldronPos);
                }

                // Handle Nether Wart special case:
                // - Only allowed if cauldron is non-empty and in EMPTY state
                // - Triggers BASE_READY transition + visuals
                if (itemStack.getItem() == Items.NETHER_WART
                    && currentState == CauldronBrewState.EMPTY
                    && state.getValue(BrewCauldronBlock.LEVEL) > 0)
                {
                    BrewablesMod.LOGGER.debug(
                            "[ITEM HANDLER] Nether Wart triggered block update at {} with LEVEL={}", cauldronPos,
                            state.getValue(BrewCauldronBlock.LEVEL));

                    // Play a sound effect for adding Nether Wart
                    level.playSound(
                    null,
                    cauldronPos,
                    SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + level.random.nextFloat() * 0.3F);

                    // Spawn a particle effect for visual feedback
                    if (level instanceof ServerLevel serverLevel)
                    {
                        serverLevel.sendParticles(
                            ParticleTypes.SPLASH,
                            cauldronPos.getX() + 0.5,
                            cauldronPos.getY() + 0.9,
                            cauldronPos.getZ() + 0.5,
                            8, // count
                            0.3, 0.1, 0.3, // x, y, z spread
                            0.02 // speed
                        );
                    }

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
                ingredientList.add(itemStack.copy());

                // Play a sound effect for adding an ingredient
                level.playSound(
                        null,
                        cauldronPos,
                        SoundEvents.AMETHYST_BLOCK_HIT,
                        SoundSource.BLOCKS,
                        1.4F,
                        0.9F + level.random.nextFloat() * 0.1F);

                // Spawn a particle effect for visual feedback
                if (level instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(
                            ParticleTypes.SPLASH,
                            cauldronPos.getX() + 0.5,
                            cauldronPos.getY() + 0.9,
                            cauldronPos.getZ() + 0.5,
                            8, // count
                            0.3, 0.1, 0.3, // x, y, z spread
                            0.02 // speed
                    );
                }

                // Log added ingredient
                System.out.println("Added " + itemStack.getCount() + "x " + itemStack.getDisplayName().getString() + " to cauldron at " + cauldronPos);

                // Discard the item entity
                item.discard();
            }
        }
    }

    // Helper method to check if a specific item is contained in the cauldron at a given position.
    public static boolean contains(BlockPos pos, Item target)
    {
        List<ItemStack> items = ingredientsByCauldron.get(pos);
        if (items == null) return false;

        for (ItemStack stack : items)
        {
            if (stack.getItem() == target)
            {
                return true;
            }
        }
        return false;
    }
}