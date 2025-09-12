package com.quantumgarbage.egregiouscore;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.common.CommonModelBuilders;
import net.swedz.tesseract.neoforge.registry.common.MICommonCapabitilies;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

public class EgregiousItems {
  public static final ItemHolder<ProspectorItem> PROSPECTOR =
      create(
              "prospector",
              "Ore Prospector",
              (p) -> new ProspectorItem(p, 1_000_000L),
              SortOrder.UNSORTED)
          .withCapabilities(MICommonCapabitilies::simpleEnergyItem)
          .withModelBuilder(CommonModelBuilders::handheld)
          .register();

  public static void init(IEventBus bus) {
    EgregiousItems.Registry.init(bus);
  }

  public static Set<ItemHolder> values() {
    return Set.copyOf(EgregiousItems.Registry.HOLDERS);
  }

  public static ItemHolder valueOf(String id) {
    return EgregiousItems.Registry.HOLDERS.stream()
        .filter((holder) -> holder.identifier().id().equals(id))
        .findFirst()
        .orElseThrow();
  }

  public static <Type extends Item> ItemHolder<Type> create(
      String id, String englishName, Function<Item.Properties, Type> creator, SortOrder sortOrder) {
    ItemHolder<Type> holder =
        new ItemHolder<>(EgregiousCore.id(id), englishName, EgregiousItems.Registry.ITEMS, creator)
            .sorted(sortOrder);
    EgregiousItems.Registry.include(holder);
    return holder;
  }

  public static final class Registry {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(EgregiousCore.ID);
    private static final Set<ItemHolder> HOLDERS = Sets.newHashSet();

    private static void init(IEventBus bus) {
      ITEMS.register(bus);
    }

    public static void include(ItemHolder holder) {
      HOLDERS.add(holder);
    }
  }
}
