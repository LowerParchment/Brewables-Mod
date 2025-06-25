package com.LowerParchment.brewables;

import com.LowerParchment.brewables.item.StirringRodItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(BrewablesMod.MODID)
public class BrewablesMod {
    public static final String MODID = "brewables";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Deferred registers for items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // ... and creative tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Registering the Stirring Rod item
    public static final RegistryObject<Item> STIRRING_ROD = ITEMS.register("stirring_rod",
        () -> new StirringRodItem(new Item.Properties()));

    // Registering the Brewables Creative Mode Tab
    public static final RegistryObject<CreativeModeTab> BREWABLES_TAB = CREATIVE_TABS.register("brewables_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.brewables.brewables_tab"))
                    .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
                    .icon(() -> {
                        Item item = STIRRING_ROD.get();
                        return item != null ? new ItemStack(item) : new ItemStack(Items.STICK);
                    })
                    .displayItems((parameters, output) -> {
                        output.accept(STIRRING_ROD.get());
                    }).build());

    public BrewablesMod(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addToCreativeTab);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Brewables Mod Loaded!");
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == BREWABLES_TAB.get()) {
            event.accept(STIRRING_ROD.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server is starting up with Brewables loaded.");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LogUtils.getLogger().info("Client-side setup complete.");
        }
    }
}
