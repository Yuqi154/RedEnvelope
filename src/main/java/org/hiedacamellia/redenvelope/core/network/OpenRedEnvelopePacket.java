package org.hiedacamellia.redenvelope.core.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.util.REUtil;

import java.util.UUID;

public record OpenRedEnvelopePacket(UUID uuid) implements CustomPacketPayload {

    public static final Codec<OpenRedEnvelopePacket> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    REUtil.UUID_CODEC.fieldOf("uuid").forGetter(OpenRedEnvelopePacket::uuid)
            ).apply(instance, OpenRedEnvelopePacket::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRedEnvelopePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(REUtil.UUID_CODEC), OpenRedEnvelopePacket::uuid,
            OpenRedEnvelopePacket::new
    );

    public static final Type<OpenRedEnvelopePacket> TYPE = new CustomPacketPayload.Type<>(RedEnvelope.rl("open_hong_bao"));

    public static void handleServer(OpenRedEnvelopePacket message, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if(context.player() instanceof ServerPlayer serverPlayer) _handleServer(message, serverPlayer);
                }
        );
    }
    public static void _handleServer(OpenRedEnvelopePacket message, ServerPlayer serverPlayer) {
        REUtil.open(serverPlayer,message.uuid());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
