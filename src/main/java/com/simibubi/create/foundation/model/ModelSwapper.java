package com.simibubi.create.foundation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;

public class ModelSwapper implements AfterBake {

	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();

	private Map<ModelResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> swaps = null;

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	public void registerListeners() {
		ModelLoadingPlugin.register(ctx -> ctx.modifyModelAfterBake().register(this));
	}

	@Override
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (swaps == null)
			collectSwaps();
		ModelResourceLocation id = context.topLevelId();
		if (id == null)
			return model;
		NonNullFunction<BakedModel, ? extends BakedModel> swap = swaps.get(id);
		return swap != null ? swap.apply(model) : model;
	}

	private void collectSwaps() {
		this.swaps = new HashMap<>();

		customBlockModels.forEach((block, swapper) -> getAllBlockStateModelLocations(block).forEach(id -> swaps.put(id, swapper)));
		customItemModels.forEach((item, swapper) -> swaps.put(getItemModelLocation(item), swapper));
		CustomRenderedItems.forEach(item -> swaps.put(getItemModelLocation(item), CustomRenderedItemModel::new));
	}

	public static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		var blockRl = RegisteredObjectsHelper.getKeyOrThrow(block);
		block.getStateDefinition()
			.getPossibleStates()
			.forEach(state -> {
				models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
			});
		return models;
	}

	public static ModelResourceLocation getItemModelLocation(Item item) {
		return new ModelResourceLocation(RegisteredObjectsHelper.getKeyOrThrow(item), "inventory");
	}

}
