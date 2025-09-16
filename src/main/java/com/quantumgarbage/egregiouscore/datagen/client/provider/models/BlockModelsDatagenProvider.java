package com.quantumgarbage.egregiouscore.datagen.client.provider.models;

import com.quantumgarbage.egregiouscore.EgregiousBlocks;
import com.quantumgarbage.egregiouscore.EgregiousCore;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;

public class BlockModelsDatagenProvider extends BlockStateProvider {
  public BlockModelsDatagenProvider(GatherDataEvent event) {
    super(event.getGenerator().getPackOutput(), EgregiousCore.ID, event.getExistingFileHelper());
  }

  @Override
  protected void registerStatesAndModels() {
    for (BlockHolder<?> block : EgregiousBlocks.values()) {
      if (block.hasModelProvider()) {
        block.modelProvider().accept(this);
      }
    }
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }
}
