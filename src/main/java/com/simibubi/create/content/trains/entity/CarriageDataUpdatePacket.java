package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class CarriageDataUpdatePacket implements ClientboundPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, CarriageDataUpdatePacket> STREAM_CODEC =
		StreamCodec.of((b, v) -> v.write(b), CarriageDataUpdatePacket::new);

	private final int entity;
	private final CarriageSyncData data;

	public CarriageDataUpdatePacket(CarriageContraptionEntity entity) {
		this.entity = entity.getId();
		this.data = entity.carriageData;
	}

	public CarriageDataUpdatePacket(FriendlyByteBuf buf) {
		this.entity = buf.readVarInt();
		this.data = new CarriageSyncData();
		this.data.read(buf);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(entity);
		this.data.write(buffer);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entity = player.clientLevel.getEntity(this.entity);
		if (entity instanceof CarriageContraptionEntity carriage) {
			carriage.onCarriageDataUpdate(this.data);
		} else {
			Create.LOGGER.error("Invalid CarriageDataUpdatePacket for non-carriage entity: " + entity);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CARRIAGE_DATA_UPDATE;
	}
}
