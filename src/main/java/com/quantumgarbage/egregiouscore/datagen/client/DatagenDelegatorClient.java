package com.quantumgarbage.egregiouscore.datagen.client;

import com.quantumgarbage.egregiouscore.datagen.client.provider.LanguageDatagenProvider;
import com.quantumgarbage.egregiouscore.datagen.client.provider.models.BlockModelsDatagenProvider;
import com.quantumgarbage.egregiouscore.datagen.client.provider.models.ItemModelsDatagenProvider;
import java.util.function.Function;
import net.minecraft.data.DataProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.datagen.mi.MIDatagenHooks;

public final class DatagenDelegatorClient {
  public static void configure(GatherDataEvent event) {
    MIDatagenHooks.Client.includeMISprites(event);
    add(event, BlockModelsDatagenProvider::new);
    add(event, ItemModelsDatagenProvider::new);
    add(event, LanguageDatagenProvider::new);
  }

  private static void add(
      GatherDataEvent event, Function<GatherDataEvent, DataProvider> providerCreator) {
    event.getGenerator().addProvider(event.includeClient(), providerCreator.apply(event));
  }
}
