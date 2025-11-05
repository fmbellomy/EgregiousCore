package com.quantumgarbage.egregiouscore.machines.blockentity.multiblock;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.helper.SteamHelper;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.util.Simulation;
import com.quantumgarbage.egregiouscore.EgregiousCore;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.EgregiousText;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import com.quantumgarbage.egregiouscore.machines.component.OreDrillComponent;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.swedz.tesseract.neoforge.compat.mi.guicomponent.modularmultiblock.ModularMultiblockGui;
import net.swedz.tesseract.neoforge.compat.mi.guicomponent.modularmultiblock.ModularMultiblockGuiLine;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.multiblock.BasicMultiblockMachineBlockEntity;

public class OreDrillingPlantBlockEntity extends BasicMultiblockMachineBlockEntity {
  private static final String[][] layers = {
    {"P P", "HHH", " P "}, //
    {" C ", "HCH", " P "},
    {"P P", "H#H", " P "}
  };
  private static final HatchFlags hatches =
      new HatchFlags.Builder()
          .with(HatchTypes.FLUID_INPUT, HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT)
          .build();

  private static final ShapeTemplate[] SHAPES =
      new ShapeTemplate[] {
        new ShapeTemplate.LayeredBuilder(MachineCasings.BRONZE, layers)
            .key(
                'C',
                SimpleMember.forBlockId(ResourceLocation.parse("minecraft:chain")),
                HatchFlags.NO_HATCH)
            .key('H', SimpleMember.forBlockId(MI.id("bronze_machine_casing")), hatches)
            .key(
                'P',
                SimpleMember.forBlockId(MI.id("bronze_machine_casing_pipe")),
                HatchFlags.NO_HATCH)
            .build()
      };

  protected final MultiblockInventoryComponent inventory;
  private final OreDrillComponent oreDrillComponent;

  public OreDrillingPlantBlockEntity(BEP bep) {
    super(
        bep,
        new MachineGuiParameters.Builder(
                EgregiousCore.id("ore_drilling_plant"),
                false) // make sure to pass in a resource location and not just a string
            .backgroundHeight(200)
            .build(),
        SHAPES);
    inventory = new MultiblockInventoryComponent();
    oreDrillComponent = new OreDrillComponent(this, inventory);
    this.registerGuiComponent(
        new ModularMultiblockGui.Server(
            0,
            ModularMultiblockGui.HEIGHT,
            content -> {
              content
                  .add(
                      (this.isShapeValid()
                              ? MIText.MultiblockShapeValid
                              : MIText.MultiblockShapeInvalid)
                          .text(),
                      this.isShapeValid()
                          ? ModularMultiblockGuiLine.WHITE
                          : ModularMultiblockGuiLine.RED)
                  .add(
                      this.oreDrillComponent.hasActiveRecipe()
                          ? Component.literal(
                              "%.1f / 100%%".formatted(oreDrillComponent.getProgress() * 100))
                          : Component.literal(""))
                  .add(
                      this.oreDrillComponent.getNextOreToMine() != null
                          ? Component.literal("Mining ")
                              .append(
                                  getLevel()
                                      .getBlockState(oreDrillComponent.getNextOreToMine())
                                      .getBlock()
                                      .getName())
                          : Component.literal(""))
                  .add(
                      this.inventory.getItemInputs() == null
                              || this.inventory.getItemInputs().isEmpty()
                          ? EgregiousText.PlantMissingDrills
                          : EgregiousText.Empty,
                      this.oreDrillComponent.hasActiveRecipe()
                          ? ModularMultiblockGuiLine.RED
                          : ModularMultiblockGuiLine.WHITE)
                  .add(
                      this.oreDrillComponent.isDone()
                          ? Component.literal("Drill finished mining.")
                          : Component.literal(""),
                      this.oreDrillComponent.hasActiveRecipe()
                          ? ModularMultiblockGuiLine.RED
                          : ModularMultiblockGuiLine.WHITE);
            }));
  }

  public static void registerReiShapes() {
    ReiMachineRecipes.registerMultiblockShape(
        EgregiousCore.id("ore_drilling_plant"), SHAPES[0], "");
  }

  public final long consumeEu(long max, Simulation simulation) {
    return SteamHelper.consumeSteamEu(inventory.getFluidInputs(), max, simulation);
  }

  @Override
  protected void onRematch(ShapeMatcher shapeMatcher) {
    super.onRematch(shapeMatcher);
    if (shapeMatcher.isMatchSuccessful()) {
      inventory.rebuild(shapeMatcher);
    }
  }

  @Override
  public final void tick() {
    if (!level.isClientSide) {
      link();
      if (!isShapeValid()) {
        return;
      }
      boolean newActive = false;

      for (ConfigurableItemStack stack : inventory.getItemInputs()) {
        Optional<DrillingPlantInput> drillInput = getDrillInput(stack);
        // if the present item is actually a drill
        boolean isValidDrill = drillInput.map(oreDrillComponent::trySetActiveRecipe).isPresent();
        if (isValidDrill) {
          oreDrillComponent.tickRecipe(this, stack, drillInput.get());
          break;
        } else {
          oreDrillComponent.setUsedEnergy(0);
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
    return new MachineModelClientData(MachineCasings.BRONZE, orientation.facingDirection)
        .active(isActive.isActive);
  }
}
