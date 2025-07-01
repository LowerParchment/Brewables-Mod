// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class BrewCauldronBlock extends CauldronBlock
{
    public static final EnumProperty<CauldronBrewState> BREW_STATE = EnumProperty.create("brew_state", CauldronBrewState.class);
    public static final EnumProperty<BrewColorType> COLOR = EnumProperty.create("color", BrewColorType.class);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    // Constructor for the BrewCauldronBlock class, setting the default state to clear color.
    public BrewCauldronBlock(Properties properties)
    {
        super(properties);

        // Set empty water level by default
        this.registerDefaultState(this.defaultBlockState().setValue(COLOR, BrewColorType.CLEAR).setValue(LEVEL, 0).setValue(BREW_STATE, CauldronBrewState.EMPTY));
    }

    // Define the state of the block, including its properties.
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR, LEVEL, BREW_STATE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        BrewablesMod.LOGGER.debug("[BLOCK TRACE] onPlace triggered at {} → LEVEL={}", pos, state.getValue(BrewCauldronBlock.LEVEL));
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(oldState, level, pos, newState, isMoving);

        if (!level.isClientSide) {
            // If the cauldron was replaced by a different block, clear stored data.
            if (oldState.getBlock() != newState.getBlock()) {
                BrewablesMod.LOGGER.debug("[BREW_CAULDRON] Cleared data on block removal at {}", pos);
                ItemInCauldronHandler.clearIngredients(pos);
                CauldronStateTracker.reset(pos);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level l && l.isClientSide) {
            l.sendBlockUpdated(pos, state, state, 3);
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }
}