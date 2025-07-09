// Import user defined dependencies
package com.LowerParchment.brewables.client;
import com.LowerParchment.brewables.ModEntities;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;
import static com.LowerParchment.brewables.BrewablesMod.MODID;

// Import Minecraft dependencies
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Registers entity renderers for custom thrown potion entities in Brewables.
// Ensures proper rendering of splash and lingering Witch's Wart items.
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BrewablesEntityRenderers
{
    // Registers renderers with Forge during client-side mod loading
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

// Note: Subclassing ThrownItemRenderer will be necessary if custom rendering logic or throw arcs is needed.