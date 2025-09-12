package com.quantumgarbage.egregiouscore;

import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public class EgregiousRecipeTypes {
  private static final List<MachineRecipeType> recipeTypes = new ArrayList<>();
  public static final MachineRecipeType ORE_DRILLING_PLANT =
      create("ore_drilling_plant").withItemInputs().withItemOutputs();

  public static MachineRecipeType create(String name) {
    return create(name, MachineRecipeType::new);
  }

  private static MachineRecipeType create(
      String name, Function<ResourceLocation, MachineRecipeType> ctor) {
    MachineRecipeType type = ctor.apply(EgregiousCore.id(name));
    MIRegistries.RECIPE_SERIALIZERS.register(name, () -> type);
    MIRegistries.RECIPE_TYPES.register(name, () -> type);
    recipeTypes.add(type);
    return type;
  }
}
