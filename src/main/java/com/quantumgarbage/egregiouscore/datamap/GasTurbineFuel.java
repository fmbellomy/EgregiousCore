package com.quantumgarbage.egregiouscore.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.swedz.tesseract.neoforge.helper.RegistryHelper;

public record GasTurbineFuel(long eut) {
  public static final Codec<GasTurbineFuel> CODEC =
      RecordCodecBuilder.create(
          (instance) ->
              instance
                  .group(Codec.LONG.fieldOf("eut").forGetter(GasTurbineFuel::eut))
                  .apply(instance, GasTurbineFuel::new));

  public static GasTurbineFuel getFor(Fluid fluid) {
    return RegistryHelper.holder(BuiltInRegistries.FLUID, fluid)
        .getData(EgregiousDatamaps.GAS_TURBINE_FUEL);
  }
}
