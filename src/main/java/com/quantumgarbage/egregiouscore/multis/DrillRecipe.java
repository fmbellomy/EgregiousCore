package com.quantumgarbage.egregiouscore.multis;

import aztech.modern_industrialization.MI;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class DrillRecipe {
  public static ArrayList<DrillRecipe> RECIPES;

  static {
    RECIPES = new ArrayList<>();
    RECIPES.add(new DrillRecipe(2, MI.id("copper_drill"), 0.1f, 1.0f, "copper_drill"));
    RECIPES.add(new DrillRecipe(8, MI.id("steel_drill"), 0.1f, 2.0f, "steel_drill"));
    RECIPES.add(
        new DrillRecipe(32, MI.id("stainless_steel_drill"), 0.075f, 3.0f, "stainless_steel_drill"));
    RECIPES.add(new DrillRecipe(128, MI.id("titanium_drill"), 0.035f, 4.0f, "titanium_drill"));
    RECIPES.add(new DrillRecipe(256, MI.id("iridium_drill"), 0.001f, 5.0f, "iridium_drill"));
  }

  public int eu;
  public int duration;
  public ResourceLocation drillInput;
  public float multiplier;
  public float breakChance;
  private String id;

  public DrillRecipe(
      int eu, ResourceLocation drillInput, float breakChance, float multiplier, String id) {
    this.eu = eu;
    this.duration = 20;
    this.drillInput = drillInput;
    this.breakChance = breakChance;
    this.multiplier = multiplier;
    this.id = id;
  }

  public static Collection<DrillRecipe> getMatchingRecipes(Item item) {
    return RECIPES.stream()
        .filter(drillRecipe -> drillRecipe.drillInput == BuiltInRegistries.ITEM.getKey(item))
        .toList();
  }

  public String id() {
    return this.id;
  }

  public long getTotalEu() {
    return (long) eu * duration;
  }
}
