package com.simibubi.create.compat.tconstruct;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public enum SpoutCasting implements BlockSpoutingBehaviour {
	INSTANCE;

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null)
			return 0;

		Storage<FluidVariant> handler = TransferUtil.getFluidStorage(level, pos, blockEntity, Direction.UP);
		if (handler == null)
			return 0;

		// Do not fill if it would only partially fill the table (unless > 1000mb)
		long amount = availableFluid.getAmount();
		try (Transaction t = TransferUtil.openNestedOrOuter()) {
			long inserted = handler.insert(availableFluid.getVariant(), amount, t);
			if (amount < FluidConstants.BUCKET) {
				try (Transaction nested = t.openNested()) {
					if (handler.insert(availableFluid.getVariant(), 1, nested) == 1)
						return 0;
				}
			}

			if (!simulate) t.commit();
			return inserted;
		}
	}

	private boolean enabled() {
		return AllConfigs.server().recipes.allowCastingBySpout.get();
	}
}
