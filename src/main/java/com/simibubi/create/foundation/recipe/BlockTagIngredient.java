package com.simibubi.create.foundation.recipe;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class BlockTagIngredient implements CustomIngredient {
	protected final TagKey<Block> tag;

	protected BlockTagIngredient(TagKey<Block> tag) {
		this.tag = tag;
	}

	public static BlockTagIngredient create(TagKey<Block> tag) {
		return new BlockTagIngredient(tag);
	}

	@Override
	public boolean test(ItemStack stack) {
		return Block.byItem(stack.getItem()).defaultBlockState().is(tag);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		ImmutableList.Builder<ItemStack> stacks = ImmutableList.builder();
		for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
			if (block.value().asItem() != Items.AIR)
				stacks.add(new ItemStack(block.value().asItem()));
		}
		return stacks.build();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements CustomIngredientSerializer<BlockTagIngredient> {
		public static final ResourceLocation ID = Create.asResource("block_tag_ingredient");
		public static final Serializer INSTANCE = new Serializer();

		private static final MapCodec<BlockTagIngredient> CODEC = ResourceLocation.CODEC
			.xmap(id -> new BlockTagIngredient(TagKey.create(Registries.BLOCK, id)), ingredient -> ingredient.tag.location())
			.fieldOf("tag");
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = (StreamCodec) ResourceLocation.STREAM_CODEC
			.map(id -> new BlockTagIngredient(TagKey.create(Registries.BLOCK, id)), ingredient -> ingredient.tag.location());

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<BlockTagIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	}
}
