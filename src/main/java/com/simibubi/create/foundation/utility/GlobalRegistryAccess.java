package com.simibubi.create.foundation.utility;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

public final class GlobalRegistryAccess {
	private static Supplier<@Nullable RegistryAccess> supplier;
	private static MinecraftServer currentServer;

	static {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> supplier = () -> {
			ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
			if (packetListener == null) {
				return null;
			}
			return packetListener.registryAccess();
		});

		if (supplier == null) {
			supplier = () -> {
				MinecraftServer server = currentServer;
				if (server == null) {
					return null;
				}
				return server.registryAccess();
			};
		}
	}

	@Nullable
	public static RegistryAccess get() {
		return supplier.get();
	}

	public static RegistryAccess getOrThrow() {
		RegistryAccess registryAccess = get();
		if (registryAccess == null) {
			throw new IllegalStateException("Could not get RegistryAccess");
		}
		return registryAccess;
	}

	public static void setCurrentServer(@Nullable MinecraftServer server) {
		currentServer = server;
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
