package org.hiedacamellia.redenvelope.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;


public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue TTL = BUILDER.comment("The time to live of the red envelope")
            .comment("\u7ea2\u5305\u7684\u5b58\u6d3b\u65f6\u95f4")
            .defineInRange("ttl", 1200, 1, 24000);

    public static final ModConfigSpec SPEC = BUILDER.build();

}
