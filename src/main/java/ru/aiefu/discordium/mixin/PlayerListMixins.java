package ru.aiefu.discordium.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.discordium.config.ConfigManager;
import ru.aiefu.discordium.config.LinkedProfile;
import ru.aiefu.discordium.discord.DiscordConfig;
import ru.aiefu.discordium.discord.DiscordLink;
import ru.aiefu.discordium.discord.VerificationData;

import java.io.FileNotFoundException;
import java.net.SocketAddress;
import java.util.Random;


@Mixin(PlayerList.class)
public class PlayerListMixins {

    private final Random r = new Random();

    @Inject(method = "placeNewPlayer", at =@At("HEAD"))
    private void sendDiscordWelcomeMsg(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci){
        DiscordLink.greetingMsg(serverPlayer.getScoreboardName(), serverPlayer.getStringUUID());
    }

    @Inject(method = "remove", at =@At("HEAD"))
    private void sendLogoutMessage(ServerPlayer serverPlayer, CallbackInfo ci){
        DiscordLink.logoutMsg(serverPlayer.getScoreboardName(), serverPlayer.getStringUUID());
        DiscordLink.linkedPlayers.remove(serverPlayer.getGameProfile().getId().toString());
    }

    @Inject(method = "canPlayerLogin", at =@At("HEAD"), cancellable = true)
    private void checkLink(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) throws FileNotFoundException {
        DiscordConfig cfg = DiscordLink.config;
        if(cfg.enableAccountLinking && cfg.forceLinking){
            String uuid = gameProfile.getId().toString();
            LinkedProfile profile = ConfigManager.getLinkedProfile(uuid);
            if(profile != null){
                DiscordLink.linkedPlayers.put(uuid, profile);
            } else {
                if(!DiscordLink.pendingPlayersUUID.containsKey(uuid)) {
                    int authCode = r.nextInt(100_000, 1_000_000);
                    while (DiscordLink.pendingPlayers.containsKey(authCode)) {
                        authCode = r.nextInt(100_000, 1_000_000);
                    }
                    String auth = String.valueOf(authCode);
                    DiscordLink.pendingPlayers.put(authCode, new VerificationData(gameProfile.getName(), uuid, DiscordLink.currentTime + 600_000));
                    DiscordLink.pendingPlayersUUID.put(uuid, authCode);
                    cir.setReturnValue(new TextComponent(cfg.vDisconnectMsg1.replaceAll("\\{botname}", DiscordLink.botName))
                            .append(new TextComponent(auth).withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                            .append(new TextComponent(cfg.vDisconnectMsg2.replaceAll("\\{botname}", DiscordLink.botName)).withStyle(ChatFormatting.WHITE)));
                } else {
                    cir.setReturnValue(new TextComponent(cfg.vDisconnectMsg1.replaceAll("\\{botname}", DiscordLink.botName))
                            .append(new TextComponent(" " + DiscordLink.pendingPlayersUUID.get(uuid)).withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                            .append(new TextComponent(cfg.vDisconnectMsg2.replaceAll("\\{botname}", DiscordLink.botName)).withStyle(ChatFormatting.WHITE)));
                }
            }
        }
    }
}
