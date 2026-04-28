package com.simibubi.create;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class AllMapDecorationTypes {
	public static final Holder<MapDecorationType> STATION_MAP_DECORATION = Registry.registerForHolder(
		BuiltInRegistries.MAP_DECORATION_TYPE, Create.asResource("station"),
		new MapDecorationType(Create.asResource("station"), true, -1, false, true));

	@Internal
	public static void register() {
	}
}
