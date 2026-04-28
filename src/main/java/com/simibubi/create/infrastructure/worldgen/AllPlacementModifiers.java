package com.simibubi.create.infrastructure.worldgen;

import java.util.function.Supplier;

import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllPlacementModifiers {
	private static final ResourceLocation CONFIG_FILTER_ID = Create.asResource("config_filter");
	private static PlacementModifierType<ConfigPlacementFilter> configFilter;

	public static final Supplier<PlacementModifierType<ConfigPlacementFilter>> CONFIG_FILTER = () -> configFilter;

	@Internal
	public static void register() {
		if (configFilter == null)
			configFilter = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, CONFIG_FILTER_ID, () -> ConfigPlacementFilter.CODEC);
	}
}
