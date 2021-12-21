package ru.aiefu.discordlink;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.server.level.ServerPlayer;



public interface OnPlayerMessageEvent {

    Event<OnPlayerMessageEvent> EVENT = EventFactory.createArrayBacked(OnPlayerMessageEvent.class,
            (listeners) -> (player, msg, text) -> {
                for (OnPlayerMessageEvent listener : listeners) {
                    listener.onMessage(player, msg, text);
                }
            });

    void onMessage(ServerPlayer player, String msg, BaseComponent textComponent);
}
