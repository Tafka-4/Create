package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;

import io.github.fabricators_of_create.porting_lib.resources.conditions.ICondition;
import io.github.fabricators_of_create.porting_lib.resources.conditions.ModLoadedCondition;
import io.github.fabricators_of_create.porting_lib.resources.conditions.NotCondition;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

/**
 * The builder for building Mechanical Crafting recipes.
 *
 * @see MechanicalCraftingRecipeGen
 */
public class MechanicalCraftingRecipeBuilder {

	private final Item result;
	private final int count;
	private final List<String> pattern = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private boolean acceptMirrored;
	private final List<ICondition> recipeConditions;

	public MechanicalCraftingRecipeBuilder(ItemLike result, int resultCount) {
		this.result = result.asItem();
		count = resultCount;
		acceptMirrored = true;
		recipeConditions = new ArrayList<>();
	}

	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike result) {
		return shapedRecipe(result, 1);
	}

	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike result, int resultCount) {
		return new MechanicalCraftingRecipeBuilder(result, resultCount);
	}

	public MechanicalCraftingRecipeBuilder key(Character c, TagKey<Item> tag) {
		return this.key(c, Ingredient.of(tag));
	}

	public MechanicalCraftingRecipeBuilder key(Character c, ItemLike item) {
		return this.key(c, Ingredient.of(item));
	}

	public MechanicalCraftingRecipeBuilder key(Character c, Ingredient ingredient) {
		if (this.key.containsKey(c)) {
			throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
		} else if (c == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(c, ingredient);
			return this;
		}
	}

	public MechanicalCraftingRecipeBuilder patternLine(String line) {
		if (!this.pattern.isEmpty() && line.length() != this.pattern.get(0).length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.pattern.add(line);
			return this;
		}
	}

	public MechanicalCraftingRecipeBuilder disallowMirrored() {
		acceptMirrored = false;
		return this;
	}

	public void build(RecipeOutput output) {
		this.build(output, RegisteredObjectsHelper.getKeyOrThrow(this.result));
	}

	public void build(RecipeOutput output, String id) {
		ResourceLocation resourcelocation = RegisteredObjectsHelper.getKeyOrThrow(this.result);
		ResourceLocation idRs = ResourceLocation.parse(id);
		if (idRs.equals(resourcelocation)) {
			throw new IllegalStateException("Shaped Recipe " + id + " should remove its 'id' argument");
		} else {
			this.build(output, idRs);
		}
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		validate(id);
		MechanicalCraftingRecipe recipe = new MechanicalCraftingRecipe(
			"",
			CraftingBookCategory.MISC,
			ShapedRecipePattern.of(key, pattern),
			new ItemStack(result, count),
			acceptMirrored
		);
		output.accept(id, recipe, null, recipeConditions.toArray(ICondition[]::new));
	}

	private void validate(ResourceLocation recipeId) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + recipeId + "!");
		}

		Set<Character> set = Sets.newHashSet(key.keySet());
		set.remove(' ');

		for (String s : pattern) {
			for (int i = 0; i < s.length(); ++i) {
				char c0 = s.charAt(i);
				if (!key.containsKey(c0) && c0 != ' ')
					throw new IllegalStateException("Pattern in recipe " + recipeId + " uses undefined symbol '" + c0 + "'");
				set.remove(c0);
			}
		}

		if (!set.isEmpty())
			throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + recipeId);
	}

	public MechanicalCraftingRecipeBuilder whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	public MechanicalCraftingRecipeBuilder whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	public MechanicalCraftingRecipeBuilder withCondition(ICondition condition) {
		recipeConditions.add(condition);
		return this;
	}
}
