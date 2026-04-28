package com.simibubi.create.compat.trainmap;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.mixin.compat.ftbchunks.LargeMapScreenAccessor;
import com.simibubi.create.foundation.mixin.compat.ftbchunks.RegionMapPanelAccessor;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;

public class FTBChunksTrainMap {

	private static int cancelTooltips = 0;
	private static boolean renderingTooltip = false;
	private static boolean requesting;

	public static void tick() {
		if (cancelTooltips > 0)
			cancelTooltips--;

		LargeMapScreen mapScreen = getAsLargeMapScreen(Minecraft.getInstance().screen);

		if (!AllConfigs.client().showTrainMapOverlay.get() || mapScreen == null) {
			if (requesting)
				TrainMapSyncClient.stopRequesting();
			requesting = false;
			return;
		}

		TrainMapManager.tick(mapScreen.currentDimension());
		requesting = true;
		TrainMapSyncClient.requestData();
	}

	public static boolean cancelTooltips() {
		if (getAsLargeMapScreen(Minecraft.getInstance().screen) == null)
			return false;
		if (renderingTooltip || cancelTooltips == 0)
			return false;
		return true;
	}

	public static boolean mouseClick(Screen screen, int mouseX, int mouseY) {
		LargeMapScreen map = getAsLargeMapScreen(screen);
		if (map == null)
			return false;

		return TrainMapManager.handleToggleWidgetClick(mouseX, mouseY, 20, 2);
	}

	public static void renderGui(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		LargeMapScreen largeMapScreen = getAsLargeMapScreen(screen);
		if (largeMapScreen == null)
			return;
		RegionMapPanel regionMapPanel = ((LargeMapScreenAccessor) largeMapScreen).getRegionPanel();

		if (!AllConfigs.client().showTrainMapOverlay.get()) {
			renderToggleWidgetAndTooltip(mouseX, mouseY, largeMapScreen, graphics);
			return;
		}

		int blocksPerRegion = 16 * 32;
		int minX = Mth.floor(regionMapPanel.getScrollX());
		int minY = Mth.floor(regionMapPanel.getScrollY());
		float regionTileSize = largeMapScreen.getRegionTileSize() / (float) blocksPerRegion;
		int regionMinX = ((RegionMapPanelAccessor) regionMapPanel).getRegionMinX();
		int regionMinZ = ((RegionMapPanelAccessor) regionMapPanel).getRegionMinZ();

		boolean linearFiltering = largeMapScreen.getRegionTileSize() * Minecraft.getInstance()
			.getWindow()
			.getGuiScale() < 512D;

		PoseStack pose = graphics.pose();
		pose.pushPose();

		pose.translate(-minX, -minY, 0);
		pose.scale(regionTileSize, regionTileSize, 1);
		pose.translate(-regionMinX * blocksPerRegion, -regionMinZ * blocksPerRegion, 0);

		mouseX += minX;
		mouseY += minY;
		mouseX /= regionTileSize;
		mouseY /= regionTileSize;
		mouseX += regionMinX * blocksPerRegion;
		mouseY += regionMinZ * blocksPerRegion;

		Rect2i bounds = new Rect2i(Mth.floor(minX / regionTileSize + regionMinX * blocksPerRegion),
			Mth.floor(minY / regionTileSize + regionMinZ * blocksPerRegion),
			Mth.floor(largeMapScreen.width / regionTileSize), Mth.floor(largeMapScreen.height / regionTileSize));

		List<FormattedText> tooltip = TrainMapManager.renderAndPick(graphics, Mth.floor(mouseX), Mth.floor(mouseY),
			linearFiltering, bounds);

		pose.popPose();

		if (!renderToggleWidgetAndTooltip(mouseX, mouseY, largeMapScreen, graphics) && tooltip != null) {
			renderingTooltip = true;
			RemovedGuiUtils.drawHoveringText(graphics, tooltip, mouseX, mouseY,
				largeMapScreen.width, largeMapScreen.height, 256, Minecraft.getInstance().font);
			renderingTooltip = false;
			cancelTooltips = 5;
		}

		pose.pushPose();
		pose.translate(0, 0, 300);
		for (Widget widget : largeMapScreen.getWidgets()) {
			if (!widget.isEnabled())
				continue;
			if (widget == regionMapPanel)
				continue;
			widget.draw(graphics, largeMapScreen.getTheme(), widget.getPosX(), widget.getPosY(), widget.getWidth(),
				widget.getHeight());
		}
		pose.popPose();
	}

	private static boolean renderToggleWidgetAndTooltip(int mouseX, int mouseY, LargeMapScreen largeMapScreen,
		GuiGraphics graphics) {
		TrainMapManager.renderToggleWidget(graphics, 20, 2);
		if (!TrainMapManager.isToggleWidgetHovered(mouseX, mouseY, 20, 2))
			return false;

		renderingTooltip = true;
		RemovedGuiUtils.drawHoveringText(graphics, List.of(CreateLang.translate("train_map.toggle")
			.component()), mouseX, mouseY + 20, largeMapScreen.width, largeMapScreen.height, 256,
			Minecraft.getInstance().font);
		renderingTooltip = false;
		cancelTooltips = 5;
		return true;
	}

	private static LargeMapScreen getAsLargeMapScreen(Screen screen) {
		if (!(screen instanceof ScreenWrapper screenWrapper))
			return null;
		BaseScreen wrapped = screenWrapper.getGui();
		if (!(wrapped instanceof LargeMapScreen largeMapScreen))
			return null;
		return largeMapScreen;
	}

}
