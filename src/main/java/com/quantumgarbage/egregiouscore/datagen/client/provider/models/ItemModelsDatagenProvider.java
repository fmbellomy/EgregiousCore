package com.quantumgarbage.egregiouscore.datagen.client.provider.models;

import com.quantumgarbage.egregiouscore.EgregiousCore;
import com.quantumgarbage.egregiouscore.EgregiousItems;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

public class ItemModelsDatagenProvider extends ItemModelProvider {
  public ItemModelsDatagenProvider(GatherDataEvent event) {
    super(event.getGenerator().getPackOutput(), EgregiousCore.ID, event.getExistingFileHelper());
  }

  @Override
  protected void registerModels() {
    for (ItemHolder item : EgregiousItems.values()) {
      if (item.hasModelProvider()) {
        item.modelProvider().accept(this);
      }
    }
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }
}
