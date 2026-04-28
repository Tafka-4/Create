package com.simibubi.create.foundation.utility.fabric;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;

public record ListeningStorageView<T>(StorageView<T> wrapped, Runnable onUpdate) implements StorageView<T> {
	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		TransactionCallback.onSuccess(transaction, onUpdate);
		return wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public boolean isResourceBlank() {
		return wrapped.isResourceBlank();
	}

	@Override
	public T getResource() {
		return wrapped.getResource();
	}

	@Override
	public long getAmount() {
		return wrapped.getAmount();
	}

	@Override
	public long getCapacity() {
		return wrapped.getCapacity();
	}

	@Override
	public StorageView<T> getUnderlyingView() {
		return wrapped;
	}
}
