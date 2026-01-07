package io.github.atrimilan.lockblockstate.services;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpeedService {

    private static final String SELF_FLYSPEED_MSG = "<green>Successfully set your flight speed to <gold><speed>";
    private static final String OTHER_FLYSPEED_SENDER_MSG = "<gray>Successfully set <aqua><target></aqua>'s flight speed to <gold><speed>";
    private static final String OTHER_FLYSPEED_TARGET_MSG = "<aqua><sender></aqua> <gray>has set your flight speed to <gold><speed>";

    /**
     * Define the flight speed of the selected player
     *
     * @param source The command source
     * @param target The player to set the flight speed for
     * @param speed  The flight speed to set
     */
    public int setFlySpeed(CommandSourceStack source, Player target, float speed) {
        target.setFlySpeed(speed * 0.1f);

        MiniMessage mm = MiniMessage.miniMessage();

        if (source.getExecutor() == target) {
            target.sendMessage(mm.deserialize(SELF_FLYSPEED_MSG, //
                    Placeholder.unparsed("speed", String.valueOf(speed))));

        } else {
            CommandSender sender = source.getSender();

            sender.sendMessage(mm.deserialize(OTHER_FLYSPEED_SENDER_MSG, //
                    Placeholder.component("target", target.name()), // target.name() is already a Component
                    Placeholder.unparsed("speed", String.valueOf(speed))));

            target.sendMessage(mm.deserialize(OTHER_FLYSPEED_TARGET_MSG, //
                    Placeholder.unparsed("sender", sender.getName()),
                    Placeholder.unparsed("speed", String.valueOf(speed))));
        }

        return Command.SINGLE_SUCCESS;
    }
}
