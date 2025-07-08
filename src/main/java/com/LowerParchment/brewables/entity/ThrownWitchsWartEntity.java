package com.LowerParchment.brewables.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.item.WitchsWartItem;

public class ThrownWitchsWartEntity extends ThrowableItemProjectile
{
    // Constructor for the ThrownWitchsWartEntity
    public ThrownWitchsWartEntity(EntityType<? extends ThrownWitchsWartEntity> type, Level level)
    {
        super(type, level);
    }

    // Constructor for the ThrownWitchsWartEntity that takes a thrower
    public ThrownWitchsWartEntity(Level level, LivingEntity thrower)
    {
        super(ModEntities.THROWN_WITCHS_WART_ENTITY.get(), thrower, level);
    }

    // Constructor for the ThrownWitchsWartEntity that takes position and velocity
    @Override
    protected Item getDefaultItem()
    {
        return BrewablesMod.SPLASH_WITCHS_WART.get();
    }

    // Override the onHit method to handle the splash effect
    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        if (!level().isClientSide())
        {
            MobEffectInstance effect = WitchsWartItem.getRandomEffectStatic();

            // AoE splash logic
            double radius = 4.0D;
            for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius)))
            {
                double distanceSq = this.distanceToSqr(entity);
                if (distanceSq < radius * radius)
                {
                    entity.addEffect(new MobEffectInstance(effect));
                }
            }

            // Spawn particles
            ((ServerLevel) level()).sendParticles(
                ParticleTypes.WITCH,
                this.getX(), this.getY(), this.getZ(),
                20, 0.25, 0.25, 0.25, 0.05
            );
        }

        level().broadcastEntityEvent(this, (byte) 3); // vanilla splash poof
        discard();
    }
}
