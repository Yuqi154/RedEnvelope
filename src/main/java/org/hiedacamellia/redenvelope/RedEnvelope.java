package org.hiedacamellia.redenvelope;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.hiedacamellia.redenvelope.core.config.ServerConfig;
import org.hiedacamellia.redenvelope.registries.REAttachment;
import org.slf4j.Logger;

@Mod(RedEnvelope.MODID)
public class RedEnvelope
{
    public static final String MODID = "red_envelope";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RedEnvelope(IEventBus modEventBus, ModContainer modContainer)
    {
        REAttachment.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC);
    }

    public static ResourceLocation rl(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
