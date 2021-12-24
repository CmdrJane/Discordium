package ru.aiefu.discordium;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import ru.aiefu.discordium.discord.DiscordLink;

public class DiscordiumCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("discord").then(Commands.literal("enable-mention-notification")
                .then(Commands.argument("soundstate", BoolArgumentType.bool()).executes(context ->
                        switchNotifySoundState(context.getSource(), BoolArgumentType.getBool(context,"soundstate"))))));
    }

    private static int switchNotifySoundState(CommandSourceStack source, boolean state) throws CommandSyntaxException {
        IServerPlayer player = (IServerPlayer) source.getPlayerOrException();
        player.setNotifyState(state);
        source.sendSuccess(new TextComponent(DiscordLink.config.mentionState.replaceAll("\\{state}", String.valueOf(state))), false);
        return 0;
    }
}
