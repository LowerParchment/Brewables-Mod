// Import user defined dependencies
package com.LowerParchment.brewables;
import com.LowerParchment.brewables.entity.ThrownLingeringWitchsWartEntity;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;

// Import Minecraft and Forge dependencies
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Handles all custom entity registrations for the Brewables mod
public class ModEntities
{
    // Global deferred register for all custom entities in this mod
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BrewablesMod.MODID);

    // --- Splash Potion Variant ---

    // Thrown Witch's Wart entity that behaves like a splash potion
    public static final RegistryObject<EntityType<ThrownWitchsWartEntity>> THROWN_WITCHS_WART_ENTITY =
        ENTITY_TYPES.register("thrown_witchs_wart", () ->
            EntityType.Builder.<ThrownWitchsWartEntity>of(ThrownWitchsWartEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("thrown_witchs_wart"));

    // --- Lingering Potion Variant ---

    // Thrown Lingering Witch's Wart entity that attempts to leave a lingering effect cloud
    public static final RegistryObject<EntityType<ThrownLingeringWitchsWartEntity>> THROWN_LINGERING_WITCHS_WART_ENTITY =
    ENTITY_TYPES.register("thrown_lingering_witchs_wart", () ->
        EntityType.Builder.<ThrownLingeringWitchsWartEntity>of(ThrownLingeringWitchsWartEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("thrown_lingering_witchs_wart"));
}
