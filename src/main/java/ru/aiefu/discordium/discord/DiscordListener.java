package ru.aiefu.discordium.discord;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.discordium.config.ConfigManager;
import ru.aiefu.discordium.config.LinkedProfile;
import ru.aiefu.discordium.discord.msgparsers.DefaultParser;
import ru.aiefu.discordium.discord.msgparsers.MentionParser;
import ru.aiefu.discordium.discord.msgparsers.MsgParser;

import java.util.List;
import java.util.UUID;

public class DiscordListener extends ListenerAdapter {

    private final MsgParser chatHandler;

    public DiscordListener(){
        if(DiscordLink.config.enableMentions){
            chatHandler = new MentionParser();
        } else {
            chatHandler = new DefaultParser();
        }
    }

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
                tryVerify(e, server);
            }
        }
    }

    private void handleChatInput(MessageReceivedEvent e, DedicatedServer server){
        String msg = e.getMessage().getContentRaw();
        if(!msg.isEmpty()) {
            if (!msg.startsWith("!@") && msg.startsWith("!")) {
                handleCommandInput(e, server, msg.substring(1));
            } else {
                chatHandler.handleChat(e, server, msg);
            }
        }
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

    private void tryVerify(MessageReceivedEvent e, DedicatedServer server){
        String msg = e.getMessage().getContentRaw();
        if(msg.length() == 6 && msg.matches("[0-9]+")){
            int code = Integer.parseInt(msg);
            VerificationData data = DiscordLink.pendingPlayers.get(code);
            if(data != null){
                String id = data.uuid();
                String discordId = e.getAuthor().getId();
                LinkedProfile profile = new LinkedProfile(data.name(), id, discordId);
                ConfigManager.saveLinkedProfile(profile);
                DiscordLink.pendingPlayersUUID.remove(id);
                DiscordLink.pendingPlayers.remove(code);
                if(!DiscordLink.config.forceLinking){
                    ServerPlayer player = server.getPlayerList().getPlayer(UUID.fromString(id));
                    if(player != null){
                        DiscordLink.linkedPlayers.put(id, profile);
                        DiscordLink.linkedPlayersByDiscordId.put(profile.discordId, player.getGameProfile().getName());
                    }
                }
                e.getChannel().sendMessage(DiscordLink.config.successfulVerificationMsg
                        .replaceAll("\\{username}", data.name()).replaceAll("\\{uuid}", id)).queue();
                DiscordLink.logger.info(String.format("Linked game profile %s to discord profile %s", data.name(), e.getAuthor().getName()));
            }
        }
    }
    
}
