package com.simibubi.create.compat.recipeViewerCommon;

import java.util.Arrays;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

import io.github.fabricators_of_create.porting_lib.mixin.accessors.common.accessor.ItemValueAccessor;
import io.github.fabricators_of_create.porting_lib.mixin.accessors.common.accessor.TagValueAccessor;

public final class ToolboxColoringRecipeMaker {

	public static Stream<CraftingRecipe> createRecipes() {
		String group = "create.toolbox.color";
		ItemStack baseToolboxStack = AllBlocks.TOOLBOXES.get(DyeColor.BROWN).asStack();
		Ingredient baseToolboxIngredient = Ingredient.of(baseToolboxStack);

		return Arrays.stream(DyeColor.values())
			.filter(dc -> dc != DyeColor.BROWN)
			.map(color -> {
				DyeItem dye = DyeItem.byColor(color);
				ItemStack dyeStack = new ItemStack(dye);
				TagKey<Item> colorTag = color.getTag();
				Ingredient.Value dyeList = ItemValueAccessor.createItemValue(dyeStack);
				Ingredient.Value colorList = TagValueAccessor.createTagValue(colorTag);
				Ingredient colorIngredient = Ingredient.fromValues(Stream.of(dyeList, colorList));
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseToolboxIngredient, colorIngredient);
				Block coloredToolbox = AllBlocks.TOOLBOXES.get(color).get();
				ItemStack output = new ItemStack(coloredToolbox);
				return new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
			});
	}

	private ToolboxColoringRecipeMaker() {}
}
