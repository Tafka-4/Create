package com.simibubi.create.foundation.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashBiMap;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class CopperRegistries {
	private static final Map<Holder<Block>, Holder<Block>> WEATHERING = HashBiMap.create();
	private static final Map<Holder<Block>, Holder<Block>> WAXABLE = HashBiMap.create();
	private static final List<CopperPair> PENDING_WEATHERING = new ArrayList<>();
	private static final List<CopperPair> PENDING_WAXABLE = new ArrayList<>();

	public static Map<Holder<Block>, Holder<Block>> getWeatheringView() {
		register();
		return Collections.unmodifiableMap(WEATHERING);
	}

	public static Map<Holder<Block>, Holder<Block>> getWaxableView() {
		register();
		return Collections.unmodifiableMap(WAXABLE);
	}

	public static synchronized void addWeathering(Holder<Block> original, Holder<Block> weathered) {
		WEATHERING.put(original, weathered);
	}

	public static synchronized void addWaxable(Holder<Block> original, Holder<Block> waxed) {
		WAXABLE.put(original, waxed);
	}

	public static synchronized void addWeathering(Supplier<Holder<Block>> original, Supplier<Holder<Block>> weathered) {
		PENDING_WEATHERING.add(new CopperPair(original, weathered));
	}

	public static synchronized void addWaxable(Supplier<Holder<Block>> original, Supplier<Holder<Block>> waxed) {
		PENDING_WAXABLE.add(new CopperPair(original, waxed));
	}

	public static synchronized void register() {
		PENDING_WEATHERING.removeIf(pair -> {
			addWeathering(pair.original.get(), pair.changed.get());
			return true;
		});
		PENDING_WAXABLE.removeIf(pair -> {
			addWaxable(pair.original.get(), pair.changed.get());
			return true;
		});
	}

	private record CopperPair(Supplier<Holder<Block>> original, Supplier<Holder<Block>> changed) {
	}
}
