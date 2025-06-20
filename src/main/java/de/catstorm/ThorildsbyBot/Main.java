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
        switch (event.getName()) {
            case "lockdown" -> {
                //noinspection DataFlowIssue
                var mediaOnly = event.getOption("mediaonly").getAsBoolean();
                lockedDownChannels.put(event.getChannel().getId(), mediaOnly);
                if (mediaOnly) event.reply("This channel is now locked-down for messages containing any media." +
                    "\n\n**ANY FURTHER MESSAGES CONTAINING IMAGES, GIFS OR VIDEOS WILL BE DELETED**").queue();
                else event.reply("This channel is now locked-down for moderation purposes." +
                    "\n\n**ANY FURTHER MESSAGES WILL BE DELETED**").queue();
            }
            case "unlock" -> {
                lockedDownChannels.remove(event.getChannel().getId());
                event.reply("This channel is no longer locked down, all messaging can resume normally!").queue();
            }
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
            if (lockedDownChannels.containsKey(event.getChannel().getId())) {
                if (!lockedDownChannels.get(event.getChannel().getId()))
                    event.getMessage().delete().queue();
                else if (!event.getMessage().getEmbeds().isEmpty() || !event.getMessage().getAttachments().isEmpty())
                    event.getMessage().delete().queue();
            }

        //Command registering
        if (event.getChannel().getId().equals(ownerOnlyID) &&
            event.getMessage().getContentRaw().equals("!ThorildsbyBotLoadCommands")) {
            event.getGuild().updateCommands().addCommands(
                Commands.slash("lockdown", "Locks down the current channel")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .addOption(OptionType.BOOLEAN, "mediaonly", "Should this lockdown only apply to media?", true),
                Commands.slash("unlock", "Removes this channel's lockdown")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            ).queue();
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
        //TODO: ↑↑↑↑↑
    }
}