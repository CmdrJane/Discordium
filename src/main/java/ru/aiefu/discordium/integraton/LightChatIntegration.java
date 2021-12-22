package ru.aiefu.discordium.integraton;

import ru.aiefu.discordium.discord.DiscordLink;
import ru.aiefu.lightchat.OnPlayerGlobalMessageEvent;

public class LightChatIntegration {
    public void onGlobalMsgSubscribe(){
        OnPlayerGlobalMessageEvent.EVENT.register(DiscordLink::onPlayerMessage);
    }
}
