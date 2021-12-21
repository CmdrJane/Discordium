package ru.aiefu.discordlink.discord;

public class DiscordConfig {
    public String token = "";
    public String chatChannelId = "";
    public String consoleChannelId = "";
    public boolean enableLogsForwarding = true;
    public String serverId = "";
    public boolean preloadDiscordMembers = false;

    public String playerHeadsUrl = "https://ely.by/services/skins-renderer?url=http://skinsystem.ely.by/skins/{username}.png&scale=18.9&renderFace=1";
    public boolean enableWebhook = false;
    public String webhookUrl = "";
    public boolean enableAccountLinking = false;
    public boolean forceLinking = false;
    public boolean useDiscordData = false;

    public String targetLocalization = "en_us";
    public boolean isBidirectional = false;

    public String startupMsg = ":white_check_mark: **Server started!**";
    public String serverStopMsg = ":octagonal_sign: **Server stopped!**";

    public String joinMessage = "{username} joined the server!";
    public String disconnectMessage = "{username} left the server!";
    public String advancementMsg = "{username} has made the achievement {advancement}";

    public String onlinePlayersMsg = "Players online: ";
    public String noPlayersMsg = "Currently there are no players on server";
    public String channelTopicMsg = "Players online: ";
    public String shutdownTopicMsg = "Server offline";
    public String verificationDisconnect = "You need to verify your account via discord. Your code is {code}. Send this code to {botname} PM.";
    public String successfulVerificationMsg = "Successfully linked discord account to your game account {username}({uuid})";
    public String commandLinkMsg = "Your code is {code}. Send this code to {botname} PM.";


    public transient String vDisconnectMsg1;
    public transient String vDisconnectMsg2 = "";
    public transient String cLinkMsg1;
    public transient String cLinkMsg2 = "";

    public void setup(){
        String msg = commandLinkMsg;
        int i = msg.indexOf("{code}");
        if(i != -1){
            cLinkMsg1 = msg.substring(0, i);
            cLinkMsg2 = msg.substring(i +6);
        } else cLinkMsg1 = msg;

        String msg2 = verificationDisconnect;
        int j = msg2.indexOf("{code}");
        if(j != -1){
            vDisconnectMsg1 = msg2.substring(0, i);
            vDisconnectMsg2 = msg2.substring(i +6);
        } else vDisconnectMsg1 = msg2;
    }
}
