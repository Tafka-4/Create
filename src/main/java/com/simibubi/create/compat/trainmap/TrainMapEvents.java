package com.simibubi.create.compat.trainmap;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.Mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import io.github.fabricators_of_create.porting_lib.event.client.PreRenderTooltipCallback;

public class TrainMapEvents {

	public static void tick(Minecraft mc) {
		if (mc.level == null)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.tick();
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.tick();
	}

	public static boolean mouseClick(Screen screen, double mouseX, double mouseY, int button) {
		if (Mods.FTBCHUNKS.isLoaded() && FTBChunksTrainMap.mouseClick(screen, (int) mouseX, (int) mouseY))
			return false;
		if (Mods.JOURNEYMAP.isLoaded() && JourneyTrainMap.mouseClick(screen, (int) mouseX, (int) mouseY))
			return false;

		return true;
	}

	public static boolean cancelTooltips(ItemStack stack, PoseStack matrices, int x, int y, int width, int height, Font font, List<ClientTooltipComponent> tooltip) {
		if (Mods.FTBCHUNKS.isLoaded()) {
			return FTBChunksTrainMap.cancelTooltips();
		}

		return false;
	}

	public static void renderGui(Screen screen, GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
		if (Mods.FTBCHUNKS.isLoaded()) {
			FTBChunksTrainMap.renderGui(screen, graphics, (int) mouseX, (int) mouseY, partialTicks);
		}
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(TrainMapEvents::tick);
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			ScreenMouseEvents.allowMouseClick(screen).register(TrainMapEvents::mouseClick);
			ScreenEvents.afterRender(screen).register(TrainMapEvents::renderGui);
		});
		PreRenderTooltipCallback.EVENT.register(TrainMapEvents::cancelTooltips);
	}
}
