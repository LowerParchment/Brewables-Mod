package com.LowerParchment.brewables.entity;
import com.LowerParchment.brewables.BrewablesMod;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import java.util.List;
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
        return BrewablesMod.LINGERING_WITCHS_WART.get();
    }

    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        if (!this.level().isClientSide())
        {
            // Get actual effects
            ItemStack stack = this.getItem();
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);

            // Create lingering cloud like vanilla
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            if (this.getOwner() instanceof LivingEntity living)
            {
                cloud.setOwner(living);
            }
            cloud.setRadius(3.0F);
            cloud.setRadiusOnUse(-0.5F);
            cloud.setWaitTime(10);
            cloud.setDuration(200);
            cloud.setRadiusPerTick(-0.03F);
            
            cloud.setPotion(Potions.WATER);
            cloud.setParticle(ParticleTypes.WITCH);

            for (MobEffectInstance effect : effects)
            {
                cloud.addEffect(new MobEffectInstance(effect));
            }

            this.level().addFreshEntity(cloud);
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
