package com.quantumgarbage.egregiouscore;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your
// config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
  static final ModConfigSpec SPEC = BUILDER.build();
  private static final ModConfigSpec.IntValue PROSPECTOR_RADIUS =
      BUILDER.comment("").defineInRange("prospectorRadius", 20, 3, 64);
}
