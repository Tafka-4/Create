package com.simibubi.create.compat.emi.recipes.fan;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.material.Fluids;

public class FanBlastingEmiRecipe extends FanEmiRecipe<AbstractCookingRecipe> {

	public FanBlastingEmiRecipe(AbstractCookingRecipe recipe) {
		super(CreateEmiPlugin.FAN_BLASTING, recipe);
	}

	public FanBlastingEmiRecipe(AbstractCookingRecipe recipe, ResourceLocation recipeId) {
		this(recipe);
		this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/fan_blasting/" + recipeId.getNamespace() + "/" + recipeId.getPath());
	}

	@Override
	protected void renderAttachedBlock(GuiGraphics graphics) {
		GuiGameElement.of(Fluids.LAVA)
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(CreateEmiAnimations.DEFAULT_LIGHTING)
			.render(graphics);
	}
}
