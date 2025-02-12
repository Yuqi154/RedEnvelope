package org.hiedacamellia.redenvelope.core.event;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.hiedacamellia.redenvelope.core.config.ServerConfig;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopePacket;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopeStorage;
import org.hiedacamellia.redenvelope.core.util.REUtil;
import org.hiedacamellia.redenvelope.registries.REAttachment;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber
public class REPlayerEvent {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        if(player instanceof ServerPlayer serverPlayer)
            PacketDistributor.sendToPlayer(serverPlayer,new RedEnvelopeStorage(serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy()));
    }

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event){
        Player player = event.getEntity();
        if(player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy().values().forEach(
                    redEnvelopePacket -> {
                        if(redEnvelopePacket.extraData().getUUID("from")==serverPlayer.getUUID())
                            REUtil.withdraw(serverPlayer.server,redEnvelopePacket,3);
                    }
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        if(player instanceof ServerPlayer serverPlayer){
            long gameTime = serverPlayer.level().getGameTime();
            if(gameTime % 100 ==0){
                var map = serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy();
                map.values().forEach(hongBaoPacket -> {
                    if(hongBaoPacket.extraData().getLong("time") + ServerConfig.TTL.get() < System.currentTimeMillis()/1000){
                        REUtil.withdraw(serverPlayer.server,hongBaoPacket,1);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event){
        ServerPlayer player = event.getPlayer();
        String string = event.getMessage().getString();
        player.getData(REAttachment.HONGBAO_STORAGE).copy().values().forEach(
                redEnvelopePacket -> {
                    if(!redEnvelopePacket.hbtype().equals("spell")) return;
                    if(redEnvelopePacket.extraData().getString("spell").equals(string)){
                        REUtil.open(player,redEnvelopePacket.uuid());
                    }
                }
        );
    }

}
