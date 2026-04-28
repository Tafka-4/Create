package com.simibubi.create.compat.rei.category;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ProcessingViaFanCategory<T extends Recipe<?>> extends CreateRecipeCategory<T> {

	protected static final int SCALE = 24;

	public ProcessingViaFanCategory(Info<T> info) {
		super(info);
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setHoverName(CreateLang.translateDirect("recipe." + name + ".fan").withStyle(style -> style.withItalic(false)));
	}

	@Override
	public void addWidgets(CreateDisplay<T> display, List<Widget> ingredients, Point origin) {
		ingredients.add(basicSlot(origin.x + 21, origin.y + 48)
				.markInput()
				.entries(display.getInputEntries().get(0)));
		ingredients.add(basicSlot(origin.x + 141, origin.y + 48)
				.markOutput()
				.entries(display.getOutputEntries().get(0)));
	}

	@Override
	public void draw(T recipe, GuiGraphics graphics, double mouseX, double mouseY) {
		renderWidgets(graphics, recipe, mouseX, mouseY);

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		translateFan(matrixStack);
		matrixStack.mulPose(Axis.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));

		AnimatedKinetics.defaultBlockElement(AllPartialModels.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(SCALE)
			.render(graphics);

		AnimatedKinetics.defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(SCALE)
			.render(graphics);

		renderAttachedBlock(graphics);
		matrixStack.popPose();
	}

	protected void renderWidgets(GuiGraphics graphics, T recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 46, 29);
		getBlockShadow().render(graphics, 65, 39);
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 51);
		AllGuiTextures.JEI_SLOT.render(graphics, 20, 47);
		AllGuiTextures.JEI_SLOT.render(graphics, 140, 47);
	}

	protected AllGuiTextures getBlockShadow() {
		return AllGuiTextures.JEI_SHADOW;
	}

	protected void translateFan(PoseStack matrixStack) {
		matrixStack.translate(56, 33, 0);
	}

	protected abstract void renderAttachedBlock(GuiGraphics graphics);

	public static abstract class MultiOutput<T extends ProcessingRecipe<?, ?>> extends ProcessingViaFanCategory<T> {

		public MultiOutput(Info<T> info) {
			super(info);
		}

		@Override
		public void addWidgets(CreateDisplay<T> display, List<Widget> ingredients, Point origin) {
			List<ProcessingOutput> results = display.getRecipe().getRollableResults();
			int xOffsetAmount = 1 - Math.min(3, results.size());

			ingredients.add(basicSlot(origin.x + 5 * xOffsetAmount + 21, origin.y + 48)
					.markInput()
					.entries(display.getInputEntries().get(0)));

			int xOffsetOutput = 9 * xOffsetAmount;
			boolean excessive = results.size() > 9;
			for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
				int xOffset = (outputIndex % 3) * 19 + xOffsetOutput;
				int yOffset = (outputIndex / 3) * -19 + (excessive ? 8 : 0);

				ProcessingOutput output = results.get(outputIndex);
				EntryStack<ItemStack> stack = EntryStack.of(VanillaEntryTypes.ITEM, output.getStack());
				float chance = output.getChance();

				if (chance != 1) {
					Component component = CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
							.withStyle(ChatFormatting.GOLD);
					stack.tooltip(component);
				}

				ingredients.add(basicSlot(origin.x + 141 + xOffset, origin.y + 48 + yOffset)
						.markOutput()
						.entries(List.of(stack)));
			}
		}

		@Override
		protected void renderWidgets(GuiGraphics graphics, T recipe, double mouseX, double mouseY) {
			int size = recipe.getRollableResultsAsItemStacks()
				.size();
			int xOffsetAmount = 1 - Math.min(3, size);

			AllGuiTextures.JEI_SHADOW.render(graphics, 46, 29);
			getBlockShadow().render(graphics, 65, 39);
			AllGuiTextures.JEI_LONG_ARROW.render(graphics, 7 * xOffsetAmount + 54, 51);
			AllGuiTextures.JEI_SLOT.render(graphics, 5 * xOffsetAmount + 20, 47);

			int xOffsetOutput = 9 * xOffsetAmount;
			boolean excessive = size > 9;
			for (int i = 0; i < size; i++) {
				int xOffset = (i % 3) * 19 + xOffsetOutput;
				int yOffset = (i / 3) * -19 + (excessive ? 8 : 0);
				getRenderedSlot(recipe, i).render(graphics, 140 + xOffset, 47 + yOffset);
			}
		}

	}

}
