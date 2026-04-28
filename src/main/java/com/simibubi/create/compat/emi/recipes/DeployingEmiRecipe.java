package com.simibubi.create.compat.emi.recipes;

import java.util.List;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.crafting.RecipeHolder;

public class DeployingEmiRecipe extends CreateEmiRecipe<DeployerApplicationRecipe> {

	public DeployingEmiRecipe(DeployerApplicationRecipe recipe) {
		super(CreateEmiPlugin.DEPLOYING, recipe, 134, 80, (r) -> {});
		EmiIngredient held = EmiIngredient.of(recipe.getRequiredHeldItem());
		if (recipe.shouldKeepHeldItem()) {
			for (EmiStack stack : held.getEmiStacks()) {
				stack.setRemainder(stack);
			}
		}
		this.input = List.of(EmiIngredient.of(recipe.getProcessedItem()), held);
		this.output = List.of(getResultEmi(recipe));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 104, 29);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 40, 65);

		addSlot(widgets, input.get(0), 5, 51);
		addSlot(widgets, input.get(1), 29, 5);

		addSlot(widgets, output.get(0), 110, 51).recipeContext(this);

		CreateEmiAnimations.addDeployer(widgets, widgets.getWidth() / 2 - 13, 30);
	}

	public static DeployingEmiRecipe fromSandpaper(RecipeHolder<SandPaperPolishingRecipe> recipe) {
		RecipeHolder<DeployerApplicationRecipe> converted = DeployerApplicationRecipe.convert(recipe);
		DeployingEmiRecipe emiRecipe = new DeployingEmiRecipe(converted.value());
		emiRecipe.setRecipeId(converted.id());
		return emiRecipe;
	}

	public static DeployingEmiRecipe fromItemApplication(RecipeHolder<ManualApplicationRecipe> recipe) {
		RecipeHolder<DeployerApplicationRecipe> converted = ManualApplicationRecipe.asDeploying(recipe);
		DeployingEmiRecipe emiRecipe = new DeployingEmiRecipe(converted.value());
		emiRecipe.setRecipeId(converted.id());
		return emiRecipe;
	}
}
