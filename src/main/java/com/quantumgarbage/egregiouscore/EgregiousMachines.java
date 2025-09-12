package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.machines.models.MachineCasings;
import com.quantumgarbage.egregiouscore.machines.blockentity.multiblock.OreDrillingPlantBlockEntity;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.MultiblockMachinesMIHookContext;

public class EgregiousMachines {
  public static void multiblocks(MultiblockMachinesMIHookContext hook) {
    hook.builder("ore_drilling_plant", "Ore Drilling Plant", OreDrillingPlantBlockEntity::new)
        .builtinModel(
            MachineCasings.STEEL,
            "ore_drilling_plant",
            (model) -> model.front(true).top(true).side(false).active(true))
        .registrator((__) -> OreDrillingPlantBlockEntity.registerReiShapes())
        .registerMachine();
  }
}
