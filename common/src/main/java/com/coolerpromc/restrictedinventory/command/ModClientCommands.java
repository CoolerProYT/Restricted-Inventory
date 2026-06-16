package com.coolerpromc.restrictedinventory.command;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.screen.RestrictionConfigScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

public class ModClientCommands {
    public static <T extends SharedSuggestionProvider> void register(CommandDispatcher<T> dispatcher){
        dispatcher.register(LiteralArgumentBuilder.<T>literal(Constants.MODID).requires(ModClientCommands::canConfig)
                .then(LiteralArgumentBuilder.<T>literal("config").requires(ModClientCommands::canConfig).executes(ModClientCommands::config)));
    }

    private static <T extends SharedSuggestionProvider> boolean canConfig(T commandSourceStack) {
        return CommonConfig.clientCache.useClientRestriction() || Commands.hasPermission(Commands.LEVEL_ADMINS).test(commandSourceStack);
    }

    private static <T extends SharedSuggestionProvider> int config(CommandContext<T> context) {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(new RestrictionConfigScreen()));
        return Command.SINGLE_SUCCESS;
    }
}
