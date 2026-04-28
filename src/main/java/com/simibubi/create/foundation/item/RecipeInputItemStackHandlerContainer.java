package com.simibubi.create.foundation.item;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class RecipeInputItemStackHandlerContainer extends ItemStackHandler implements RecipeInput {
	public RecipeInputItemStackHandlerContainer(int stacks) {
		super(stacks);
	}

	@Override
	public ItemStack getItem(int index) {
		return getStackInSlot(index);
	}

	@Override
	public int size() {
		return getSlotCount();
	}
}
