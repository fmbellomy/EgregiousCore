package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

@Mod(dist = Dist.CLIENT, value = EgregiousCore.ID)
@EventBusSubscriber(value = Dist.CLIENT, modid = EgregiousCore.ID)
public final class EgregiousClient {
  @SubscribeEvent
  private static void registerItemProperties(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> EgregiousItems.values().forEach(ItemHolder::triggerClientRegistrationListener));
  }

  @SubscribeEvent
  private static void registerBlockEntityRenderers(FMLClientSetupEvent event) {
    for (DeferredHolder<Block, ? extends Block> blockDef :
        EgregiousBlocks.Registry.BLOCKS.getEntries()) {
      if (blockDef.get() instanceof MachineBlock machine) {
        try {
          MachineBlockEntity blockEntity = machine.getBlockEntityInstance();
          BlockEntityType type = blockEntity.getType();

          BlockEntityRendererProvider provider =
              switch (blockEntity) {
                case MultiblockMachineBlockEntity be -> MultiblockMachineBER::new;
                default -> MachineBlockEntityRenderer::new;
              };
          BlockEntityRenderers.register(type, provider);
        } catch (Exception ex) {
          throw new RuntimeException(
              "Failed to register BER for %s".formatted(blockDef.getId()), ex);
        }
      }
    }
  }
}
