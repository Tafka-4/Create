package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import io.github.fabricators_of_create.porting_lib.util.StorageProvider;

import net.createmod.catnip.math.BlockFace;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import io.github.fabricators_of_create.porting_lib.util.StorageProvider;

public abstract class CapManipulationBehaviourBase<T, S extends CapManipulationBehaviourBase<?, ?>>
	extends BlockEntityBehaviour {

	// fabric: move to StorageProvider, big changes

	protected InterfaceProvider target;
	protected StorageProvider<T> targetStorageProvider;
	protected Filter<T> filter;
	protected boolean simulateNext;
	protected boolean bypassSided;
	protected Direction side;
	private boolean findNewNextTick;

	public CapManipulationBehaviourBase(SmartBlockEntity be, InterfaceProvider target) {
		super(be);
		setLazyTickRate(5);
		this.target = target;
		targetStorageProvider = null;
		simulateNext = false;
		bypassSided = false;
		filter = (storage, provider) -> true;
	}

	protected abstract StorageProvider<T> getProvider(BlockPos pos, boolean bypassSided);

	@Override
	public void initialize() {
		super.initialize();
		findNewNextTick = true;
	}

	public BlockFace getTarget() {
		return this.target.getTarget(getWorld(), blockEntity.getBlockPos(), blockEntity.getBlockState());
	}

	@Override
	public void onNeighborChanged(BlockPos neighborPos) {
		if (this.getTarget().getConnectedPos().equals(neighborPos))
			onHandlerInvalidated();
	}

	@SuppressWarnings("unchecked")
	public S bypassSidedness() {
		bypassSided = true;
		return (S) this;
	}

	/**
	 * Only simulate the upcoming operation
	 */
	@SuppressWarnings("unchecked")
	public S simulate() {
		simulateNext = true;
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withFilter(Filter<T> filter) {
		this.filter = filter;
		return (S) this;
	}

	public boolean hasInventory() {
		return getInventory() != null;
	}

	@Nullable
	public Storage<T> getInventory() {
		if (targetStorageProvider == null || side == null)
			return null;
		Storage<T> storage = targetStorageProvider.get(side);
		return this.filter.test(storage, this.targetStorageProvider) ? storage : null;
	}

	@Nullable
	public InventoryIdentifier getIdentifier() {
		return !this.hasInventory() ? null : InventoryIdentifier.get(getWorld(), getTarget().getOpposite());
	}

	protected boolean onHandlerInvalidated() {
		if (this.targetStorageProvider == null)
			return false;
		findNewNextTick = true;
		this.setProvider(null, null);
		return true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (targetStorageProvider == null)
			findNewCapability();
	}

	@Override
	public void tick() {
		super.tick();
		if (findNewNextTick || getWorld().getGameTime() % 64 == 0) {
			findNewNextTick = false;
			findNewCapability();
		}
	}

	public int getAmountFromFilter() {
		int amount = -1;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.anyAmount())
			amount = filter.getAmount();
		return amount;
	}

	public ExtractionCountMode getModeFromFilter() {
		ExtractionCountMode mode = ExtractionCountMode.UPTO;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.upTo)
			mode = ExtractionCountMode.EXACTLY;
		return mode;
	}

	public void findNewCapability() {
		Level world = getWorld();
		BlockFace targetBlockFace = this.getTarget().getOpposite();
		BlockPos pos = targetBlockFace.getPos();
		this.setProvider(null, null);
		if (!world.isLoaded(pos))
			return;
		StorageProvider<T> provider = this.getProvider(pos, this.bypassSided);
		Direction side = targetBlockFace.getFace();
		this.setProvider(provider, side);
	}

	private void setProvider(StorageProvider<T> provider, Direction side) {
		this.targetStorageProvider = provider;
		this.side = side;
	}

	@FunctionalInterface
	public interface InterfaceProvider {

		public static InterfaceProvider towardBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING)
					: s.getValue(BlockStateProperties.HORIZONTAL_FACING));
		}

		public static InterfaceProvider oppositeOfBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				(s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING)
					: s.getValue(BlockStateProperties.HORIZONTAL_FACING)).getOpposite());
		}

		public BlockFace getTarget(Level world, BlockPos pos, BlockState blockState);
	}

	@FunctionalInterface
	public interface Filter<T> {
		boolean test(Storage<T> storage, StorageProvider<T> provider);
	}

	public abstract static class UnsidedStorageProvider<T> extends StorageProvider<T> {
		protected UnsidedStorageProvider(BlockApiLookup<Storage<T>, Direction> lookup, Level level, BlockPos pos) {
			super(lookup, level, pos);
		}

		@Override
		@Nullable
		public Storage<T> get(Direction direction) {
			return get();
		}

		@Nullable
		public abstract Storage<T> get();
	}

}
