package ru.aiefu.discordium.discord.msgparsers;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.dedicated.DedicatedServer;

public class DefaultParser implements MsgParser{
    @Override
    public void handleChat(MessageReceivedEvent e, DedicatedServer server, String msg) {
        TextComponent cp = new TextComponent("[Discord] ");
        Style s = cp.getStyle();
        Member member = e.getMember();
        if(msg.contains("<@")){
            msg = msg.replaceAll("<@.*?>", "");
            if(msg.isEmpty()){
                return;
            }
        }
        if(member != null) {
            String role = member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName();
            cp.withStyle(s.withColor(6955481))
                    .append(getChatComponent(role, member))
                    .append(new TextComponent(" >> " + msg).withStyle(ChatFormatting.WHITE));
            server.getPlayerList().broadcastMessage(cp, ChatType.CHAT, Util.NIL_UUID);
        }
    }

    private MutableComponent getChatComponent(String role, Member member){
        return new TextComponent(role + " " + member.getEffectiveName()).setStyle(Style.EMPTY.withColor(member.getColorRaw()));
    }
}
