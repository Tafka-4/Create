package com.simibubi.create.content.contraptions.minecart;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.infrastructure.fabric.ProcessingIterator;

import com.simibubi.create.infrastructure.fabric.transfer.ChangeListeningStorageWrapper;
import com.simibubi.create.infrastructure.fabric.transfer.ChangeListeningViewWrapper;
import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;

import com.simibubi.create.infrastructure.fabric.util.OneTimeRunnable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class TrainCargoManager extends MountedStorageManager {

	int ticksSinceLastExchange;
	AtomicInteger version;

	public TrainCargoManager() {
		version = new AtomicInteger();
		ticksSinceLastExchange = 0;
	}

	@Override
	public void initialize() {
		super.initialize();
		this.items = new CargoInvWrapper(this.items);
		this.allItems = this.items;
		if (this.fuelItems != null) {
			this.fuelItems = new CargoInvWrapper(this.fuelItems);
		}
		this.fluids = new CargoTankWrapper(this.fluids);
	}

	@Override
	public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(nbt, registries, clientPacket);
		nbt.putInt("TicksSinceLastExchange", ticksSinceLastExchange);
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, @Nullable Contraption contraption) {
		super.read(nbt, registries, clientPacket, contraption);
		ticksSinceLastExchange = nbt.getInt("TicksSinceLastExchange");
	}

	public void resetIdleCargoTracker() {
		ticksSinceLastExchange = 0;
	}

	public void tickIdleCargoTracker() {
		ticksSinceLastExchange++;
	}

	public int getTicksSinceLastExchange() {
		return ticksSinceLastExchange;
	}

	public int getVersion() {
		return version.get();
	}

	void changeDetected() {
		version.incrementAndGet();
		resetIdleCargoTracker();
	}

	// fabric: direct access to the storage maps of these wrappers bypasses update checking.
	// this is also the case on forge, and should be fine, since what matters is automation going through Storage

	class CargoInvWrapper extends MountedItemStorageWrapper {
		CargoInvWrapper(MountedItemStorageWrapper wrapped) {
			super(wrapped.storages);
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			TransactionSuccessCallback.register(transaction, TrainCargoManager.this::changeDetected);
			return super.insert(resource, maxAmount, transaction);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			TransactionSuccessCallback.register(transaction, TrainCargoManager.this::changeDetected);
			return super.extract(resource, maxAmount, transaction);
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator() {
			return ChangeListeningStorageWrapper.wrapIterator(super.iterator(), TrainCargoManager.this::changeDetected);
		}

		@Override
		public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
			return ChangeListeningStorageWrapper.wrapIterator(super.nonEmptyIterator(), TrainCargoManager.this::changeDetected);
		}

		@Override
		public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
			return this::nonEmptyIterator;
		}
	}

	class CargoTankWrapper extends MountedFluidStorageWrapper {
		CargoTankWrapper(MountedFluidStorageWrapper wrapped) {
			super(wrapped.storages);
		}

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			TransactionSuccessCallback.register(transaction, TrainCargoManager.this::changeDetected);
			return super.insert(resource, maxAmount, transaction);
		}

		@Override
		public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			TransactionSuccessCallback.register(transaction, TrainCargoManager.this::changeDetected);
			return super.extract(resource, maxAmount, transaction);
		}

		@Override
		public Iterator<StorageView<FluidVariant>> iterator() {
			return ChangeListeningStorageWrapper.wrapIterator(super.iterator(), TrainCargoManager.this::changeDetected);
		}

		@Override
		public Iterator<StorageView<FluidVariant>> nonEmptyIterator() {
			return ChangeListeningStorageWrapper.wrapIterator(super.nonEmptyIterator(), TrainCargoManager.this::changeDetected);
		}

		@Override
		public Iterable<StorageView<FluidVariant>> nonEmptyViews() {
			return this::nonEmptyIterator;
		}
	}
}
