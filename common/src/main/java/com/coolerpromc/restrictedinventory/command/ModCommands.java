package com.coolerpromc.restrictedinventory.command;

import com.coolerpromc.restrictedinventory.Constants;
import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(Constants.MODID).requires(ModCommands::canConfig)
                .then(Commands.literal("config").requires(ModCommands::canConfig).executes(_ -> 1)));
    }

    private static <T extends SharedSuggestionProvider> boolean canConfig(T commandSourceStack) {
        return CommonConfig.useClientRestriction() || Commands.hasPermission(Commands.LEVEL_ADMINS).test(commandSourceStack);
    }
}
