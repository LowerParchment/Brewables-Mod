// Importing user created dependency files
package com.LowerParchment.brewables.block;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.event.CauldronBrewState;
import com.LowerParchment.brewables.handler.CauldronStateTracker;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;

// Importing Minecraft and Forge classes
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

// Class that handles the Brew Cauldron block, extending the functionality of the vanilla CauldronBlock.
public class BrewCauldronBlock extends CauldronBlock
{
    // BlockState properties controlling brew behavior, water color, and fluid level.
    public static final EnumProperty<CauldronBrewState> BREW_STATE = EnumProperty.create("brew_state", CauldronBrewState.class);
    public static final EnumProperty<BrewColorType> COLOR = EnumProperty.create("color", BrewColorType.class);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    // Variable to prevent sound muddying
    private long lastSoundTime = 0;

    // Constructor for the BrewCauldronBlock class, setting the default state to clear color.
    public BrewCauldronBlock(Properties properties)
    {
        super(properties);

        // Set empty water level by default
        this.registerDefaultState(this.defaultBlockState()
        .setValue(COLOR, BrewColorType.CLEAR)
        .setValue(LEVEL, 0)
        .setValue(BREW_STATE, CauldronBrewState.EMPTY));
    }

    // Define the state of the block, including its properties.
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL, COLOR, BREW_STATE);
    }

    // Handle instantiation of the block, setting its initial state.
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    // Handle the removal of the block, clearing any stored data if the block is replaced.
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        super.onRemove(oldState, level, pos, newState, isMoving);

        // Only act if the level is server-side
        if (!level.isClientSide)
        {
            // If the cauldron was replaced by a different block, clear stored data.
            if (oldState.getBlock() != newState.getBlock())
            {
                // Clear the data associated with the cauldron
                ItemInCauldronHandler.clearIngredients(pos);
                CauldronStateTracker.reset(pos);
            }
        }
    }

    // Update the block state when it is placed or changed, ensuring the cauldron's properties are correctly set.
    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        // Force visual update on client when neighboring blocks change.
        if (level instanceof Level l && l.isClientSide)
        {
            l.sendBlockUpdated(pos, state, state, 3);
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    // Animate tick method to show appropriate particles when conditions are met: Campfire is underneath and cauldron is not empty.
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

        // Don't let bubbles spawn at empty
        int levelVal = state.getValue(LEVEL);
        if (levelVal == 0) return;

        // Determine height for the bubbling effect based on the cauldron's water level
        double y;
        switch (levelVal)
        {
            // Either Level 1-3
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

        // Play the lava ambient sound near constantly, but not too frequently.
        if (level.getGameTime() - lastSoundTime > 5 && random.nextFloat() < 0.95F)
        {
            lastSoundTime = level.getGameTime();
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

    // Scheduled tick logic to reset a cauldron block to its default state.
    public void tick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.util.RandomSource random)
    {
        // Only act server-side
        if (level.isClientSide)
            return;

        // Update the block state to clear color and empty brew state
        BlockState cleared = BrewablesMod.BREW_CAULDRON.get().defaultBlockState()
                .setValue(BrewCauldronBlock.LEVEL, 0)
                .setValue(BrewCauldronBlock.COLOR, BrewColorType.CLEAR)
                .setValue(BrewCauldronBlock.BREW_STATE, CauldronBrewState.EMPTY);
        level.setBlock(pos, cleared, 3);
        level.sendBlockUpdated(pos, state, cleared, 3);
    }
}