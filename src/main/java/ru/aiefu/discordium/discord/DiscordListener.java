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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.discordium.config.ConfigManager;
import ru.aiefu.discordium.config.LinkedProfile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                tryVerify(e, server);
            }
        }
    }

    private final Pattern pattern = Pattern.compile("(?<=!@).+?(?=!@|$|\\s)");
    private final Pattern pattern2 = Pattern.compile("(?<=<@!).+?(?=>)");

    private void handleChatInput(MessageReceivedEvent e, DedicatedServer server){
        String msg = e.getMessage().getContentRaw();
        if(!msg.isEmpty()) {
            if (!msg.startsWith("!@") && msg.startsWith("!")) {
                handleCommandInput(e, server, msg.substring(1));
            } else {
                Member member = e.getMember();
                Set<String> playerNames = new HashSet<>();
                if(msg.contains("!@")){
                    playerNames = pattern.matcher(msg).results().map(matchResult -> matchResult.group(0).toLowerCase()).collect(Collectors.toSet());
                }
                if(msg.contains("<@!")){
                    List<String> ids = pattern2.matcher(msg).results().map(matchResult -> matchResult.group(0)).collect(Collectors.toList());
                    for (String s : ids){
                        String name = DiscordLink.linkedPlayersByDiscordId.get(s);
                        if(name != null){
                            playerNames.add(name.toLowerCase());
                            msg = msg.replaceAll("<@!" + s +">", "!@"+name);
                        } else {
                            Member m = e.getGuild().getMemberById(s);
                            if(m != null){
                                msg = msg.replaceAll("<@!" +s +">", "@" + m.getEffectiveName());
                            } else msg = msg.replaceAll("<@!" +s + ">", "@Unknown Discord User");
                        }
                    }
                }
                if(member != null) {
                    String role = member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName();
                    MutableComponent cp = new TextComponent("[Discord] ").withStyle(style -> style.withColor(6955481))
                            .append(getChatComponent(role, member))
                            .append(new TextComponent(" >> " + msg).withStyle(ChatFormatting.WHITE));
                    if(!playerNames.isEmpty()){
                        MutableComponent cp2 = new TextComponent("[Discord] ").withStyle(Style.EMPTY.withColor(6955481))
                                .append(getChatComponent(role, member));
                        for(ServerPlayer player : server.getPlayerList().getPlayers()){
                            if(!playerNames.contains(player.getScoreboardName().toLowerCase())){
                                player.sendMessage(cp, ChatType.CHAT, Util.NIL_UUID);
                            } else {
                                player.sendMessage(cp2.append(new TextComponent(" >> " + msg.replaceAll("(?i)!@" + player.getScoreboardName(),"§a$0§r")).withStyle(ChatFormatting.WHITE)), ChatType.CHAT, Util.NIL_UUID);
                                player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 1.0F, 1.0F);
                            }
                        }
                    } else server.getPlayerList().broadcastMessage(cp, ChatType.CHAT, Util.NIL_UUID);
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
