package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.world.level.Level;

public class LevelSnapshots {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void updateSnapshots(Level level, TransactionContext ctx) {
		if (level instanceof io.github.fabricators_of_create.porting_lib.extensions.common.LevelExtensions extensions) {
			extensions.snapshotParticipant()
				.updateSnapshots(ctx);
			return;
		}
		if (level instanceof io.github.fabricators_of_create.porting_lib.extensions.extensions.LevelExtensions extensions) {
			extensions.snapshotParticipant()
				.updateSnapshots(ctx);
			return;
		}
		throw new UnsupportedOperationException("Level does not expose a Porting Lib snapshot participant");
	}

}
