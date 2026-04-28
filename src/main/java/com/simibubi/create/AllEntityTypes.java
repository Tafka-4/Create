package com.simibubi.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.contraptions.gantry.GantryContraptionEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.createmod.catnip.lang.Lang;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

public class AllEntityTypes {

	private static final String CONTRAPTION_RENDERER =
		"com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer";
	private static final String ORIENTED_CONTRAPTION_RENDERER =
		"com.simibubi.create.content.contraptions.render.OrientedContraptionEntityRenderer";
	private static final String CARRIAGE_CONTRAPTION_RENDERER =
		"com.simibubi.create.content.trains.entity.CarriageContraptionEntityRenderer";
	private static final String CONTRAPTION_VISUAL =
		"com.simibubi.create.content.contraptions.render.ContraptionVisual";
	private static final String CARRIAGE_CONTRAPTION_VISUAL =
		"com.simibubi.create.content.trains.entity.CarriageContraptionVisual";
	private static final String SUPER_GLUE_RENDERER =
		"com.simibubi.create.content.contraptions.glue.SuperGlueRenderer";
	private static final String BLUEPRINT_RENDERER =
		"com.simibubi.create.content.equipment.blueprint.BlueprintRenderer";
	private static final String POTATO_PROJECTILE_RENDERER =
		"com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileRenderer";
	private static final String SEAT_RENDERER =
		"com.simibubi.create.content.contraptions.actors.seat.SeatEntity$Render";
	private static final String PACKAGE_RENDERER =
		"com.simibubi.create.content.logistics.box.PackageRenderer";
	private static final String PACKAGE_VISUAL =
		"com.simibubi.create.content.logistics.box.PackageVisual";

	public static final EntityEntry<OrientedContraptionEntity> ORIENTED_CONTRAPTION = AllEntityTypes.<OrientedContraptionEntity>contraption("contraption",
		OrientedContraptionEntity::new, ORIENTED_CONTRAPTION_RENDERER, 5, 3, true)
		.visual(CONTRAPTION_VISUAL)
		.register();
	public static final EntityEntry<ControlledContraptionEntity> CONTROLLED_CONTRAPTION =
		AllEntityTypes.<ControlledContraptionEntity>contraption("stationary_contraption", ControlledContraptionEntity::new, CONTRAPTION_RENDERER,
			20, 40, false)
			.visual(CONTRAPTION_VISUAL)
			.register();
	public static final EntityEntry<GantryContraptionEntity> GANTRY_CONTRAPTION = AllEntityTypes.<GantryContraptionEntity>contraption("gantry_contraption",
		GantryContraptionEntity::new, CONTRAPTION_RENDERER, 10, 40, false)
		.visual(CONTRAPTION_VISUAL)
		.register();
	public static final EntityEntry<CarriageContraptionEntity> CARRIAGE_CONTRAPTION =
		AllEntityTypes.<CarriageContraptionEntity>contraption("carriage_contraption", CarriageContraptionEntity::new,
			CARRIAGE_CONTRAPTION_RENDERER, 15, 3, true)
			.visual(CARRIAGE_CONTRAPTION_VISUAL)
			.register();

	public static final EntityEntry<SuperGlueEntity> SUPER_GLUE =
		AllEntityTypes.<SuperGlueEntity>register("super_glue", SuperGlueEntity::new, SUPER_GLUE_RENDERER, MobCategory.MISC, 10,
			Integer.MAX_VALUE, false, true, SuperGlueEntity::build).register();

	public static final EntityEntry<BlueprintEntity> CRAFTING_BLUEPRINT =
		AllEntityTypes.<BlueprintEntity>register("crafting_blueprint", BlueprintEntity::new, BLUEPRINT_RENDERER, MobCategory.MISC, 10,
			Integer.MAX_VALUE, false, true, BlueprintEntity::build).register();

	public static final EntityEntry<PotatoProjectileEntity> POTATO_PROJECTILE =
		AllEntityTypes.<PotatoProjectileEntity>register("potato_projectile", PotatoProjectileEntity::new, POTATO_PROJECTILE_RENDERER,
			MobCategory.MISC, 4, 20, true, false, PotatoProjectileEntity::build).register();

	public static final EntityEntry<SeatEntity> SEAT = AllEntityTypes.<SeatEntity>register("seat", SeatEntity::new, SEAT_RENDERER,
		MobCategory.MISC, 5, Integer.MAX_VALUE, false, true, SeatEntity::build).register();

	public static final EntityEntry<PackageEntity> PACKAGE = AllEntityTypes.<PackageEntity>register("package", PackageEntity::new, PACKAGE_RENDERER,
		MobCategory.MISC, 10, 3, true, false, PackageEntity::build)
		.visual(PACKAGE_VISUAL, true)
		.attributes(PackageEntity::createPackageAttributes)
		.register();

	//

	private static <T extends Entity> CreateEntityBuilder<T, ?> contraption(String name, EntityFactory<T> factory,
																			String rendererClassName, int range,
																			int updateFrequency, boolean sendVelocity) {
		return register(name, factory, rendererClassName, MobCategory.MISC, range, updateFrequency, sendVelocity, true,
			AbstractContraptionEntity::build);
	}

	private static <T extends Entity> CreateEntityBuilder<T, ?> register(String name, EntityFactory<T> factory,
																		 String rendererClassName,
																		 MobCategory group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
																		 NonNullConsumer<FabricEntityTypeBuilder<T>> propertyBuilder) {
		String id = Lang.asId(name);
		CreateEntityBuilder<T, ?> builder = (CreateEntityBuilder<T, ?>) Create.registrate()
			.entity(id, factory, group)
			.properties(b -> b.trackRangeChunks(range)
				.trackedUpdateRate(updateFrequency)
				.forceTrackedVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.properties(b -> {
				if (immuneToFire)
					b.fireImmune();
			});
		return builder.renderer(rendererClassName);
	}

	// fabric: handled with EntityBuilder#attributes
//	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
//		event.put(PACKAGE.get(), PackageEntity.createPackageAttributes()
//			.build());
//	}

	public static void register() {
	}
}
