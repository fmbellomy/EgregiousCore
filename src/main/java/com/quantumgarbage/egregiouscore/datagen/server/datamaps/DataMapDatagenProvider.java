package com.quantumgarbage.egregiouscore.datagen.server.datamaps;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataMapDatagenProvider extends DataMapProvider {
  public DataMapDatagenProvider(GatherDataEvent event) {
    super(event.getGenerator().getPackOutput(), event.getLookupProvider());
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }
}
