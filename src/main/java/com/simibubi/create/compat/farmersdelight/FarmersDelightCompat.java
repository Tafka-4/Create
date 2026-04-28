package com.simibubi.create.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FarmersDelightCompat {
	private static final Block RICH_SOIL = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "rich_soil"));

	public static boolean shouldHarvestMushroom(Level world, BlockPos pos, BlockState state) {
		return !world.getBlockState(pos.below()).is(RICH_SOIL);
	}
}
