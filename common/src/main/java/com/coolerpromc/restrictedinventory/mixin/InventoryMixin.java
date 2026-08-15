package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.config.CommonConfig;
import com.coolerpromc.restrictedinventory.config.util.Restriction;
import com.coolerpromc.restrictedinventory.mixin.accessor.InventoryAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Inject(method = "addResource(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void onAddResource(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        Inventory self = (Inventory)(Object)this;
        Map<Integer, Restriction> restricted = CommonConfig.restrictedSlots(self.player);

        int slot = restrictedInventory$findValidSlot(self, itemStack, restricted);

        if (slot == -1) {
            cir.setReturnValue(itemStack.getCount());
        } else {
            cir.setReturnValue(((InventoryAccessor) self).callAddResource(slot, itemStack));
        }
    }

    @WrapOperation(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSlotWithRemainingSpace(Lnet/minecraft/world/item/ItemStack;)I"))
    private int getSlotWithRemainingSpace(Inventory instance, ItemStack newItemStack, Operation<Integer> original){
        Map<Integer, Restriction> restricted = CommonConfig.restrictedSlots(instance.player);
        return restrictedInventory$findValidSlot(instance, newItemStack, restricted);
    }

    @WrapOperation(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getFreeSlot()I"))
    private int getFreeSlot(Inventory instance, Operation<Integer> original, @Local(argsOnly = true) ItemStack itemStack){
        Map<Integer, Restriction> restricted = CommonConfig.restrictedSlots(instance.player);
        return restrictedInventory$findValidSlot(instance, itemStack, restricted);
    }

    @Unique
    private int restrictedInventory$findValidSlot(Inventory inv, ItemStack incoming, Map<Integer, Restriction> restricted) {
        RegistryAccess registries = inv.player.registryAccess();

        for (int i = 0; i < 36; i++) {
            if (!restrictedInventory$isSlotAllowed(i, incoming, restricted, registries)) continue;
            ItemStack existing = inv.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, incoming) && existing.getCount() < inv.getMaxStackSize(existing)) {
                return i;
            }
        }

        for (Map.Entry<Integer, Restriction> entry : restricted.entrySet()) {
            int slot = entry.getKey();
            if (!inv.getItem(slot).isEmpty()) continue;
            if (restrictedInventory$isSlotAllowed(slot, incoming, restricted, registries)) return slot;
        }

        for (int i = 0; i < 36; i++) {
            if (restricted.containsKey(i)) continue;
            if (inv.getItem(i).isEmpty()) return i;
        }

        return -1;
    }

    @Unique
    private boolean restrictedInventory$isSlotAllowed(int slot, ItemStack incoming, Map<Integer, Restriction> restricted, RegistryAccess registries) {
        if (!restricted.containsKey(slot)) return true;

        return restricted.get(slot).matches(incoming, registries);
    }
}
