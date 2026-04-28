package com.simibubi.create.content.logistics.packager.fabric;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.packager.fabric.InventoryIdentifier.MultiBlock;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;

public class AllInventoryIdentifiers {
	public static void registerDefaults() {
		// this is fine since providers added later take priority
		InventoryIdentifier.BY_BLOCK_ENTITY.registerProvider(type -> AllInventoryIdentifiers::worldlyContainerBe);

		InventoryIdentifier.BY_BLOCK_ENTITY.register(
			AllBlockEntityTypes.ITEM_VAULT.get(),
			(level, pos, state, side, be) -> face -> ((ItemVaultBlockEntity) be).getInvId().contains(face)
		);

		InventoryIdentifier.BY_BLOCK.registerProvider(block -> {
			Collection<Property<?>> properties = block.getStateDefinition().getProperties();
			if (properties.contains(ChestBlock.TYPE) && properties.contains(ChestBlock.FACING)) {
				return AllInventoryIdentifiers::chest;
			}
			return null;
		});

		InventoryIdentifier.BY_BLOCK.registerProvider(block -> {
			if (block instanceof WorldlyContainerHolder) {
				return AllInventoryIdentifiers::worldlyContainerBlock;
			}
			return null;
		});
	}

	public static InventoryIdentifier chest(Level level, BlockPos pos, BlockState state, Direction side) {
		ChestType type = state.getValue(ChestBlock.TYPE);

		if (type != ChestType.SINGLE) {
			Direction toOther = ChestBlock.getConnectedDirection(state);
			BlockPos otherPos = pos.relative(toOther);
			BlockState otherState = level.getBlockState(otherPos);
			if (otherState.is(state.getBlock()) && ChestBlock.getConnectedDirection(otherState) == toOther.getOpposite()) {
				return new MultiBlock(Set.of(pos, otherPos));
			}
		}

		// null falls back to single block
		return null;
	}

	private static InventoryIdentifier worldlyContainerBlock(Level level, BlockPos pos, BlockState state, Direction side) {
		WorldlyContainerHolder holder = (WorldlyContainerHolder) state.getBlock();
		WorldlyContainer container = holder.getContainer(state, level, pos);
		return ofWorldlyContainer(container, pos, side);
	}

	private static InventoryIdentifier worldlyContainerBe(Level level, BlockPos pos, BlockState state, Direction side, BlockEntity be) {
		return be instanceof WorldlyContainer container ? ofWorldlyContainer(container, pos, side) : null;
	}

	private static InventoryIdentifier ofWorldlyContainer(WorldlyContainer container, BlockPos pos, Direction side) {
		int[] slots = container.getSlotsForFace(side);
		// get all faces that have the same slots as the given one
		Set<Direction> directions = EnumSet.of(side);
		for (Direction direction : Iterate.directions) {
			if (direction != side) {
				int[] faceSlots = container.getSlotsForFace(direction);
				if (Arrays.equals(slots, faceSlots)) {
					directions.add(direction);
				}
			}
		}
		return (face) -> face.getPos().equals(pos) && directions.contains(face.getFace());
	}
}
