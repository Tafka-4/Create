package com.simibubi.create.foundation.item;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import com.simibubi.create.infrastructure.fabric.item.ItemUtils;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.function.Consumer;

public class SmartInventory extends ItemStackHandler {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected int stackSize;

	public SmartInventory(int slots, SyncedBlockEntity be) {
		this(slots, be, 64, false);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables) {
		super(slots);
		this.stackNonStackables = stackNonStackables;
		insertionAllowed = true;
		extractionAllowed = true;
		this.stackSize = stackSize;
		this.blockEntity = be;
	}

	public SmartInventory withMaxStackSize(int maxStackSize) {
		stackSize = maxStackSize;
		return this;
	}

	public SmartInventory whenContentsChanged(Consumer<Integer> updateCallback) {
		this.updateCallback = updateCallback;
		return this;
	}

	public SmartInventory allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartInventory allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartInventory forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartInventory forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

	public net.minecraft.world.item.ItemStack getItem(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!insertionAllowed)
			return 0;
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!extractionAllowed)
			return 0;
		if (stackNonStackables) {
			try (Transaction t = transaction.openNested()) {
				long extracted = super.extract(resource, maxAmount, t);
				t.abort();
				int maxStackSize = ItemUtils.getMaxStackSize(resource);
				if (extracted != 0 && maxStackSize < extracted)
					maxAmount = maxStackSize;
			}
		}
		return super.extract(resource, maxAmount, transaction);
	}

	// fabric: merge SyncedStackHandler, it exists only to be wrapped, and removing it allows avoiding extending RecipeWrapper

	private SyncedBlockEntity blockEntity;
	private Consumer<Integer> updateCallback;

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		if (updateCallback != null)
			updateCallback.accept(slot);
		blockEntity.notifyUpdate();
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
	}
}
