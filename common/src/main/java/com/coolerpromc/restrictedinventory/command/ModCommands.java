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
                .then(Commands.literal("config").requires(ModCommands::canConfig).executes(s -> 1)));
    }

    private static boolean canConfig(CommandSourceStack commandSourceStack) {
        return CommonConfig.useClientRestriction() || commandSourceStack.hasPermission(4);
    }
}
