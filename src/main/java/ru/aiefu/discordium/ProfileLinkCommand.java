package ru.aiefu.discordium;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import ru.aiefu.discordium.config.ConfigManager;
import ru.aiefu.discordium.config.LinkedProfile;
import ru.aiefu.discordium.discord.DiscordConfig;
import ru.aiefu.discordium.discord.DiscordLink;
import ru.aiefu.discordium.discord.VerificationData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class ProfileLinkCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("discord").then(Commands.literal("link").executes(context -> generateLinkCode(context.getSource()))));
        dispatcher.register(Commands.literal("discord").then(Commands.literal("unlink").executes(context -> {
            try {
                return unlink(context.getSource());
            } catch (IOException e) {
                e.printStackTrace();
                context.getSource().sendFailure(new TextComponent("IO Exception occurred during this operation"));
            }
            return 0;
        })));
        dispatcher.register(Commands.literal("discord").then(Commands.literal("enable-mention-notification")
                .then(Commands.argument("soundstate", BoolArgumentType.bool()).executes(context ->
                        switchNotifySoundState(context.getSource(), BoolArgumentType.getBool(context,"soundstate"))))));
    }

    private static int generateLinkCode(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String uuid = player.getStringUUID();
        DiscordConfig cfg = DiscordLink.config;
        if(DiscordLink.pendingPlayersUUID.containsKey(uuid)){
            int code = DiscordLink.pendingPlayersUUID.get(uuid);
            source.sendSuccess(new TextComponent(cfg.cLinkMsg1).withStyle(ChatFormatting.WHITE).append(new TextComponent(String.valueOf(code)).withStyle(style -> style.withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(code))))).append(cfg.cLinkMsg2).withStyle(ChatFormatting.WHITE), false);
            return 0;
        }
        Random r = player.getRandom();
        int authCode = r.nextInt(100_000, 1_000_000);
        while (DiscordLink.pendingPlayers.containsKey(authCode)) {
            authCode = r.nextInt(100_000, 1_000_000);
        }
        DiscordLink.pendingPlayers.put(authCode, new VerificationData(player.getScoreboardName(), uuid, DiscordLink.currentTime + 600_000));
        DiscordLink.pendingPlayersUUID.put(uuid, authCode);
        int finalAuthCode = authCode;
        source.sendSuccess(new TextComponent(cfg.cLinkMsg1.replaceAll("\\{botname}", DiscordLink.botName)).withStyle(ChatFormatting.WHITE).append(new TextComponent(String.valueOf(authCode)).withStyle(style -> style.withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(finalAuthCode))))).append(cfg.cLinkMsg2.replaceAll("\\{botname}", DiscordLink.botName)).withStyle(ChatFormatting.WHITE), false);
        return 0;
    }

    private static int unlink(CommandSourceStack source) throws CommandSyntaxException, IOException {
        String id = source.getPlayerOrException().getStringUUID();
        LinkedProfile profile = ConfigManager.getLinkedProfile(id);
        if(profile != null){
            DiscordLink.linkedPlayersByDiscordId.remove(profile.discordId);
            DiscordLink.linkedPlayers.remove(id);
            Files.delete(Paths.get(String.format("./config/discord-chat/linked-profiles/%s.json", id)));
            source.sendSuccess(new TextComponent(DiscordLink.config.codeUnlinkMsg), false);
        } else source.sendFailure(new TextComponent(DiscordLink.config.codeUnlinkFail));
        return 0;
    }

    private static int switchNotifySoundState(CommandSourceStack source, boolean state) throws CommandSyntaxException {
        IServerPlayer player = (IServerPlayer) source.getPlayerOrException();
        player.setNotifyState(state);
        source.sendSuccess(new TextComponent(DiscordLink.config.mentionState.replaceAll("\\{state}", String.valueOf(state))), false);
        return 0;
    }
}
