package com.simibubi.create.content.kinetics.deployer;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.item.RecipeInputItemStackHandlerContainer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DeployerRecipeSearchEvent {
	private boolean canceled = false;
	private final DeployerBlockEntity blockEntity;
	private final RecipeInputItemStackHandlerContainer inventory;
	@Nullable
	RecipeHolder<? extends Recipe<? extends RecipeInput>> recipe = null;
	private int maxPriority = 0;

	public static final Event<DeployerRecipeSearchCallback> EVENT = EventFactory.createArrayBacked(DeployerRecipeSearchCallback.class, callbacks -> (event) -> {
		for (DeployerRecipeSearchCallback callback : callbacks) {
			callback.handle(event);
		}
	});

	@FunctionalInterface
	public interface DeployerRecipeSearchCallback {
		void handle(DeployerRecipeSearchEvent event);
	}

	public DeployerRecipeSearchEvent(DeployerBlockEntity blockEntity, RecipeInputItemStackHandlerContainer inventory) {
		this.blockEntity = blockEntity;
		this.inventory = inventory;
	}

	public DeployerBlockEntity getBlockEntity() {
		return blockEntity;
	}

	public RecipeInputItemStackHandlerContainer getInventory() {
		return inventory;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	// lazyness to not scan for recipes that aren't selected
	public boolean shouldAddRecipeWithPriority(int priority) {
		return !canceled && priority > maxPriority;
	}

	@Nullable
	public RecipeHolder<? extends Recipe<? extends RecipeInput>> getRecipe() {
		if (isCanceled())
			return null;
		return recipe;
	}

	public void addRecipe(Supplier<Optional<? extends RecipeHolder<? extends Recipe<? extends RecipeInput>>>> recipeSupplier, int priority) {
		if (!shouldAddRecipeWithPriority(priority))
			return;
		recipeSupplier.get().ifPresent(newRecipe -> {
			this.recipe = newRecipe;
			maxPriority = priority;
		});
	}
}
