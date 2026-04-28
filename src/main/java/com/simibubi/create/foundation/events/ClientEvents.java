package com.simibubi.create.foundation.events;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.compat.trainmap.TrainMapEvents;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.contraptions.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.minecart.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.CouplingRenderer;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfoManager;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import com.simibubi.create.content.decoration.girder.GirderWrenchBehavior;
import com.simibubi.create.content.equipment.armor.BacktankArmorLayer;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandlerClient;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.simibubi.create.content.equipment.hats.CreateHatArmorLayer;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItemRenderer;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.equipment.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.kinetics.turntable.TurntableHandler;
import com.simibubi.create.content.logistics.box.PackageClientInteractionHandler;
import com.simibubi.create.content.logistics.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import com.simibubi.create.content.logistics.tableCloth.TableClothOverlayRenderer;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackPlacement;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.TickBasedCache;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.fabric.RenderItemDecorationsCallback;
import com.simibubi.create.infrastructure.gui.OpenCreateMenuButton;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.StitchedSprite;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import io.github.fabricators_of_create.porting_lib.entity.events.player.AttackEntityEvent;
import io.github.fabricators_of_create.porting_lib.event.client.DrawSelectionEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents.ColorData;
import io.github.fabricators_of_create.porting_lib.event.client.InteractEvents;
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback;
import io.github.fabricators_of_create.porting_lib.client_events.event.client.RenderHandEvent;
import io.github.fabricators_of_create.porting_lib.event.client.RenderPlayerEvents;

public class ClientEvents {
	private static ClientLevel currentClientWorld;

	public static void onTickStart(Minecraft client) {
		LinkedControllerClientHandler.tick();
		ControlsHandler.tick();
		AirCurrent.Client.tickClientPlayerSounds();
		// fabric: fix #608
		// This tracks the current held item. Because of event order differences from forge, it gets changed too
		// quickly for the update packet to work properly. Move to here to fix.
		ArmInteractionPointHandler.tick();
	}

	public static void onTick(Minecraft client) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;

		SoundScapes.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.GLUE_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);
		CreateClient.RAILWAYS.clientTick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		GirderWrenchBehavior.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		// fabric: fix #608, see above
//		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		ContraptionRenderInfoManager.tickFor(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
		RadialWrenchHandler.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		TrainRelocator.clientTick();
		ClickToLinkBlockItem.clientTick();
		CurvedTrackInteraction.clientTick();
		CameraDistanceModifier.tick();
		CameraAngleAnimationService.tick();
		TrainHUD.tick();
		ClipboardValueSettingsHandler.clientTick();
		CreateClient.VALUE_SETTINGS_HANDLER.tick();
		ScrollValueHandler.tick();
		NetheriteBacktankFirstPersonRenderer.clientTick();
		ContraptionPlayerPassengerRotation.tick();
		ChainConveyorInteractionHandler.clientTick();
		ChainConveyorRidingHandler.clientTick();
		ChainConveyorConnectionHandler.clientTick();
		PackagePortTargetSelectionHandler.tick();
		LogisticallyLinkedClientHandler.tick();
		TableClothOverlayRenderer.tick();
		CardboardArmorStealthOverlay.clientTick();
		FactoryPanelConnectionHandler.clientTick();
		TickBasedCache.clientTick();
		// fabric: see comment
		AllKeys.fixBinds();
	}

	public static void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLeave(ClientPacketListener handler, Minecraft client) {
		CreateClient.RAILWAYS.cleanUp();
	}

	public static void onLoadWorld(Minecraft client, ClientLevel world) {
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientLevel)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(Minecraft client, ClientLevel world) {
		if (world
			.isClientSide()) {
			CreateClient.invalidateRenderers();
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
			AnimationTickHolder.reset();
			ControlsHandler.levelUnloaded(world);
		}
	}

	public static void onClientWorldChange(Minecraft client, ClientLevel world) {
		if (currentClientWorld != null && currentClientWorld != world) {
			onUnloadWorld(client, currentClientWorld);
			CommonEvents.onUnloadWorld(client, currentClientWorld);
		}
		currentClientWorld = world;
		if (world != null) {
			onLoadWorld(client, world);
			CommonEvents.onLoadWorld(client, world);
		}
	}

	public static void onRenderWorld(WorldRenderContext event) {
		PoseStack ms = event.matrixStack();
		ms.pushPose();
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();

		TrackBlockOutline.drawCurveSelection(ms, buffer, camera);
		TrackTargetingClient.render(ms, buffer, camera);
		CouplingRenderer.renderAll(ms, buffer, camera);
		CarriageCouplingRenderer.renderAll(ms, buffer, camera);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer, camera);
		ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, camera);

		buffer.draw();
		RenderSystem.enableCull();
		ms.popPose();

		ContraptionPlayerPassengerRotation.frame();
	}

	public static void addToItemTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag iTooltipFlag, List<Component> itemTooltip) {
		if (!AllConfigs.client().tooltips.get())
			return;
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		Item item = stack.getItem();
		TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);
		if (modifier != null && modifier != TooltipModifier.EMPTY) {
			modifier.modify(stack, player, iTooltipFlag, itemTooltip);
		}

		SequencedAssemblyRecipe.addToTooltip(stack, itemTooltip);
	}

	public static void onRenderTick(WorldRenderContext context) {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderFrame(context.tickCounter());
	}

	public static void tickClientPlayers(Minecraft client) {
		if (client.level == null)
			return;
		for (Player player : client.level.players()) {
			ContraptionHandlerClient.preventRemotePlayersWalkingAnimations(player);
			CardboardArmorHandlerClient.keepCacheAliveDesignDespiteNotRendering(player);
		}
	}

	public static boolean onMount(Entity vehicle, Entity passenger) {
		if (passenger == Minecraft.getInstance().player && vehicle instanceof CarriageContraptionEntity)
			CameraDistanceModifier.zoomOut();
		return true;
	}

	public static boolean onDismount(Entity vehicle, Entity passenger) {
		CameraDistanceModifier.reset();
		return true;
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static boolean getFogDensity(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, FogShape shape, FogEvents.FogData fogData) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = camera.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (camera.getPosition().y >= blockPos.getY() + fluidState.getHeight(level, blockPos))
			return false;
		Fluid fluid = fluidState.getType();
		Entity entity = camera.getEntity();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			fogData.scaleFarPlaneDistance(1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF());
			return true;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			fogData.scaleFarPlaneDistance(1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF());
			return true;
		}

		if (entity.isSpectator())
			return false;

		ItemStack divingHelmet = DivingHelmetItem.getWornItem(entity);
		if (!divingHelmet.isEmpty()) {
			if (FluidHelper.isWater(fluid)) {
				fogData.scaleFarPlaneDistance(6.25f);
				return true;
			} else if (FluidHelper.isLava(fluid) && NetheriteDivingHandler.isNetheriteDivingHelmet(divingHelmet)) {
				fogData.setNearPlaneDistance(-4.0f);
				fogData.setFarPlaneDistance(20.0f);
				return true;
			}
		}
		return false;
	}

	public static void getFogColor(ColorData event, float partialTicks) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			event.setRed(98 / 255f);
			event.setGreen(32 / 255f);
			event.setBlue(32 / 255f);
			return;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			event.setRed(234 / 255f);
			event.setGreen(174 / 255f);
			event.setBlue(47 / 255f);
			return;
		}
	}

	public static void leftClickEmpty(LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			CatnipServices.NETWORK.sendToServer(LeftClickPacket.INSTANCE);
		}
	}

	public static class ModBusEvents {

		public static void registerClientReloadListeners() {
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(TrainHatInfoReloadListener.LISTENER);
		}
	}

	public static void addEntityRendererLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer,
											   RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
		BacktankArmorLayer.registerOn(entityRenderer, registrationHelper);
		CreateHatArmorLayer.registerOn(entityRenderer, registrationHelper);
	}

	public static void registerItemDecorations() {
		RenderItemDecorationsCallback.EVENT.register(PotatoCannonItemRenderer.DECORATOR);
	}

	public static void register() {
//		ModBusEvents.registerClientReloadListeners();
		registerItemDecorations();

		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTickStart);
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::tickClientPlayers);
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(ClientEvents::onClientWorldChange);
		ClientChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ClientPlayConnectionEvents.JOIN.register(ClientEvents::onJoin);
		ClientEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEvents::onRenderWorld);
		ItemTooltipCallback.EVENT.register(ClientEvents::addToItemTooltip);
		FogEvents.RENDER_FOG.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);
		WorldRenderEvents.START.register(ClientEvents::onRenderTick);
		UseBlockCallback.EVENT.register(TrackBlockItem::sendExtenderPacket);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(ClientEvents::addEntityRendererLayers);
		DrawSelectionEvents.BLOCK.register(ClipboardValueSettingsHandler::drawCustomBlockSelection);

		// External Events

		ClientTickEvents.END_CLIENT_TICK.register(SymmetryHandler::onClientTick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SymmetryHandler::render);
		UseBlockCallback.EVENT.register(ArmInteractionPointHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(EjectorTargetHandler::rightClickingBlocksSelectsThem);
		AttackBlockCallback.EVENT.register(ArmInteractionPointHandler::leftClickingBlocksDeselectsThem);
		AttackBlockCallback.EVENT.register(EjectorTargetHandler::leftClickingBlocksDeselectsThem);
		ParticleManagerRegistrationCallback.EVENT.register(AllParticleTypes::registerFactories);
		RenderHandEvent.EVENT.register(ExtendoGripRenderHandler::onRenderPlayerHand);
		InteractEvents.USE.register(ContraptionHandlerClient::rightClickingOnContraptionsGetsHandledLocally);
		RenderPlayerEvents.PRE.register(CardboardArmorHandlerClient::playerRendersAsBoxWhenSneaking);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientEvents::onLeave);
		DrawSelectionEvents.BLOCK.register(TrackBlockOutline::drawCustomBlockSelection);
		AttackEntityEvent.EVENT.register(PackageClientInteractionHandler::onPlayerPunchPackage);
		UseBlockCallback.EVENT.register(ChainConveyorConnectionHandler::onItemUsedOnBlock);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ChainConveyorInteractionHandler::hideVanillaBlockSelection);

		// we need to add our config button after mod menu, so we register our event with a phase that comes later
		ResourceLocation latePhase = Create.asResource("late");
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase);
		ScreenEvents.AFTER_INIT.register(latePhase, OpenCreateMenuButton.OpenConfigButtonHandler::onGuiInit);

		TrainMapEvents.init();

		// Flywheel Events
		ReloadLevelRendererCallback.EVENT.register(ContraptionRenderInfoManager::onReloadLevelRenderer);
	}
}
