package com.quantumgarbage.egregiouscore;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EgregiousRecipeTypes {
  public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, EgregiousCore.ID);

  public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, EgregiousCore.ID);

  public static void init(IEventBus bus) {
    RECIPE_SERIALIZERS.register(bus);
    RECIPE_TYPES.register(bus);
  }
}
