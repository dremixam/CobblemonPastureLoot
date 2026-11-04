package com.dremixam.pastureloot.minecraft;

import com.dremixam.pastureloot.PastureLoot;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonBridge.PokemonRef;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonPreview;
import com.dremixam.pastureloot.minecraft.cobblemon.CobblemonPreview.InspectedPokemon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** /pastureloot reload, preview and inspect, for operators only. */
public final class PastureLootCommand {

    /** Operators, the level of /gamerule and /give. */
    private static final int PERMISSION_LEVEL = 2;
    private static final String PROPERTIES = "properties";
    private static final String TARGET = "target";

    private PastureLootCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(PastureLoot.MOD_ID)
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.literal("reload").executes(PastureLootCommand::reload))
                .then(Commands.literal("preview")
                        .then(Commands.argument(PROPERTIES, StringArgumentType.greedyString())
                                .executes(PastureLootCommand::preview)))
                .then(Commands.literal("inspect")
                        .then(Commands.argument(TARGET, EntityArgument.entity())
                                .executes(PastureLootCommand::inspect))));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        List<String> warnings = new ArrayList<>();
        PastureLoot.reload(warnings::add);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("PastureLoot reloaded: " + PastureLoot.overrides().size()
                + " override rules, " + warnings.size() + " warnings.").withStyle(ChatFormatting.GOLD), true);
        for (String warning : warnings) {
            source.sendSuccess(() -> Component.literal(warning).withStyle(ChatFormatting.YELLOW), false);
        }
        return warnings.isEmpty() ? 1 : 0;
    }

    private static int preview(CommandContext<CommandSourceStack> context) {
        String properties = StringArgumentType.getString(context, PROPERTIES);
        CommandSourceStack source = context.getSource();
        Optional<PokemonRef> pokemon;
        try {
            pokemon = CobblemonPreview.fromProperties(properties);
        } catch (RuntimeException exception) {
            pokemon = Optional.empty();
        }
        if (pokemon.isEmpty()) {
            source.sendFailure(Component.literal("\"" + properties + "\" does not name a species."));
            return 0;
        }
        BlockPos place = BlockPos.containing(source.getPosition());
        send(source, PreviewReport.of(pokemon.get(), source.getLevel(), place, "Where you stand", false));
        return 1;
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Optional<InspectedPokemon> inspected = CobblemonPreview.fromEntity(EntityArgument.getEntity(context, TARGET));
        if (inspected.isEmpty()) {
            source.sendFailure(Component.literal("This entity is not a pokemon."));
            return 0;
        }
        InspectedPokemon pokemon = inspected.get();
        String placeLabel = pokemon.pastured() ? "Pasture" : "Where it stands (not in a pasture)";
        send(source, PreviewReport.of(pokemon.pokemon(), pokemon.level(), pokemon.place(), placeLabel, true));
        return 1;
    }

    private static void send(CommandSourceStack source, List<Component> lines) {
        for (Component line : lines) {
            source.sendSuccess(() -> line, false);
        }
    }
}
