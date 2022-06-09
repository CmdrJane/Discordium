package ru.aiefu.discordium.discord.msgparsers;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import ru.aiefu.discordium.IServerPlayer;

public class DefaultParser implements MsgParser{
    @Override
    public void handleChat(MessageReceivedEvent e, DedicatedServer server, String msg) {
        MutableComponent cp = Component.literal("[Discord] ");
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
                    .append(Component.literal(" >> " + msg).withStyle(ChatFormatting.WHITE));
            for (ServerPlayer p :  server.getPlayerList().getPlayers()){
                if(((IServerPlayer)p).isAcceptingChatType(ChatType.CHAT)){
                    p.sendSystemMessage(cp);
                }
            }
        }
    }

    private MutableComponent getChatComponent(String role, Member member){
        return Component.literal(role + " " + member.getEffectiveName()).setStyle(Style.EMPTY.withColor(member.getColorRaw()));
    }
}
