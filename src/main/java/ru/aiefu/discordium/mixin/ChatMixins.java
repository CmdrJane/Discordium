package ru.aiefu.discordium.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.discordium.OnPlayerMessageEvent;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixins {

	@Shadow public ServerPlayer player;

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/server/network/FilteredText;)V", at =@At(value = "INVOKE", target = "net/minecraft/server/players/PlayerList.broadcastChatMessage (Lnet/minecraft/server/network/FilteredText;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V", ordinal = 0, shift = At.Shift.BEFORE))
	private void onPlayerMessageEvent(FilteredText<PlayerChatMessage> filteredText, CallbackInfo ci){
		String msg = filteredText.raw().signedContent().getString();
		MutableComponent text = Component.translatable("chat.type.text", this.player.getDisplayName(), msg);
		OnPlayerMessageEvent.EVENT.invoker().onMessage(player, msg, text);
	}
}
