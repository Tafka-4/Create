package com.simibubi.create.impl.unpacking;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public enum DefaultUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrder order, boolean simulate) {
		BlockEntity targetBE = level.getBlockEntity(pos);

		Storage<ItemVariant> targetInv = ItemStorage.SIDED.find(level, pos, state, targetBE, side);
		if (targetInv == null)
			return false;

		try (Transaction t = TransferUtil.openNestedOrOuter()) {
			for (ItemStack stack : items) {
				long inserted = targetInv.insert(ItemVariant.of(stack), stack.getCount(), t);
				if (inserted != stack.getCount()) {
					return false;
				}
			}

			if (!simulate) {
				t.commit();
			}

		}

		return true;
	}
}
