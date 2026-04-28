package com.simibubi.create.foundation.recipe;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllIngredients {

	@Internal
	public static void register() {
		net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer.register(BlockTagIngredient.Serializer.INSTANCE);
	}
}
