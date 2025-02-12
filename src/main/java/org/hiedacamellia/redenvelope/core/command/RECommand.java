package org.hiedacamellia.redenvelope.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class RECommand {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("hongbao")
                 .then(Commands.literal("luck").then(Commands.argument("count",IntegerArgumentType.integer(1)).then(Commands.argument("itemcount",IntegerArgumentType.integer(1)).executes(CommandExecutes::sendLuckWithItemCount)).executes(CommandExecutes::sendLuckWithoutItemCount))
                ).then(Commands.literal("common").then(Commands.argument("count",IntegerArgumentType.integer(1)).then(Commands.argument("single",IntegerArgumentType.integer(1)).executes(CommandExecutes::sendCommon)))
                ).then(Commands.literal("spell").then(Commands.argument("count",IntegerArgumentType.integer(1))
                                .then(Commands.argument("itemcount",IntegerArgumentType.integer(1))
                                        .then(Commands.argument("words",StringArgumentType.greedyString())
                                                .executes(CommandExecutes::sendSpellWithItemCount)))
                                .then(Commands.argument("words",StringArgumentType.greedyString())
                                        .executes(CommandExecutes::sendSpellWithoutItemCount)))
                ).then(Commands.literal("open").then(Commands.argument("uuid", StringArgumentType.string()).executes(CommandExecutes::open))
                ).then(Commands.literal("withdraw").then(Commands.argument("uuid", StringArgumentType.string()).executes(CommandExecutes::withdraw))
                ).then(Commands.literal("query").then(Commands.argument("uuid", StringArgumentType.string()).executes(CommandExecutes::queryWithUUID)).then(Commands.argument("all", StringArgumentType.string()).executes(CommandExecutes::queryWithUUID)).executes(CommandExecutes::queryList)
                )
        );
    }
}
