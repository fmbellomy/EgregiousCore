package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.util.TagHelper;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.common.CommonLootTableBuilders;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;
import net.swedz.tesseract.neoforge.registry.holder.BlockWithItemHolder;

public class EgregiousBlocks {
  public static void init(IEventBus bus) {
    EgregiousBlocks.Registry.init(bus);
  }

  public static Set<BlockHolder> values() {
    return Set.copyOf(EgregiousBlocks.Registry.HOLDERS);
  }

  public static Block get(String id) {
    return EgregiousBlocks.Registry.HOLDERS.stream()
        .filter((b) -> b.identifier().id().equals(id))
        .findFirst()
        .orElseThrow()
        .get();
  }

  public static <BlockType extends Block> BlockHolder<BlockType> create(
      String id, String englishName, Function<BlockBehaviour.Properties, BlockType> blockCreator) {
    BlockHolder<BlockType> holder =
        new BlockHolder<>(
            EgregiousCore.id(id), englishName, EgregiousBlocks.Registry.BLOCKS, blockCreator);
    EgregiousBlocks.Registry.include(holder);
    return holder;
  }

  public static <BlockType extends Block, ItemType extends BlockItem>
      BlockWithItemHolder<BlockType, ItemType> create(
          String id,
          String englishName,
          Function<BlockBehaviour.Properties, BlockType> blockCreator,
          BiFunction<Block, Item.Properties, ItemType> itemCreator,
          SortOrder sortOrder) {
    BlockWithItemHolder<BlockType, ItemType> holder =
        new BlockWithItemHolder<>(
            EgregiousCore.id(id),
            englishName,
            EgregiousBlocks.Registry.BLOCKS,
            blockCreator,
            EgregiousItems.Registry.ITEMS,
            itemCreator);
    holder.item().sorted(sortOrder);
    EgregiousBlocks.Registry.include(holder);
    EgregiousItems.Registry.include(holder.item());
    return holder;
  }

  public static BlockHolder<Block> createSimple(
      String id,
      String englishName,
      SortOrder sortOrder,
      MapColor mapColor,
      float destroyTime,
      float explosionResistance) {
    return create(id, englishName, Block::new, BlockItem::new, sortOrder)
        .withProperties(
            (p) ->
                p.mapColor(mapColor)
                    .destroyTime(destroyTime)
                    .explosionResistance(explosionResistance)
                    .requiresCorrectToolForDrops())
        .tag(TagHelper.getMiningLevelTag(1))
        .withLootTable(CommonLootTableBuilders::self);
  }

  public static final class Registry {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(EgregiousCore.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EgregiousCore.ID);
    private static final Set<BlockHolder> HOLDERS = Sets.newHashSet();

    private static void init(IEventBus bus) {
      BLOCKS.register(bus);
      BLOCK_ENTITIES.register(bus);
    }

    public static void include(BlockHolder holder) {
      HOLDERS.add(holder);
    }
  }
}
