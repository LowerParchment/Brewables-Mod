// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BrewCauldronBlock extends CauldronBlock
{
    public static final EnumProperty<BrewColorType> COLOR = EnumProperty.create("color", BrewColorType.class);

    // Constructor for the BrewCauldronBlock class, setting the default state to clear color.
    public BrewCauldronBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(COLOR, BrewColorType.CLEAR));
    }

    // Override the getColor method to return the color of the cauldron based on its state.
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR);
    }
}