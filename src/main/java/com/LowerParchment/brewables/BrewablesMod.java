// Import self-created package declaration
package com.LowerParchment.brewables;
import com.LowerParchment.brewables.item.StirringRodItem;
import com.LowerParchment.brewables.block.BrewCauldronBlock;
import com.LowerParchment.brewables.event.PlayerInteractionHandler;
import com.LowerParchment.brewables.handler.ItemInCauldronHandler;

// Import dependencies and classes
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
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


// BrewablesMod class definition
@Mod(BrewablesMod.MODID)
public class BrewablesMod
{
    // Mod ID and logger
    public static final String MODID = "brewables";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Deferred registers for items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // ... and creative tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Registering the Stirring Rod item
    public static final RegistryObject<Item> STIRRING_ROD = ITEMS.register("stirring_rod",
        () -> new StirringRodItem(new Item.Properties()));

    // Registering the Witch's Wart item
    public static final RegistryObject<Item> WITCHS_WART = ITEMS.register("witchs_wart",
            () -> new Item(new Item.Properties().stacksTo(16)));

    // Registering the Brew Cauldron block, and its corresponding item
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> BREW_CAULDRON = BLOCKS.register("brew_cauldron", () ->
        new BrewCauldronBlock(BlockBehaviour.Properties.copy(Blocks.WATER_CAULDRON)));
    public static final RegistryObject<Item> BREW_CAULDRON_ITEM = ITEMS.register("brew_cauldron", () ->
        new BlockItem(BREW_CAULDRON.get(), new Item.Properties()));

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
                        output.accept(BREW_CAULDRON_ITEM.get());
                    }).build());

    // Integer property for the water level of the cauldron
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    // Constructor to initialize the mod
    public BrewablesMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addToCreativeTab);
    }

    // Common setup method for the mod
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Brewables Mod Loaded!");
    }

    // Method to add items to the Brewables creative tab
    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab() == BREWABLES_TAB.get())
        {
            event.accept(STIRRING_ROD.get());
        }
    }

    // Event handler for server starting
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Server is starting up with Brewables loaded.");
    }

    // Client-side setup for the mod
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LogUtils.getLogger().info("Client-side setup complete.");
        }
    }
}
