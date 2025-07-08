// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.block;

import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        builder.add(LEVEL, COLOR, BREW_STATE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        BrewablesMod.LOGGER.debug("[BLOCK TRACE] onPlace triggered at {} → LEVEL={}", pos, state.getValue(BrewCauldronBlock.LEVEL));
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        super.onRemove(oldState, level, pos, newState, isMoving);

        if (!level.isClientSide)
        {
            // If the cauldron was replaced by a different block, clear stored data.
            if (oldState.getBlock() != newState.getBlock())
            {
                BrewablesMod.LOGGER.debug("[BREW_CAULDRON] Cleared data on block removal at {}", pos);
                ItemInCauldronHandler.clearIngredients(pos);
                CauldronStateTracker.reset(pos);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (level instanceof Level l && l.isClientSide)
        {
            l.sendBlockUpdated(pos, state, state, 3);
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    // Animate tick method to show bubbling particles when conditions are met: Campfire is underneath and cauldron is not empty.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        super.animateTick(state, level, pos, random);

        // Only show bubbles if a lit campfire is underneath
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!(belowState.getBlock() instanceof CampfireBlock) || !belowState.getValue(CampfireBlock.LIT))
        {
            return;
        }

        // Optional: you can limit to certain water levels if needed
        int levelVal = state.getValue(LEVEL);
        if (levelVal == 0)
        {
            return;
        }

        // Pick y based on water level height
        double y;
        switch (levelVal)
        {
            case 1 -> y = pos.getY() + 0.61;
            case 2 -> y = pos.getY() + 0.78;
            case 3 -> y = pos.getY() + 0.95;

            // Fallback case for any unexpected values
            default -> y = pos.getY() + 0.61;
        };

        // Spawn some bubbling particles
        for (int i = 0; i < 6 + random.nextInt(4); i++)
        {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;

            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.02D, 0.0D);
        }

        // Play occasional boiling sounds (lava ambient + pop)
        if (random.nextFloat() < 0.05F)
        {
            ((ClientLevel) level).playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS,
                    0.5F,
                    0.9F + random.nextFloat() * 0.2F,
                    false);
        }

        if (random.nextFloat() < 0.02F)
        {
            ((ClientLevel) level).playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.LAVA_AMBIENT,
                    SoundSource.BLOCKS,
                    0.4F,
                    0.8F + random.nextFloat() * 0.2F,
                    false);
        }
    }

    public void tick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.util.RandomSource random)
    {
        if (level.isClientSide)
            return; // only act server-side

        BlockState cleared = BrewablesMod.BREW_CAULDRON.get().defaultBlockState()
                .setValue(BrewCauldronBlock.LEVEL, 0)
                .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR)
                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.EMPTY);

        level.setBlock(pos, cleared, 3);

        BrewablesMod.LOGGER.info("[SCHEDULED-RESET] Placed empty cauldron at {} LEVEL={} BREW_STATE={}", pos,
                cleared.getValue(BrewCauldronBlock.LEVEL), cleared.getValue(BrewCauldronBlock.BREW_STATE));

        level.sendBlockUpdated(pos, state, cleared, 3);
    }
}