package com.quantumgarbage.egregiouscore.datagen;

import com.quantumgarbage.egregiouscore.datagen.client.DatagenDelegatorClient;
import com.quantumgarbage.egregiouscore.datagen.server.DatagenDelegatorServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DatagenDelegator {
  @SubscribeEvent
  public void gatherData(GatherDataEvent event) {
    DatagenDelegatorClient.configure(event);
    DatagenDelegatorServer.configure(event);
  }
}
