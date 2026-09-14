package com.coolerpromc.restrictedinventory.mixin;

import com.coolerpromc.restrictedinventory.helper.SlotHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Unique
    private boolean restrictedInventory$movedToDesignatedSlot;

    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void restrictedInventory$prioritizeDesignatedSlot(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir) {
        restrictedInventory$movedToDesignatedSlot = false;
        if (stack.isEmpty()) return;

        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;

        if (stack.isStackable()) {
            restrictedInventory$fillDesignatedSlots(self, stack, startIndex, endIndex, reverseDirection);
        }

        if (!stack.isEmpty()) {
            restrictedInventory$fillEmptyDesignatedSlot(self, stack, startIndex, endIndex, reverseDirection);
        }

        if (stack.isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void restrictedInventory$fillDesignatedSlots(AbstractContainerMenu menu, ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        int i = reverseDirection ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
            Slot slot = menu.slots.get(i);
            ItemStack existing = slot.getItem();
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(stack, existing) && SlotHelper.isDesignatedSlot(slot, stack)) {
                int max = slot.getMaxStackSize(existing);
                int combined = existing.getCount() + stack.getCount();
                if (combined <= max) {
                    stack.setCount(0);
                    existing.setCount(combined);
                } else if (existing.getCount() < max) {
                    stack.shrink(max - existing.getCount());
                    existing.setCount(max);
                }
                slot.setChanged();
                restrictedInventory$movedToDesignatedSlot = true;
            }

            i += reverseDirection ? -1 : 1;
        }
    }

    @Unique
    private void restrictedInventory$fillEmptyDesignatedSlot(AbstractContainerMenu menu, ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        int i = reverseDirection ? endIndex - 1 : startIndex;
        while (reverseDirection ? i >= startIndex : i < endIndex) {
            Slot slot = menu.slots.get(i);
            if (slot.getItem().isEmpty() && SlotHelper.isDesignatedSlot(slot, stack)) {
                int max = slot.getMaxStackSize(stack);
                slot.setByPlayer(stack.split(Math.min(stack.getCount(), max)));
                slot.setChanged();
                restrictedInventory$movedToDesignatedSlot = true;
                return;
            }

            i += reverseDirection ? -1 : 1;
        }
    }

    @ModifyReturnValue(method = "moveItemStackTo", at = @At("RETURN"))
    private boolean restrictedInventory$includeDesignatedSlotMove(boolean original) {
        return original || restrictedInventory$movedToDesignatedSlot;
    }
}
