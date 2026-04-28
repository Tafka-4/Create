package net.fabricmc.fabric.api.item.v1;

import net.minecraft.world.item.Item;

/**
 * Compatibility shim for older Fabric dependencies that still instantiate
 * FabricItemSettings on Minecraft 1.21.1, where Fabric API has removed it.
 */
@Deprecated
public class FabricItemSettings extends Item.Properties {
	public FabricItemSettings() {
		super();
	}
}
