package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ClipboardValueSettingsHandler {

	@Environment(EnvType.CLIENT)
	public static boolean drawCustomBlockSelection(LevelRenderer context, Camera camera, HitResult hitResult, DeltaTracker partialTicks, PoseStack ms, MultiBufferSource buffers) {
		Minecraft mc = Minecraft.getInstance();
		BlockHitResult target = (BlockHitResult) hitResult;
		BlockPos pos = target.getBlockPos();
		BlockState blockstate = mc.level.getBlockState(pos);

		if (mc.player == null || mc.player.isSpectator())
			return false;
		if (!mc.level.getWorldBorder()
			.isWithinBounds(pos))
			return false;
		if (!AllBlocks.CLIPBOARD.isIn(mc.player.getMainHandItem()))
			return false;
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return false;
		if (!(smartBE instanceof ClipboardBlockEntity) && !smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection()))
			&& !(smartBE instanceof ClipboardCloneable))
			return false;

		VoxelShape shape = blockstate.getShape(mc.level, pos);
		if (shape.isEmpty())
			return false;

		VertexConsumer vb = buffers
			.getBuffer(RenderType.lines());
		Vec3 camPos = camera
			.getPosition();

		ms.pushPose();
		ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
		TrackBlockOutline.renderShape(shape, ms, vb, true);

		ms.popPose();
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult target))
			return;
		Player player = mc.player; // fabric: keep LocalPlayer out of lambdas
		if (!AllBlocks.CLIPBOARD.isIn(player.getMainHandItem()))
			return;
		BlockPos pos = target.getBlockPos();
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;

		if (smartBE instanceof ClipboardBlockEntity) {
			List<MutableComponent> tip = new ArrayList<>();
			tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.copy_other_clipboard", Component.keybind("key.use")));
			CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
			return;
		}

		CompoundTag tagElement = mc.player.getMainHandItem().get(AllDataComponents.CLIPBOARD_COPIED_VALUES);

		boolean canCopy = smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection()))
			|| smartBE instanceof ClipboardCloneable ccbe
				&& ccbe.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection());

		boolean canPaste = tagElement != null && (smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc && cc.readFromClipboard(mc.level.registryAccess(),
				tagElement.getCompound(cc.getClipboardKey()), mc.player, target.getDirection(), true))
			|| smartBE instanceof ClipboardCloneable ccbe && ccbe.readFromClipboard(mc.level.registryAccess(),
				tagElement.getCompound(ccbe.getClipboardKey()), mc.player, target.getDirection(), true));

		if (!canCopy && !canPaste)
			return;

		List<MutableComponent> tip = new ArrayList<>();
		tip.add(CreateLang.translateDirect("clipboard.actions"));
		if (canCopy)
            tip.add(CreateLang.translateDirect("clipboard.to_copy", Component.keybind("key.use")));
		if (canPaste)
            tip.add(CreateLang.translateDirect("clipboard.to_paste", Component.keybind("key.attack")));

		CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
	}

	public static InteractionResult rightClickToCopy(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		return interact(player.getItemInHand(hand), hitResult.getBlockPos(), world, player, hitResult.getDirection(), false);
	}

	public static InteractionResult leftClickToPaste(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		return interact(player.getItemInHand(hand), pos, world, player, direction, true);
	}

	private static InteractionResult interact(ItemStack itemStack, BlockPos pos, Level world, Player player, Direction face, boolean paste) {
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return InteractionResult.PASS;

		if (player != null && player.isSpectator() || AdventureUtil.isAdventure(player))
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return InteractionResult.PASS;

		if (smartBE instanceof ClipboardBlockEntity cbe) {
			if (!world.isClientSide()) {
				List<List<ClipboardEntry>> listTo = ClipboardEntry.readAll(itemStack);
				List<List<ClipboardEntry>> listFrom = ClipboardEntry.readAll(cbe.dataContainer);
				List<ClipboardEntry> toAdd = new ArrayList<>();

				for (List<ClipboardEntry> page : listFrom) {
					Copy: for (ClipboardEntry entry : page) {
						String entryToAdd = entry.text.getString();
						for (List<ClipboardEntry> pageTo : listTo)
							for (ClipboardEntry existing : pageTo)
								if (entryToAdd.equals(existing.text.getString()))
									continue Copy;
						toAdd.add(new ClipboardEntry(entry.checked, entry.text));
					}
				}

				for (ClipboardEntry entry : toAdd) {
					List<ClipboardEntry> page = null;
					for (List<ClipboardEntry> freePage : listTo) {
						if (freePage.size() > 11)
							continue;
						page = freePage;
						break;
					}
					if (page == null) {
						page = new ArrayList<>();
						listTo.add(page);
					}
					page.add(entry);
					ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
				}

				ClipboardContent content = itemStack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
				itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, content.setType(ClipboardType.WRITTEN).setPages(listTo));
			}

			player.displayClientMessage(CreateLang.translate("clipboard.copied_from_clipboard", world.getBlockState(pos)
				.getBlock()
				.getName()
				.withStyle(ChatFormatting.WHITE))
				.style(ChatFormatting.GREEN)
				.component(), true);
			return InteractionResult.SUCCESS;
		}

		CompoundTag tag = itemStack.get(AllDataComponents.CLIPBOARD_COPIED_VALUES);
		if (paste && tag == null)
			return InteractionResult.PASS;
		if (!paste)
			tag = new CompoundTag();

		boolean anySuccess = false;
		boolean anyValid = false;
		for (BlockEntityBehaviour behaviour : smartBE.getAllBehaviours()) {
			if (!(behaviour instanceof ClipboardCloneable cc))
				continue;
			anyValid = true;
			String clipboardKey = cc.getClipboardKey();
			if (paste) {
				anySuccess |=
					cc.readFromClipboard(world.registryAccess(), tag.getCompound(clipboardKey), player, face, world.isClientSide());
				continue;
			}
			CompoundTag compoundTag = new CompoundTag();
			boolean success = cc.writeToClipboard(world.registryAccess(), compoundTag, face);
			anySuccess |= success;
			if (success)
				tag.put(clipboardKey, compoundTag);
		}

		if (smartBE instanceof ClipboardCloneable ccbe) {
			anyValid = true;
			String clipboardKey = ccbe.getClipboardKey();
			if (paste) {
				anySuccess |= ccbe.readFromClipboard(world.registryAccess(), tag.getCompound(clipboardKey), player, face,
					world.isClientSide());
			} else {
				CompoundTag compoundTag = new CompoundTag();
				boolean success = ccbe.writeToClipboard(world.registryAccess(), compoundTag, face);
				anySuccess |= success;
				if (success)
					tag.put(clipboardKey, compoundTag);
			}
		}

		if (!anyValid)
			return InteractionResult.PASS;

		if (world.isClientSide())
			return InteractionResult.SUCCESS;
		if (!anySuccess)
			return InteractionResult.SUCCESS;

		player.displayClientMessage(CreateLang
			.translate(paste ? "clipboard.pasted_to" : "clipboard.copied_from", world.getBlockState(pos)
				.getBlock()
				.getName()
				.withStyle(ChatFormatting.WHITE))
			.style(ChatFormatting.GREEN)
			.component(), true);

		if (!paste) {
			ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
			itemStack.set(AllDataComponents.CLIPBOARD_COPIED_VALUES, tag);
		}
		return InteractionResult.SUCCESS;
	}

}
