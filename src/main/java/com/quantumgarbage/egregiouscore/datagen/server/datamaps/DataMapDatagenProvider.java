package com.quantumgarbage.egregiouscore.datagen.server.datamaps;

import aztech.modern_industrialization.MI;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataMapDatagenProvider extends DataMapProvider {
  // i SHOULD be able to get away with doing this, but we'll see i guess.
  private static final ResourceLocation BRONZE_DRILL = MI.id("bronze_drill");
  private static final ResourceLocation STEEL_DRILL = MI.id("steel_drill");
  private static final ResourceLocation ALUMINUM_DRILL = MI.id("aluminum_drill");
  private static final ResourceLocation STAINLESS_STEEL_DRILL = MI.id("stainless_steel_drill");
  private static final ResourceLocation TITANIUM_DRILL = MI.id("titanium_drill");

  public DataMapDatagenProvider(GatherDataEvent event) {
    super(event.getGenerator().getPackOutput(), event.getLookupProvider());
  }

  @Override
  protected void gather() {
    this.addDrillingPlantInput(BRONZE_DRILL, 4L, 1.25f, 5f);
    this.addDrillingPlantInput(STEEL_DRILL, 16L, 1.75f, 5f);
    this.addDrillingPlantInput(ALUMINUM_DRILL, 64, 2.25f, 5f);
    this.addDrillingPlantInput(STAINLESS_STEEL_DRILL, 256L, 3f, 3f);
    this.addDrillingPlantInput(TITANIUM_DRILL, 512L, 5f, 2f);
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  private void addDrillingPlantInput(
      ResourceLocation item, long euCost, float multiplier, float breakProbability) {
    this.builder(EgregiousDatamaps.DRILLING_PLANT_INPUT)
        .add(item, new DrillingPlantInput(euCost, multiplier, breakProbability), false);
  }
}
