package com.quantumgarbage.egregiouscore;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your
// config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
  public static final ModConfigSpec.IntValue PROSPECTOR_RADIUS =
      BUILDER
          .comment("The number of chunks out from the center to check when using the prospector.")
          .defineInRange("prospectorRadius", 1, 0, 4);
  public static final ModConfigSpec.LongValue PROSPECTOR_ENERGY_CAPACITY =
      BUILDER
          .comment("The energy capacity of the prospector.")
          .defineInRange("prospectorEnergyCapacity", 1_048_576L, 0, Long.MAX_VALUE);
  public static final ModConfigSpec.LongValue PROSPECTOR_ENERGY_COST =
      BUILDER
          .comment("How much energy the prospector should consume on each use.")
          .defineInRange("prospectorEnergyCost", 8192L, 0, Long.MAX_VALUE);
  static final ModConfigSpec SPEC = BUILDER.build();
}
