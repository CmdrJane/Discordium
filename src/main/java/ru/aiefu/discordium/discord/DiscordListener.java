package ru.aiefu.discordium.discord;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.discordium.config.ConfigManager;
import ru.aiefu.discordium.config.LinkedProfile;

import java.util.List;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        DedicatedServer server = DiscordLink.server;
        if (e.getAuthor() != e.getJDA().getSelfUser() && !e.getAuthor().isBot() && server != null){
            String channelId = e.getChannel().getId();
            if(channelId.equals(DiscordLink.config.chatChannelId)){
                handleChatInput(e, server);
            } else if(channelId.equals(DiscordLink.config.consoleChannelId)){
                handleConsoleInput(e, server);
            } else if(DiscordLink.config.enableAccountLinking && e.getChannelType() == ChannelType.PRIVATE){
                tryVerify(e);
            }
        }
    }



    private void handleChatInput(MessageReceivedEvent e, DedicatedServer server){
        String msg = e.getMessage().getContentRaw();
        if(!msg.isEmpty()) {
            if (msg.startsWith("!")) {
                handleCommandInput(e, server, msg.substring(1));
            } else {
                TextComponent cp = new TextComponent("[Discord] ");
                Style s = cp.getStyle();
                Member member = e.getMember();
                if(member != null) {
                    String role = member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName();
                    cp.withStyle(s.withColor(6955481))
                            .append(getChatComponent(role, member))
                            .append(new TextComponent(" >> " + msg).withStyle(ChatFormatting.WHITE));
                    server.getPlayerList().broadcastMessage(cp, ChatType.CHAT, Util.NIL_UUID);
                }
            }
        }
    }

    private MutableComponent getChatComponent(String role, Member member){
        return new TextComponent(role + " " + member.getEffectiveName()).setStyle(Style.EMPTY.withColor(member.getColorRaw()));
    }

    private void handleConsoleInput(MessageReceivedEvent e, DedicatedServer server){
        String msg = e.getMessage().getContentRaw();
        DiscordLink.logger.info("Discord user " +e.getAuthor().getName() + " running command " + msg);
        server.execute(() -> server.handleConsoleInput(msg, server.createCommandSourceStack()));
    }

    private void handleCommandInput(MessageReceivedEvent e, DedicatedServer server, String command){
        if(command.startsWith("list")){
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            if(players.isEmpty()){
                DiscordLink.sendMessage(e.getChannel(), DiscordLink.config.noPlayersMsg);
                return;
            }
            StringBuilder sb = new StringBuilder(DiscordLink.config.onlinePlayersMsg);
            for (ServerPlayer p : players){
                sb.append(p.getScoreboardName()).append(", ");
            }
            DiscordLink.sendMessage(e.getChannel(),sb.substring(0, sb.length() - 2));
        }
    }

    private void tryVerify(MessageReceivedEvent e){
        String msg = e.getMessage().getContentRaw();
        if(msg.matches("[0-9]+")){
            int code = Integer.parseInt(msg);
            VerificationData data = DiscordLink.pendingPlayers.get(code);
            if(data != null){
                String id = data.uuid();
                String discordId = e.getAuthor().getId();
                ConfigManager.saveLinkedProfile(new LinkedProfile(data.name(), id, discordId));
                DiscordLink.pendingPlayersUUID.remove(id);
                DiscordLink.pendingPlayers.remove(code);
                e.getChannel().sendMessage(DiscordLink.config.successfulVerificationMsg
                        .replaceAll("\\{username}", data.name()).replaceAll("\\{uuid}", id)).queue();
                DiscordLink.logger.info(String.format("Linked game profile %s to discord profile %s", data.name(), e.getAuthor().getName()));
            }
        }
    }
    
}
