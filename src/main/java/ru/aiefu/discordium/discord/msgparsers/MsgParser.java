package ru.aiefu.discordium.discord.msgparsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.dedicated.DedicatedServer;

public interface MsgParser {
    void handleChat(MessageReceivedEvent e, DedicatedServer server, String msg);
}
