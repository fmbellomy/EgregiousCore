package com.quantumgarbage.egregiouscore.machines.component;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import com.quantumgarbage.egregiouscore.EgregiousDatamaps;
import com.quantumgarbage.egregiouscore.datamap.DrillingPlantInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

public class OreDrillComponent implements IComponent.ServerOnly {
  protected final MachineProcessCondition.Context conditionContext;

  private final CrafterComponent.Inventory inventory;
  private final int maxRadius = 25;
  private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();
  protected int x = Integer.MAX_VALUE;
  protected int y = Integer.MAX_VALUE;
  protected int z = Integer.MAX_VALUE;
  protected int startX = Integer.MAX_VALUE;
  protected int startY = Integer.MAX_VALUE;
  protected int startZ = Integer.MAX_VALUE;
  protected int drillY = Integer.MAX_VALUE;
  protected int mineX = Integer.MAX_VALUE;
  protected int mineY = Integer.MAX_VALUE;
  protected int mineZ = Integer.MAX_VALUE;
  protected long usedEnergy;
  protected long drillEnergyCost;
  protected long drillMaxEu;
  protected int progressCounter;
  protected int lastInvHash = 0;
  protected int lastForcedTick = 0;
  protected ResourceLocation delayedActiveRecipe;
  protected int drillLength = 0;
  protected boolean isDone;
  protected int minBuildHeight = Integer.MAX_VALUE;
  protected int maxBuildHeight = Integer.MAX_VALUE;
  private DrillingPlantInput activeRecipe;
  private Random drillRNG;
  private Direction dir = Direction.DOWN;

  public OreDrillComponent(MachineBlockEntity blockEntity, CrafterComponent.Inventory inventory) {
    this.conditionContext = () -> blockEntity;
    this.inventory = inventory;
    this.drillRNG = new Random();
    this.isDone = false;
    initPos(conditionContext.getBlockEntity().getBlockPos(), maxRadius);
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

  private static BlockState findMiningReplacementBlock() {
    return Blocks.COBBLESTONE.defaultBlockState();
  }

  public void inValid() {
    this.drillLength = 0;
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

  public boolean trySetActiveRecipe(DrillingPlantInput recipe) {
    if (activeRecipe == null) {
      activeRecipe = recipe;
      return true;
    }
    return false;
  }

  public List<ItemStack> getOutputsFor(Item rawOre, Random rand) {
    ServerLevel level = conditionContext.getLevel();
    List<RecipeHolder<MachineRecipe>> recipes =
        MIMachineRecipeTypes.MACERATOR.getMatchingRecipes(level, rawOre).stream().toList();
    if (!recipes.isEmpty()) {
      List<MachineRecipe.ItemOutput> itemOutputs = recipes.getFirst().value().itemOutputs;
      HashMap<ItemVariant, Integer> outputStacks = new HashMap<>();
      for (MachineRecipe.ItemOutput output : itemOutputs) {
        if (rand.nextFloat(0, 1) < output.probability()) {
          outputStacks.merge(output.variant(), output.amount(), Integer::sum);
        }
      }
      ArrayList<ItemStack> outputs = new ArrayList<>();
      for (var output : outputStacks.entrySet()) {
        outputs.add(new ItemStack(output.getKey().getItem(), output.getValue()));
      }
      return outputs;
    }
    return List.of();
  }

  public boolean tickRecipe() {
    if (this.conditionContext.getBlockEntity().getLevel().isClientSide()) {
      throw new IllegalStateException("May not call client side.");
    }

    boolean active = false;
    long eu = 0;

    boolean finished = false;
    if (activeRecipe != null) {
      for (ConfigurableItemStack stack : inventory.getItemInputs()) {
        if (consumeDrill(stack, true)) {
          eu = this.activeRecipe.euCost();
          usedEnergy += eu;

          if (usedEnergy == activeRecipe.euCost() * 20) {
            List<ItemStack> output = findAndMineOre();
            for (ItemStack out : output) {
              this.putOutputs(false, false, out);
            }
            this.clearLocks();
            if (drillRNG.nextFloat(0, 100) < activeRecipe.breakProbability()) {
              consumeDrill(stack, false);
            }

            usedEnergy = 0;
            finished = true;
          }
          break;
        }
      }
    }

    if (finished) {
      // If the recipe is done, allow starting another one
      this.activeRecipe = null;
    }

    return active;
  }

  /**
   * @return true if the miner is able to mine, else false
   */
  protected boolean checkCanMine() {
    // if the miner is finished, the target coordinates are invalid, or it cannot drain storages,
    // stop
    // if the miner is not finished and has invalid coordinates, get new and valid starting
    // coordinates
    if (!isDone && checkCoordinatesInvalid()) {
      initPos(getMiningPos(), maxRadius);
    }
    return !isDone;
  }

  public List<ItemStack> findAndMineOre() {
    ServerLevel world = conditionContext.getLevel();

    // drill a hole beneath the miner and extend the pipe downwards by one
    if ((dir == Direction.DOWN && mineY < drillY) || (dir == Direction.UP && mineY > drillY)) {
      var miningPos = getMiningPos();
      var pipePos = new BlockPos(miningPos.getX(), drillY, miningPos.getZ());
      if (world.getBlockState(pipePos).getDestroySpeed(world, pipePos) < 0) {
        isDone = true;
      }
      world.destroyBlock(pipePos, false);
      if (dir == Direction.UP) {
        ++drillY;
      } else {
        --drillY;
      }
      incrementPipeLength();
    }
    System.out.println("CHECKING BLOCKS TO MINE AT " + drillY);
    checkBlocksToMine();
    if (blocksToMine.isEmpty()) {
      return List.of();
    }
    mineX = blocksToMine.getFirst().getX();
    mineY = blocksToMine.getFirst().getY();
    mineZ = blocksToMine.getFirst().getZ();

    BlockState ore = world.getBlockState(blocksToMine.getFirst());
    world.setBlock(blocksToMine.getFirst(), findMiningReplacementBlock(), 3);
    blocksToMine.removeFirst();
    System.out.println("MINING " + ore);

    LootParams.Builder builder =
        new LootParams.Builder(conditionContext.getLevel())
            .withParameter(LootContextParams.BLOCK_STATE, ore)
            .withParameter(
                LootContextParams.TOOL,
                BuiltInRegistries.ITEM
                    .get(
                        ResourceLocation.parse(
                            "minecraft:netherite_pickaxe")) // not sure how if this is actually
                    // necessary, but why not?
                    .getDefaultInstance());
    NonNullList<ItemStack> blockDrops = NonNullList.create();

    getRegularBlockDrops(blockDrops, ore, builder);

    return getOutputsFor(blockDrops.getFirst().getItem(), drillRNG).stream()
        .map(
            (drop) -> {
              float multiplier = activeRecipe.multiplier();
              int dropCount = drop.getCount();
              // integer part of multiplier
              drop.setCount(dropCount * (int) multiplier);
              float remainder = drillRNG.nextFloat(0, 1);
              if (remainder < (multiplier % 1)) {
                drop.setCount(drop.getCount() + dropCount);
              }
              return drop;
            })
        .toList();
  }

  protected void getRegularBlockDrops(
      NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder) {
    blockDrops.addAll(
        blockState.getDrops(
            builder.withParameter(
                LootContextParams.ORIGIN,
                Vec3.atCenterOf(conditionContext.getBlockEntity().getBlockPos()))));
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
      drillY = pos.getY() + 1;
    } else {
      drillY = pos.getY() - 1;
    }
    mineX = pos.getX() - currentRadius;
    mineZ = pos.getZ() - currentRadius;
    if (dir == Direction.UP) {
      mineY = pos.getY() + 1;
    } else {
      mineY = pos.getY() - 1;
    }
  }

  /**
   * Increments the pipe rendering length by one, signaling that the miner's y level has moved down
   * by one
   */
  private void incrementPipeLength() {
    this.drillLength++;
  }

  /**
   * Checks if the current coordinates are invalid
   *
   * @return {@code true} if the coordinates are invalid, else false
   */
  private boolean checkCoordinatesInvalid() {
    return x == Integer.MAX_VALUE && y == Integer.MAX_VALUE && z == Integer.MAX_VALUE;
  }

  /** Recalculates the mining area, refills the block list and restarts the miner, if it was done */
  public void resetArea(boolean checkToMine) {
    initPos(getMiningPos(), maxRadius);

    this.isDone = false;
    if (checkToMine) {
      blocksToMine.clear();
      checkBlocksToMine();
    }
  }

  /** Checks whether there are any more blocks to mine, if there are currently none queued */
  public void checkBlocksToMine() {
    if (blocksToMine.isEmpty()) blocksToMine.addAll(getBlocksToMine());
  }

  /**
   * Gets the blocks to mine
   *
   * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
   */
  private LinkedList<BlockPos> getBlocksToMine() {
    x = mineX;
    y = mineY;
    z = mineZ;
    LinkedList<BlockPos> blocks = new LinkedList<>();

    // determine how many blocks to retrieve this time
    var level = conditionContext.getBlockEntity().getLevel();
    assert level != null;

    int calcAmount = 64; // get 64 ore blocks
    int calculated = 0;

    if (this.minBuildHeight == Integer.MAX_VALUE) this.minBuildHeight = level.getMinBuildHeight();

    if (this.maxBuildHeight == Integer.MAX_VALUE) this.maxBuildHeight = level.getMaxBuildHeight();

    // keep getting blocks until the target amount is reached
    while (calculated < calcAmount) {
      // moving down the y-axis
      if (y > minBuildHeight && y < maxBuildHeight) {
        // moving across the z-axis
        if (z <= startZ + maxRadius * 2) {
          // check every block along the x-axis
          if (x <= startX + maxRadius * 2) {
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

  /**
   * @return the position to start mining from
   */
  public BlockPos getMiningPos() {
    return conditionContext.getBlockEntity().getBlockPos();
  }
}
