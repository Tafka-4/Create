package com.simibubi.create.foundation.data;

import com.simibubi.create.api.data.recipe.DatagenMod;
import com.tterrag.registrate.util.DataIngredient;

import net.minecraft.world.item.crafting.Ingredient;

public class SimpleDatagenIngredient {
	private final DatagenMod mod;
	private final String id;

	public SimpleDatagenIngredient(DatagenMod mod, String id) {
		this.mod = mod;
		this.id = id;
	}

	public Ingredient toVanilla() {
		return DataIngredient.ingredient(null, mod.asResource(id)).toVanilla();
	}
}
