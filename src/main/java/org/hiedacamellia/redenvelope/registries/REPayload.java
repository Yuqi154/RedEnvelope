package org.hiedacamellia.redenvelope.registries;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopePacket;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopeStorage;
import org.hiedacamellia.redenvelope.core.network.OpenRedEnvelopePacket;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class REPayload {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");
        registrar.playBidirectional(
                RedEnvelopePacket.TYPE,
                RedEnvelopePacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        RedEnvelopePacket::handleServer,
                        RedEnvelopePacket::handleClient
                )
        );
        registrar.playToClient(
                RedEnvelopeStorage.TYPE,
                RedEnvelopeStorage.STREAM_CODEC,
                RedEnvelopeStorage::handleClient
        );
        registrar.playToServer(
                OpenRedEnvelopePacket.TYPE,
                OpenRedEnvelopePacket.STREAM_CODEC,
                OpenRedEnvelopePacket::handleServer
        );
    }
}
