package com.simibubi.create.compat.trainmap;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class XaeroTrainMap {

	public static void tick() {
	}

	@Nullable
	public static ResourceKey<Level> getRenderedDimension() {
		return null;
	}

	public static boolean isMapOpen(Screen screen) {
		return false;
	}

}
