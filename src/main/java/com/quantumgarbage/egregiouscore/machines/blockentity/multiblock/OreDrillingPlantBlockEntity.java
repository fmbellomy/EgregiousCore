package com.quantumgarbage.egregiouscore.machines.blockentity.multiblock;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import com.quantumgarbage.egregiouscore.EgregiousCore;
import net.minecraft.resources.ResourceLocation;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.multiblock.BasicMultiblockMachineBlockEntity;

public class OreDrillingPlantBlockEntity extends BasicMultiblockMachineBlockEntity {
  private static final String[][] layers = {
    {"M M", "HHH", " # "}, //
    {" C ", "HCH", "HPH"}, //
    {"M M", "HHH", " H "}
  };
  private static final HatchFlags hatches =
      new HatchFlags.Builder()
          .with(HatchTypes.ENERGY_INPUT, HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT)
          .build();
  ;
  private static final ShapeTemplate SHAPE =
      new ShapeTemplate.LayeredBuilder(MachineCasings.STEEL, layers)
          .key('M', SimpleMember.forBlockId(MI.id("steel_machine_casing")), HatchFlags.NO_HATCH)
          .key(
              'C',
              SimpleMember.forBlockId(ResourceLocation.parse("minecraft:chain")),
              HatchFlags.NO_HATCH)
          .key('H', SimpleMember.forBlockId(MI.id("steel_machine_casing")), hatches)
          .key(
              'P', SimpleMember.forBlockId(MI.id("steel_machine_casing_pipe")), HatchFlags.NO_HATCH)
          .build();

  public OreDrillingPlantBlockEntity(BEP bep) {
    super(
        bep,
        new MachineGuiParameters.Builder("ore_drilling_plant", false).backgroundHeight(128).build(),
        new ShapeTemplate[] {SHAPE});
  }

  public static void registerReiShapes() {
    ReiMachineRecipes.registerMultiblockShape(EgregiousCore.id("ore_drilling_plant"), SHAPE, "");
  }

  /**
   * @return The inventory that will be synced with the client.
   */
  @Override
  public MIInventory getInventory() {
    return MIInventory.EMPTY;
  }

  public boolean isEnabled() {
    return true;
  }

  @Override
  protected MachineModelClientData getMachineModelData() {
    return new MachineModelClientData(MachineCasings.STEEL, orientation.facingDirection)
        .active(isActive.isActive);
  }
}
