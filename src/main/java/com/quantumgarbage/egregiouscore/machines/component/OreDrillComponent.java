package com.quantumgarbage.egregiouscore.machines.component;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class OreDrillComponent implements IComponent.ServerOnly {
  protected final MachineProcessCondition.Context conditionContext;

  private final CrafterComponent.Inventory inventory;
  protected long usedEnergy;
  protected long drillEnergyCost;
  protected long drillMaxEu;
  protected int progressCounter;
  protected int lastInvHash = 0;
  protected int lastForcedTick = 0;
  protected ResourceLocation delayedActiveRecipe;
  private DrillingPlantInput activeRecipe;
  private Random drillRNG;

  public OreDrillComponent(MachineBlockEntity blockEntity, CrafterComponent.Inventory inventory) {
    this.conditionContext = () -> blockEntity;
    this.inventory = inventory;
    this.drillRNG = new Random();
  }

  /**
   * Try to consume the drill in the stack.
   *
   * @param stack The drill stack
   * @param simulate If true, the stack will not be modified
   * @return false if the fuel could not be consumed, true otherwise
   */
  public static boolean consumeDrill(ConfigurableItemStack stack, boolean simulate) {
    if (stack.isResourceBlank()) return false;
    var itemStack = stack.toStack();
    if (!simulate) {
      stack.decrement(1);
    }
    return true;
  }

  protected long transformEuCost(long eu) {
    return eu;
  }

  protected void onTick() {}

  protected DrillingPlantInput getDrillingPlantData(ConfigurableItemStack stack) {
    return stack
        .getResource()
        .getItem()
        .getDefaultInstance()
        .getItemHolder()
        .getData(EgregiousDatamaps.DRILLING_PLANT_INPUT);
  }

  protected boolean takeInputs(DrillingPlantInput recipe, boolean simulate) {
    for (ConfigurableItemStack stack : this.inventory.getItemInputs()) {
      if (consumeDrill(stack, true)) {
        return consumeDrill(stack, false);
      }
      return false;
    }
    return false;
  }

  protected boolean putOutputs(boolean simulate, boolean toggleLock, ItemStack output) {
    return this.putItemOutputs(simulate, toggleLock, output);
  }

  protected boolean putItemOutputs(boolean simulate, boolean toggleLock, ItemStack output) {

    List<ConfigurableItemStack> baseList = inventory.getItemOutputs();
    List<ConfigurableItemStack> stacks =
        simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

    List<Integer> locksToToggle = new ArrayList<>();
    List<Item> lockItems = new ArrayList<>();

    boolean ok = true;

    int remainingAmount = output.getCount();
    // Try to insert in non-empty stacks or locked first, then also allow insertion
    // in empty stacks.
    for (int loopRun = 0; loopRun < 2; loopRun++) {
      int stackId = 0;
      for (ConfigurableItemStack stack : stacks) {
        stackId++;
        Item key = stack.getResource().getItem();
        if (key.equals(output.getItem()) || stack.getVariant().isBlank()) {
          // If simulating, respect the adjusted capacity.
          // If putting the output, don't respect the adjusted capacity in case it was
          // reduced during the processing.
          int remainingCapacity =
              simulate
                  ? (int) stack.getRemainingCapacityFor(ItemVariant.of(output))
                  : ItemVariant.of(output).getMaxStackSize() - (int) stack.getAmount();

          int ins = Math.min(remainingAmount, remainingCapacity);
          if (ins > 0) {
            if (ItemVariant.of(key).isBlank()) {
              if ((stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1)
                  && stack.isValid(output)) {
                stack.setAmount(ins);
                stack.setKey(ItemVariant.of(output));
              } else {
                ins = 0;
              }
            } else {
              stack.increment(ins);
            }
          }
          remainingAmount -= ins;
          // ins changed inside of previous if, need to check again!
          if (ins > 0) {
            locksToToggle.add(stackId - 1);
            lockItems.add(output.getItem());
          }
          if (remainingAmount == 0) {
            break;
          }
        }
      }
    }
    if (remainingAmount > 0) {
      ok = false;
    }

    if (toggleLock) {
      for (int i = 0; i < locksToToggle.size(); i++) {
        baseList.get(locksToToggle.get(i)).enableMachineLock(lockItems.get(i));
      }
    }
    return ok;
  }

  public boolean hasActiveRecipe() {
    return activeRecipe != null;
  }

  public DrillingPlantInput getActiveRecipe() {
    return activeRecipe;
  }

  public float getProgress() {
    return (float) usedEnergy / (this.activeRecipe.euCost() * 20);
  }

  public long getCurrentRecipeEu() {
    return drillMaxEu;
  }

  public boolean tickRecipe() {
    if (this.conditionContext.getBlockEntity().getLevel().isClientSide()) {
      throw new IllegalStateException("May not call client side.");
    }

    this.onTick();

    boolean active = false;
    boolean started = false;
    long eu = 0;

    boolean finished = false;
    if (activeRecipe != null) {
      eu = this.activeRecipe.euCost();
      usedEnergy += eu;

      if (usedEnergy == activeRecipe.euCost() * 20) {
        ItemStack output = findAndMineOre();
        this.putOutputs(false, false, output);

        this.clearLocks();
        if (drillRNG.nextFloat(0, 100) > activeRecipe.breakProbability()) {
          for (ConfigurableItemStack stack : inventory.getItemInputs()) {
            if (consumeDrill(stack, true)) {
              consumeDrill(stack, false);
            }
          }
        }
        usedEnergy = 0;
        finished = true;
      }
    }

    if (finished) {
      // If the recipe is done, allow starting another one when the efficiency reaches 0
      this.activeRecipe = null;
    }

    return active;
  }

  public ItemStack findAndMineOre() {
    BlockState ore =
        BuiltInRegistries.BLOCK.get(ResourceLocation.parse("iron_ore")).defaultBlockState();
    LootParams.Builder builder =
        new LootParams.Builder(conditionContext.getLevel())
            .withParameter(LootContextParams.BLOCK_STATE, ore)
            .withParameter(
                LootContextParams.TOOL,
                BuiltInRegistries.ITEM
                    .get(ResourceLocation.parse("minecraft:netherite_pickaxe"))
                    .getDefaultInstance());
    NonNullList<ItemStack> blockDrops = NonNullList.create();

    getRegularBlockDrops(blockDrops, ore, builder);

    ItemStack drop = blockDrops.getFirst();
    float multiplier = activeRecipe.multiplier();
    int dropCount = drop.getCount();
    while (multiplier >= 1) {
      drop.setCount(drop.getCount() + dropCount);
      multiplier -= 1;
    }
    if (drillRNG.nextFloat() < multiplier) {
      multiplier = 0;
      drop.setCount(drop.getCount() + dropCount);
    }
    return drop;
  }

  protected void getRegularBlockDrops(
      NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder) {
    blockDrops.addAll(blockState.getDrops(builder));
  }

  @Override
  public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
    tag.putLong("usedEnergy", usedEnergy);
    tag.putLong("drillEnergyCost", drillEnergyCost);
    tag.putLong("drillMaxEu", drillMaxEu);
  }

  @Override
  public void readNbt(
      CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
    usedEnergy = tag.getInt("usedEnergy");
    drillEnergyCost = tag.getInt("drillEnergyCost");
    drillMaxEu = tag.getInt("drillMaxEu");
  }

  protected void clearLocks() {
    for (ConfigurableItemStack stack : inventory.getItemOutputs()) {
      if (stack.isMachineLocked()) {
        stack.disableMachineLock();
      }
    }
    for (ConfigurableFluidStack stack : inventory.getFluidOutputs()) {
      if (stack.isMachineLocked()) {
        stack.disableMachineLock();
      }
    }
  }
}
