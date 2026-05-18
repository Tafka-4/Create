package com.simibubi.create.infrastructure.fabric.transfer;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferUtil {
	@SuppressWarnings("deprecation")
	public static Transaction openNestedOrOuter() {
		return Transaction.openNested(Transaction.getCurrentUnsafe());
	}

	public static long insert(Storage<FluidVariant> storage, FluidStack stack) {
		try (Transaction t = openNestedOrOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack) {
		try (Transaction t = openNestedOrOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<FluidVariant> storage, FluidStack stack, TransactionContext ctx) {
		return storage.insert(stack.getVariant(), stack.getAmount(), ctx);
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack, TransactionContext ctx) {
		return storage.insert(ItemVariant.of(stack), stack.getCount(), ctx);
	}

	public static long extract(Storage<ItemVariant> storage, ItemVariant variant, long amount) {
		return commit(t -> storage.extract(variant, amount, t));
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, int amount) {
		ResourceAmount<ItemVariant> extracted = extractAny(storage, amount);
		return extracted == null ? ItemStack.EMPTY : extracted.resource().toStack(truncateLong(extracted.amount()));
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, int amount, TransactionContext ctx) {
		ResourceAmount<ItemVariant> extracted = extractAny(storage, amount, ctx);
		return extracted == null ? ItemStack.EMPTY : extracted.resource().toStack(truncateLong(extracted.amount()));
	}

	public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long amount) {
		ResourceAmount<FluidVariant> extracted = extractAny(storage, amount);
		return FluidStack.of(extracted);
	}

	public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long amount, TransactionContext ctx) {
		ResourceAmount<FluidVariant> extracted = extractAny(storage, amount, ctx);
		return FluidStack.of(extracted);
	}

	public static Optional<FluidStack> getFluidContained(ItemStack stack) {
		return getFluidContained(stack, null);
	}

	public static Optional<FluidStack> getFluidContained(ItemStack stack, @Nullable TransactionContext ctx) {
		Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
		if (storage == null)
			return Optional.empty();
		ResourceAmount<FluidVariant> content = simulate(t -> StorageUtil.findExtractableContent(storage, t), ctx);
		if (content == null || content.amount() <= 0 || content.resource().isBlank())
			return Optional.empty();
		return Optional.of(new FluidStack(content));
	}

	public static List<ItemStack> getAllItems(Storage<ItemVariant> storage) {
		List<ItemStack> stacks = new ArrayList<>();
		for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
			ItemVariant resource = view.getResource();
			if (!resource.isBlank() && view.getAmount() > 0)
				stacks.add(resource.toStack(truncateLong(view.getAmount())));
		}
		return stacks;
	}

	public static List<ItemStack> extractAllAsStacks(Storage<ItemVariant> storage) {
		List<ItemStack> stacks = new ArrayList<>();
		try (Transaction t = openNestedOrOuter()) {
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				long extracted = view.extract(resource, view.getAmount(), t);
				if (extracted > 0)
					stacks.add(resource.toStack(truncateLong(extracted)));
			}
			t.commit();
		}
		return stacks;
	}

	public static int truncateLong(long value) {
		if (value > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		if (value < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return (int) value;
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount) {
		return commit(t -> StorageUtil.extractAny(storage, maxAmount, t));
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount, TransactionContext ctx) {
		return StorageUtil.extractAny(storage, maxAmount, ctx);
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractMatching(Storage<T> storage, Predicate<T> predicate, long maxAmount, TransactionContext ctx) {
		T resourceExtracting = null;
		long extracted = 0;

		for (StorageView<T> view : storage.nonEmptyViews()) {
			T resource = view.getResource();

			// see if a resource has already been chosen
			if (resourceExtracting != null && !resourceExtracting.equals(resource))
				continue;

			// if one hasn't, see if this one matches
			if (resourceExtracting == null && predicate.test(resource)) {
				resourceExtracting = resource;
			} else {
				// nope, skip
				continue;
			}

			extracted += view.extract(resource, maxAmount - extracted, ctx);
			if (extracted >= maxAmount) {
				return new ResourceAmount<>(resource, extracted);
			}
		}

		return resourceExtracting != null ? new ResourceAmount<>(resourceExtracting, extracted) : null;
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(BlockEntity be) {
		return ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		return ItemStorage.SIDED.find(level, pos, be == null ? null : be.getBlockState(), be, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos, Direction direction) {
		BlockEntity be = level.getBlockEntity(pos);
		return ItemStorage.SIDED.find(level, pos, be == null ? null : be.getBlockState(), be, direction);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos) {
		return getFluidStorage(level, pos, null);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, Direction direction) {
		BlockEntity be = level.getBlockEntity(pos);
		return getFluidStorage(level, pos, be, direction);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, @Nullable BlockEntity be, Direction direction) {
		return FluidStorage.SIDED.find(level, pos, be == null ? null : be.getBlockState(), be, direction);
	}

	public static OptionalLong firstCapacity(Storage<?> storage) {
		for (StorageView<?> view : storage) {
			return OptionalLong.of(view.getCapacity());
		}
		return OptionalLong.empty();
	}

	public static FluidStack firstOrEmpty(Storage<FluidVariant> storage) {
		for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
			if (!view.getResource().isBlank() && view.getAmount() > 0)
				return new FluidStack(view);
		}
		return FluidStack.EMPTY;
	}

	public static long totalCapacity(Storage<?> storage) {
		long capacity = 0;
		for (StorageView<?> view : storage)
			capacity += view.getCapacity();
		return capacity;
	}

	public static <T> void clear(Storage<T> storage) {
		try (Transaction t = openNestedOrOuter()) {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				view.extract(view.getResource(), view.getAmount(), t);
			}
			t.commit();
		}
	}

	public static <T> T commit(Function<TransactionContext, T> function) {
		try (Transaction t = openNestedOrOuter()) {
			T value = function.apply(t);
			t.commit();
			return value;
		}
	}

	public static <T> T simulate(Function<TransactionContext, T> function) {
		try (Transaction t = openNestedOrOuter()) {
			return function.apply(t);
		}
	}

	public static <T> T simulate(Function<TransactionContext, T> function, @Nullable TransactionContext ctx) {
		if (ctx == null)
			return simulate(function);
		try (Transaction nested = ctx.openNested()) {
			return function.apply(nested);
		}
	}
}
