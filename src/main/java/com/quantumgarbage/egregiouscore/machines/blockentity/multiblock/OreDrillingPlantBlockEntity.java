package com.quantumgarbage.egregiouscore.machines.blockentity.multiblock;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import com.quantumgarbage.egregiouscore.EgregiousCore;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import com.quantumgarbage.egregiouscore.machines.component.OreDrillComponent;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.swedz.tesseract.neoforge.compat.mi.guicomponent.modularmultiblock.ModularMultiblockGui;
import net.swedz.tesseract.neoforge.compat.mi.guicomponent.modularmultiblock.ModularMultiblockGuiLine;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.multiblock.BasicMultiblockMachineBlockEntity;

public class OreDrillingPlantBlockEntity extends BasicMultiblockMachineBlockEntity {
  private static final String[][] layers = {
    {"M M", "HHH", " H "}, //
    {" C ", "HCH", "HPH"}, //
    {"M M", "HHH", " # "}
  };
  private static final HatchFlags hatches =
      new HatchFlags.Builder()
          .with(HatchTypes.ENERGY_INPUT, HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT)
          .build();

  private static final ShapeTemplate[] SHAPES =
      new ShapeTemplate[] {
        new ShapeTemplate.LayeredBuilder(MachineCasings.STEEL, layers)
            .key('M', SimpleMember.forBlockId(MI.id("steel_machine_casing")), HatchFlags.NO_HATCH)
            .key(
                'C',
                SimpleMember.forBlockId(ResourceLocation.parse("minecraft:chain")),
                HatchFlags.NO_HATCH)
            .key('H', SimpleMember.forBlockId(MI.id("steel_machine_casing")), hatches)
            .key(
                'P',
                SimpleMember.forBlockId(MI.id("steel_machine_casing_pipe")),
                HatchFlags.NO_HATCH)
            .build()
      };

  protected final MultiblockInventoryComponent inventory;
  private final OreDrillComponent oreDrillComponent;

  public OreDrillingPlantBlockEntity(BEP bep) {
    super(
        bep,
        new MachineGuiParameters.Builder("ore_drilling_plant", false).backgroundHeight(200).build(),
        SHAPES);
    inventory = new MultiblockInventoryComponent();
    oreDrillComponent = new OreDrillComponent(this, inventory);
    this.registerGuiComponent(
        new ModularMultiblockGui.Server(
            0,
            ModularMultiblockGui.HEIGHT,
            content -> {
              content.add(
                  (this.isShapeValid()
                          ? MIText.MultiblockShapeValid
                          : MIText.MultiblockShapeInvalid)
                      .text(),
                  this.isShapeValid()
                      ? ModularMultiblockGuiLine.WHITE
                      : ModularMultiblockGuiLine.RED);
            }));
  }

  public static void registerReiShapes() {
    ReiMachineRecipes.registerMultiblockShape(
        EgregiousCore.id("ore_drilling_plant"), SHAPES[0], "");
  }

  @Override
  protected void onRematch(ShapeMatcher shapeMatcher) {
    if (shapeMatcher.isMatchSuccessful()) {
      inventory.rebuild(shapeMatcher);
    }
  }

  @Override
  public final void tick() {
    if (!level.isClientSide) {
      link();

      boolean newActive = false;

      for (ConfigurableItemStack stack : inventory.getItemInputs()) {
        Optional<DrillingPlantInput> drillInput = getDrillInput(stack);

        // if the present item is actually a drill
        if (drillInput.map(recipe -> oreDrillComponent.trySetActiveRecipe(recipe)).isPresent()) {
          oreDrillComponent.tickRecipe();
        }
      }

      isActive.updateActive(newActive, this);
    }
  }

  public Optional<DrillingPlantInput> getDrillInput(ConfigurableItemStack stack) {
    return Optional.ofNullable(
        stack
            .getVariant()
            .getItem()
            .getDefaultInstance()
            .getItemHolder()
            .getData(EgregiousDatamaps.DRILLING_PLANT_INPUT));
  }

  @Override
  public ShapeTemplate getBigShape() {
    return SHAPES[SHAPES.length - 1];
  }

  /**
   * @return The inventory that will be synced with the client.
   */
  @Override
  public MIInventory getInventory() {
    return MIInventory.EMPTY;
  }

  @Override
  public MultiblockInventoryComponent getMultiblockInventoryComponent() {
    return inventory;
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
