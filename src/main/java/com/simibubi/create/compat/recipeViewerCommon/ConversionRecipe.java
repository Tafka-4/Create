package com.simibubi.create.compat.recipeViewerCommon;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
public class ConversionRecipe extends StandardProcessingRecipe<RecipeInput> {

	static int counter = 0;

	public static ConversionRecipe create(ItemStack from, ItemStack to) {
		ResourceLocation recipeId = Create.asResource("conversion_" + counter++);
		return new StandardProcessingRecipe.Builder<>(ConversionRecipe::new, recipeId)
			.withItemIngredients(Ingredient.of(from))
			.withSingleItemOutput(to)
			.build();
	}

	public static RecipeHolder<ConversionRecipe> createHolder(ItemStack from, ItemStack to) {
		ConversionRecipe recipe = create(from, to);
		ResourceLocation recipeId = recipe.id != null ? recipe.id : Create.asResource("conversion_" + counter++);
		return new RecipeHolder<>(recipeId, recipe);
	}

	public ConversionRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CONVERSION, params);
	}

	@Override
	public boolean matches(RecipeInput inv, Level worldIn) {
		return false;
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}
}
