// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.handler;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import com.LowerParchment.brewables.block.BrewColorType;

public class BrewRecipeRegistry
{
    // Establish the BrewResult record
    public record BrewResult(Potion basePotion, boolean useGlowstone, boolean useRedstone, boolean useGunpowder, boolean useDragonBreath) {}

    // Define a map to hold brewing recipes
    private static final Map<List<Item>, Potion> RECIPE_MAP = new HashMap<>();

    // Helper method to create a potion stack based on the potion type and whether it is splash or lingering.
    public static ItemStack createPotionStack(Potion potion, boolean isSplash, boolean isLingering)
    {
        Item baseItem = isLingering
            ? Items.LINGERING_POTION
            : isSplash
                ? Items.SPLASH_POTION
                : Items.POTION;

        return PotionUtils.setPotion(new ItemStack(baseItem), potion);
    }

    // Method to get the base variant of a potion, stripping any modifiers like strong or long for json consistency.
    public static Potion getBaseVariant(Potion potion)
    {
        if (potion == Potions.STRONG_SWIFTNESS || potion == Potions.LONG_SWIFTNESS) return Potions.SWIFTNESS;
        if (potion == Potions.STRONG_HEALING) return Potions.HEALING;
        if (potion == Potions.STRONG_POISON || potion == Potions.LONG_POISON) return Potions.POISON;
        if (potion == Potions.STRONG_REGENERATION || potion == Potions.LONG_REGENERATION) return Potions.REGENERATION;
        if (potion == Potions.STRONG_HARMING) return Potions.HARMING;
        if (potion == Potions.STRONG_LEAPING || potion == Potions.LONG_LEAPING) return Potions.LEAPING;
        if (potion == Potions.STRONG_SLOWNESS || potion == Potions.LONG_SLOWNESS) return Potions.SLOWNESS;
        if (potion == Potions.LONG_FIRE_RESISTANCE) return Potions.FIRE_RESISTANCE;
        if (potion == Potions.LONG_WATER_BREATHING) return Potions.WATER_BREATHING;
        if (potion == Potions.LONG_NIGHT_VISION) return Potions.NIGHT_VISION;
        if (potion == Potions.LONG_INVISIBILITY) return Potions.INVISIBILITY;
        if (potion == Potions.LONG_WEAKNESS) return Potions.WEAKNESS;
        if (potion == Potions.LONG_TURTLE_MASTER || potion == Potions.STRONG_TURTLE_MASTER) return Potions.TURTLE_MASTER;
        if (potion == Potions.LONG_SLOW_FALLING) return Potions.SLOW_FALLING;
        return potion;
    }

    // Method to apply modifiers to a base potion based on the presence of Glowstone, Redstone dust, or Dragon's Breath.
    public static Potion applyModifiers(Potion base, boolean useGlowstone, boolean useRedstone)
    {
        if (useGlowstone && useRedstone)
        {
            // Both modifiers together are invalid in vanilla – return base or null to trigger failure
            System.out.println("[WARN] Both Glowstone and Redstone present – invalid combo.");
            return base;
        }
    
        if (useGlowstone)
        {
            if (base == Potions.SWIFTNESS) return Potions.STRONG_SWIFTNESS;
            if (base == Potions.HEALING) return Potions.STRONG_HEALING;
            if (base == Potions.POISON) return Potions.STRONG_POISON;
            if (base == Potions.REGENERATION) return Potions.STRONG_REGENERATION;
            if (base == Potions.HARMING) return Potions.STRONG_HARMING;
            if (base == Potions.LEAPING) return Potions.STRONG_LEAPING;
            if (base == Potions.SLOWNESS) return Potions.STRONG_SLOWNESS;
            if (base == Potions.TURTLE_MASTER) return Potions.STRONG_TURTLE_MASTER;
        }
    
        if (useRedstone)
        {
            if (base == Potions.SWIFTNESS) return Potions.LONG_SWIFTNESS;
            if (base == Potions.LEAPING) return Potions.LONG_LEAPING;
            if (base == Potions.POISON) return Potions.LONG_POISON;
            if (base == Potions.REGENERATION) return Potions.LONG_REGENERATION;
            if (base == Potions.SLOWNESS) return Potions.LONG_SLOWNESS;
            if (base == Potions.WEAKNESS) return Potions.LONG_WEAKNESS;
            if (base == Potions.NIGHT_VISION) return Potions.LONG_NIGHT_VISION;
            if (base == Potions.INVISIBILITY) return Potions.LONG_INVISIBILITY;
            if (base == Potions.FIRE_RESISTANCE) return Potions.LONG_FIRE_RESISTANCE;
            if (base == Potions.WATER_BREATHING) return Potions.LONG_WATER_BREATHING;
            if (base == Potions.TURTLE_MASTER) return Potions.LONG_TURTLE_MASTER;
            if (base == Potions.SLOW_FALLING) return Potions.LONG_SLOW_FALLING;
        }
    
        // Return the base potion if no modifiers apply
        return base;
    }

    // Static block to initialize the recipe map with known brewing recipes : 15 total
    static
    {
        RECIPE_MAP.put(List.of(Items.SUGAR), Potions.SWIFTNESS);
        RECIPE_MAP.put(List.of(Items.FERMENTED_SPIDER_EYE, Items.SUGAR), Potions.SLOWNESS);
        RECIPE_MAP.put(List.of(Items.RABBIT_FOOT), Potions.LEAPING);
        RECIPE_MAP.put(List.of(Items.BLAZE_POWDER), Potions.STRENGTH);
        RECIPE_MAP.put(List.of(Items.GLISTERING_MELON_SLICE), Potions.HEALING);
        RECIPE_MAP.put(List.of(Items.GLISTERING_MELON_SLICE, Items.FERMENTED_SPIDER_EYE), Potions.HARMING);
        RECIPE_MAP.put(List.of(Items.SPIDER_EYE), Potions.POISON);
        RECIPE_MAP.put(List.of(Items.GHAST_TEAR), Potions.REGENERATION);
        RECIPE_MAP.put(List.of(Items.MAGMA_CREAM), Potions.FIRE_RESISTANCE);
        RECIPE_MAP.put(List.of(Items.PUFFERFISH), Potions.WATER_BREATHING);
        RECIPE_MAP.put(List.of(Items.GOLDEN_CARROT), Potions.NIGHT_VISION);
        RECIPE_MAP.put(List.of(Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE), Potions.INVISIBILITY);
        RECIPE_MAP.put(List.of(Items.TURTLE_HELMET), Potions.TURTLE_MASTER);
        RECIPE_MAP.put(List.of(Items.PHANTOM_MEMBRANE), Potions.SLOW_FALLING);
        RECIPE_MAP.put(List.of(Items.FERMENTED_SPIDER_EYE), Potions.WEAKNESS);
    }

    // Method to get the BrewColorType based on the potion type from above
    public static BrewColorType getColorForPotion(Potion potion)
    {
        // Normalize the potion to its base variant
        Potion base = getBaseVariant(potion);

        if (base == Potions.SWIFTNESS) return BrewColorType.SWIFTNESS;
        if (base == Potions.SLOWNESS) return BrewColorType.SLOWNESS;
        if (base == Potions.LEAPING) return BrewColorType.LEAPING;
        if (base == Potions.STRENGTH) return BrewColorType.STRENGTH;
        if (base == Potions.HEALING) return BrewColorType.HEALING;
        if (base == Potions.HARMING) return BrewColorType.HARMING;
        if (base == Potions.POISON) return BrewColorType.POISON;
        if (base == Potions.REGENERATION) return BrewColorType.REGENERATION;
        if (base == Potions.FIRE_RESISTANCE) return BrewColorType.FIRE_RESISTANCE;
        if (base == Potions.WATER_BREATHING) return BrewColorType.WATER_BREATHING;
        if (base == Potions.NIGHT_VISION) return BrewColorType.NIGHT_VISION;
        if (base == Potions.INVISIBILITY) return BrewColorType.INVISIBILITY;
        if (base == Potions.TURTLE_MASTER) return BrewColorType.TURTLE_MASTER;
        if (base == Potions.SLOW_FALLING) return BrewColorType.SLOW_FALLING;
        if (base == Potions.WEAKNESS) return BrewColorType.WEAKNESS;

        // Default fallback
        return BrewColorType.WART;
    }

    // Method to increase effect duration or effectiveness
    public static Optional<BrewResult> match(List<ItemStack> ingredients)
    {
        List<Item> baseIngredients = new ArrayList<>();
        boolean hasGlowstone = false;
        boolean hasRedstone = false;
        boolean hasGunpowder = false;
        boolean useDragonBreath = false;

        for (ItemStack stack : ingredients)
        {
            Item item = stack.getItem();
            if (item == Items.GLOWSTONE_DUST) hasGlowstone = true;
            else if (item == Items.REDSTONE) hasRedstone = true;
            else if (item == Items.GUNPOWDER) hasGunpowder = true;
            else if (item == Items.DRAGON_BREATH) useDragonBreath = true;
            else baseIngredients.add(item);
        }

        // Sort for consistent matching
        baseIngredients.sort(Comparator.comparing(Item::getDescriptionId));

        for (Map.Entry<List<Item>, Potion> entry : RECIPE_MAP.entrySet())
        {
            List<Item> key = new ArrayList<>(entry.getKey());
            key.sort(Comparator.comparing(Item::getDescriptionId));

            if (key.equals(baseIngredients))
            {
                return Optional.of(new BrewResult(entry.getValue(), hasGlowstone, hasRedstone, hasGunpowder, useDragonBreath));
            }
        }

        // If no match found, return empty
        return Optional.empty();
    }
}