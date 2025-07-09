// Import user created dependencies
package com.LowerParchment.brewables.entity;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.item.WitchsWartItem;

// Import Minecraft, Forge, and Java dependencies
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

// Entity class for thrown Lingering Witch's Wart potions.
// On impact, spawns a cloud with a randomized status effect from the Witch's Wart pool.
public class ThrownLingeringWitchsWartEntity extends ThrowableItemProjectile
{
    // Constructor for the world-spawned entity
    public ThrownLingeringWitchsWartEntity(EntityType<? extends ThrownLingeringWitchsWartEntity> type, Level level)
    {
        super(type, level);
    }

    // Constructor for the entity when thrown by a LivingEntity (like a player).
    public ThrownLingeringWitchsWartEntity(Level level, LivingEntity thrower)
    {
        super(ModEntities.THROWN_LINGERING_WITCHS_WART_ENTITY.get(), thrower, level);
    }

    // Returns the default item used to render and identify the entity
    @Override
    protected Item getDefaultItem()
    {
        return BrewablesMod.LINGERING_WITCHS_WART.get();
    }

    // Called when the potion hits a block or entity.
    // Spawns an AreaEffectCloud that applies a random Witch's Wart effect.
    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        // Server-side only
        if (!this.level().isClientSide())
        {
            ServerLevel server = (ServerLevel) this.level();

            // Create the area effect cloud and set its properties
            AreaEffectCloud cloud = new AreaEffectCloud(server, this.getX(), this.getY(), this.getZ());
            if (this.getOwner() instanceof LivingEntity living)
            {
                cloud.setOwner(living);
            }
            cloud.setRadius(3.0F);
            cloud.setRadiusOnUse(-0.5F);
            cloud.setWaitTime(10);
            cloud.setDuration(600);
            cloud.setRadiusPerTick(-0.005F);        // Gradually shrinks the cloud radius over time.
            cloud.setParticle(ParticleTypes.WITCH);

            // Add a random status effect from the Witch's Wart pool
            MobEffectInstance wartEffect = WitchsWartItem.getRandomEffectStatic();
            cloud.addEffect(wartEffect);

            // Emit sounds and particles
            server.addFreshEntity(cloud);
            server.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 1.0F, 0.8F);
            server.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.8F, 1.0F);
            server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), 10, 0.2, 0.2, 0.2, 0.01);

            // Broadcast the entity event to clients
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    // Ensures correct syncing and client-side rendering of the entity
    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
