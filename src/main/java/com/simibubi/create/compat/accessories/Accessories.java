package com.simibubi.create.compat.accessories;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import io.wispforest.accessories.api.AccessoriesCapability;

public class Accessories {
	public static void init() {
		GogglesItem.addIsWearingPredicate(player -> AccessoriesCapability.getOptionally(player)
			.map(capability -> capability.isEquipped(AllItems.GOGGLES.get()))
			.orElse(false));
	}
}
