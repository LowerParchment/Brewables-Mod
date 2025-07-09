// Import user defined dependencies
package com.LowerParchment.brewables.entity;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.item.WitchsWartItem;

// Import Minecraft dependencies
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

// Entity class for splash-type Witch's Wart potions.
// Applies a random negative effect in an area on impact.
public class ThrownWitchsWartEntity extends ThrowableItemProjectile
{
    // Constructor for world-spawned entity
    public ThrownWitchsWartEntity(EntityType<? extends ThrownWitchsWartEntity> type, Level level)
    {
        super(type, level);
    }

    // Constructor for the entity when thrown by a LivingEntity (like a player)
    public ThrownWitchsWartEntity(Level level, LivingEntity thrower)
    {
        super(ModEntities.THROWN_WITCHS_WART_ENTITY.get(), thrower, level);
    }

    // Defines the item used for rendering and identification
    @Override
    protected Item getDefaultItem()
    {
        return BrewablesMod.SPLASH_WITCHS_WART.get();
    }

    // Called when the potion hits a block or entity.
    // Applies the random Witch’s Wart effect to nearby entities in a radius,
    // and plays impact sounds/particles.
    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        if (!level().isClientSide())
        {
            MobEffectInstance effect = WitchsWartItem.getRandomEffectStatic();

             // Apply the effect to nearby living entities within a radius
            double radius = 4.0D;
            for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius)))
            {
                double distanceSq = this.distanceToSqr(entity);
                if (distanceSq < radius * radius)
                {
                    entity.addEffect(new MobEffectInstance(effect));
                }
            }

            // Emit impact particles
            ServerLevel server = (ServerLevel) level();
            server.sendParticles(
                ParticleTypes.WITCH,
                this.getX(), this.getY(), this.getZ(),
                20, 0.3, 0.25, 0.3, 0.02
            );
            server.sendParticles(
                ParticleTypes.SMOKE,
                this.getX(), this.getY(), this.getZ(),
                15, 0.3, 0.25, 0.3, 0.01
            );
            server.sendParticles(
                ParticleTypes.SOUL,
                this.getX(), this.getY(), this.getZ(),
                10, 0.2, 0.2, 0.2, 0.01
            );

            // Play impact sounds
            server.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 1.2F,
                    0.7F + server.random.nextFloat() * 0.3F);
            server.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F,
                    0.9F + server.random.nextFloat() * 0.2F);
        }

        // Vanilla splash bottle break effect
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }
}
