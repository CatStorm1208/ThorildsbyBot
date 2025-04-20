package de.catstorm.ThorildsbyBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public class Main {
    public static String messageID;
    public static String roleID = "1363262935182344333";
    public static String chirperID = "1266843571583979552";
    public static String ownerOnlyID = "1251525167977463858";

    public static void main(String[] args) throws IOException {
        JDA jda = JDABuilder.createDefault(Files.readString(Path.of("./token")).split("\\s")[0])
            .setEventManager(new AnnotatedEventManager())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(new Main()).build();
        messageID = args[0].split("\\s")[0];
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
                event.getGuild().removeRoleFromMember(Objects.requireNonNull(event.getMember()),
                    Objects.requireNonNull(event.getGuild().getRoleById(roleID))).queue();
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onMessageReceive(MessageReceivedEvent event) {
        String regex = "\\*\\*.+\\*\\* [*_]@[a-zA-Z0-9]+[*_]\n.*";

        if (event.getChannel().getId().equals(chirperID) && !event.getMessage().getContentRaw().matches(regex)) {
            event.getMessage().createThreadChannel("Miscounts detected").queue(threadChannel -> threadChannel.sendMessage("**Chirper Police** @Chirperpol\n" +
                "We have detected incorrect formatting in your chirper post. Please read the pinned message for proper formatting. The admin-team will be informed.\n\n" +
                "*This message was sent automatically. If you believe this to be a mistake, please contact a server admin.*").queue());
            event.getMessage().forwardTo(Objects.requireNonNull(event.getGuild().getTextChannelById(ownerOnlyID))).queue();
        }
    }
}