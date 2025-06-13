package de.catstorm.ThorildsbyBot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

@SuppressWarnings({"unused", "CallToPrintStackTrace", "deprecation", "ResultOfMethodCallIgnored"})
public class Main {
    public static String messageID;
    public static String roleID = "1363262935182344333";
    public static String chirperID = "1266843571583979552";
    public static String ownerOnlyID = "1251525167977463858";
    public static URL sightEngineURL;
    public static URLConnection sightEngineConnection;
    public static HttpURLConnection sightEngineHTTP;
    public static Gson gson = new Gson();
    public static HashMap<String, Boolean> lockedDownChannels = new HashMap<>();

    public static void main(String[] args) throws IOException {
        JDA jda = JDABuilder.createDefault(Files.readString(Path.of("./token")).split("\\s")[0])
            .setEventManager(new AnnotatedEventManager())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.AUTO_MODERATION_EXECUTION)
            .addEventListeners(new Main()).build();
        messageID = args[0].split("\\s")[0];

        sightEngineURL = new URL("https://api.sightengine.com/1.0/check.json");
    }

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("lockdown")) {
            //noinspection DataFlowIssue
            var mediaOnly = event.getOption("mediaOnly").getAsBoolean();
            lockedDownChannels.put(event.getChannel().getId(), mediaOnly);
            if (mediaOnly) event.getChannel().sendMessage("This channel is now locked-down for messages containing any media." +
                "\n\n**ANY FURTHER MESSAGES CONTAINING IMAGES, GIFS OR VIDEOS WILL BE DELETED**");
            else event.getChannel().sendMessage("This channel is now locked-down for moderation purposes." +
                "\n\n**ANY FURTHER MESSAGES WILL BE DELETED**");
        }
    }

    @SubscribeEvent
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        try {
            if (event.getMessageId().equals(messageID) &&
                event.getEmoji().asUnicode().toString().equals("UnicodeEmoji(codepoints=U+1f525)")) {
                event.getGuild().addRoleToMember(Objects.requireNonNull(event.getMember()),
                    Objects.requireNonNull(event.getGuild().getRoleById(roleID))).queue();
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        try {
            if (event.getMessageId().equals(messageID) &&
                event.getEmoji().asUnicode().toString().equals("UnicodeEmoji(codepoints=U+1f525)")) {
                event.getGuild().removeRoleFromMember(event.retrieveMember().complete(),
                    Objects.requireNonNull(event.getGuild().getRoleById(roleID))).queue();
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onMessageReceive(MessageReceivedEvent event) {
        //Lockdown handling
        if (!Objects.requireNonNull(event.getMember()).getPermissions().contains(Permission.MODERATE_MEMBERS))
            if (lockedDownChannels.containsKey(event.getChannel().getId()))
                if (!lockedDownChannels.get(event.getChannel().getId())) event.getMessage().delete().queue();
                else if (!event.getMessage().getAttachments().isEmpty()) event.getMessage().delete().queue();

        //Command registering
        if (event.getMessageId().equals(ownerOnlyID) &&
            event.getMessage().getContentRaw().equals("!ThorildsbyBotLoadCommands")) {
            event.getGuild().updateCommands().addCommands(
                Commands.slash("lockdown", "Locks down the current channel")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .addOption(OptionType.BOOLEAN, "mediaOnly", "Should this lockdown only apply to media?"),
                Commands.slash("Unlock", "Removes this channel's lockdown")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            );
        }

        //Chirper Police
        String regex = "\\*\\*.+\\*\\* [*_]?(_?<)?@[a-zA-Z0-9]+(>_?)?[*_]?\\s?\n[\\s\\S]*";
        String chirperpolText = """
                **Chirper Police** *@Chirperpol*
                We have detected incorrect formatting in your chirper post. Please read the pinned message for proper formatting. The admin-team will be informed.

                *This message was sent automatically. If you believe this to be a mistake, please contact a server admin.*""";

        if (event.getChannel().getId().equals(chirperID) && !event.getMessage().getContentRaw().matches(regex)) {
            event.getMessage().createThreadChannel("Miscounts detected").queue(threadChannel ->
                threadChannel.sendMessage(chirperpolText).queue());
            event.getMessage().forwardTo(Objects.requireNonNull(event.getGuild().getTextChannelById(ownerOnlyID))).queue();
        }

        try {
            if(event.getChannel().getType().isThread() &&
                ((ThreadChannel) event.getChannel()).getParentChannel().getId().equals(chirperID) &&
                !event.getMessage().getContentRaw().matches(regex)) {
                event.getMessage().reply(chirperpolText).queue();
                event.getMessage().forwardTo(Objects.requireNonNull(event.getGuild().getTextChannelById(ownerOnlyID))).queue();
            }
        }
        catch (Exception e) {
            //lol
        }

        //Image moderation
        //Thx to Ferrybig on stackoverflow for explaining POST request in Java
        boolean shouldForward = false;
        try {
            sightEngineConnection = sightEngineURL.openConnection();
            sightEngineHTTP = (HttpURLConnection) sightEngineConnection;
            sightEngineHTTP.setRequestMethod("POST");
            sightEngineHTTP.setDoOutput(true);

            for (var attachement : event.getMessage().getAttachments()) if (attachement.isImage()) {
                Map<String,String> arguments = new HashMap<>();
                arguments.put("url", attachement.getUrl());
                arguments.put("models", "nudity-2.1,alcohol,recreational_drug,medical,offensive-2.0,scam,text-content,gore-2.0,qr-content,genai,self-harm");
                arguments.put("api_user", "934535508");
                arguments.put("api_secret", Files.readString(Path.of("./sightEngineSecret")));

                StringJoiner sj = new StringJoiner("&");
                for(Map.Entry<String,String> entry : arguments.entrySet())
                    sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                sightEngineHTTP.setFixedLengthStreamingMode(length);
                sightEngineHTTP.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                sightEngineHTTP.connect();
                try(OutputStream os = sightEngineHTTP.getOutputStream()) {
                    os.write(out);
                }
                try(InputStream is = sightEngineHTTP.getInputStream()) {
                    Files.deleteIfExists(Path.of("./sightEngineOut.json"));

                    //TODO: get this shit working somehow
                    var jsonData = Arrays.toString(is.readAllBytes());
                    try {
                        SightEngineOut[] jsonOut = gson.fromJson(jsonData, SightEngineOut[].class);
                        System.out.println(jsonOut[0].type.ai_generated);
                    }
                    catch (JsonSyntaxException e) {
                        if (e.getMessage().equals("Expected BEGIN_OBJECT but was BEGIN_ARRAY")) {
                            JsonArray jsonArray = new JsonParser().parse(jsonData).getAsJsonArray();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                SightEngineOut jsonOut = gson.fromJson(jsonArray.get(i), SightEngineOut.class);

                                if (jsonOut.type.ai_generated < 0.9 && jsonOut.type.ai_generated >= 0.5) shouldForward = true;
                                else if (jsonOut.type.ai_generated >= 0.9) {
                                    Objects.requireNonNull(event.getMember()).timeoutFor(Duration.ofMinutes(15)).queue();
                                    System.out.println("Timed out: " + event.getMember().getNickname());
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (shouldForward) event.getMessage().forwardTo(Objects.requireNonNull(event.getGuild().getTextChannelById(ownerOnlyID))).queue();
    }
}