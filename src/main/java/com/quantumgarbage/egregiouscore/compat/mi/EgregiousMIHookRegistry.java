package com.quantumgarbage.egregiouscore.compat.mi;

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import com.quantumgarbage.egregiouscore.EgregiousBlocks;
import com.quantumgarbage.egregiouscore.EgregiousItems;
import com.quantumgarbage.egregiouscore.EgregiousRecipeTypes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEntrypoint;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookRegistry;
import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

@MIHookEntrypoint
public class EgregiousMIHookRegistry implements MIHookRegistry {
  @Override
  public DeferredRegister.Blocks blockRegistry() {
    return EgregiousBlocks.Registry.BLOCKS;
  }

  @Override
  public DeferredRegister<BlockEntityType<?>> blockEntityRegistry() {
    return EgregiousBlocks.Registry.BLOCK_ENTITIES;
  }

  @Override
  public DeferredRegister.Items itemRegistry() {
    return EgregiousItems.Registry.ITEMS;
  }

  @Override
  public DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry() {
    return EgregiousRecipeTypes.RECIPE_SERIALIZERS;
  }

  @Override
  public DeferredRegister<RecipeType<?>> recipeTypeRegistry() {
    return EgregiousRecipeTypes.RECIPE_TYPES;
  }

  @Override
  public void onBlockRegister(BlockHolder blockHolder) {
    EgregiousBlocks.Registry.include(blockHolder);
  }

  @Override
  public void onBlockEntityRegister(BlockEntityType<?> blockEntityType) {}

  @Override
  public void onItemRegister(ItemHolder itemHolder) {
    EgregiousItems.Registry.include(itemHolder);
  }

  @Override
  public void onMachineRecipeTypeRegister(MachineRecipeType machineRecipeType) {}

  @Override
  public SortOrder sortOrderMachines() {
    return SortOrder.UNSORTED;
  }
}
