package io.github.fabricators_of_create.porting_lib.entity.events;

import java.util.Collection;

import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingDropsEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingExperienceDropEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingHurtEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingKnockBackEvent;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public final class LivingEntityEvents {
	public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class, callbacks -> entity -> {
		for (Tick callback : callbacks)
			callback.onTick(entity);
	});

	public static final Event<ExperienceDrop> EXPERIENCE_DROP = EventFactory.createArrayBacked(ExperienceDrop.class, callbacks -> (experience, player, entity) -> {
		int result = experience;
		for (ExperienceDrop callback : callbacks)
			result = callback.onDropExperience(result, player, entity);
		return result;
	});

	public static final Event<Hurt> HURT = EventFactory.createArrayBacked(Hurt.class, callbacks -> (source, entity, amount) -> {
		float result = amount;
		for (Hurt callback : callbacks)
			result = callback.onHurt(source, entity, result);
		return result;
	});

	public static final Event<KnockbackStrength> KNOCKBACK_STRENGTH =
		EventFactory.createArrayBacked(KnockbackStrength.class, callbacks -> (strength, player) -> {
			double result = strength;
			for (KnockbackStrength callback : callbacks)
				result = callback.modifyKnockback(result, player);
			return result;
		});

	public static final Event<Drops> DROPS = EventFactory.createArrayBacked(Drops.class, callbacks -> (target, source, drops, lootingLevel, recentlyHit) -> {
		boolean canceled = false;
		for (Drops callback : callbacks)
			canceled |= callback.onDrops(target, source, drops, lootingLevel, recentlyHit);
		return canceled;
	});

	public static final Event<LootingLevel> LOOTING_LEVEL = EventFactory.createArrayBacked(LootingLevel.class, callbacks -> (lootingLevel, target, source) -> {
		int result = lootingLevel;
		for (LootingLevel callback : callbacks)
			result = callback.modifyLootingLevel(result, target, source);
		return result;
	});

	static {
		LivingExperienceDropEvent.EVENT.register(event ->
			event.setDroppedExperience(EXPERIENCE_DROP.invoker()
				.onDropExperience(event.getDroppedExperience(), event.getAttackingPlayer(), event.getEntity())));

		LivingHurtEvent.EVENT.register(event ->
			event.setAmount(HURT.invoker()
				.onHurt(event.getSource(), event.getEntity(), event.getAmount())));

		LivingKnockBackEvent.EVENT.register(event -> {
			Player player = event.getEntity() instanceof Player p ? p : null;
			event.setStrength((float) KNOCKBACK_STRENGTH.invoker()
				.modifyKnockback(event.getStrength(), player));
		});

		LivingDropsEvent.EVENT.register(event -> {
			if (DROPS.invoker()
				.onDrops(event.getEntity(), event.getSource(), event.getDrops(), 0, event.isRecentlyHit()))
				event.setCanceled(true);
		});
	}

	private LivingEntityEvents() {}

	@FunctionalInterface
	public interface Tick {
		void onTick(LivingEntity entity);
	}

	@FunctionalInterface
	public interface ExperienceDrop {
		int onDropExperience(int experience, Player player, LivingEntity entity);
	}

	@FunctionalInterface
	public interface Hurt {
		float onHurt(DamageSource source, LivingEntity entity, float amount);
	}

	@FunctionalInterface
	public interface KnockbackStrength {
		double modifyKnockback(double strength, Player player);
	}

	@FunctionalInterface
	public interface Drops {
		boolean onDrops(LivingEntity target, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit);
	}

	@FunctionalInterface
	public interface LootingLevel {
		int modifyLootingLevel(int lootingLevel, LivingEntity target, DamageSource source);
	}

	public static final class LivingVisibilityEvent {
		public static final Event<LivingEvents.LivingVisibilityEvent.Callback> VISIBILITY =
			LivingEvents.LivingVisibilityEvent.EVENT;

		private LivingVisibilityEvent() {}
	}
}
