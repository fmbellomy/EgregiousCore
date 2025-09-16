package com.quantumgarbage.egregiouscore.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DrillingPlantInput(long euCost, float multiplier, float breakProbability) {

  public static final Codec<DrillingPlantInput> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("eu_cost").forGetter(DrillingPlantInput::euCost),
                      Codec.floatRange(1.0f, 10.0f)
                          .fieldOf("multiplier")
                          .forGetter(DrillingPlantInput::multiplier),
                      Codec.floatRange(0.00f, 100.0f)
                          .fieldOf("break_probability")
                          .forGetter(DrillingPlantInput::breakProbability))
                  .apply(instance, DrillingPlantInput::new));

  public DrillingPlantInput {}
}
