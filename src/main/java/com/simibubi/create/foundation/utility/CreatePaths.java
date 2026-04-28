package com.simibubi.create.foundation.utility;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public class CreatePaths {
	// These are all absolute, so anything that is resolved via Path#resolve on these paths will also always be absolute
	public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public static final Path MODS_DIR = GAME_DIR.resolve("mods");

	public static final Path SCHEMATICS_DIR = GAME_DIR.resolve("schematics");
	public static final Path UPLOADED_SCHEMATICS_DIR = SCHEMATICS_DIR.resolve("uploaded");

	private CreatePaths() {
	}
}
