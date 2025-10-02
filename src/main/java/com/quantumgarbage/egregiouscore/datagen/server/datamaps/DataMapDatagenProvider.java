package com.quantumgarbage.egregiouscore.datagen.server.datamaps;

import aztech.modern_industrialization.MI;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import com.quantumgarbage.egregiouscore.datamap.GasTurbineFuel;
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

  // this last one only exists in Egregious Industrialization by virtue of MI's KJS runtime data
  // generation.
  private static final ResourceLocation IRIDIUM_DRILL = MI.id("iridium_drill");

  // all of these only make sense in Egregious Industrialization since gas turbines aren't real.
  private static final ResourceLocation BENZENE = MI.id("benzene");
  private static final ResourceLocation PHENOL = MI.id("phenol");
  private static final ResourceLocation TOLUENE = MI.id("phenol");
  private static final ResourceLocation METHANE = MI.id("methane");
  private static final ResourceLocation NAPHTHA = MI.id("naphtha");

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
    this.addDrillingPlantInput(IRIDIUM_DRILL, 2048L, 7.5f, 0.5f);

    this.addGasTurbineFuel(BENZENE, 800);
    this.addGasTurbineFuel(METHANE, 500);
    this.addGasTurbineFuel(NAPHTHA, 120);
    this.addGasTurbineFuel(PHENOL, 360);
    this.addGasTurbineFuel(TOLUENE, 400);
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

  private void addGasTurbineFuel(ResourceLocation item, long eut) {
    this.builder(EgregiousDatamaps.GAS_TURBINE_FUEL).add(item, new GasTurbineFuel(eut), false);
  }
}
