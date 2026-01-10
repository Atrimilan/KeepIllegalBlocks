package io.github.atrimilan.keepillegalblocks.commands;


import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.atrimilan.keepillegalblocks.services.FlySpeedService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Example command to change the fly speed of a player
 */
public class FlySpeedCommand {

    public static final String DESCRIPTION = "Sets the fly speed of a player";
    public static final Set<String> ALIASES = Set.of("fspeed", "fs");

    private static final FloatArgumentType FLYSPEED_LIMIT = FloatArgumentType.floatArg(0f, 10f);
    private static final String SPEED_ARG = "speed";
    private static final String PLAYER_ARG = "player";

    private final FlySpeedService flySpeedService;

    public FlySpeedCommand(FlySpeedService flySpeedService) {
        this.flySpeedService = flySpeedService;
    }

    /**
     * @return A LiteralCommandNode of the "/flyspeed" command, ready to be registered
     */
    public LiteralCommandNode<CommandSourceStack> create() {
        // Usage: /flyspeed [<player>] <speed>
        return Commands.literal("flyspeed")
                .then(Commands.argument(SPEED_ARG, FLYSPEED_LIMIT)
                        .requires(ctx -> ctx.getSender().hasPermission("ppt.flyspeed.self"))
                        .executes(this::setSelfFlySpeed))
                .then(Commands.argument(PLAYER_ARG, ArgumentTypes.player())
                        .requires(ctx -> ctx.getSender().hasPermission("ppt.flyspeed.others"))
                        .then(Commands.argument(SPEED_ARG, FLYSPEED_LIMIT).executes(this::setTargetFlySpeed)))
                .build();
    }

    /**
     * Define the flight speed of the player executing the command
     */
    private int setSelfFlySpeed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!(ctx.getSource().getExecutor() instanceof Player target))
            throw new SimpleCommandExceptionType(() -> "Only players can fly!").create();
        return flySpeedService.setFlySpeed(ctx.getSource(), target, FloatArgumentType.getFloat(ctx, SPEED_ARG));
    }

    /**
     * Define the flight speed of the specified player
     */
    private int setTargetFlySpeed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player target = ctx.getArgument(PLAYER_ARG, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())
                .getFirst();
        return flySpeedService.setFlySpeed(ctx.getSource(), target, FloatArgumentType.getFloat(ctx, SPEED_ARG));
    }
}
