package ru.aiefu.discordium.language;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.discordium.discord.DiscordConfig;
import ru.aiefu.discordium.discord.DiscordLink;


public class ServerLanguageManager implements ResourceManagerReloadListener {
    private static final ServerLanguage language = new ServerLanguage();

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        DiscordConfig cfg = DiscordLink.config;
        if(cfg.targetLocalization.length() > 0) {
            language.loadAllLanguagesIncludingModded(cfg.targetLocalization, cfg.isBidirectional);
        }
    }
}
