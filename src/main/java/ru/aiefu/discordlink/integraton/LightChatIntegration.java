package ru.aiefu.discordlink.integraton;

import ru.aiefu.discordlink.discord.DiscordLink;
import ru.aiefu.lightchat.OnPlayerGlobalMessageEvent;

public class LightChatIntegration {
    public void onGlobalMsgSubscribe(){
        OnPlayerGlobalMessageEvent.EVENT.register(DiscordLink::onPlayerMessage);
    }
}
