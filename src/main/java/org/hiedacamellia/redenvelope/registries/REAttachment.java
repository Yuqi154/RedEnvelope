package org.hiedacamellia.redenvelope.registries;

import com.mojang.datafixers.util.Pair;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopeStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class REAttachment {


    protected static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RedEnvelope.MODID);

    public static final Supplier<AttachmentType<RedEnvelopeStorage>> HONGBAO_STORAGE = ATTACHMENTS.register(
            "currency_storage", () -> AttachmentType.builder(()->new RedEnvelopeStorage(new Pair<>(new ArrayList<>(),new ArrayList<>()))).serialize(RedEnvelopeStorage.CODEC).copyOnDeath().build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }

}
