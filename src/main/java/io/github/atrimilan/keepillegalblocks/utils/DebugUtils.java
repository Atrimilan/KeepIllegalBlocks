package io.github.atrimilan.keepillegalblocks.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Server;

import java.util.function.Supplier;

/**
 * To enable debug mode:
 * <li>If you are using the "runServer" Gradle task, add the Gradle script parameter {@code -Pkeepillegalblocks.debug} in your run configuration.</li>
 * <li>Otherwise, add {@code -Dkeepillegalblocks.debug=true} to the JVM arguments of your Minecraft server.</li>
 */
public class DebugUtils {

    private static boolean debugEnabled = Boolean.parseBoolean(System.getProperty("keepillegalblocks.debug"));

    private static Server server;

    public static void setServer(Server s) {
        server = s;
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public enum MessageType {
        OK, INFO, WARN, ERROR
    }

    /**
     * Send a global message in the game chat.
     *
     * @param messageSupplier String supplier of the message to send (MiniMessage format allowed)
     * @param type            Type of the message
     */
    public static void sendChat(Supplier<String> messageSupplier, MessageType type) {
        if (debugEnabled) {
            server.broadcast(MiniMessage.miniMessage().deserialize(switch (type) {
                case OK -> "<green>";
                case INFO -> "<aqua>";
                case WARN -> "<yellow>";
                case ERROR -> "<red>";
            } + messageSupplier.get()));
        }
    }
}
