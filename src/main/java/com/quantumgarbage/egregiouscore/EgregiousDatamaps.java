package com.quantumgarbage.egregiouscore;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class EgregiousDatamaps {
  public static final Set<DataMapType<?, ?>> DATA_MAPS = Sets.newHashSet();
  public static final DataMapType<Item, DrillingPlantInput> DRILLING_PLANT_INPUT =
      create("drilling_plant_input", Registries.ITEM, DrillingPlantInput.CODEC, true);

  private static <R, T> DataMapType<R, T> create(
      String name, ResourceKey<Registry<R>> registry, Codec<T> codec, boolean sync) {
    var builder = DataMapType.builder(EgregiousCore.id(name), registry, codec);
    if (sync) {
      builder = builder.synced(codec, true);
    }
    var type = builder.build();
    DATA_MAPS.add(type);
    return type;
  }

  public static void init(RegisterDataMapTypesEvent event) {
    DATA_MAPS.forEach(event::register);
  }
}
