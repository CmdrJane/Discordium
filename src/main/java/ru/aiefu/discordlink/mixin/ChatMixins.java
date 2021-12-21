package ru.aiefu.discordlink.mixin;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.discordlink.OnPlayerMessageEvent;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixins {

	@Shadow public ServerPlayer player;

	@Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at =@At(value = "INVOKE", target = "net/minecraft/server/players/PlayerList.broadcastMessage (Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", ordinal = 0, shift = At.Shift.BEFORE))
	private void onPlayerMessageEvent(TextFilter.FilteredText filteredText, CallbackInfo ci){
		String msg = filteredText.getRaw();
		BaseComponent text = new TranslatableComponent("chat.type.text", this.player.getDisplayName(), msg);
		OnPlayerMessageEvent.EVENT.invoker().onMessage(player, msg, text);
	}
}
