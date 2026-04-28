package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<IJeiFluidIngredient> {
	@Override
	public String apply(IJeiFluidIngredient jeiIngredient, UidContext context) {
		FluidStack ingredient = CreateRecipeCategory.fromJei(jeiIngredient);
		if (ingredient.getComponentsPatch().isEmpty())
			return IIngredientSubtypeInterpreter.NONE;

		PotionContents contents = ingredient.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		String potionTypeString = BuiltInRegistries.FLUID.getKey(ingredient.getFluid()).toString();
		String bottleType = ingredient.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR).name();

		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<MobEffectInstance> effects = contents.customEffects();

		stringBuilder.append(";")
				.append(bottleType);
		contents.potion().ifPresent(p -> {
			for (MobEffectInstance effect : p.value().getEffects())
				stringBuilder.append(";")
					.append(effect);
		});
		for (MobEffectInstance effect : effects)
			stringBuilder.append(";")
					.append(effect);
		return stringBuilder.toString();
	}
}
