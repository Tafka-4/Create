package com.simibubi.create.compat.emi.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;



public class PolishingEmiRecipe extends CreateEmiRecipe<SandPaperPolishingRecipe> {

	public PolishingEmiRecipe(SandPaperPolishingRecipe recipe) {
		super(CreateEmiPlugin.SANDPAPER_POLISHING, recipe, 134, 55);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 39, 25);
		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 30, 37);

		addSlot(widgets, input.get(0), 5, 33);

		addSlot(widgets, output.get(0), 110, 33).recipeContext(this);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!ingredients.isEmpty() && !ingredients.get(0).isEmpty()) {
			ItemStack[] matchingStacks = ingredients.get(0).getItems();
			ItemStack stack = AllItems.SAND_PAPER.asStack();
			stack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(matchingStacks[0].copy()));
			stack.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
			widgets.addDrawable(49, 4, 0, 0, (graphics, mouseX, mouseY, delta) -> {
				PoseStack matrices = graphics.pose();
				matrices.translate(0, 0, 100);
				matrices.scale(2, 2, 1);
				GuiGameElement.of(stack)
						.<GuiGameElement.GuiRenderBuilder>at(0, 0, 1)
						.scale(1)
						.render(graphics);
			});
		}
	}
}
