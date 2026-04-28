package com.simibubi.create.content.kinetics.drill;

import java.util.HashMap;

import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickPriority;

public class CobbleGenLevel extends WrappedLevel {

	public HashMap<BlockPos, BlockState> blocksAdded = new HashMap<>();

	public CobbleGenLevel(Level level) {
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

	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		blocksAdded.put(pos.immutable(), newState);
		return true;
	}

	@Override
	public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
		return setBlock(pos, state, 0);
	}

	@Override
	public void scheduleTick(BlockPos pos, Block block, int delay) {}

	@Override
	public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {}

	@Override
	public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {}

	@Override
	public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {}

	@Override
	public void levelEvent(int type, BlockPos pos, int data) {}

	@Override
	public void levelEvent(Player player, int type, BlockPos pos, int data) {}

	@Override
	public void blockEvent(BlockPos pos, Block block, int eventID, int eventParam) {}

}
