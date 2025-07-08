package com.LowerParchment.brewables.entity;
import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.item.WitchsWartItem;

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

public class ThrownLingeringWitchsWartEntity extends ThrowableItemProjectile
{
    public ThrownLingeringWitchsWartEntity(EntityType<? extends ThrownLingeringWitchsWartEntity> type, Level level)
    {
        super(type, level);
    }

    public ThrownLingeringWitchsWartEntity(Level level, LivingEntity thrower)
    {
        super(ModEntities.THROWN_LINGERING_WITCHS_WART_ENTITY.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem()
    {
        return com.LowerParchment.brewables.BrewablesMod.LINGERING_WITCHS_WART.get();
    }

    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        if (!level().isClientSide)
        {
            MobEffectInstance effect = WitchsWartItem.getRandomEffectStatic();

            AreaEffectCloud cloud = new AreaEffectCloud(level(), this.getX(), this.getY(), this.getZ());
            if (this.getOwner() instanceof LivingEntity owner) cloud.setOwner(owner);
            cloud.setRadius(3.0F);
            cloud.setDuration(200);
            cloud.setWaitTime(10);
            cloud.setRadiusPerTick(-0.03F);
            cloud.setParticle(ParticleTypes.WITCH);
            cloud.addEffect(effect);

            ((ServerLevel) level()).addFreshEntity(cloud);

            // Spawn impact particles
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

            // Play impact sound
            server.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 1.2F,
                    0.7F + server.random.nextFloat() * 0.3F);
            server.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F,
                    0.9F + server.random.nextFloat() * 0.2F);
        }

        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }

    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
