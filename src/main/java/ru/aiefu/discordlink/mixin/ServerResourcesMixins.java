package ru.aiefu.discordlink.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.discordlink.language.ServerLanguageManager;

@Mixin(ServerResources.class)
public class ServerResourcesMixins {
    @Shadow @Final private ReloadableResourceManager resources;

    private final ServerLanguageManager serverLanguageManager = new ServerLanguageManager();

    @Inject(method = "<init>", at =@At("TAIL"))
    private void registerLanguageManager(RegistryAccess registryAccess, Commands.CommandSelection commandSelection, int i, CallbackInfo ci){
        this.resources.registerReloadListener(serverLanguageManager);
    }
}
