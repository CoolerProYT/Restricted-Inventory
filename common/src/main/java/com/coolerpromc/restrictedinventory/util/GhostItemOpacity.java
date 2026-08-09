package com.coolerpromc.restrictedinventory.util;

public final class GhostItemOpacity {
    private static final ThreadLocal<Float> CURRENT = ThreadLocal.withInitial(() -> 1.0F);

    private GhostItemOpacity() {
    }

    public static float current() {
        return CURRENT.get();
    }

    public static void render(float opacity, Runnable renderer) {
        float previous = CURRENT.get();
        CURRENT.set(Math.clamp(opacity, 0.0F, 1.0F));

        try {
            renderer.run();
        } finally {
            CURRENT.set(previous);
        }
    }
}