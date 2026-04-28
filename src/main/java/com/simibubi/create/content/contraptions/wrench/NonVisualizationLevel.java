package com.simibubi.create.content.contraptions.wrench;

import dev.engine_room.flywheel.api.visualization.VisualizationLevel;
import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class NonVisualizationLevel extends WrappedLevel implements VisualizationLevel {
	public NonVisualizationLevel(Level level) {
		super(level);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public SnapshotParticipant snapshotParticipant() {
		if (level instanceof io.github.fabricators_of_create.porting_lib.extensions.common.LevelExtensions extensions)
			return extensions.snapshotParticipant();
		if (level instanceof io.github.fabricators_of_create.porting_lib.extensions.extensions.LevelExtensions extensions)
			return extensions.snapshotParticipant();
		throw new UnsupportedOperationException("Wrapped level does not expose a Porting Lib snapshot participant");
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	@Override
	public boolean supportsVisualization() {
		return false;
	}
}
