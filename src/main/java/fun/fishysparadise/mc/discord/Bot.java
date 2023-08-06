package fun.fishysparadise.mc.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import fun.fishysparadise.mc.FishysParadisePlugin;
import fun.fishysparadise.mc.tables.User;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.ChatColor;
import okhttp3.OkHttpClient;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import javax.security.auth.login.LoginException;

public class Bot extends ListenerAdapter {


    private FishysParadisePlugin main;
    private boolean shutdown = false;

    // Connection variables
    private JDA jda;
    private WebhookClient webhook;

    public Bot(FishysParadisePlugin plugin) throws LoginException, ClassCastException {
        // Reloading configuration before launching
        setMain(plugin);
        main.reloadConfig();
        // Setting up bot
        setJda(JDABuilder.createDefault(
                        getMain().getBOT_TOKEN()
                ).setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT).addEventListeners(this).build());
        // Setting up webhook
        setWebhook(new WebhookClientBuilder(getMain().getWEBHOOK_URL())
                .setThreadFactory(Thread::new)
                .setDaemon(true)
                .setWait(true)
                .setHttpClient(new OkHttpClient())
                .build());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Checking against whether message is from the specific channel from the Discord server
        // Preventing messages if bot is about to shut down
        if (isShuttingDown() || !(event.isFromGuild()) || event.getChannel().getIdLong() != getMain().getTEXT_CHANNEL_ID() ||
                event.isWebhookMessage() ||
                event.getMember() == null ||
                event.getAuthor().isBot() ||
                event.getMessage().isEdited()) {
            return;
        }
        // Checking if member is verified
        User user = getMain().getMember(event.getMember());
        // Using user details if user is verified, reverting to event details
        String prefix = ChatColor.of("#738abd") + "[Discord] " + (user != null ? (ChatColor.of("#aaff00") + "[Verified]" + ChatColor.RESET) : (ChatColor.of("#808080") + "[Unverified]" +ChatColor.RESET));

        String name = user != null ? user.getUsername() : event.getMember().getEffectiveName();
        String hexColor = String.format("#%06X", (0xFFFFFF & event.getMember().getColorRaw())).toLowerCase();
        // Sending formatted message
        getMain().broadcastFormattedMessage("%s%s %s§r §7>§r %s", prefix, ChatColor.of(hexColor).toString(), name,  ChatColor.stripColor(MarkdownSanitizer.sanitize(event.getMessage().getContentRaw())));
    }

    public void sendSanitisedMessageToDiscord(Player player, String message) {
        // Removing colour data before sending messages
        this.sendMessageToDiscord(player, ChatColor.stripColor(MarkdownSanitizer.sanitize(message)));
    }

    public void sendMessageToDiscord(Player player, String message) {
        if (isShuttingDown()) return;
        // Sending message via webhook with player details
        try {
            getWebhook().send(new WebhookMessageBuilder()
                    .setAvatarUrl(String.format(getMain().getAVATAR_URL(), player.getName()))
                    .setUsername(ChatColor.stripColor(player.getDisplayName()))
                    .setContent(message)
                    .build());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void shutdown() {
        setShutdown(true);
        getJda().shutdownNow();
        getWebhook().close();
    }

    public FishysParadisePlugin getMain() {
        return main;
    }

    public void setMain(FishysParadisePlugin main) {
        this.main = main;
    }

    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public WebhookClient getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookClient webhook) {
        this.webhook = webhook;
    }

    public boolean isShuttingDown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

}
