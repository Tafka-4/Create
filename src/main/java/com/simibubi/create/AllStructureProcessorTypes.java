package com.simibubi.create;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.Supplier;

import com.simibubi.create.content.schematics.SchematicProcessor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class AllStructureProcessorTypes {
	private static final StructureProcessorType<SchematicProcessor> SCHEMATIC_TYPE = () -> SchematicProcessor.CODEC;

	public static final Supplier<StructureProcessorType<SchematicProcessor>> SCHEMATIC = () -> SCHEMATIC_TYPE;

	@Internal
	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Create.asResource("schematic"), SCHEMATIC_TYPE);
	}
}
