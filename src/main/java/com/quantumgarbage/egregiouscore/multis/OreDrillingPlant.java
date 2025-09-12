package com.quantumgarbage.egregiouscore.multis;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.api.machine.component.EnergyAccess;
import aztech.modern_industrialization.api.machine.component.InventoryAccess;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.OverdriveComponent;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.util.Simulation;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.swedz.tesseract.neoforge.api.Tickable;
import org.jetbrains.annotations.Nullable;

public class OreDrillingPlant extends MultiblockMachineBlockEntity
    implements Tickable,
        EnergyListComponentHolder,
        MultiblockInventoryComponentHolder,
        DrillPlantComponent.Behavior {
  private static final ShapeTemplate[] shapeTemplates = new ShapeTemplate[1];

  static {
    MachineCasing steelMachineCasing = MachineCasings.get(MI.id("steel"));
    HatchFlags hatches =
        new HatchFlags.Builder()
            .with(HatchTypes.ENERGY_INPUT, HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT)
            .build();

    String[][] layers = {
      {"M M", "HHH", " # "},
      {" C ", "HCH", "HPH"},
      {"M M", "HHH", " H "}
    };

    shapeTemplates[0] =
        new ShapeTemplate.LayeredBuilder(steelMachineCasing, layers)
            .key('M', SimpleMember.forBlockId(MI.id("steel_machine_casing")), HatchFlags.NO_HATCH)
            .key('H', SimpleMember.forBlockId(MI.id("steel_machine_casing")), hatches)
            .key(
                'C',
                SimpleMember.forBlockId(ResourceLocation.parse("minecraft:chain")),
                HatchFlags.NO_HATCH)
            .key(
                'P',
                SimpleMember.forBlockId(MI.id("steel_machine_casing_pipe")),
                HatchFlags.NO_HATCH)
            .build();
    ReiMachineRecipes.registerMultiblockShape("ore_drilling_plant", shapeTemplates[0]);
  }

  protected final ActiveShapeComponent activeShape;
  protected final DrillInventoryComponent inventory;
  protected final DrillPlantComponent drill;
  private final IsActiveComponent isActive;
  private final UpgradeComponent upgrades;
  private final OverdriveComponent overdrive;

  public OreDrillingPlant(BEP bep) {
    super(
        bep,
        new MachineGuiParameters.Builder("ore_drilling_plant", false).backgroundHeight(128).build(),
        new OrientationComponent.Params(false, false, false));
    this.activeShape = new ActiveShapeComponent(shapeTemplates);
    this.inventory = new DrillInventoryComponent();
    this.drill = new DrillPlantComponent(this, inventory, this);
    this.upgrades = new UpgradeComponent();
    this.overdrive = new OverdriveComponent();
    this.isActive = new IsActiveComponent();
    registerGuiComponent(
        new SlotPanel.Server(this).withUpgrades(upgrades).withOverdrive(overdrive));
  }

  public static void registerReiShapes() {
    ReiMachineRecipes.registerMultiblockShape("ore_drilling_plant", shapeTemplates[0], "");
  }

  @Override
  public List<? extends EnergyAccess> getEnergyComponents() {
    return List.of();
  }

  @Override
  public ShapeTemplate getActiveShape() {
    return null;
  }

  /**
   * @return The inventory that will be synced with the client.
   */
  @Override
  public MIInventory getInventory() {
    return null;
  }

  @Override
  protected MachineModelClientData getMachineModelData() {
    return null;
  }

  @Override
  public InventoryAccess getMultiblockInventoryComponent() {
    return null;
  }

  @Override
  public void tick() {}

  @Override
  public long consumeEu(long max, Simulation simulation) {
    return 0;
  }

  @Override
  public long getBaseRecipeEu() {
    return 0;
  }

  @Override
  public long getMaxRecipeEu() {
    return 0;
  }

  @Override
  public ServerLevel getCrafterWorld() {
    return null;
  }

  @Override
  public @Nullable UUID getOwnerUuid() {
    return null;
  }
}
