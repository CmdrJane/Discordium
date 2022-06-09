package ru.aiefu.discordium;

import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;

public interface IServerPlayer {
    boolean getNotifyState();
    void setNotifyState(boolean bl);
    boolean isAcceptingChatType(ResourceKey<ChatType> t);
}
