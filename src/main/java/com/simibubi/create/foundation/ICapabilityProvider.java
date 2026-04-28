package com.simibubi.create.foundation;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface ICapabilityProvider<T> {
	@Nullable
	T getCapability();

	static <T> ICapabilityProvider<T> of(Supplier<T> supplier) {
		return new SupplierProvider<>(supplier);
	}

	static <T> ICapabilityProvider<T> of(T cap) {
		return new SimpleProvider<>(cap);
	}

	class SupplierProvider<T> implements ICapabilityProvider<T> {
		private final Supplier<T> inner;

		private SupplierProvider(Supplier<T> inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner == null ? null : inner.get();
		}
	}

	@ApiStatus.Internal
	class SimpleProvider<T> implements ICapabilityProvider<T> {
		private final T inner;

		private SimpleProvider(T inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner;
		}
	}
}
