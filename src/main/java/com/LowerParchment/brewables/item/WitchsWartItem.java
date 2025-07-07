// Import self-created package declaration
package com.LowerParchment.brewables.item;

// Import dependencies and classes
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class WitchsWartItem extends PotionItem {
    public WitchsWartItem(Properties properties)
    {
        super(properties);
    }

    // Override the getName method to provide a custom name for the item
    @Override
    public Component getName(ItemStack stack)
    {
        return Component.translatable("item.brewables.witchs_wart");
    }

    // Add lore to the item when hovered over in the inventory
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
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
}
