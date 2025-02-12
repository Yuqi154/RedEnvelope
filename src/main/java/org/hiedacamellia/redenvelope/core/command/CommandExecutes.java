package org.hiedacamellia.redenvelope.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.hiedacamellia.redenvelope.RedEnvelope;
import org.hiedacamellia.redenvelope.core.network.OpenRedEnvelopePacket;
import org.hiedacamellia.redenvelope.core.network.RedEnvelopePacket;
import org.hiedacamellia.redenvelope.core.util.REUtil;
import org.hiedacamellia.redenvelope.registries.REAttachment;

import java.util.Map;
import java.util.UUID;

public class CommandExecutes {

    public static int sendLuckWithItemCount(CommandContext<CommandSourceStack> context) {
        int itemcount = IntegerArgumentType.getInteger(context, "itemcount");
        return sendLuck(context,itemcount);
    }
    public static int sendLuckWithoutItemCount(CommandContext<CommandSourceStack> context) {
        return sendLuck(context,-1);
    }
    public static int sendLuck(CommandContext<CommandSourceStack> context,int itemcount) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        ItemStack mainHandItem = player.getMainHandItem();
        if(mainHandItem.isEmpty()) return 0;
        int count = IntegerArgumentType.getInteger(context, "count");
        if(mainHandItem.getCount()< itemcount && itemcount!=-1) return 0;
        ItemStack copy = mainHandItem.copy();
        if(itemcount != -1)
            copy.setCount(itemcount);
        else
            itemcount = copy.getCount();

        RedEnvelopePacket luck = REUtil.create(player, copy, "luck", count);
        RedEnvelope.LOGGER.debug("send luck packet");
        RedEnvelopePacket._handleServer(luck,player);
        mainHandItem.shrink(itemcount);
        return 1;
    }

    public static int sendSpellWithItemCount(CommandContext<CommandSourceStack> context) {
        int itemcount = IntegerArgumentType.getInteger(context, "itemcount");
        return sendSpell(context,itemcount);
    }
    public static int sendSpellWithoutItemCount(CommandContext<CommandSourceStack> context) {
        return sendSpell(context,-1);
    }
    public static int sendSpell(CommandContext<CommandSourceStack> context,int itemcount) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        ItemStack mainHandItem = player.getMainHandItem();
        if(mainHandItem.isEmpty()) return 0;
        int count = IntegerArgumentType.getInteger(context, "count");
        if(mainHandItem.getCount()< itemcount && itemcount!=-1) return 0;
        ItemStack copy = mainHandItem.copy();
        if(itemcount != -1)
            copy.setCount(itemcount);

        RedEnvelopePacket spell = REUtil.create(player, copy, "spell", count);
        spell.extraData().putString("spell",StringArgumentType.getString(context, "words"));
        RedEnvelope.LOGGER.debug("send spell packet");
        RedEnvelopePacket._handleServer(spell,player);
        mainHandItem.shrink(itemcount);
        return 1;
    }

    public static int sendCommon(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        ItemStack mainHandItem = player.getMainHandItem();
        if(mainHandItem.isEmpty()) return 0;
        int count = IntegerArgumentType.getInteger(context, "count");
        int single = IntegerArgumentType.getInteger(context, "single");
        if(mainHandItem.getCount()< single*count) {
            player.sendSystemMessage(Component.translatable("chat.red_envelope.notenough"));
            return 0;
        }
        ItemStack copy = mainHandItem.copy();
        copy.setCount(single);
        RedEnvelopePacket common = REUtil.create(player, copy, "common", count);
        common.extraData().putInt("single",single);
        RedEnvelope.LOGGER.debug("send common packet");
        RedEnvelopePacket._handleServer(common,player);
        mainHandItem.shrink(single*count);
        return 1;
    }

    public static int open(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        String uuid = StringArgumentType.getString(context, "uuid");
        OpenRedEnvelopePacket._handleServer(new OpenRedEnvelopePacket(UUID.fromString(uuid)),player);
        return 1;
    }

    public static int withdraw(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        String uuid = StringArgumentType.getString(context, "uuid");
        var copy = player.getData(REAttachment.HONGBAO_STORAGE).copy();
        REUtil.withdraw(player.server,copy.get(UUID.fromString(uuid)),2);
        return 1;
    }

    public static int queryWithUUID(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        String uuid = StringArgumentType.getString(context, "uuid");
        REUtil.query(player,UUID.fromString(uuid));
        return 1;
    }

    public static int query(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        REUtil.query(player);
        return 1;
    }

    public static int queryList(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if(player == null) return 0;
        REUtil.queryList(player);
        return 1;
    }

}
