package com.simibubi.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.foundation.item.CombinedSlottedStackStorage;

import net.minecraft.core.BlockPos;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 * 
 * Uses O(1) lookup arrays instead of O(n) linear scan.
 */
public class MountedItemStorageWrapper extends CombinedSlottedStackStorage<MountedItemStorage> {
	public final ImmutableMap<BlockPos, MountedItemStorage> storages;

	public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
		super(storages.values().stream().toList());
		this.storages = storages;
	}
}
