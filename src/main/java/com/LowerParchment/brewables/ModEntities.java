package com.LowerParchment.brewables;

import com.LowerParchment.brewables.entity.ThrownLingeringWitchsWartEntity;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities
{
    // Mod ID for Brewables
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BrewablesMod.MODID);

    // Registering the ThrownWitchsWartEntity for splash potion effects
    public static final RegistryObject<EntityType<ThrownWitchsWartEntity>> THROWN_WITCHS_WART_ENTITY =
        ENTITY_TYPES.register("thrown_witchs_wart", () ->
            EntityType.Builder.<ThrownWitchsWartEntity>of(ThrownWitchsWartEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("thrown_witchs_wart"));

    // Registering the ThrownLingeringWitchsWartEntity for lingering potion effects
    public static final RegistryObject<EntityType<ThrownLingeringWitchsWartEntity>> THROWN_LINGERING_WITCHS_WART_ENTITY =
    ENTITY_TYPES.register("thrown_lingering_witchs_wart", () ->
        EntityType.Builder.<ThrownLingeringWitchsWartEntity>of(ThrownLingeringWitchsWartEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("thrown_lingering_witchs_wart"));
}
