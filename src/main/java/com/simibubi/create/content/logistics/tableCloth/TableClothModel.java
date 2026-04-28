package com.simibubi.create.content.logistics.tableCloth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class TableClothModel extends ForwardingBakedModel {

	private static final Map<TableClothBlock, List<List<BakedQuad>>> CORNERS = new HashMap<>();

	public TableClothModel(BakedModel originalModel) {
		wrapped = originalModel;
	}

	public static void reload() {
		CORNERS.clear();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	private List<BakedQuad> getCorner(TableClothBlock block, int corner, @NotNull RandomSource rand) {
		if (!CORNERS.containsKey(block)) {
			TextureAtlasSprite targetSprite = getParticleIcon();
			List<List<BakedQuad>> list = new ArrayList<>();

			for (PartialModel pm : List.of(AllPartialModels.TABLE_CLOTH_SW, AllPartialModels.TABLE_CLOTH_NW,
				AllPartialModels.TABLE_CLOTH_NE, AllPartialModels.TABLE_CLOTH_SE))
				list.add(getCornerQuads(rand, targetSprite, pm));

			CORNERS.put(block, list);
		}

		return CORNERS.get(block)
			.get(corner);
	}

	private List<BakedQuad> getCornerQuads(RandomSource rand, TextureAtlasSprite targetSprite, PartialModel pm) {
		List<BakedQuad> quads = new ArrayList<>();

		for (BakedQuad quad : pm.get()
			.getQuads(null, null, rand)) {
			TextureAtlasSprite original = quad.getSprite();
			BakedQuad newQuad = BakedQuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();
			for (int vertex = 0; vertex < 4; vertex++) {
				BakedQuadHelper.setU(vertexData, vertex, targetSprite
					.getU(SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex))));
				BakedQuadHelper.setV(vertexData, vertex, targetSprite
					.getV(SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex))));
			}
			quads.add(newQuad);
		}

		return quads;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
		@NotNull RandomSource rand) {
		List<BakedQuad> mainQuads = super.getQuads(state, side, rand);
		if (side == null || side.getAxis() == Axis.Y)
			return mainQuads;
		if (state == null || !(state.getBlock() instanceof TableClothBlock tableCloth))
			return mainQuads;

		List<BakedQuad> copy = new ArrayList<>(mainQuads);
		copy.addAll(getCorner(tableCloth, side.get2DDataValue(), rand));
		return copy;
	}

}
