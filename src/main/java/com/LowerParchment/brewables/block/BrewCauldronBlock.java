// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import com.LowerParchment.brewables.event.CauldronBrewState;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    // Override the getColor method to return the color of the cauldron based on its state.
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR, LEVEL, BREW_STATE);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                Player player, InteractionHand hand, BlockHitResult hit) {
        // Prevent vanilla water cauldron interaction logic
        return InteractionResult.PASS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Prevent vanilla cauldron tick behavior (e.g., rain refilling the cauldron)
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // Prevent vanilla CauldronBlock placement logic from resetting LEVEL
    }
}