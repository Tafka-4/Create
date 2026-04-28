package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;

final class ClientCommandRegistration {

	private ClientCommandRegistration() {}

	static void registerRootCommands(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(FabulousWarningCommand.register())
			.then(OverlayConfigCommand.register());

		if (CatnipServices.PLATFORM.isDevelopmentEnvironment())
			root.then(CreateTestCommand.register());
	}

	static void registerUtilityCommands(LiteralArgumentBuilder<CommandSourceStack> util) {
		util.then(ClearBufferCacheCommand.register())
			.then(CameraDistanceCommand.register());
	}
}
