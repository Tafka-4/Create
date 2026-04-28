package com.simibubi.create.foundation.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;

@Environment(EnvType.CLIENT)
public class FluidRenderer {
	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax, float yMax,
		float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses,
		Object components) {
		FluidVariant variant = components instanceof DataComponentPatch patch ? FluidVariant.of(fluid, patch) : FluidVariant.of(fluid);
		renderFluidBox(new FluidStack(variant, amount), xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, renderBottom,
			invertGasses);
	}

	public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax,
		float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, FluidRenderHelper.getFluidBuilder(buffer), ms,
			light, renderBottom, invertGasses);
	}

	public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax,
		float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		if (fluidStack.isEmpty())
			return;

		FluidVariant fluidVariant = fluidStack.getVariant();
		TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(fluidVariant);
		if (sprites == null)
			return;

		TextureAtlasSprite fluidTexture = sprites[0];
		int color = FluidVariantRendering.getColor(fluidVariant);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, FluidVariantAttributes.getLuminance(fluidVariant));
		light = (light & 0xF00000) | luminosity << 4;

		for (Direction side : Direction.values()) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			if (side.getAxis().isHorizontal()) {
				if (side.getAxis() == Direction.Axis.X) {
					FluidRenderHelper.renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
						builder, ms, light, color, fluidTexture);
				} else {
					FluidRenderHelper.renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
						builder, ms, light, color, fluidTexture);
				}
			} else {
				FluidRenderHelper.renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
					builder, ms, light, color, fluidTexture);
			}
		}
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound, FluidRenderHelper.getFluidBuilder(buffer), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		FluidVariant fluidVariant = fluidStack.getVariant();
		TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(fluidVariant);
		if (sprites == null) {
			return;
		}
		TextureAtlasSprite flowTexture = sprites[1];
		TextureAtlasSprite stillTexture = sprites[0];

		int color = FluidVariantRendering.getColor(fluidVariant);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, FluidVariantAttributes.getLuminance(fluidVariant));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
			.uncenter();
		ms.translate(.5, 0, .5);

		float h = radius;
		float hMin = -radius;
		float hMax = radius;
		float y = inbound ? 1 : .5f;
		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
		float yMax = y;

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h, builder, ms, light, color, flowTexture);
			ms.popPose();
			msr.rotateYDegrees(90);
		}

		if (progress != 1)
			FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin, builder, ms, light,
				color, stillTexture);

		ms.popPose();
	}

	public static void renderFlowingTiledFace(Direction dir, float left, float down, float right, float up,
		float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
	}
}
