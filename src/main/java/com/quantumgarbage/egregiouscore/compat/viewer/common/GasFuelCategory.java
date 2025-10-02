package com.quantumgarbage.egregiouscore.compat.viewer.common;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.TextHelper;
import com.quantumgarbage.egregiouscore.EgregiousCore;
import com.quantumgarbage.egregiouscore.EgregiousText;
import com.quantumgarbage.egregiouscore.datamap.GasTurbineFuel;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;

public final class GasFuelCategory extends ViewerCategory<Fluid> {
  public GasFuelCategory() {
    super(
        Fluid.class,
        EgregiousCore.id("gas_turbine_fuels"),
        EgregiousText.GasFuels.text(),
        MIFluids.BENZENE.asFluid().getBucket().getDefaultInstance(),
        150,
        45);
  }

  @Override
  public ResourceLocation getRecipeId(Fluid fluid) {
    ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
    return EgregiousCore.id(
        "/gas_turbine_fuels/%s/%s".formatted(key.getNamespace(), key.getPath()));
  }

  @Override
  public void buildWorkstations(WorkstationConsumer consumer) {
    consumer.accept(
        BuiltInRegistries.ITEM.get(
            ResourceLocation.parse("modern_industrialization:lv_gas_turbine")),
        BuiltInRegistries.ITEM.get(
            ResourceLocation.parse("modern_industrialization:mv_gas_turbine")),
        BuiltInRegistries.ITEM.get(
            ResourceLocation.parse("modern_industrialization:hv_gas_turbine")));
  }

  @Override
  public void buildRecipes(
      RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<Fluid> consumer) {
    for (Fluid fluid : registryAccess.registryOrThrow(Registries.FLUID)) {
      if (GasTurbineFuel.getFor(fluid) != null) {
        consumer.accept(fluid);
      }
    }
  }

  @Override
  public void buildLayout(Fluid recipe, LayoutBuilder builder) {
    builder.inputSlot(15, 15).variant(FluidVariant.of(recipe));
  }

  @Override
  public void buildWidgets(Fluid recipe, WidgetList widgets) {
    GasTurbineFuel gasTurbineFuel = GasTurbineFuel.getFor(recipe);
    TextHelper.Amount amt = TextHelper.getAmount(gasTurbineFuel.eut());
    Component rate = MIText.EuInDieselGenerator.text(amt.digit(), amt.unit());
    widgets.secondaryText(rate, 40, 18);
  }
}
