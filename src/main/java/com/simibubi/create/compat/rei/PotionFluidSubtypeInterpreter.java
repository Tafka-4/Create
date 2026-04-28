package com.simibubi.create.compat.rei;

import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter {

	public String apply(FluidStack ingredient) {
		if (ingredient.getComponentsPatch().isEmpty())
			return "";

		PotionContents contents = ingredient.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		String potionTypeString = ingredient.getDescriptionId();
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
