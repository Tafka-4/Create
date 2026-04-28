package com.simibubi.create.compat.sandwichable;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;

import io.github.foundationgames.sandwichable.Sandwichable;
import io.github.foundationgames.sandwichable.items.ItemsRegistry;
import io.github.foundationgames.sandwichable.util.Sandwich;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class SequencedSandwiching {
	/**
	 * Only allow sequenced sandwiching on sandwiches (or bread, becoming sandwiches),
	 * AND if the item-to-add can be put on a sandwich.
	 */
	public static boolean shouldSandwich(ItemStack handling, ItemStack held, Level level) {
		boolean eligible = Sandwich.canAdd(held) && (Sandwichable.isBread(handling) || handling.is(ItemsRegistry.SANDWICH));
		int max = level.getGameRules().getInt(Sandwichable.SANDWICH_SIZE_RULE);
		Sandwich sandwich = sandwichFromStack(handling);
		if (sandwich != null && max > 0) {
			return eligible && sandwich.getFoodList().size() < max;
		}
		return eligible;
	}

	/**
	 * Actually assemble a sandwich.
	 * @param transported the sandwich stack, passing below the deployer
	 * @param handler the deployer's handler
	 * @param deployer the block entity of the deployer
	 */
	public static void activateSandwich(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
										DeployerBlockEntity deployer) {

		TransportedItemStack transportedRemainder = transported.copy();
		DeployerFakePlayer player = deployer.getPlayer();
		transportedRemainder.stack.shrink(1);
		ItemStack heldItem = player.getMainHandItem();

		ItemStack newSandwich = stackOnSandwich(transported.stack, heldItem, deployer);
		if (newSandwich.isEmpty())
			return;

		TransportedItemStack output = transported.copy();
		boolean centered = BeltHelper.isItemUpright(newSandwich);
		output.stack = newSandwich;
		output.angle = centered ? 180 : Create.RANDOM.nextInt(360);

		handler.handleProcessingOnItem(transported, TransportedResult
				.convertToAndLeaveHeld(ImmutableList.of(output), transportedRemainder));

		heldItem.shrink(1);

		BlockPos pos = deployer.getBlockPos();
		Level world = deployer.getLevel();
		if (heldItem.isEmpty())
			world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, .25f, 1);
		world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .25f, .75f);

		deployer.sendData();
	}

	/**
	 * Stacks 'toAdd' on top of the given sandwich.
	 * If the item to add is a spread (and has a remainder) it is added to the deployer's overflow items.
	 * @return the new sandwich, with the item stacked on top, or EMPTY if nothing stacked
	 */
	public static ItemStack stackOnSandwich(ItemStack sandwich, ItemStack toAdd, DeployerBlockEntity deployer) {
		Level level = deployer.getLevel();
		Sandwich s = sandwichFromStack(sandwich);
		if (s != null) {
			// null - not added
			// empty - do not return an item
			// non-empty - return item
			ItemStack result = s.tryAddTopFoodFrom(level, toAdd.copy());
			if (result == null) {
				return sandwich;
			} else if (!result.isEmpty()) {
				deployer.getOverflowItems().add(result.copy());
			}
			CompoundTag newTag = s.writeToNbt(new CompoundTag());
			ItemStack newSandwich = sandwich.copy();
			newSandwich.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(newTag));
			return newSandwich;
		} else if (Sandwichable.isBread(sandwich)) {
			s = new Sandwich();
			s.addTopFoodFrom(sandwich.copy());
			ItemStack result = s.tryAddTopFoodFrom(level, toAdd.copy());
			if (result == null) {
				return sandwich;
			} else if (!result.isEmpty()) {
				deployer.getOverflowItems().add(result.copy());
			}
			CompoundTag newTag = s.writeToNbt(new CompoundTag());
			ItemStack freshSandwich = ItemsRegistry.SANDWICH.getDefaultInstance();
			freshSandwich.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(newTag));
			return freshSandwich;
		}
		return ItemStack.EMPTY;
	}

	@Nullable
	public static Sandwich sandwichFromStack(ItemStack stack) {
		if (stack.is(ItemsRegistry.SANDWICH)) {
			CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
			if (blockEntityData != null) {
				CompoundTag tag = blockEntityData.copyTag();
				Sandwich s = new Sandwich();
				s.addFromNbt(tag);
				return s;
			}
		}
		return null;
	}
}
