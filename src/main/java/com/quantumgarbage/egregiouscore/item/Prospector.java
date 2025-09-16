package com.quantumgarbage.egregiouscore.item;

import aztech.modern_industrialization.MIComponents;
import com.quantumgarbage.egregiouscore.Config;
import com.quantumgarbage.gtmogs.integration.map.cache.server.ServerCache;
import dev.technici4n.grandpower.api.ISimpleEnergyItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class Prospector extends Item implements ISimpleEnergyItem {
  private long energyCapacity;

  public Prospector(Properties properties, long energyCapacity) {
    super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).component(MIComponents.ENERGY.get(), 0L));
    this.energyCapacity = energyCapacity;
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
   * @param energyCapacity New energy capacity for the prospector.
   */
  public void setEnergyCapacity(long energyCapacity) {
    this.energyCapacity = energyCapacity;
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
  public int getBarWidth(ItemStack stack) {
    return (int)
        Math.round(this.getStoredEnergy(stack) / (double) this.getEnergyCapacity(stack) * 13);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return 0xFF0000;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    Function<Long, String> formatter = (l) -> NumberFormat.getNumberInstance(Locale.US).format(l);
    String eu = formatter.apply(stack.get(MIComponents.ENERGY.get()));
    String capacity = formatter.apply(this.getEnergyCapacity(stack));
    tooltip.add(
        Component.literal(
            ChatFormatting.AQUA + eu + ChatFormatting.RESET + " / " + capacity + " EU"));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    return super.use(level, player, hand);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (!context.getLevel().isClientSide()
        && this.tryUseEnergy(context.getItemInHand(), Config.PROSPECTOR_ENERGY_COST.get())) {
      ChunkPos center = context.getLevel().getChunk(context.getClickedPos()).getPos();

      int radius = Config.PROSPECTOR_RADIUS.getAsInt();
      for (int i = -1 * radius; i <= radius; i++) {
        for (int j = -1 * radius; j <= radius; j++) {
          ChunkPos prospectedChunk = new ChunkPos(center.x + i, center.z + j);
          ServerCache.instance.prospectAllInChunk(
              context.getLevel().dimension(), prospectedChunk, (ServerPlayer) context.getPlayer());
        }
      }
      return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
    return super.useOn(context);
  }
}
