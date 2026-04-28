package com.simibubi.create.foundation.mixin.fabric.gametest;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.simibubi.create.foundation.utility.fabric.StructureBlockEntityExtensions;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

@Mixin(value = StructureUtils.class, priority = 900) // apply before FAPI, run earlier
public class StructureUtilsMixin {
	@ModifyReceiver(
		method = "createStructureBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;setIgnoreEntities(Z)V"
		)
	)
	private static StructureBlockEntity markGameTest(StructureBlockEntity instance, boolean ignoreEntities) {
		((StructureBlockEntityExtensions) instance).create$markGameTest();
		return instance;
	}
}
