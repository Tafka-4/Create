package com.simibubi.create.compat.curios;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CuriosDataGenerator implements DataProvider {
	public CuriosDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Object fileHelper) {
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return "Create Curios compatibility data (disabled on Fabric)";
	}
}
