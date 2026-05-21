package com.simibubi.create.api.contraption.storage.item.simple;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;

import com.simibubi.create.foundation.codec.CreateCodecs;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

/**
 * Widely-applicable mounted storage implementation.
 * Gets an item handler from the mounted block, copies it to an ItemStackHandler,
 * and then copies the inventory back to the target when unmounting.
 * All blocks for which this mounted storage is registered must provide a
 * {@link SlottedStorage} to {@link ItemStorage#SIDED}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllTags.AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 * It is also possible to extend this class to create your own implementation.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final MapCodec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

	public SimpleMountedStorage(MountedItemStorageType<?> type, ItemStackHandler handler) {
		super(type, handler);
	}

	public SimpleMountedStorage(ItemStackHandler handler) {
		this(AllMountedStorageTypes.SIMPLE.get(), handler);
	}

	public SimpleMountedStorage(SlottedStorage<ItemVariant> storage) {
		this(copyToItemStackHandler(storage));
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, be, null);
		if (storage instanceof SlottedStorage<ItemVariant> slotted && this.validate(slotted)) {
			try (Transaction t = TransferUtil.openNestedOrOuter()) {
				for (int i = 0; i < slotted.getSlotCount(); i++) {
					SingleSlotStorage<ItemVariant> slot = slotted.getSlot(i);
					if (!slot.isResourceBlank()) {
						slot.extract(slot.getResource(), slot.getAmount(), t);
					}

					ItemStack stack = this.getStackInSlot(i);
					if (!stack.isEmpty()) {
						slot.insert(ItemVariant.of(stack), stack.getCount(), t);
					}
				}

				t.commit();
			}
		}
	}

	/**
	 * Make sure the targeted handler is valid for copying items back into.
	 * It is highly recommended to call super in overrides.
	 */
	protected boolean validate(SlottedStorage<ItemVariant> storage) {
		return this.getSlotCount() == storage.getSlotCount();
	}

	public static <T extends SimpleMountedStorage> MapCodec<T> codec(Function<ItemStackHandler, T> factory) {
		return CreateCodecs.ITEM_STACK_HANDLER.xmap(factory, storage -> storage.wrapped).fieldOf("value");
	}
}
