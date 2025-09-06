package org.gabalus.iss_damage_types.client.energyshield;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class ESHudOverlay {
    private ESHudOverlay() {}

    public static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null) return;

        double cur = p.getPersistentData().getDouble("es_current_client");
        double max = p.getPersistentData().getDouble("es_max_client");
        if (max <= 0) return;

        int x = 10, y = 10, w = 90, h = 6;
        int filled = (int) Math.round(w * (cur / max));

        gfx.fill(x, y, x + w, y + h, 0xAA000000);                 // bg
        gfx.fill(x + 1, y + 1, x + 1 + filled, y + h - 1, 0xFF5EC8FF); // bar
        gfx.fill(x, y, x + w, y + 1, 0xFFFFFFFF);                 // border
        gfx.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        gfx.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        gfx.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);

        gfx.drawString(mc.font, String.format("Shield: %.0f / %.0f", cur, max),
                x, y + h + 3, 0xA0D8FF, false);
    }
}