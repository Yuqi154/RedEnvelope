package org.hiedacamellia.redenvelope.core.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.util.REUtil;

import java.util.List;
import java.util.UUID;

public record RedEnvelopePacket(ItemStack itemStack, String hbtype, int count, UUID uuid , CompoundTag extraData) implements CustomPacketPayload {

    public RedEnvelopePacket copy(){
        return new RedEnvelopePacket(itemStack.copy(),hbtype,count,uuid,extraData.copy());
    }

    public static final Codec<RedEnvelopePacket> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemStack.CODEC.fieldOf("itemStack").forGetter(RedEnvelopePacket::itemStack),
                    Codec.STRING.fieldOf("type").forGetter(RedEnvelopePacket::hbtype),
                    Codec.INT.fieldOf("count").forGetter(RedEnvelopePacket::count),
                    REUtil.UUID_CODEC.fieldOf("uuid").forGetter(RedEnvelopePacket::uuid),
                    CompoundTag.CODEC.fieldOf("extraData").forGetter(RedEnvelopePacket::extraData)
            ).apply(instance, RedEnvelopePacket::new)
    );
    public static final MapCodec<RedEnvelopePacket> MAP_CODEC = MapCodec.assumeMapUnsafe(CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, RedEnvelopePacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, RedEnvelopePacket::itemStack,
            ByteBufCodecs.STRING_UTF8, RedEnvelopePacket::hbtype,
            ByteBufCodecs.INT, RedEnvelopePacket::count,
            ByteBufCodecs.fromCodec(REUtil.UUID_CODEC), RedEnvelopePacket::uuid,
            ByteBufCodecs.fromCodec(CompoundTag.CODEC), RedEnvelopePacket::extraData,
            RedEnvelopePacket::new
    );

    public static final Type<RedEnvelopePacket> TYPE = new CustomPacketPayload.Type<>(RedEnvelope.rl("hong_bao"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RedEnvelopePacket message, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if(context.player() instanceof ServerPlayer serverPlayer)_handleServer(message, serverPlayer);
                }
        );
    }
    public static void _handleServer(RedEnvelopePacket message, ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        PlayerList playerList = server.getPlayerList();
        List<ServerPlayer> players = playerList.getPlayers();
        players.forEach(serverPlayer -> RedEnvelopeStorage.add(serverPlayer,message.copy()));
        players.forEach(serverPlayer -> REUtil.receiveMessage(serverPlayer,message.copy()));
        if(server.isDedicatedServer())
            PacketDistributor.sendToAllPlayers(message);
    }
    public static void handleClient(RedEnvelopePacket message, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    Player player = context.player();
                    RedEnvelopeStorage.add(player,message.copy());
                }
        );
    }
}
