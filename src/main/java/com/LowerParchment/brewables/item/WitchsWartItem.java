// Import self-created package declaration
package com.LowerParchment.brewables.item;

// Import dependencies and classes
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;

import java.util.List;
import java.util.Random;

public class WitchsWartItem extends PotionItem
{
    public WitchsWartItem(Properties properties)
    {
        super(properties);
    }

    // Override the getName method to provide a custom name for the item
    public Component getName(ItemStack stack)
    {
        return Component.translatable("item.brewables.witchs_wart").withStyle(ChatFormatting.RESET);
    }

    // Add lore to the item when hovered over in the inventory
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        // Add a custom tooltip with lore
        tooltip.add(Component.translatable("item.brewables.witchs_wart.tooltip").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    // Override the finishUsingItem method to apply random effects when consumed
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {
        if (!level.isClientSide)
        {
            MobEffectInstance effect = getRandomEffect();
            entity.addEffect(effect);
        }

        return super.finishUsingItem(stack, level, entity);
    }

    // Utility method to generate random effects (weighted toward bad ones)
    private static final Random RANDOM = new Random();
    private static final List<MobEffect> BAD_EFFECTS = List.of(
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.CONFUSION,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.HARM,
            MobEffects.SLOW_FALLING,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.LEVITATION,
            MobEffects.WEAKNESS,
            MobEffects.UNLUCK,
            MobEffects.HUNGER,
            MobEffects.DARKNESS,
            MobEffects.GLOWING,
            MobEffects.BAD_OMEN);

    private static final List<MobEffect> GOOD_EFFECTS = List.of(
            MobEffects.LUCK,
            MobEffects.ABSORPTION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.HEALTH_BOOST,
            MobEffects.SATURATION,
            MobEffects.NIGHT_VISION,
            MobEffects.JUMP,
            MobEffects.INVISIBILITY,
            MobEffects.WATER_BREATHING,
            MobEffects.DIG_SPEED,
            MobEffects.DAMAGE_BOOST,
            MobEffects.REGENERATION,
            MobEffects.HERO_OF_THE_VILLAGE,
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DOLPHINS_GRACE,
            MobEffects.CONDUIT_POWER,
            MobEffects.FIRE_RESISTANCE);

    private MobEffectInstance getRandomEffect()
    {
        boolean isGood = RANDOM.nextFloat() < 0.2f; // 20% chance for good

        MobEffect effect;
        int duration = 1800; // 90 seconds flat
        int amplifier;

        if (isGood)
        {
            effect = GOOD_EFFECTS.get(RANDOM.nextInt(GOOD_EFFECTS.size()));
            amplifier = 1 + RANDOM.nextInt(2); // 1–2
        }
        else
        {
            effect = BAD_EFFECTS.get(RANDOM.nextInt(BAD_EFFECTS.size()));
            amplifier = 2 + RANDOM.nextInt(3); // 2–4
        }

        return new MobEffectInstance(effect, duration, amplifier);
    }

    // Static method to get a random effect instance
    public static MobEffectInstance getRandomEffectStatic()
    {
        Random random = new Random();
        boolean isGood = random.nextFloat() < 0.2f;
    
        MobEffect effect;
        int duration = 1800;
        int amplifier;
    
        if (isGood)
        {
            effect = GOOD_EFFECTS.get(random.nextInt(GOOD_EFFECTS.size()));
            amplifier = 1 + random.nextInt(2);
        }
        else
        {
            effect = BAD_EFFECTS.get(random.nextInt(BAD_EFFECTS.size()));
            amplifier = 2 + random.nextInt(3);
        }
    
        return new MobEffectInstance(effect, duration, amplifier);
    }

    // Override the use method to throw a ThrownWitchsWartEntity when used
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);

        // Play throw sound on client only
        if (level.isClientSide)
        {
            user.playSound(
                    SoundEvents.SPLASH_POTION_THROW, 1.0F, 0.8F + level.random.nextFloat() * 0.2F);
        }

        if (!level.isClientSide)
        {
            ThrownWitchsWartEntity wart = new ThrownWitchsWartEntity(level, user);
            wart.setItem(stack);
            wart.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.75F, 1.0F);
            level.addFreshEntity(wart);
        }

        if (!user.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

}
