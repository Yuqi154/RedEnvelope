package org.hiedacamellia.redenvelope.core.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopePacket;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopeStorage;
import org.hiedacamellia.redenvelope.registries.REAttachment;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class REUtil {

    public static final Codec<UUID> UUID_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.fieldOf("most").forGetter(UUID::getMostSignificantBits),
                    Codec.LONG.fieldOf("least").forGetter(UUID::getLeastSignificantBits)
            ).apply(instance, UUID::new)
    );
    public static final MapCodec<UUID> UUID_MAP_CODEC = MapCodec.assumeMapUnsafe(UUID_CODEC);

    public static RedEnvelopePacket create(ServerPlayer player, ItemStack itemStack, String hbtype, int count) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putUUID("from", player.getUUID());
        compoundTag.putBoolean("opened", false);
        compoundTag.putLong("time", (System.currentTimeMillis() / 1000));
        return new RedEnvelopePacket(itemStack, hbtype, count, UUID.randomUUID(), compoundTag);
    }


    public static void receiveMessage(Player player, RedEnvelopePacket packet) {
        CompoundTag compoundTag = packet.extraData();
        UUID from = compoundTag.getUUID("from");
        Component fromDisplayName = player.level().getServer().getPlayerList().getPlayer(from).getDisplayName();
        MutableComponent component = Component.translatable("chat.red_envelope.receive." + packet.hbtype(), fromDisplayName);
        if(!packet.hbtype().equals("spell"))
            component.append(Component.translatable("chat.red_envelope.open")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hongbao open " + packet.uuid())).withUnderlined(true))
                    .withStyle(ChatFormatting.GREEN));
        else
            component.append(Component.literal("[").append(packet.extraData().getString("spell")).append("]").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.translatable("chat.red_envelope.send")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, packet.extraData().getString("spell"))).withUnderlined(true))
                    .withStyle(ChatFormatting.GREEN));

        player.sendSystemMessage(component);
        RedEnvelope.LOGGER.debug("receive red envelope");
    }

    public static void open(ServerPlayer player, UUID uuid) {
        var data = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        RedEnvelopePacket redEnvelopePacket = data.get(uuid);
        if (redEnvelopePacket == null) {
            return;
        }
        if (redEnvelopePacket.extraData().getBoolean("opened")) {
            player.sendSystemMessage(Component.translatable("chat.red_envelope.opened.done"));
            return;
        }
        CompoundTag compoundTag = redEnvelopePacket.extraData();
        UUID from = compoundTag.getUUID("from");
        ServerPlayer serverPlayer = player.level().getServer().getPlayerList().getPlayer(from);
        var map = serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy();
        RedEnvelopePacket real = map.get(uuid);
        RedEnvelopePacket left = open(player, real);
        if (left != null) {
            if (left.count() >= 1) {
                map.replace(uuid, left);
            } else {
                map.remove(uuid);
            }
        } else {
            destroy(player.server, uuid);
        }
        if (left != null)
            if (serverPlayer.getUUID() != player.getUUID()) {
                player.setData(REAttachment.HONGBAO_STORAGE, new RedEnvelopeStorage(data));
                left.extraData().putBoolean("opened", true);
                data.replace(uuid, left);
            } else {
                left.extraData().putBoolean("opened", true);
                map.replace(uuid, left);
            }
        serverPlayer.setData(REAttachment.HONGBAO_STORAGE, new RedEnvelopeStorage(map));
    }

    public static void query(ServerPlayer player) {
        var copy = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        copy.keys().forEach(uuid -> query(player, uuid));
    }

    public static void queryList(ServerPlayer player) {
        var copy = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        copy.keys().forEach(uuid -> {
            player.sendSystemMessage(Component.literal(uuid.toString()).append(
                    Component.translatable("chat.red_envelope.action.query")
                            .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hongbao query " + uuid)).withUnderlined(true))
            ));
        });
    }


    public static void query(ServerPlayer player, UUID uuid) {

        RedEnvelope.LOGGER.debug("query red_envelope " + uuid);

        var data = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        RedEnvelopePacket redEnvelopePacket = data.get(uuid);
        if (redEnvelopePacket == null) {
            RedEnvelope.LOGGER.debug("red_envelope not found");
            return;
        }
        CompoundTag compoundTag = redEnvelopePacket.extraData();
        UUID from = compoundTag.getUUID("from");
        boolean opened = compoundTag.getBoolean("opened");
        ServerPlayer serverPlayer = player.level().getServer().getPlayerList().getPlayer(from);
        var map = serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy();
        RedEnvelopePacket real = map.get(uuid);
        MutableComponent component = Component.translatable("chat.red_envelope.query." + real.hbtype());

        MutableComponent returned = Component.empty();
        ItemStack itemStack = real.itemStack();
        returned.append(String.valueOf(itemStack.getCount())).append("x ").append(itemStack.getDisplayName());

        component.append(Component.translatable("chat.red_envelope.query.from", serverPlayer.getDisplayName(), real.count(), returned).append(" "));
        if (opened)
            component.append(Component.translatable("chat.red_envelope.query.opened")
                    .withStyle(ChatFormatting.AQUA));
        else
            component.append(Component.translatable("chat.red_envelope.query.unopened")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hongbao open " + real.uuid())).withUnderlined(true))
                    .withStyle(ChatFormatting.GREEN));

        if (player.getUUID() == serverPlayer.getUUID())
            component.append(Component.translatable("chat.red_envelope.action.withdraw")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hongbao withdraw " + uuid)).withUnderlined(true)
                    ));


        player.sendSystemMessage(component);
    }

    public static void destroy(ServerPlayer player, UUID uuid) {
        var data = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        data.remove(uuid);
        player.setData(REAttachment.HONGBAO_STORAGE, new RedEnvelopeStorage(data));
    }

    public static void destroy(MinecraftServer server, UUID uuid) {
        server.getPlayerList().getPlayers().forEach(player -> destroy(player, uuid));
    }

    public static void withdraw(MinecraftServer server, RedEnvelopePacket packet, int type) {
        CompoundTag compoundTag = packet.extraData();
        UUID from = compoundTag.getUUID("from");
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(from);
        var map = serverPlayer.getData(REAttachment.HONGBAO_STORAGE).copy();
        RedEnvelopePacket real = map.get(packet.uuid());
        MutableComponent returned = Component.empty();
        ItemStack itemStack = real.itemStack();
        if(Objects.equals(real.hbtype(), "common")){
            itemStack.setCount(real.count()*real.extraData().getInt("single"));
        }
        serverPlayer.addItem(itemStack.copy());
        returned.append(String.valueOf(itemStack.getCount())).append("x ").append(itemStack.getDisplayName());
        switch (type) {
            case 1:
                serverPlayer.sendSystemMessage(Component.translatable("chat.red_envelope.returned", returned));
                break;
            case 2:
                serverPlayer.sendSystemMessage(Component.translatable("chat.red_envelope.withdraw", returned));
                break;
            case 3:
                break;
        }
        destroy(server, real.uuid());
    }

    @Nullable
    private static RedEnvelopePacket open(ServerPlayer player, RedEnvelopePacket packet) {
        String hbtype = packet.hbtype();
        RedEnvelopePacket left = packet;
        MutableComponent got = Component.empty();
        if (hbtype.equals("luck")||hbtype.equals("spell")) {
            int count = packet.count();
            if (count == 1) {
                ItemStack itemStack = packet.itemStack();
                player.addItem(itemStack.copy());
                left = null;
                got.append(String.valueOf(itemStack.getCount())).append("x ").append(itemStack.getDisplayName());
            } else {
                ItemStack itemStack = packet.itemStack();
                int itemStackCount = itemStack.getCount();
                RandomSource random = player.level().getRandom();
                int i = random.nextIntBetweenInclusive(1, itemStackCount - count);
                ItemStack copy = itemStack.copy();
                copy.setCount(i);
                itemStack.shrink(i);
                player.addItem(copy);
                left = new RedEnvelopePacket(itemStack, hbtype, count - 1, packet.uuid(), packet.extraData());
                got.append(String.valueOf(i)).append("x ").append(itemStack.getDisplayName());
            }
        }
        if (hbtype.equals("common")) {
            int count = packet.count();
            int single = packet.extraData().getInt("single");
            ItemStack itemStack = packet.itemStack();
            ItemStack copy = itemStack.copy();
            player.addItem(copy);
            left = new RedEnvelopePacket(itemStack, hbtype, count - 1, packet.uuid(), packet.extraData());
            got.append(String.valueOf(single)).append("x ").append(itemStack.getDisplayName());
        }
        player.sendSystemMessage(Component.translatable("chat.red_envelope.opened." + packet.hbtype(), got));
        return left;
    }
}
