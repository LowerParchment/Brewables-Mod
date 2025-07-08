package com.LowerParchment.brewables.client;

import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.LowerParchment.brewables.BrewablesMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BrewablesEntityRenderers
{
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        // Register the ThrownWitchsWartEntity renderer for splash potion effects
        event.registerEntityRenderer(ModEntities.THROWN_WITCHS_WART_ENTITY.get(),
            context -> new ThrownItemRenderer<ThrownWitchsWartEntity>(context));

        // Register the ThrownLingeringWitchsWartEntity renderer for lingering potion effects
        event.registerEntityRenderer(ModEntities.THROWN_LINGERING_WITCHS_WART_ENTITY.get(),
            context -> new ThrownItemRenderer<>(context));
    }
}
