// Importing necessary packages for the event handler in Minecraft Forge modding.
package com.LowerParchment.brewables.handler;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import com.LowerParchment.brewables.block.BrewColorType;

public class BrewRecipeRegistry
{
    // Establish the BrewResult record
    public record BrewResult(Potion basePotion, boolean useGlowstone, boolean useRedstone, boolean useGunpowder) {}

    // Define a map to hold brewing recipes
    private static final Map<List<Item>, Potion> RECIPE_MAP = new HashMap<>();

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
        if (potion == Potions.SWIFTNESS) return BrewColorType.SWIFTNESS;
        if (potion == Potions.SLOWNESS) return BrewColorType.SLOWNESS;
        if (potion == Potions.LEAPING) return BrewColorType.LEAPING;
        if (potion == Potions.STRENGTH) return BrewColorType.STRENGTH;
        if (potion == Potions.HEALING) return BrewColorType.HEALING;
        if (potion == Potions.HARMING) return BrewColorType.HARMING;
        if (potion == Potions.POISON) return BrewColorType.POISON;
        if (potion == Potions.REGENERATION) return BrewColorType.REGENERATION;
        if (potion == Potions.FIRE_RESISTANCE) return BrewColorType.FIRE_RESISTANCE;
        if (potion == Potions.WATER_BREATHING) return BrewColorType.WATER_BREATHING;
        if (potion == Potions.NIGHT_VISION) return BrewColorType.NIGHT_VISION;
        if (potion == Potions.INVISIBILITY) return BrewColorType.INVISIBILITY;
        if (potion == Potions.TURTLE_MASTER) return BrewColorType.TURTLE_MASTER;
        if (potion == Potions.SLOW_FALLING) return BrewColorType.SLOW_FALLING;
        if (potion == Potions.WEAKNESS) return BrewColorType.WEAKNESS;

        // Default fallback
        System.out.println("[WARN] Unmapped potion in getColorForPotion: " + potion);
        return BrewColorType.WART;
    }

    // Method to increase effect duration or effectiveness
    public static Optional<BrewResult> match(List<ItemStack> ingredients)
    {
        List<Item> baseIngredients = new ArrayList<>();
        boolean hasGlowstone = false;
        boolean hasRedstone = false;
        boolean hasGunpowder = false;

        for (ItemStack stack : ingredients)
        {
            Item item = stack.getItem();
            if (item == Items.GLOWSTONE_DUST) hasGlowstone = true;
            else if (item == Items.REDSTONE) hasRedstone = true;
            else if (item == Items.GUNPOWDER) hasGunpowder = true;
            else baseIngredients.add(item);
        }

        // Sort for consistent matching
        baseIngredients.sort(Comparator.comparing(Item::getDescriptionId));

        for (Map.Entry<List<Item>, Potion> entry : RECIPE_MAP.entrySet())
        {
            List<Item> key = new ArrayList<>(entry.getKey());
            key.sort(Comparator.comparing(Item::getDescriptionId));

            if (key.equals(baseIngredients)) {
                return Optional.of(new BrewResult(entry.getValue(), hasGlowstone, hasRedstone, hasGunpowder));
            }
        }

        // If no match found, return empty
        return Optional.empty();
    }
}