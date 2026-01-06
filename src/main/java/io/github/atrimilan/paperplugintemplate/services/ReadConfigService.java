package io.github.atrimilan.paperplugintemplate.services;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ReadConfigService {

    private static final String READ_CONFIG_PLAYER_MSG = "Player <aqua><uuid></aqua> is at <gold><x> <y> <z>";

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final FileConfiguration config;

    public ReadConfigService(FileConfiguration config) {
        this.config = config;
    }

    public int readSimpleValue(CommandSender sender, String path, String template) throws CommandSyntaxException {
        Object value = config.get(path);
        if (value == null)
            throw new SimpleCommandExceptionType(() -> "Path " + path + " not found in config").create();

        sender.sendMessage(mm.deserialize(template, Placeholder.unparsed("value", String.valueOf(value))));
        return Command.SINGLE_SUCCESS;
    }

    public int readPlayer(CommandSender sender) throws CommandSyntaxException {
        ConfigurationSection player = config.getConfigurationSection("example.player");
        if (player == null)
            throw new SimpleCommandExceptionType(() -> "Player not found in config").create();

        String uuid = player.getString("uuid");
        if (uuid == null)
            throw new SimpleCommandExceptionType(() -> "Player UUID is missing").create();

        if (!player.contains("location.x") || !player.contains("location.y") || !player.contains("location.z"))
            throw new SimpleCommandExceptionType(() -> "Player location is missing").create();

        sender.sendMessage(mm.deserialize(READ_CONFIG_PLAYER_MSG,
                Placeholder.unparsed("uuid", uuid),
                Placeholder.unparsed("x", String.valueOf(Math.round(player.getDouble("location.x")))),
                Placeholder.unparsed("y", String.valueOf(Math.round(player.getDouble("location.y")))),
                Placeholder.unparsed("z", String.valueOf(Math.round(player.getDouble("location.z"))))));
        return Command.SINGLE_SUCCESS;
    }
}
