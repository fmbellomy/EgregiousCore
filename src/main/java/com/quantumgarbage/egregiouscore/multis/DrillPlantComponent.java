package com.quantumgarbage.egregiouscore.multis;

import static aztech.modern_industrialization.util.Simulation.ACT;
import static aztech.modern_industrialization.util.Simulation.SIMULATE;

import aztech.modern_industrialization.api.machine.component.CrafterAccess;
import aztech.modern_industrialization.api.machine.component.InventoryAccess;
import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.stats.PlayerStatistics;
import aztech.modern_industrialization.stats.PlayerStatisticsData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import com.quantumgarbage.gtmogs.api.worldgen.ores.GeneratedVeinMetadata;
import com.quantumgarbage.gtmogs.integration.map.cache.server.ServerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

public class DrillPlantComponent<R> implements IComponent.ServerOnly, CrafterAccess {
  private static final byte POWER = 5;
  private static final byte TICK_TOLERANCE = 20;
  private static final short MAX_SPEED = Short.MAX_VALUE;
  private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);
  private final int MAX_RADIUS = 48;
  private final MachineProcessCondition.Context conditionContext;
  private final DrillPlantComponent.Inventory inventory;
  private final DrillPlantComponent.Behavior behavior;
  private final ArrayList<BlockPos> blocksToMine = new ArrayList<>();
  int pipeY = Integer.MAX_VALUE;
  int x = Integer.MAX_VALUE;
  int y = Integer.MAX_VALUE;
  int z = Integer.MAX_VALUE;
  int startX = Integer.MAX_VALUE;
  int startY = Integer.MAX_VALUE;
  int startZ = Integer.MAX_VALUE;
  int mineX = Integer.MAX_VALUE;
  int mineY = Integer.MAX_VALUE;
  int mineZ = Integer.MAX_VALUE;
  int pipeLength = 0;
  private int minBuildHeight = Integer.MAX_VALUE;
  private int maxBuildHeight = Integer.MAX_VALUE;
  private int currentRadius;
  private Direction dir = Direction.DOWN;
  private boolean isDone;
  private long usedEnergy;
  private long recipeEnergy;
  private long recipeMaxEu;
  private int efficiencyTicks;
  private int maxEfficiencyTicks;
  private long previousBaseEu = -1;
  private long previousMaxEu = -1;
  @Nullable private DrillRecipe activeRecipe = null;
  private int lastInvHash = 0;
  private int lastForcedTick = 0;

  public DrillPlantComponent(
      MachineBlockEntity blockEntity,
      DrillPlantComponent.Inventory inventory,
      DrillPlantComponent.Behavior behavior) {
    this.inventory = inventory;
    this.behavior = behavior;
    this.conditionContext = () -> blockEntity;
    this.isDone = false;
  }

  private static void lockAll(List<? extends AbstractConfigurableStack<?, ?>> stacks) {
    for (var stack : stacks) {
      if (stack.isEmpty() && stack.getLockedInstance() == null) {
        stack.togglePlayerLock();
      }
    }
  }

  /**
   * @param world the {@link Level} to get the average tick time of
   * @return the mean tick time
   */
  private static double getMeanTickTime(Level world) {
    return mean(Objects.requireNonNull(world.getServer()).getTickTimesNanos()) * 1.0E-6D;
  }

  /**
   * gets the quotient for determining the amount of blocks to mine
   *
   * @param base is a value used for calculation, intended to be the mean tick time of the world the
   *     miner is in
   * @return the quotient
   */
  private static double getQuotient(double base) {
    return DIVIDEND / Math.pow(base, POWER);
  }

  /**
   * @param values to find the mean of
   * @return the mean value
   */
  private static long mean(long[] values) {
    if (values.length == 0L) return 0L;

    long sum = 0L;
    for (long v : values) sum += v;
    return sum / values.length;
  }

  private static BlockState findMiningReplacementBlock(Level level) {
    return Blocks.COBBLESTONE.defaultBlockState();
  }

  /**
   * This method designates the starting position for mining blocks
   *
   * @param pos the {@link BlockPos} of the miner itself
   * @param currentRadius the currently set mining radius
   */
  public void initPos(BlockPos pos, int currentRadius) {
    x = pos.getX() - currentRadius;
    z = pos.getZ() - currentRadius;
    if (dir == Direction.UP) {
      y = pos.getY() + 1;
    } else {
      y = pos.getY() - 1;
    }
    startX = pos.getX() - currentRadius;
    startZ = pos.getZ() - currentRadius;
    startY = pos.getY();
    if (dir == Direction.UP) {
      pipeY = pos.getY() + 1;
    } else {
      pipeY = pos.getY() - 1;
    }
    mineX = pos.getX() - currentRadius;
    mineZ = pos.getZ() - currentRadius;
    if (dir == Direction.UP) {
      mineY = pos.getY() + 1;
    } else {
      mineY = pos.getY() - 1;
    }
    onRemove();
  }

  public void onRemove() {
    pipeLength = 0;
  }

  /**
   * called in order to insert the mined items into the inventory and actually remove the block in
   * world marks the inventory as full if the items cannot fit, and not full if it previously was
   * full and items could fit
   *
   * @param blockDrops the List of items to insert
   * @param world the {@link ServerLevel} the miner is in
   */
  private void mineAndInsertItems(NonNullList<ItemStack> blockDrops, ServerLevel world) {
    // If the block's drops can fit in the inventory, move the previously mined position to the
    // block
    // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
    // remove the ore block's position from the mining queue
    for (var blockDrop : blockDrops) {
      if (putItemOutputs(blockDrop, true)) {
        putItemOutputs(blockDrop, false);
        world.setBlock(blocksToMine.getFirst(), findMiningReplacementBlock(world), 3);
        mineX = blocksToMine.getFirst().getX();
        mineZ = blocksToMine.getFirst().getZ();
        mineY = blocksToMine.getFirst().getY();
        blocksToMine.removeFirst();
        onMineOperation();
      }
    }
  }

  /** Called after each block is mined, used to perform additional actions afterwards */
  protected void onMineOperation() {}

  /**
   * @return true if the miner is able to mine, else false
   */
  protected boolean checkCanMine() {
    // if the miner is finished, the target coordinates are invalid, or it cannot drain storages,
    // stop
    // if the miner is not finished and has invalid coordinates, get new and valid starting
    // coordinates
    if (!isDone && checkCoordinatesInvalid()) {
      initPos(getMiningPos(), currentRadius);
    }
    return !isDone;
  }

  /**
   * called to handle mining regular ores and blocks
   *
   * @param blockDrops the List of items to fill after the operation
   * @param blockState the {@link BlockState} of the block being mined
   */
  protected void getRegularBlockDrops(
      NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder) {
    blockDrops.addAll(blockState.getDrops(builder));
  }

  /**
   * @return the position to start mining from
   */
  public BlockPos getMiningPos() {
    return this.conditionContext.getBlockEntity().getBlockPos();
  }

  /**
   * Increments the pipe rendering length by one, signaling that the miner's y level has moved down
   * by one
   */
  private void incrementPipeLength() {
    this.pipeLength++;
  }

  @Override
  public float getProgress() {
    return (float) usedEnergy / recipeEnergy;
  }

  @Override
  public int getEfficiencyTicks() {
    return efficiencyTicks;
  }

  @Override
  public int getMaxEfficiencyTicks() {
    return maxEfficiencyTicks;
  }

  @Override
  public boolean hasActiveRecipe() {
    return activeRecipe != null;
  }

  public DrillPlantComponent.Inventory getInventory() {
    return inventory;
  }

  public DrillPlantComponent.Behavior getBehavior() {
    return behavior;
  }

  public void decreaseEfficiencyTicks() {
    efficiencyTicks = Math.max(efficiencyTicks - 1, 0);
    clearActiveRecipeIfPossible();
  }

  public void increaseEfficiencyTicks(int increment) {
    efficiencyTicks = Math.min(efficiencyTicks + increment, maxEfficiencyTicks);
  }

  @Override
  public long getCurrentRecipeEu() {
    Preconditions.checkArgument(hasActiveRecipe());
    return recipeMaxEu;
  }

  @Override
  public long getBaseRecipeEu() {
    Preconditions.checkArgument(hasActiveRecipe());
    return activeRecipe.eu;
  }

  /** Perform a crafter tick, and return whether the crafter is active after the tick. */
  public boolean tickRecipe() {
    if (isDone) {
      return false;
    }
    if (behavior.getCrafterWorld().isClientSide()) {
      throw new IllegalStateException("May not call client side.");
    }
    boolean isActive;
    boolean isEnabled = behavior.isEnabled();

    // START RECIPE IF NECESSARY
    // usedEnergy == 0 means that no recipe is currently started
    boolean recipeStarted = false;
    if (usedEnergy == 0 && isEnabled) {
      if (behavior.consumeEu(1, SIMULATE) == 1) {
        recipeStarted = updateActiveRecipe();
      }
    }

    if (activeRecipe != null) {
      lastForcedTick = 0;
    }

    // PROCESS RECIPE TICK
    long eu = 0;
    boolean finishedRecipe = false; // whether the recipe finished this tick
    if (activeRecipe != null && isEnabled) {
      if (usedEnergy > 0 || recipeStarted) {
        recipeMaxEu = getRecipeMaxEu(activeRecipe.eu, recipeEnergy, efficiencyTicks);
        eu = behavior.consumeEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), ACT);

        isActive = eu > 0;
        usedEnergy += eu;
        if (usedEnergy == recipeEnergy) {
          clearLocks();
          usedEnergy = 0;
          finishedRecipe = true;
          behavior.onCraft();
        }
      } else if (behavior.isOverdriving()) {
        eu = behavior.consumeEu(recipeMaxEu, ACT);
        isActive = eu > 0;
      } else {
        isActive = false;
      }
    } else {
      isActive = false;
    }

    if (activeRecipe != null) {
      if (previousBaseEu != behavior.getBaseRecipeEu()
          || previousMaxEu != behavior.getMaxRecipeEu()) {
        previousBaseEu = behavior.getBaseRecipeEu();
        previousMaxEu = behavior.getMaxRecipeEu();
        maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(activeRecipe);
        efficiencyTicks = Math.min(efficiencyTicks, maxEfficiencyTicks);
      }
    }

    // ADD OR REMOVE EFFICIENCY TICKS
    // If we finished a recipe, we can add an efficiency tick
    if (finishedRecipe) {
      BlockEntity controller = conditionContext.getBlockEntity();
      Level level = controller.getLevel();
      // i have no idea if this could ever even be true but i'm paranoid at this point
      ChunkAccess chunk = level.getChunk(controller.getBlockPos());
      List<GeneratedVeinMetadata> nearbyVeins =
          ServerCache.instance.getNearbyVeins(level.dimension(), controller.getBlockPos(), 48);

      if ((dir == Direction.DOWN && mineY < pipeY) || (dir == Direction.UP && mineY > pipeY)) {
        var miningPos = getMiningPos();
        var pipePos = new BlockPos(miningPos.getX(), pipeY, miningPos.getZ());
        // we have hit bedrock.
        if (level.getBlockState(pipePos).getDestroySpeed(level, pipePos) < 0) {
          isDone = true;
          return true;
        }
        if (dir == Direction.UP) {
          ++pipeY;
        } else {
          --pipeY;
        }
        incrementPipeLength();
      }

      // check if the miner needs new blocks to mine
      checkBlocksToMine();
      if (!blocksToMine.isEmpty()) {
        NonNullList<ItemStack> blockDrops = NonNullList.create();
        BlockState bs = level.getBlockState(blocksToMine.getFirst());
        while (!bs.is(Tags.Blocks.ORES)) {
          blocksToMine.removeFirst();
          if (blocksToMine.isEmpty()) break;
          bs = level.getBlockState(blocksToMine.getFirst());
        }

        if (!blocksToMine.isEmpty() & bs.is(Tags.Blocks.ORES)) {
          LootParams.Builder builder =
              new LootParams.Builder((ServerLevel) level)
                  .withParameter(LootContextParams.BLOCK_STATE, bs)
                  .withParameter(
                      LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blocksToMine.getFirst()))
                  .withParameter(
                      LootContextParams.TOOL,
                      BuiltInRegistries.ITEM
                          .get(ResourceLocation.parse("minecraft:netherite_pickaxe"))
                          .getDefaultInstance());
          getRegularBlockDrops(blockDrops, bs, builder);
          mineAndInsertItems(blockDrops, (ServerLevel) level);
        }
      }

      if (efficiencyTicks < maxEfficiencyTicks) ++efficiencyTicks;
    } else if (eu
        < recipeMaxEu) { // If we didn't use the max energy this tick and the recipe is still
      // ongoing,
      // remove one efficiency tick
      if (efficiencyTicks > 0) {
        efficiencyTicks--;
      }
    }

    // If the recipe is done, allow starting another one when the efficiency reaches zero
    clearActiveRecipeIfPossible();

    return isActive;
  }

  private boolean checkCoordinatesInvalid() {
    return x == Integer.MAX_VALUE && y == Integer.MAX_VALUE && z == Integer.MAX_VALUE;
  }

  /** Checks whether there are any more blocks to mine, if there are currently none queued */
  public void checkBlocksToMine() {
    if (blocksToMine.isEmpty()) blocksToMine.addAll(getBlocksToMine());
  }

  /** Recalculates the mining area, refills the block list and restarts the miner, if it was done */
  public void resetArea(boolean checkToMine) {
    initPos(getMiningPos(), currentRadius);
    this.isDone = false;
    if (checkToMine) {
      blocksToMine.clear();
      checkBlocksToMine();
    }
  }

  /**
   * Gets the blocks to mine
   *
   * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
   */
  private LinkedList<BlockPos> getBlocksToMine() {
    LinkedList<BlockPos> blocks = new LinkedList<>();

    // determine how many blocks to retrieve this time
    var level = conditionContext.getBlockEntity().getLevel();
    assert level != null;
    double quotient = getQuotient(getMeanTickTime(level));
    int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
    int calculated = 0;

    if (this.minBuildHeight == Integer.MAX_VALUE) this.minBuildHeight = level.getMinBuildHeight();

    if (this.maxBuildHeight == Integer.MAX_VALUE) this.maxBuildHeight = level.getMaxBuildHeight();

    // keep getting blocks until the target amount is reached
    while (calculated < calcAmount) {
      // moving down the y-axis
      if (y > minBuildHeight && y < maxBuildHeight) {
        // moving across the z-axis
        if (z <= startZ + currentRadius * 2) {
          // check every block along the x-axis
          if (x <= startX + currentRadius * 2) {
            BlockPos blockPos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(blockPos);
            if (state.getDestroySpeed(level, blockPos) >= 0
                && level.getBlockEntity(blockPos) == null
                && state.is(Tags.Blocks.ORES)) {
              blocks.addLast(blockPos);
            }
            // move to the next x position
            ++x;
          } else {
            // reset x and move to the next z layer
            x = startX;
            ++z;
          }
        } else {
          // reset z and move to the next y layer
          z = startZ;
          if (dir == Direction.UP) {
            ++y;
          } else {
            --y;
          }
        }
      } else return blocks;

      // only count iterations where blocks were found
      if (!blocks.isEmpty()) calculated++;
    }
    return blocks;
  }

  private void clearActiveRecipeIfPossible() {
    if (efficiencyTicks == 0 && usedEnergy == 0) {
      activeRecipe = null;
    }
  }

  private boolean updateActiveRecipe() {
    // Only then can we run the iteration over the recipes
    for (DrillRecipe recipe : getRecipes()) {
      if (behavior.banRecipe(recipe)) continue;
      if (tryStartRecipe(recipe)) {
        // Make sure we recalculate the max efficiency ticks if the recipe changes or if
        // the efficiency has reached 0 (the latter is to recalculate the efficiency for
        // 0.3.6 worlds without having to break and replace the machines)
        if (activeRecipe != recipe || efficiencyTicks == 0) {
          maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(recipe);
        }
        activeRecipe = recipe;
        usedEnergy = 0;
        recipeEnergy = recipe.getTotalEu();
        recipeMaxEu = getRecipeMaxEu(recipe.eu, recipeEnergy, efficiencyTicks);
        return true;
      }
    }
    return false;
  }

  private Iterable<DrillRecipe> getRecipes() {
    if (efficiencyTicks > 0) {
      return Collections.singletonList(activeRecipe);
    } else {
      int currentHash = inventory.hash();
      if (currentHash == lastInvHash) {
        if (lastForcedTick == 0) {
          lastForcedTick = 100;
        } else {
          --lastForcedTick;
          return Collections.emptyList();
        }
      } else {
        lastInvHash = currentHash;
      }

      List<DrillRecipe> recipes = new ArrayList<>();
      for (ConfigurableItemStack stack : inventory.getItemInputs()) {
        if (!stack.isEmpty()) {
          recipes.addAll(DrillRecipe.getMatchingRecipes(stack.getResource().getItem()));
        }
      }
      return recipes;
    }
  }

  /**
   * Try to start a recipe. Return true if success, false otherwise. If false, nothing was changed.
   */
  private boolean tryStartRecipe(DrillRecipe recipe) {
    if (takeItemInputs(recipe, true)) {
      takeItemInputs(recipe, false);
      return true;
    } else {
      return false;
    }
  }

  private long getRecipeMaxEu(long recipeEu, long totalEu, int efficiencyTicks) {
    long baseEu = Math.max(behavior.getBaseRecipeEu(), recipeEu);
    long overclockedEu = baseEu + efficiencyTicks * totalEu / (20 * 30);
    return Math.min(totalEu, Math.min(overclockedEu, behavior.getMaxRecipeEu()));
  }

  private int getRecipeMaxEfficiencyTicks(DrillRecipe recipe) {
    long eu = recipe.eu;
    long totalEu = recipe.getTotalEu();
    for (int ticks = 0; true; ++ticks) {
      if (getRecipeMaxEu(eu, totalEu, ticks) == Math.min(behavior.getMaxRecipeEu(), totalEu))
        return ticks;
    }
  }

  public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
    tag.putLong("usedEnergy", this.usedEnergy);
    tag.putLong("recipeEnergy", this.recipeEnergy);
    tag.putLong("recipeMaxEu", this.recipeMaxEu);
    if (activeRecipe != null) {
      tag.putString("activeRecipe", this.activeRecipe.id());
    }
    tag.putInt("efficiencyTicks", this.efficiencyTicks);
    tag.putInt("maxEfficiencyTicks", this.maxEfficiencyTicks);
  }

  public void readNbt(
      CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
    this.usedEnergy = tag.getInt("usedEnergy");
    this.recipeEnergy = tag.getInt("recipeEnergy");
    this.recipeMaxEu = tag.getInt("recipeMaxEu");
    this.efficiencyTicks = tag.getInt("efficiencyTicks");
    this.maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
  }

  protected boolean takeItemInputs(DrillRecipe recipe, boolean simulate) {
    List<ConfigurableItemStack> baseList = inventory.getItemInputs();
    List<ConfigurableItemStack> stacks =
        simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

    boolean ok = true;
    if (!simulate
        && recipe.breakChance
            < 1) { // if we are not simulating, there is a chance we don't need to take this
      // output
      if (ThreadLocalRandom.current().nextFloat() >= recipe.breakChance) {
        return true;
      }
    }
    for (ConfigurableItemStack stack : stacks) {
      if (stack.getAmount() > 0
          && BuiltInRegistries.ITEM
              .getKey(stack.getResource().getItem())
              .equals(recipe.drillInput)) { // TODO: ItemStack creation slow?
        int taken = 1;
        if (!simulate) {
          behavior.getStatsOrDummy().addUsedItems(stack.getResource().getItem(), taken);
        }
        stack.decrement(taken);
      }
    }
    return ok;
  }

  public boolean putItemOutputs(ItemStack output, boolean simulate) {
    List<ConfigurableItemStack> baseList = inventory.getItemOutputs();
    List<ConfigurableItemStack> stacks =
        simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

    boolean ok = true;
    int remainingAmount = output.getCount();

    // Try to insert in non-empty stacks or locked first, then also allow insertion
    // in empty stacks.

    for (int loopRun = 0; loopRun < 2; loopRun++) {
      int stackId = 0;
      for (ConfigurableItemStack stack : stacks) {
        stackId++;
        Item key = stack.getResource().getItem();
        if (key.equals(output.getItem())) {
          // If simulating or chanced output, respect the adjusted capacity.
          // If putting the output, don't respect the adjusted capacity in case it was
          // reduced during the processing.
          int remainingCapacity =
              simulate
                  ? (int) stack.getRemainingCapacityFor(ItemVariant.of(output))
                  : output.getMaxStackSize() - (int) stack.getAmount();
          int ins = Math.min(remainingAmount, remainingCapacity);
          remainingAmount -= ins;
          // ins changed inside of previous if, need to check again!
          if (ins > 0) {
            if (!simulate) {
              behavior
                  .getStatsOrDummy()
                  .addProducedItems(behavior.getCrafterWorld(), output.getItem(), ins);
            }
          }
          if (remainingAmount == 0) break;
        }
      }
    }
    if (remainingAmount > 0) ok = false;
    return ok;
  }

  protected void clearLocks() {
    for (ConfigurableItemStack stack : inventory.getItemOutputs()) {
      if (stack.isMachineLocked()) stack.disableMachineLock();
    }
    for (ConfigurableFluidStack stack : inventory.getFluidOutputs()) {
      if (stack.isMachineLocked()) stack.disableMachineLock();
    }
  }

  public interface Inventory extends InventoryAccess {
    List<ConfigurableItemStack> getItemInputs();

    List<ConfigurableItemStack> getItemOutputs();

    List<ConfigurableFluidStack> getFluidInputs();

    List<ConfigurableFluidStack> getFluidOutputs();

    int hash();
  }

  public interface Behavior {
    default boolean isEnabled() {
      return true;
    }

    long consumeEu(long max, Simulation simulation);

    default boolean banRecipe(DrillRecipe recipe) {
      return recipe.eu > getMaxRecipeEu();
    }

    long getBaseRecipeEu();

    long getMaxRecipeEu();

    default boolean isOverdriving() {
      return false;
    }

    default void onCraft() {}

    // can't use getWorld() or the remapping will fail
    ServerLevel getCrafterWorld();

    default int getMaxFluidOutputs() {
      return Integer.MAX_VALUE;
    }

    @Nullable
    UUID getOwnerUuid();

    default PlayerStatistics getStatsOrDummy() {
      var uuid = getOwnerUuid();
      if (uuid == null) {
        return PlayerStatistics.DUMMY;
      } else {
        return PlayerStatisticsData.get(getCrafterWorld().getServer()).get(uuid);
      }
    }
  }
}
