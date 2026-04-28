package com.simibubi.create.foundation.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.simibubi.create.Create;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;

public class SavedDataUtil {
	public static <T extends SavedData> void saveWithDatOld(T savedData, File file, HolderLookup.Provider registries) {
		if (!savedData.isDirty())
			return;

		CompoundTag compound = new CompoundTag();
		compound.put("data", savedData.save(new CompoundTag(), registries));
		NbtUtils.addCurrentDataVersion(compound);

		String savedDataName = file.getName()
			.split("\\.")[0];

		try {
			Path temp = Files.createTempFile(file.getParentFile()
				.toPath(), savedDataName, ".dat");
			NbtIo.writeCompressed(compound, temp);
			Path oldFile = Paths.get(file.getParent(), savedDataName + ".dat_old");
			Util.safeReplaceFile(file.toPath(), temp, oldFile);
		} catch (IOException e) {
			Create.LOGGER.error("Could not save data {}", savedData, e);
		}

		savedData.setDirty(false);
	}
}
