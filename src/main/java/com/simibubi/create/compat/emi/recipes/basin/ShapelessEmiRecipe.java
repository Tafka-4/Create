package com.simibubi.create.compat.emi.recipes.basin;

import com.simibubi.create.content.processing.basin.BasinRecipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;

public class ShapelessEmiRecipe extends MixingEmiRecipe {

	public ShapelessEmiRecipe(EmiRecipeCategory category, BasinRecipe recipe) {
		super(category, recipe);
		ResourceLocation id = recipe.id;
		if (id != null)
			this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/shapeless/" + id.getNamespace() + "/" + id.getPath());
	}
}
