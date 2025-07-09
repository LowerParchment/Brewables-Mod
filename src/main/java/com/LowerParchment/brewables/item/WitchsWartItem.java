package com.LowerParchment.brewables.item;

import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class WitchsWartItem extends PotionItem
{
    public WitchsWartItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        if (this == BrewablesMod.SPLASH_WITCHS_WART.get())
        {
            return Component.translatable("item.brewables.splash_witchs_wart");
        }
        else if (this == BrewablesMod.LINGERING_WITCHS_WART.get())
        {
            return Component.translatable("item.brewables.lingering_witchs_wart");
        }
        else
        {
            return Component.translatable("item.brewables.witchs_wart");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.brewables.witchs_wart.tooltip")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {
        // Only apply effect if it's the drinkable version
        if (!level.isClientSide && this == BrewablesMod.WITCHS_WART_DRINKABLE.get())
        {
            entity.addEffect(getRandomEffect());
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);

        // If it's splash or lingering, throw it
        if (this == BrewablesMod.SPLASH_WITCHS_WART.get() || this == BrewablesMod.LINGERING_WITCHS_WART.get())
        {
            if (!level.isClientSide)
            {
                ThrownWitchsWartEntity wart = new ThrownWitchsWartEntity(level, user);
                wart.setItem(stack);
                wart.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.5F, 1.0F);
                level.addFreshEntity(wart);
            }
            else
            {
                user.playSound(SoundEvents.SPLASH_POTION_THROW, 1.0F, 0.8F + level.random.nextFloat() * 0.2F);
            }

            if (!user.getAbilities().instabuild)
            {
                stack.shrink(1);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // Else, handle drinking like normal
        return super.use(level, user, hand);
    }

    // --- Shared logic for random effect generation ---

    private static final Random RANDOM = new Random();

    private static final List<MobEffect> BAD_EFFECTS = List.of(
            MobEffects.POISON, MobEffects.WITHER, MobEffects.BLINDNESS, MobEffects.CONFUSION,
            MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HARM, MobEffects.SLOW_FALLING, MobEffects.DIG_SLOWDOWN,
            MobEffects.LEVITATION, MobEffects.WEAKNESS, MobEffects.UNLUCK, MobEffects.HUNGER,
            MobEffects.DARKNESS, MobEffects.GLOWING, MobEffects.BAD_OMEN);

    private static final List<MobEffect> GOOD_EFFECTS = List.of(
            MobEffects.LUCK, MobEffects.ABSORPTION, MobEffects.DAMAGE_RESISTANCE, MobEffects.HEALTH_BOOST,
            MobEffects.SATURATION, MobEffects.NIGHT_VISION, MobEffects.JUMP, MobEffects.INVISIBILITY,
            MobEffects.WATER_BREATHING, MobEffects.DIG_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.REGENERATION,
            MobEffects.HERO_OF_THE_VILLAGE, MobEffects.MOVEMENT_SPEED, MobEffects.DOLPHINS_GRACE,
            MobEffects.CONDUIT_POWER, MobEffects.FIRE_RESISTANCE);

    private static MobEffectInstance getRandomEffect()
    {
        boolean isGood = RANDOM.nextFloat() < 0.2f;
        MobEffect effect = isGood
                ? GOOD_EFFECTS.get(RANDOM.nextInt(GOOD_EFFECTS.size()))
                : BAD_EFFECTS.get(RANDOM.nextInt(BAD_EFFECTS.size()));

        int amplifier = isGood
                ? 1 + RANDOM.nextInt(2)
                : 2 + RANDOM.nextInt(3);

        return new MobEffectInstance(effect, 1800, amplifier); // 90 seconds
    }

    public static MobEffectInstance getRandomEffectStatic()
    {
        return getRandomEffect();
    }

    public static ItemStack getWitchsWartStack(boolean isSplash, boolean isLingering)
    {
        if (isLingering)
        {
            return new ItemStack(BrewablesMod.LINGERING_WITCHS_WART.get());
        }
        else if (isSplash)
        {
            return new ItemStack(BrewablesMod.SPLASH_WITCHS_WART.get());
        }
        else
        {
            return new ItemStack(BrewablesMod.WITCHS_WART_DRINKABLE.get());
        }
    }
}
