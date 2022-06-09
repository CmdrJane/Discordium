package ru.aiefu.discordium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import ru.aiefu.discordium.discord.DiscordConfig;
import ru.aiefu.discordium.discord.DiscordLink;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {

    public void genDiscordLinkSettings(){
        if(!Files.exists(Paths.get("./config/discord-chat/discord-link.json"))) {
            String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new DiscordConfig());
            File file = new File("./config/discord-chat/discord-link.json");
            fileWriter(file, gson);
        }
    }

    public void writeCurrentConfigInstance(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(DiscordLink.config);
        File file = new File("./config/discord-chat/discord-link.json");
        fileWriter(file, gson);
    }

    public DiscordConfig readDiscordLinkSettings() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader("./config/discord-chat/discord-link.json"), DiscordConfig.class);
    }


    public void craftPaths() throws IOException {
        if(!Files.isDirectory(Paths.get("./config/discord-chat"))){
            Files.createDirectories(Paths.get("./config/discord-chat"));
        }
        if(!Files.isDirectory(Paths.get("./config/discord-chat/linked-profiles"))){
            Files.createDirectories(Paths.get("./config/discord-chat/linked-profiles"));
        }
        if(!Files.isDirectory(Paths.get("./config/discord-chat/languages"))){
            Files.createDirectories(Paths.get("./config/discord-chat/languages"));
        }
        if(!Files.isDirectory(Paths.get("./config/discord-chat/languages/1.19"))){
            Files.createDirectories(Paths.get("./config/discord-chat/languages/1.19"));
        }
    }

    @Nullable
    public static LinkedProfile getLinkedProfile(String uuid) throws IOException {
        String path = String.format("./config/discord-chat/linked-profiles/%s.json", uuid);
        if(Files.exists(Paths.get(path))) {
            FileReader reader = new FileReader(path);
            LinkedProfile profile = new Gson().fromJson(reader, LinkedProfile.class);
            reader.close();
            return profile;
        } else return null;
    }

    public static void saveLinkedProfile(LinkedProfile profile){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(profile);
        File file = new File(String.format("./config/discord-chat/linked-profiles/%s.json", profile.uuid));
        fileWriter(file, gson);
    }

    public static void fileWriter(File file, String gson){
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(gson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
