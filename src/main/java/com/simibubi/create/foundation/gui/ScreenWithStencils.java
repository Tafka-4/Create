package com.simibubi.create.foundation.gui;

import net.minecraft.client.gui.GuiGraphics;

public interface ScreenWithStencils {
	default void startStencil(GuiGraphics graphics, int x, int y, int width, int height) {
	}

	default void endStencil() {
	}
}
