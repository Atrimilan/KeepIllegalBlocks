package io.github.atrimilan.keepillegalblocks.core.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Function;

public enum KibRule {
    MAX_BLOCKS(
            "max_blocks",
            IntegerArgumentType.integer(0), // Max: Integer.MAX_VALUE
            ctx -> IntegerArgumentType.getInteger(ctx, "value")
    ),
    ONLY_USE_KIB_IN_CREATIVE_MODE(
            "only_use_kib_in_creative_mode",
            BoolArgumentType.bool(),
            ctx -> BoolArgumentType.getBool(ctx, "value")
    ),
    USE_PACKET_EVENTS_IF_DETECTED(
            "use_packet_events_if_detected",
            BoolArgumentType.bool(),
            ctx -> BoolArgumentType.getBool(ctx, "value")
    );

    private final String name;
    private final ArgumentType<?> argumentType;
    private final Function<CommandContext<CommandSourceStack>, ?> value;

    <T> KibRule(String name, ArgumentType<T> argumentType, Function<CommandContext<CommandSourceStack>, T> value) {
        this.name = name;
        this.argumentType = argumentType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ArgumentType<?> getArgumentType() {
        return argumentType;
    }

    public Object getValue(CommandContext<CommandSourceStack> ctx) {
        return value.apply(ctx);
    }
}
