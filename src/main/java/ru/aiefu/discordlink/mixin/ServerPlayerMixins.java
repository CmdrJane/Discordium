package ru.aiefu.discordlink.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.discordlink.discord.DiscordLink;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixins extends Player {
    public ServerPlayerMixins(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "die", at =@At("TAIL"))
    public void sendDeathMsg(DamageSource damageSource, CallbackInfo ci){
        DiscordLink.sendDeathMsg(this.getScoreboardName(), damageSource.getLocalizedDeathMessage((ServerPlayer)(Object)this).getString(), this.getStringUUID());
    }
}
