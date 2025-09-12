package com.quantumgarbage.egregiouscore.multis;

import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ChangeListener;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class DrillInventoryComponent implements DrillPlantComponent.Inventory {
  private final List<ConfigurableItemStack> itemInputs = new ArrayList();
  private final List<ConfigurableItemStack> itemOutputs = new ArrayList();
  private final List<ConfigurableFluidStack> fluidInputs = new ArrayList();
  private final List<ConfigurableFluidStack> fluidOutputs = new ArrayList();
  private int invHash = 0;
  private final ChangeListener listener =
      new ChangeListener() {
        protected void onChange() {
          ++DrillInventoryComponent.this.invHash;
        }

        protected boolean isValid(Object token) {
          return true;
        }
      };

  public void rebuild(ShapeMatcher shapeMatcher) {
    List<HatchBlockEntity> sortedHatches = new ArrayList(shapeMatcher.getMatchedHatches());
    sortedHatches.sort(Comparator.comparing((h) -> h.getBlockPos().getY()));
    this.rebuildList(sortedHatches, this.itemInputs, HatchBlockEntity::appendItemInputs);
    this.rebuildList(sortedHatches, this.itemOutputs, HatchBlockEntity::appendItemOutputs);
    this.rebuildList(sortedHatches, this.fluidInputs, HatchBlockEntity::appendFluidInputs);
    this.rebuildList(sortedHatches, this.fluidOutputs, HatchBlockEntity::appendFluidOutputs);
    ++this.invHash;
  }

  private <T, Stack extends AbstractConfigurableStack<T, ? extends TransferVariant<T>>>
      void rebuildList(
          List<HatchBlockEntity> sortedHatches,
          List<Stack> stacks,
          BiConsumer<HatchBlockEntity, List<Stack>> appender) {
    for (Stack stack : stacks) {
      stack.removeListener(this.listener);
    }

    stacks.clear();

    for (HatchBlockEntity hatch : sortedHatches) {
      appender.accept(hatch, stacks);
    }

    this.listener.listenAll(stacks, (Object) null);
  }

  public List<ConfigurableItemStack> getItemInputs() {
    return this.itemInputs;
  }

  public List<ConfigurableItemStack> getItemOutputs() {
    return this.itemOutputs;
  }

  public List<ConfigurableFluidStack> getFluidInputs() {
    return this.fluidInputs;
  }

  public List<ConfigurableFluidStack> getFluidOutputs() {
    return this.fluidOutputs;
  }

  public int hash() {
    return this.invHash;
  }
}
