package com.simibubi.create.api.registry;

import com.mojang.serialization.Codec;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class CreateDataMaps {
	/**
	 * Fabric does not expose NeoForge's DataMap registry. This lightweight descriptor
	 * keeps the public constants and codecs available until a Fabric loader is wired.
	 */
	public record DataMapType<K, V>(ResourceLocation id, ResourceKey<? extends Registry<K>> registry, Codec<V> codec) {}

	public static final DataMapType<Item, BlazeBurnerFuel> REGULAR_BLAZE_BURNER_FUELS = new DataMapType<>(
		Create.asResource("regular_blaze_burner_fuels"), Registries.ITEM, BlazeBurnerFuel.CODEC);

	public static final DataMapType<Item, BlazeBurnerFuel> SUPERHEATED_BLAZE_BURNER_FUELS = new DataMapType<>(
		Create.asResource("superheated_blaze_burner_fuels"), Registries.ITEM, BlazeBurnerFuel.CODEC);

	private CreateDataMaps() {
		throw new AssertionError("This class should not be instantiated");
	}
}
