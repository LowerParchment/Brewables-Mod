// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import com.LowerParchment.brewables.event.CauldronBrewState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

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
}