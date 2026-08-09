package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(targets = "me.mgin.graves.block.utility.Inventory", remap = false)
public abstract class ForgottenGravesInventoryMixin {
    @Inject(method = "mergeInventories", at = @At("HEAD"), cancellable = true, remap = false)
    private static void restrictedinventory$mergeInventories(List<ItemStack> source, Inventory playerInventory, CallbackInfo ci) {
        Player player = playerInventory.player;
        List<ItemStack> target = playerInventory.items;

        for (int sourceSlot = 0; sourceSlot < source.size(); sourceSlot++) {
            ItemStack sourceStack = source.get(sourceSlot);

            if (sourceStack.isEmpty()) {
                continue;
            }

            for (int targetSlot = 0; targetSlot < target.size() && !sourceStack.isEmpty(); targetSlot++) {
                ItemStack targetStack = target.get(targetSlot);
                if (targetStack.isEmpty()) {
                    continue;
                }
                if (!restrictedinventory$mayPlace(player, targetSlot, sourceStack)) {
                    continue;
                }
                restrictedinventory$attemptStackConsolidation(sourceStack, targetStack);
            }

            if (sourceStack.isEmpty()) {
                continue;
            }

            if (sourceSlot < target.size() && target.get(sourceSlot).isEmpty() && restrictedinventory$mayPlace(player, sourceSlot, sourceStack)) {
                restrictedinventory$setStack(target, sourceSlot, sourceStack);
                continue;
            }

            for (int targetSlot = 0; targetSlot < target.size() && !sourceStack.isEmpty(); targetSlot++) {
                if (!target.get(targetSlot).isEmpty()) {
                    continue;
                }
                if (!restrictedinventory$mayPlace(player, targetSlot, sourceStack)) {
                    continue;
                }
                restrictedinventory$setStack(target, targetSlot, sourceStack);
            }

            if (sourceStack.isEmpty()) {
                continue;
            }

            ItemStack offhandStack = playerInventory.offhand.get(0);

            if (offhandStack.isEmpty()) {
                if (restrictedinventory$mayPlace(player, 40, sourceStack)) {
                    restrictedinventory$setStack(playerInventory.offhand, 0, sourceStack);
                }
            } else if (restrictedinventory$mayPlace(player, 40, sourceStack)) {
                restrictedinventory$attemptStackConsolidation(sourceStack, offhandStack);
            }
        }

        ci.cancel();
    }

    @Unique
    private static boolean restrictedinventory$mayPlace(Player player, int slot, ItemStack stack) {
        Map<Integer, String> restrictedSlots = CommonConfig.restrictedSlots(player);
        String restriction = restrictedSlots.get(slot);

        if (restriction == null || restriction.isBlank()) {
            return true;
        }

        if (restriction.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(restriction.substring(1));

            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            return stack.is(tag);
        }

        ResourceLocation itemId = new ResourceLocation(restriction);

        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
    }

    @Unique
    private static void restrictedinventory$attemptStackConsolidation(ItemStack source, ItemStack target) {
        if (!ItemStack.isSameItemSameTags(source, target)) {
            return;
        }

        int availableSpace = target.getMaxStackSize() - target.getCount();
        int transferAmount = Math.min(source.getCount(), availableSpace);

        if (transferAmount <= 0) {
            return;
        }

        target.grow(transferAmount);
        source.shrink(transferAmount);
    }

    @Unique
    private static void restrictedinventory$setStack(List<ItemStack> inventory, int slot, ItemStack source) {
        inventory.set(slot, source.copy());
        source.setCount(0);
    }
}