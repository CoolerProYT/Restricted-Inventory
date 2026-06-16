package com.coolerpromc.restrictedinventory.mixin.accessor;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VanillaContainerWrapper.class)
public interface VanillaContainerWrapperAccessor {
    @Accessor("container")
    Container getContainer();
}
