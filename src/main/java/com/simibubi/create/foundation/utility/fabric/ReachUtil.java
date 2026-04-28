package com.simibubi.create.foundation.utility.fabric;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ReachUtil {
	public static double reach(LivingEntity entity) {
		if (entity.getAttributes().hasAttribute(Attributes.BLOCK_INTERACTION_RANGE))
			return entity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		return entity instanceof Player player && player.isCreative() ? 5 : 4.5;
	}
}
