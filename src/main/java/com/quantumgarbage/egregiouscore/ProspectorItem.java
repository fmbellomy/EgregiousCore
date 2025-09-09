package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.MIComponents;
import dev.technici4n.grandpower.api.ISimpleEnergyItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ProspectorItem extends Item implements ISimpleEnergyItem {
  private long energyCapacity;

  public ProspectorItem(Properties properties) {
    super(properties.rarity(Rarity.RARE).stacksTo(1));
  }

  @Override
  public DataComponentType<Long> getEnergyComponent() {
    return MIComponents.ENERGY.get();
  }

  /**
   * @param stack Current stack.
   * @return The max energy that can be stored in this item stack (ignoring current stack size).
   */
  @Override
  public long getEnergyCapacity(ItemStack stack) {
    return energyCapacity;
  }

  /**
   * @param stack Current stack.
   * @return The max amount of energy that can be inserted in this item stack (ignoring current
   *     stack size) in a single operation.
   */
  @Override
  public long getEnergyMaxInput(ItemStack stack) {
    return energyCapacity;
  }

  /**
   * @param stack Current stack.
   * @return The max amount of energy that can be extracted from this item stack (ignoring current
   *     stack size) in a single operation.
   */
  @Override
  public long getEnergyMaxOutput(ItemStack stack) {
    return energyCapacity;
  }

  @Override
  public int getBarWidth(@NotNull ItemStack stack) {
    return (int)
        Math.round(this.getStoredEnergy(stack) / (double) this.getEnergyCapacity(stack) * 13);
  }

  @Override
  public int getBarColor(@NotNull ItemStack stack) {
    return 0xFF0000;
  }

  @Override
  public boolean isBarVisible(@NotNull ItemStack stack) {
    return true;
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
    return super.use(level, player, hand);
  }

  @Override
  public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
    return super.useOn(context);
  }
}
