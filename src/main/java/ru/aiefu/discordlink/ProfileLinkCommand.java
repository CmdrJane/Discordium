package ru.aiefu.discordlink;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import ru.aiefu.discordlink.discord.DiscordConfig;
import ru.aiefu.discordlink.discord.DiscordLink;
import ru.aiefu.discordlink.discord.VerificationData;

import java.util.Random;

public class ProfileLinkCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("discord").then(Commands.literal("link").executes(context -> generateLinkCode(context.getSource()))));
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
        source.sendSuccess(new TextComponent(cfg.cLinkMsg1).withStyle(ChatFormatting.WHITE).append(new TextComponent(String.valueOf(authCode)).withStyle(style -> style.withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(finalAuthCode))))).append(cfg.cLinkMsg2).withStyle(ChatFormatting.WHITE), false);
        return 0;
    }
}
