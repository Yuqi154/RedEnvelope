package org.hiedacamellia.redenvelope.core.network;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.util.REUtil;
import org.hiedacamellia.redenvelope.registries.REAttachment;

import java.util.*;

public record RedEnvelopeStorage(Pair<List<UUID>, List<RedEnvelopePacket>> map) implements CustomPacketPayload {

    public RedEnvelopeStorage copy() {
        return new RedEnvelopeStorage(new Pair<>(new ArrayList<>(map.getFirst()),new ArrayList<>(map.getSecond())));
    }

    public RedEnvelopeStorage(RedEnvelopeStorage storage){
        this(storage.copy().map());
    }

    public static final Codec<RedEnvelopeStorage> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.pair(Codec.list(REUtil.UUID_CODEC),Codec.list(RedEnvelopePacket.CODEC)).fieldOf("map").forGetter(RedEnvelopeStorage::map)
            ).apply(instance, RedEnvelopeStorage::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RedEnvelopeStorage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.pair(Codec.list(REUtil.UUID_CODEC),Codec.list(RedEnvelopePacket.CODEC)))
            , RedEnvelopeStorage::map, RedEnvelopeStorage::new
    );

    public static final Type<RedEnvelopeStorage> TYPE = new CustomPacketPayload.Type<>(RedEnvelope.rl("hong_bao_storage"));

    public static void handleClient(RedEnvelopeStorage message, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    Player player = context.player();
                    player.setData(REAttachment.HONGBAO_STORAGE, new RedEnvelopeStorage(message.map()));
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void add(Player player, RedEnvelopePacket packet){
        var data = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        data.put(packet.uuid(),packet);
        player.setData(REAttachment.HONGBAO_STORAGE,new RedEnvelopeStorage(data));
    }

    public void put(UUID uuid, RedEnvelopePacket packet){
        map.getFirst().add(uuid);
        map.getSecond().add(packet);
    }

    public void remove(UUID uuid){
        int index = map.getFirst().indexOf(uuid);
        if(index==-1) return;
        map.getFirst().remove(index);
        map.getSecond().remove(index);
    }

    public RedEnvelopePacket get(UUID uuid){
        int index = map.getFirst().indexOf(uuid);
        if(index==-1) return null;
        return map.getSecond().get(index);
    }

    public void replace(UUID uuid, RedEnvelopePacket packet){
        int index = map.getFirst().indexOf(uuid);
        if(index==-1) return;
        map.getSecond().set(index,packet);
    }

    public List<RedEnvelopePacket> values(){
        return map.getSecond();
    }

    public List<UUID> keys(){
        return map.getFirst();
    }
}
