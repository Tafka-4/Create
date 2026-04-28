package com.simibubi.create.content.logistics.packagePort;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

public class AllPackagePortTargetTypes {
	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR = register("chain_conveyor", new ChainConveyorFrogportTarget.Type());
	public static final Holder<PackagePortTargetType> TRAIN_STATION = register("train_station", new TrainStationFrogportTarget.Type());

	private static Holder<PackagePortTargetType> register(String name, PackagePortTargetType type) {
		return Registry.registerForHolder(CreateBuiltInRegistries.PACKAGE_PORT_TARGET_TYPE, Create.asResource(name), type);
	}

	@Internal
	public static void register() {
	}
}
