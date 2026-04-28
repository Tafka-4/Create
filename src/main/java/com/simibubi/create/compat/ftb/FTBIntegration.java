package com.simibubi.create.compat.ftb;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;

import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.Remove;
import net.fabricmc.fabric.api.event.Event;

public class FTBIntegration {

	public static void init() {
		ResourceLocation early = Create.asResource("early");
		ResourceLocation late = Create.asResource("late");
		// this callback needs to run before FTB since they add their buttons at the same time
		ScreenEvents.AFTER_INIT.addPhaseOrdering(early, Event.DEFAULT_PHASE);
		ScreenEvents.AFTER_INIT.register(early, (client, screen, width, height) -> {
			// ignore non-create screens
			if (!isCreate(screen))
				return;

			// grab initial button state to re-apply it on close
			boolean buttonsEnabled = FTBLibraryClientConfig.SIDEBAR_ENABLED.get();
			FTBLibraryClientConfig.SIDEBAR_ENABLED.set(false);
			Event<Remove> event = ScreenEvents.remove(screen);
			event.addPhaseOrdering(Event.DEFAULT_PHASE, late);
			event.register(late, closingScreen -> FTBLibraryClientConfig.SIDEBAR_ENABLED.set(buttonsEnabled));
		});
	}

	private static boolean isCreate(Screen screen) {
		return screen instanceof AbstractSimiContainerScreen<?> || screen instanceof AbstractSimiScreen;
	}

}
