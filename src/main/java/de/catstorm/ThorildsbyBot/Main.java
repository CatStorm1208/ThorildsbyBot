package de.catstorm.ThorildsbyBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public class Main {
    public static String messageID;
    public static String roleID = "1363262935182344333";

    public static void main(String[] args) throws IOException {
        JDA jda = JDABuilder.createDefault(Files.readString(Path.of("./token")).split("\\s")[0])
            .setEventManager(new AnnotatedEventManager()).addEventListeners(new Main()).build();
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
}