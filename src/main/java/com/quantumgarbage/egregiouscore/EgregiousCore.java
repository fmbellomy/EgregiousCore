package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.MIComponents;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.registry.common.MICommonCapabitilies;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EgregiousCore.ID)
public class EgregiousCore {

  // Define mod id in a common place for everything to reference
  public static final String ID = "egregiouscore";
  // Create a Deferred Register to hold Blocks which will all be registered under the
  // "egregiouscore" namespace
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
  // Create a Deferred Register to hold Items which will all be registered under the
  // "egregiouscore" namespace
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
  public static final DeferredItem<Item> PROSPECTOR_ITEM =
      ITEMS.register(
          "prospector",
          () ->
              new ProspectorItem(
                  new Item.Properties().component(MIComponents.ENERGY.get(), 1_000_000_000L)));
  // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the
  // "egregiouscore" namespace
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
  // Creates a creative tab with the id "egregiouscore:example_tab" for the example item, that is
  // placed after the combat tab
  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
      CREATIVE_MODE_TABS.register(
          "egregious_core_tab",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.egregious_core"))
                  .withTabsBefore(CreativeModeTabs.COMBAT)
                  .icon(() -> new ItemStack(PROSPECTOR_ITEM.get()))
                  .displayItems(
                      (params, output) -> {
                        output.accept(PROSPECTOR_ITEM.get());
                      })
                  .build());
  // Directly reference a slf4j logger
  private static final Logger LOGGER = LogUtils.getLogger();

  // The constructor for the mod class is the first code that is run when your mod is loaded.
  // FML will recognize some parameter types like IEventBus or ModContainer and pass them in
  // automatically.
  public EgregiousCore(IEventBus modEventBus, ModContainer modContainer) {
    // Register the commonSetup method for modloading
    modEventBus.addListener(this::commonSetup);

    // Register the Deferred Register to the mod event bus so blocks get registered
    BLOCKS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so items get registered
    ITEMS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so tabs get registered
    CREATIVE_MODE_TABS.register(modEventBus);

    modEventBus.addListener(this::registerCapabilities);
    // Register ourselves for server and other game events we are interested in.
    // Note that this is necessary if and only if we want *this* class (Egregious_core) to respond
    // directly to events.
    // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like
    // onServerStarting() below.
    NeoForge.EVENT_BUS.register(this);

    // Register our mod's ModConfigSpec so that FML can create and load the config file for us
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  public static ResourceLocation id(String name) {
    return ResourceLocation.fromNamespaceAndPath(ID, name);
  }

  private void registerCapabilities(RegisterCapabilitiesEvent event) {
    MICommonCapabitilies.simpleEnergyItem((ProspectorItem) PROSPECTOR_ITEM.get(), event);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {}

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {}

  // You can use EventBusSubscriber to automatically register all static methods in the class
  // annotated with @SubscribeEvent
  @EventBusSubscriber(modid = ID, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {}
  }
}
