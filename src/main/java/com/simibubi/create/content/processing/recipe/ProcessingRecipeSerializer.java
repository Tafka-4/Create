package com.simibubi.create.content.processing.recipe;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

@Deprecated
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProcessingRecipeSerializer<T extends ProcessingRecipe<?, ProcessingRecipeParams>> implements RecipeSerializer<T> {

	private final ProcessingRecipe.Factory<ProcessingRecipeParams, T> factory;
	private final MapCodec<T> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

	public ProcessingRecipeSerializer(ProcessingRecipe.Factory<ProcessingRecipeParams, T> factory) {
		this.factory = factory;
		this.codec = ProcessingRecipe.codec(factory, ProcessingRecipeParams.CODEC);
		this.streamCodec = ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.STREAM_CODEC);
	}

	@Override
	public MapCodec<T> codec() {
		return codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return streamCodec;
	}

	public ProcessingRecipe.Factory<ProcessingRecipeParams, T> getFactory() {
		return factory;
	}
}
