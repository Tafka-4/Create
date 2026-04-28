package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DatagenMod;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe.Factory;
import com.simibubi.create.foundation.data.SimpleDatagenIngredient;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.tterrag.registrate.util.DataIngredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;

import io.github.fabricators_of_create.porting_lib.resources.conditions.ICondition;
import io.github.fabricators_of_create.porting_lib.resources.conditions.ModLoadedCondition;
import io.github.fabricators_of_create.porting_lib.resources.conditions.NotCondition;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

public class ProcessingRecipeBuilder<P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>, S extends ProcessingRecipeBuilder<P, R, S>> {
	protected ResourceLocation recipeId;
	protected Factory<P, R> factory;
	protected P params;
	protected List<ICondition> recipeConditions;

	public ProcessingRecipeBuilder(Factory<P, R> factory, ResourceLocation recipeId) {
		this.recipeId = recipeId;
		this.factory = factory;
		this.params = createParams();
		this.recipeConditions = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	protected P createParams() {
		return (P) new ProcessingRecipeParams();
	}

	@SuppressWarnings("unchecked")
	public S self() {
		return (S) this;
	}

	public S withItemIngredients(Ingredient... ingredients) {
		return withItemIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
	}

	public S withItemIngredients(NonNullList<Ingredient> ingredients) {
		params.ingredients = ingredients;
		return self();
	}

	public S withSingleItemOutput(ItemStack output) {
		return withItemOutputs(new ProcessingOutput(output, 1));
	}

	public S withItemOutputs(ProcessingOutput... outputs) {
		return withItemOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
	}

	public S withItemOutputs(NonNullList<ProcessingOutput> outputs) {
		params.results = outputs;
		return self();
	}

	public S withFluidIngredients(FluidIngredient... ingredients) {
		return withFluidIngredients(NonNullList.of(FluidIngredient.EMPTY, ingredients));
	}

	public S withFluidIngredients(NonNullList<FluidIngredient> ingredients) {
		params.fluidIngredients = ingredients;
		return self();
	}

	public S withFluidOutputs(FluidStack... outputs) {
		return withFluidOutputs(NonNullList.of(FluidStack.EMPTY, outputs));
	}

	public S withFluidOutputs(NonNullList<FluidStack> outputs) {
		params.fluidResults = outputs;
		return self();
	}

	public S duration(int ticks) {
		params.processingDuration = ticks;
		return self();
	}

	public S averageProcessingDuration() {
		return duration(100);
	}

	public S requiresHeat(HeatCondition condition) {
		params.requiredHeat = condition;
		return self();
	}

	public R build() {
		validateFluidAmounts();
		R recipe = factory.create(params);
		recipe.id = recipeId;
		return recipe;
	}

	public void build(RecipeOutput consumer) {
		R recipe = build();
		IRecipeTypeInfo recipeType = recipe.getTypeInfo();
		ResourceLocation typeId = recipeType.getId();
		ResourceLocation id = recipeId.withPrefix(typeId.getPath() + "/");
		var errors = recipe.validate();
		if (!errors.isEmpty()) {
			errors.add(recipe.getClass().getSimpleName() + " with id " + id + " failed validation:");
			Create.LOGGER.warn(Joiner.on('\n').join(errors));
		}
		consumer.accept(id, recipe, null, recipeConditions.toArray(new ICondition[0]));
	}

	public static final long[] SUS_AMOUNTS = { 10, 250, 500, 1000 };

	private void validateFluidAmounts() {
		for (FluidIngredient ingredient : params.fluidIngredients) {
			for (long amount : SUS_AMOUNTS) {
				if (ingredient.getRequiredAmount() == amount) {
					Create.LOGGER.warn("Suspicious fluid amount in recipe [{}]: {}", recipeId, amount);
				}
			}
		}
	}

	public S require(TagKey<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public S require(ItemLike item) {
		return require(Ingredient.of(item));
	}

	public S require(Ingredient ingredient) {
		params.ingredients.add(ingredient);
		return self();
	}

	public S require(CustomIngredient ingredient) {
		return require(ingredient.toVanilla());
	}

	public S require(DatagenMod mod, String id) {
		params.ingredients.add(new SimpleDatagenIngredient(mod, id).toVanilla());
		return self();
	}

	public S require(com.simibubi.create.foundation.data.recipe.Mods mod, String id) {
		params.ingredients.add(new SimpleDatagenIngredient(mod, id).toVanilla());
		return self();
	}

	public S require(ResourceLocation ingredient) {
		params.ingredients.add(DataIngredient.ingredient(null, ingredient).toVanilla());
		return self();
	}

	public S require(FlowingFluid fluid, int amount) {
		return require(fluid.getSource(), amount);
	}

	public S require(Fluid fluid, long amount) {
		return require(FluidIngredient.fromFluid(fluid, amount));
	}

	public S require(TagKey<Fluid> fluidTag, long amount) {
		return require(FluidIngredient.fromTag(fluidTag, amount));
	}

	public S require(FluidIngredient ingredient) {
		params.fluidIngredients.add(ingredient);
		return self();
	}

	public S output(ItemLike item) {
		return output(item, 1);
	}

	public S output(float chance, ItemLike item) {
		return output(chance, item, 1);
	}

	public S output(ItemLike item, int amount) {
		return output(1, item, amount);
	}

	public S output(float chance, ItemLike item, int amount) {
		return output(chance, new ItemStack(item, amount));
	}

	public S output(ItemStack output) {
		return output(1, output);
	}

	public S output(float chance, ItemStack output) {
		return output(new ProcessingOutput(output, chance));
	}

	public S output(float chance, DatagenMod mod, String id, int amount) {
		return output(new ProcessingOutput(mod.asResource(id), amount, chance));
	}

	public S output(float chance, com.simibubi.create.foundation.data.recipe.Mods mod, String id, int amount) {
		return output(new ProcessingOutput(mod.asResource(id), amount, chance));
	}

	public S output(ResourceLocation id) {
		return output(1, id, 1);
	}

	public S output(DatagenMod mod, String id) {
		return output(1, mod.asResource(id), 1);
	}

	public S output(com.simibubi.create.foundation.data.recipe.Mods mod, String id) {
		return output(1, mod.asResource(id), 1);
	}

	public S output(float chance, ResourceLocation registryName, int amount) {
		return output(new ProcessingOutput(registryName, amount, chance));
	}

	public S output(ProcessingOutput output) {
		params.results.add(output);
		return self();
	}

	public S output(Fluid fluid, long amount) {
		fluid = FluidHelper.convertToStill(fluid);
		return output(new FluidStack(fluid, amount));
	}

	public S output(FluidStack fluidStack) {
		params.fluidResults.add(fluidStack);
		return self();
	}

	public S toolNotConsumed() {
		if (params instanceof ItemApplicationRecipeParams itemApplicationParams)
			itemApplicationParams.setKeepHeldItem(true);
		return self();
	}

	public S whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	public S whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	public S withCondition(ICondition condition) {
		recipeConditions.add(condition);
		return self();
	}
}
