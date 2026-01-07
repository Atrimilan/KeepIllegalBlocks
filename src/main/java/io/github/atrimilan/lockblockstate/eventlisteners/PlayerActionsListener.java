package io.github.atrimilan.lockblockstate.eventlisteners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerActionsListener implements Listener {

    /**
     * Replace the default welcome message with a custom one
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(MiniMessage.miniMessage().deserialize(
                "Welcome back <gold><player></gold>!",
                Placeholder.unparsed("player", event.getPlayer().getName())
        ));
    }
}
