package com.simibubi.create.foundation.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.infrastructure.gametest.CreateTestFunction;

import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.gametest.framework.TestFunction;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@Redirect(
			method = "createGameTestInfo(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/gametest/framework/RetryOptions;)Ljava/util/Optional;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/gametest/framework/GameTestRegistry;findTestFunction(Ljava/lang/String;)Ljava/util/Optional;"
			),
			require = 0 // don't crash if this fails. non-critical
	)
	private static Optional<TestFunction> create$getCorrectTestFunction(String testName) {
		CreateTestFunction function = CreateTestFunction.NAMES_TO_FUNCTIONS.get(testName);
		if (function != null)
			return Optional.of(function.testFunction);
		return GameTestRegistry.findTestFunction(testName);
	}
}
