package com.simibubi.create.foundation.events;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.compat.trainmap.TrainMapSync;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.simibubi.create.content.contraptions.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.simibubi.create.content.equipment.bell.HauntedBellPulser;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsInteractionHandler;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.simibubi.create.content.equipment.tool.CardboardSwordItem;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.content.equipment.wrench.WrenchEventHandler;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.fluids.FluidBottleItemHook;
import com.simibubi.create.content.fluids.FluidReactions;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.drill.CobbleGenOptimisation;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.chute.SmartChuteBlockEntity;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.processing.burner.BlazeBurnerHandler;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.simibubi.create.content.redstone.link.LinkHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.simibubi.create.content.trains.entity.CarriageEntityHandler;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.schedule.ScheduleItemEntityInteraction;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.data.RuntimeDataGenerator;
import com.simibubi.create.foundation.pack.DynamicPack;
import com.simibubi.create.foundation.pack.DynamicPackSource;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.GlobalRegistryAccess;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.TickBasedCache;
import com.simibubi.create.infrastructure.command.AllCommands;

import net.createmod.catnip.data.WorldAttached;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.EntityHitResult;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityDataEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.ProjectileImpactEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingAttackEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents.LivingVisibilityEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingChangeTargetEvent;

public class CommonEvents {

	public static void onServerTick(MinecraftServer server) {
		GlobalRegistryAccess.setCurrentServer(server);
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick(server);
		Create.RAILWAYS.sync.serverTick();
		TrainMapSync.serverTick(server);
		ServerChainConveyorHandler.tick();
		TickBasedCache.tick();
	}

	public static void onChunkUnloaded(Level world, LevelChunk chunk) {
		CapabilityMinecartController.onChunkUnloaded(world, chunk);
	}

	public static void playerLoggedIn(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ToolboxHandler.playerLogin(handler.getPlayer());
		Create.RAILWAYS.playerLogin(handler.getPlayer());
	}

	public static void playerLoggedOut(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		Player player = handler.getPlayer();
		Create.RAILWAYS.playerLogout(player);
	}

	public static void onServerWorldTick(Level world) {
		if (world.isClientSide())
			return;
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
		LinkedControllerServerHandler.tick(world);
		ControlsServerHandler.tick(world);
		Create.RAILWAYS.tick(world);
		Create.LOGISTICS.tick(world);
	}

	public static void onUpdateLivingEntity(LivingEntity livingEntity) {
		Level level = livingEntity.level();

		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(livingEntity, level);
		ToolboxHandler.entityTick(livingEntity, level);
	}

	public static void onEntityAdded(Entity entity, Level world) {
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	public static InteractionResult onEntityAttackedByPlayer(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityRayTraceResult) {
		return WrenchItem.wrenchInstaKillsMinecarts(playerEntity, world, hand, entity, entityRayTraceResult);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		AllCommands.register(dispatcher);
	}

	public static void onEntityEnterSection(Entity entity, long packedOldPos, long packedNewPos) {
		CarriageEntityHandler.onEntityEnterSection(entity, packedOldPos, packedNewPos);
	}

	public static void addReloadListeners() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RecipeFinder.LISTENER);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(BeltHelper.LISTENER);
	}

	public static void serverStopping(MinecraftServer server) {
		GlobalRegistryAccess.setCurrentServer(null);
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	public static void onLoadWorld(Executor executor, LevelAccessor world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
		Create.RAILWAYS.levelLoaded(world);
		Create.LOGISTICS.levelLoaded(world);
	}

	public static void onUnloadWorld(Executor executor, LevelAccessor world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
		Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
		CobbleGenOptimisation.invalidateWorld(world);
	}

	// handled by AbstractMinecartMixin
//	public static void attachCapabilities(AbstractMinecart cart) {
//		CapabilityMinecartController.attach(cart);
//	}

	public static void startTracking(Entity target, ServerPlayer player) {
		CapabilityMinecartController.startTracking(target);
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}

	public static class ModBusEvents {

//		@SubscribeEvent
//
	}

	public static void register() {
		// Fabric Events
		ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
		ServerChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ServerTickEvents.END_WORLD_TICK.register(CommonEvents::onServerWorldTick);
		ServerEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		ServerLifecycleEvents.SERVER_STOPPED.register(CommonEvents::serverStopping);
		ServerWorldEvents.LOAD.register(CommonEvents::onLoadWorld);
		ServerWorldEvents.UNLOAD.register(CommonEvents::onUnloadWorld);
		ServerPlayConnectionEvents.DISCONNECT.register(CommonEvents::playerLoggedOut);
		AttackEntityCallback.EVENT.register(CommonEvents::onEntityAttackedByPlayer);
		CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);
		EntityEvents.EnteringSection.EVENT.register(event -> CommonEvents.onEntityEnterSection(event.getEntity(), event.getPackedOldPos(), event.getPackedNewPos()));
		LivingEntityEvents.TICK.register(CommonEvents::onUpdateLivingEntity);
		ServerPlayConnectionEvents.JOIN.register(CommonEvents::playerLoggedIn);
		PipeCollisionEvent.FLOW.register(FluidReactions::handlePipeFlowCollisionFallback);
		PipeCollisionEvent.SPILL.register(FluidReactions::handlePipeSpillCollisionFallback);
		// fabric: some features using events on forge don't use events here.
		// they've been left in this class for upstream compatibility.
		CommonEvents.addReloadListeners();

		// External Events

		UseEntityCallback.EVENT.register(MinecartCouplingItem::handleInteractionWithMinecart);
		UseEntityCallback.EVENT.register(MinecartContraptionItem::wrenchCanBeUsedToPickUpMinecartContraptions);
		UseEntityCallback.EVENT.register(StockTickerInteractionHandler::interactWithLogisticsManager);
		UseBlockCallback.EVENT.register(WrenchEventHandler::useOwnWrenchLogicForCreateBlocks);
		UseBlockCallback.EVENT.register(LinkHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ItemUseOverrides::onBlockActivated);
		UseBlockCallback.EVENT.register(EdgeInteractionHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(FluidBottleItemHook::preventWaterBottlesFromCreatesFluids);
		UseBlockCallback.EVENT.register(SuperGlueItem::glueItemAlwaysPlacesWhenUsed);
		UseBlockCallback.EVENT.register(ManualApplicationRecipe::manualApplicationRecipesApplyInWorld);
		UseBlockCallback.EVENT.register(ValueSettingsInputHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ValveHandleBlock::onBlockActivated);
		UseBlockCallback.EVENT.register(ClipboardValueSettingsInteractionHandler::rightClickToCopy);
		UseBlockCallback.EVENT.register(ClickToLinkBlockItem::linkableItemAlwaysPlacesWhenUsed);
		AttackBlockCallback.EVENT.register(ClipboardValueSettingsInteractionHandler::leftClickToPaste);
		AttackBlockCallback.EVENT.register(ZapperInteractionHandler::leftClickingBlocksWithTheZapperSelectsTheBlock);
		UseEntityCallback.EVENT.register(ScheduleItemEntityInteraction::interactWithConductor);
		ServerTickEvents.END_WORLD_TICK.register(HauntedBellPulser::hauntedBellCreatesPulse);
		LivingEntityEvents.EXPERIENCE_DROP.register(DeployerFakePlayer::deployerKillsDoNotSpawnXP);
		LivingEntityEvents.HURT.register(ExtendoGripItem::bufferLivingAttackEvent);
		LivingEntityEvents.KNOCKBACK_STRENGTH.register(ExtendoGripItem::attacksByExtendoGripHaveMoreKnockback);
		LivingEntityEvents.TICK.register(ExtendoGripItem::holdingExtendoGripIncreasesRange);
		LivingEntityEvents.TICK.register(DivingBootsItem::accelerateDescentUnderwater);
		LivingEntityEvents.TICK.register(DivingHelmetItem::breatheUnderwater);
		LivingEntityEvents.DROPS.register(CrushingWheelBlockEntity::handleCrushedMobDrops);
		LivingEntityEvents.DROPS.register(DeployerFakePlayer::deployerCollectsDropsFromKilledEntities);
		ServerEntityEvents.EQUIPMENT_CHANGE.register(NetheriteDivingHandler::onLivingEquipmentChange);
		LivingChangeTargetEvent.EVENT.register(DeployerFakePlayer::entitiesDontRetaliate);
		EntityEvents.Size.EVENT.register(DeployerFakePlayer::deployerHasEyesOnHisFeet);
		ProjectileImpactEvent.EVENT.register(BlazeBurnerHandler::onThrowableImpact);
		EntityDataEvents.LOAD.register(ExtendoGripItem::addReachToJoiningPlayersHoldingExtendo);
		PlayerBlockBreakEvents.BEFORE.register(SymmetryHandler::onBlockDestroyed);
		PlayerBlockBreakEvents.AFTER.register(ExtendoGripItem::consumeDurabilityOnBlockBreak);
		EntityEvents.Size.EVENT.register(CardboardArmorHandler::playerHitboxChangesWhenHidingAsBox);
		LivingVisibilityEvent.VISIBILITY.register(CardboardArmorHandler::playersStealthWhenWearingCardboard);
		LivingEntityEvents.TICK.register(CardboardArmorHandler::mobsMayLoseTargetWhenItIsWearingCardboard);
		AttackBlockCallback.EVENT.register(CardboardSwordItem::cardboardSwordsMakeNoiseOnClick);
		LivingAttackEvent.EVENT.register(CardboardSwordItem::cardboardSwordsCannotHurtYou);
	}
}
