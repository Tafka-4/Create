package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ClipboardValueSettingsInteractionHandler {

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
				itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, content.setType(ClipboardType.WRITTEN)
					.setPages(listTo));
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
