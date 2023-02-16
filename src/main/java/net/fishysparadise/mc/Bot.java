package net.fishysparadise.mc;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.ChatColor;
import okhttp3.OkHttpClient;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.Objects;

public class Bot extends ListenerAdapter {

    private final Long SERVER_ID;
    private final Long CHAT_CHANNEL_ID;
    private final String AVATAR_SERVICE_URL;

    private Core core = null;
    private JDA jda = null;
    private WebhookClient webhookClient = null;
    private boolean shutdown = false;

    public Bot(Core core) throws LoginException {
        // Updating config file data
        FileConfiguration configuration = core.getConfig();
        core.reloadConfig();
        // Setting up constant values
        SERVER_ID = Long.parseLong(Objects.requireNonNull(configuration.getString("DISCORD.SERVER_ID")));
        CHAT_CHANNEL_ID = Long.parseLong(Objects.requireNonNull(configuration.getString("DISCORD.CHAT_CHANNEL_ID")));
        AVATAR_SERVICE_URL = configuration.getString("DISCORD.AVATAR_SERVICE_URL");
        // Setting up connections
        this.jda = JDABuilder.createDefault(
                        configuration.getString("DISCORD.BOT_TOKEN")
                ).setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS).addEventListeners(this).build();
        this.webhookClient = new WebhookClientBuilder(Objects.requireNonNull(configuration.getString("DISCORD.WEBHOOK_URL")))
                .setThreadFactory(Thread::new)
                .setDaemon(true)
                .setWait(true)
                .setHttpClient(new OkHttpClient())
                .build();
        this.core = core;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (shutdown ||
                event.getChannel().getIdLong() != CHAT_CHANNEL_ID ||
                event.isWebhookMessage() ||
                event.getMember() == null ||
                event.getAuthor().isBot() ||
                event.getMessage().isEdited()) {
            return;
        }

        String hexColor = String.format("#%06X", (0xFFFFFF & event.getMember().getColorRaw())).toLowerCase();

        String output = String.format("%s%s %s§r §7>§r %s",
                ChatColor.of("#738abd") + "[Discord]" + ChatColor.RESET,
                ChatColor.of(hexColor).toString(),
                event.getMember().getEffectiveName(),
                ChatColor.stripColor(MarkdownSanitizer.sanitize(event.getMessage().getContentRaw()))
        );
        core.getServer().broadcastMessage(output);
    }

    public void sendMessageToDiscord(Player player, String message) {
        if (shutdown) return;
        try {
            webhookClient.send(new WebhookMessageBuilder()
                    .setAvatarUrl(String.format(AVATAR_SERVICE_URL, player.getName()))
                    .setUsername(ChatColor.stripColor(player.getDisplayName()))
                    .setContent(message)
                    .build());
        } catch (Exception ignored) {
        }
    }

    public void sendSanitisedMessageToDiscord(Player player, String message) {
        this.sendMessageToDiscord(player, ChatColor.stripColor(MarkdownSanitizer.sanitize(message)));
    }

    public void shutdown() {
        shutdown = true;
        jda.shutdown();
        webhookClient.close();
    }

}
