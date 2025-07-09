// Import user defined dependencies
package com.LowerParchment.brewables;
import com.LowerParchment.brewables.item.StirringRodItem;
import com.LowerParchment.brewables.item.WitchsWartItem;
import com.LowerParchment.brewables.item.SplashWitchsWartItem;
import com.LowerParchment.brewables.item.LingeringWitchsWartItem;
import com.LowerParchment.brewables.block.BrewCauldronBlock;

// Import Minecraft, Forge, and Java dependencies
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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

// Main class and entrypoint for the Brewables mod
@Mod(BrewablesMod.MODID)
public class BrewablesMod
{
    // Global mod ID and logger
    public static final String MODID = "brewables";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Registers for items, blocks, and creative tab
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // --- Item registrations ---
    public static final RegistryObject<Item> STIRRING_ROD = ITEMS.register("stirring_rod", () -> new StirringRodItem(new Item.Properties()));
    public static final RegistryObject<Item> WITCHS_WART_DRINKABLE = ITEMS.register("witchs_wart", () -> new WitchsWartItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPLASH_WITCHS_WART = ITEMS.register("splash_witchs_wart", SplashWitchsWartItem::new);
    public static final RegistryObject<Item> LINGERING_WITCHS_WART = ITEMS.register("lingering_witchs_wart", LingeringWitchsWartItem::new);

    // --- Block registrations ---
    public static final RegistryObject<Block> BREW_CAULDRON = BLOCKS.register("brew_cauldron", () -> new BrewCauldronBlock(BlockBehaviour.Properties.copy(Blocks.WATER_CAULDRON)));
    public static final RegistryObject<Item> BREW_CAULDRON_ITEM = ITEMS.register("brew_cauldron", () -> new BlockItem(BREW_CAULDRON.get(), new Item.Properties()));

    // --- Creative Tab registration ---
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
                        output.accept(BREW_CAULDRON_ITEM.get());
                        output.accept(WITCHS_WART_DRINKABLE.get());
                        output.accept(SPLASH_WITCHS_WART.get());
                        output.accept(LINGERING_WITCHS_WART.get());
                    }).build());

    // Property for tracking cauldron fluid levels (used in blockstate)
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    // --- Constructor and Event Registration ---
    public BrewablesMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addToCreativeTab);
    }

    // Lifecycle method called during mod loading
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Brewables Mod Loaded!");
    }

    // Re-adds items to the tab in case another mod interferes with tab contents
    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab() == BREWABLES_TAB.get())
        {
            event.accept(STIRRING_ROD.get());
        }
    }

    // Fired when the server begins loading
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Server is starting up with Brewables loaded.");
    }

    // --- Client-side setup and renderer registration ---
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LogUtils.getLogger().info("Client-side setup complete.");
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(
                ModEntities.THROWN_WITCHS_WART_ENTITY.get(),
                context -> new ThrownItemRenderer<>(context)
            );

            event.registerEntityRenderer(
                ModEntities.THROWN_LINGERING_WITCHS_WART_ENTITY.get(),
                context -> new ThrownItemRenderer<>(context)
            );
        }
    }
}
