package com.quantumgarbage.egregiouscore.datagen.server.loottable;

import com.quantumgarbage.egregiouscore.EgregiousBlocks;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;

public class BlockLootTableDatagenProvider extends BlockLootSubProvider {
  public BlockLootTableDatagenProvider(HolderLookup.Provider registries) {
    super(Set.of(), FeatureFlags.VANILLA_SET, registries);
  }

  @Override
  protected Iterable<Block> getKnownBlocks() {
    return EgregiousBlocks.values().stream()
        .filter(BlockHolder::hasLootTable)
        .map(BlockHolder::get)
        .toList();
  }

  @Override
  protected void generate() {
    for (BlockHolder<?> block : EgregiousBlocks.values()) {
      if (block.hasLootTable()) {
        this.add(block.get(), block.buildLootTable(this));
      }
    }
  }
}
