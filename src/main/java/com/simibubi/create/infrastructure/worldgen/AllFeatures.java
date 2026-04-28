package com.simibubi.create.infrastructure.worldgen;

import java.util.function.Supplier;

import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllFeatures {
	private static final ResourceLocation LAYERED_ORE_ID = Create.asResource("layered_ore");
	private static LayeredOreFeature layeredOre;

	public static final Supplier<LayeredOreFeature> LAYERED_ORE = () -> layeredOre;

	@Internal
	public static void register() {
		if (layeredOre == null)
			layeredOre = Registry.register(BuiltInRegistries.FEATURE, LAYERED_ORE_ID, new LayeredOreFeature());
	}
}
