package com.simibubi.create.foundation.gui.menu;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface MenuOpeningData {
	StreamCodec<RegistryFriendlyByteBuf, Object> STREAM_CODEC = StreamCodec.of(MenuOpeningData::encode, MenuOpeningData::decode);

	void write(RegistryFriendlyByteBuf buffer);

	private static void encode(RegistryFriendlyByteBuf buffer, Object data) {
		if (data instanceof MenuOpeningData openingData) {
			openingData.write(buffer);
			return;
		}
		if (data instanceof BlockPos blockPos) {
			buffer.writeBlockPos(blockPos);
			return;
		}
		if (data instanceof ItemStack stack) {
			ItemStack.STREAM_CODEC.encode(buffer, stack);
			return;
		}
		if (data instanceof RegistryFriendlyByteBuf extraData) {
			buffer.writeBytes(extraData, extraData.readerIndex(), extraData.readableBytes());
			return;
		}
		throw new EncoderException("Unsupported menu opening data: " + data);
	}

	private static RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf buffer) {
		ByteBuf copy = Unpooled.buffer(buffer.readableBytes());
		copy.writeBytes(buffer, buffer.readableBytes());
		return new RegistryFriendlyByteBuf(copy, buffer.registryAccess());
	}
}
