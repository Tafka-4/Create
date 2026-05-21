package com.simibubi.create.content.kinetics.belt.transport;

import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public class BeltCrusherInteractionHandler {

	public static boolean checkForCrushers(BeltInventory beltInventory, TransportedItemStack currentItem,
										   float nextOffset) {
		boolean beltMovementPositive = beltInventory.beltMovementPositive;
		int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
		int step = beltMovementPositive ? 1 : -1;
		firstUpcomingSegment = Mth.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

		for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset
			: segment + 1 >= nextOffset; segment += step) {
			BlockPos crusherPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
				.above();
			Level world = beltInventory.belt.getLevel();
			BlockState crusherState = world.getBlockState(crusherPos);
			if (!(crusherState.getBlock() instanceof CrushingWheelControllerBlock))
				continue;
			Direction crusherFacing = crusherState.getValue(CrushingWheelControllerBlock.FACING);
			Direction movementFacing = beltInventory.belt.getMovementFacing();
			if (crusherFacing != movementFacing)
				continue;

			float crusherEntry = segment + .5f;
			crusherEntry += .399f * (beltMovementPositive ? -1 : 1);
			float postCrusherEntry = crusherEntry + .799f * (!beltMovementPositive ? -1 : 1);

			float extraOffset = BeltHelper.getSegmentBE(world, beltInventory.belt.getBlockPos().relative(movementFacing.getOpposite())) != null ? .275f : 0;
			nextOffset -= extraOffset;

			boolean hasCrossed = nextOffset > crusherEntry && nextOffset < postCrusherEntry && beltMovementPositive
				|| nextOffset < crusherEntry && nextOffset > postCrusherEntry && !beltMovementPositive;
			if (!hasCrossed)
				return false;
			currentItem.beltPosition = crusherEntry;

			BlockEntity be = world.getBlockEntity(crusherPos);
			if (!(be instanceof CrushingWheelControllerBlockEntity crusherBE))
				return true;

            ItemStack toInsert = currentItem.stack.copy();
			try (Transaction t = TransferUtil.openNestedOrOuter()) {
				long inserted = crusherBE.inventory.insert(ItemVariant.of(toInsert), toInsert.getCount(), t);
				t.commit();
				ItemStack remainder = toInsert.copyWithCount(toInsert.getCount() - (int) inserted);
				if (ItemStack.matches(toInsert, remainder))
					return true;

				int notFilled = currentItem.stack.getCount() - toInsert.getCount();
				if (!remainder.isEmpty()) {
					remainder.grow(notFilled);
				} else if (notFilled > 0)
					remainder = currentItem.stack.copyWithCount(notFilled);

				currentItem.stack = remainder;
				beltInventory.belt.sendData();
				return true;
			}
        }

		return false;
	}


}
