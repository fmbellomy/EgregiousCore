package com.quantumgarbage.egregiouscore;

import java.util.Comparator;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

public class EgregiousCreativeTab {
  private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EgregiousCore.ID);

  public static final Supplier<CreativeModeTab> CREATIVE_TAB =
      CREATIVE_MODE_TABS.register(
          EgregiousCore.ID,
          () ->
              CreativeModeTab.builder()
                  .title(
                      Component.translatable(
                          "itemGroup.%s.%s".formatted(EgregiousCore.ID, EgregiousCore.ID)))
                  .icon(
                      () -> {
                        ItemStack stack = EgregiousItems.PROSPECTOR.asItem().getDefaultInstance();
                        return stack;
                      })
                  .displayItems(
                      (params, output) -> {
                        Comparator<ItemHolder> compareBySortOrder =
                            Comparator.comparing(ItemHolder::sortOrder);
                        Comparator<ItemHolder> compareByName =
                            Comparator.comparing((i) -> i.identifier().id());
                        EgregiousItems.values().stream()
                            .sorted(compareBySortOrder.thenComparing(compareByName))
                            .forEach(output::accept);
                      })
                  .build());

  public static void init(IEventBus bus) {
    CREATIVE_MODE_TABS.register(bus);
  }
}
