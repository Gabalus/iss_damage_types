package org.gabalus.iss_damage_types.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = "iss_damage_types")
public class HudOverlays {

    @SubscribeEvent
    public static void onRenderGui(final net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics gg = e.getGuiGraphics();
        PoseStack pose = gg.pose();

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        double pThreshold = ClientThresholds.playerThreshold(mc.player);
        double pGauge     = ClientGaugeCache.gauge(mc.player.getId());
        if (pThreshold > 0) {
            double left = Math.max(0.0D, pThreshold - pGauge);
            float fill = (float)Math.max(0.0D, Math.min(1.0D, pGauge / pThreshold));
            drawBar(gg, pose, sw/2 - 91, sh - 60, 182, 6, fill,
                    "Stun: " + formatAmount(pGauge) + " / " + formatAmount(pThreshold) +
                            "  (" + formatAmount(left) + " left)");
        }


        Entity looked = null;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            looked = ((EntityHitResult) mc.hitResult).getEntity();
        }

        if (looked instanceof LivingEntity le) {
            int id = le.getId();
            double tThreshold = ClientGaugeCache.threshold(id);
            if (tThreshold <= 0.0D) tThreshold = ClientThresholds.entityThreshold(le); // fallback
            double tGauge = ClientGaugeCache.gauge(id);
            if (tThreshold > 0) {
                double left = Math.max(0.0D, tThreshold - tGauge);
                float fill = (float)Math.max(0.0D, Math.min(1.0D, tGauge / tThreshold));
                drawBar(gg, pose, sw/2 - 60, sh/2 + 10, 120, 5, fill,
                        le.getDisplayName().getString() + " — " +
                                formatAmount(tGauge) + " / " + formatAmount(tThreshold) +
                                " (" + formatAmount(left) + " left)");
            }
        }
    }

    private static String formatAmount(double v) {
        return String.format("%.1f", v);
    }

    private static void drawBar(GuiGraphics g, PoseStack pose, int x, int y, int w, int h, float fill, String label) {
        g.fill(x, y, x + w, y + h, 0x66000000);

        int fw = Math.max(0, Math.min(w, Math.round(w * fill)));
        g.fill(x, y, x + fw, y + h, 0x99FFD200);

        g.renderOutline(x, y, w, h, 0xFF000000);

        g.drawString(Minecraft.getInstance().font, label, x + 2, y - 9, 0xFFFFFF, false);
    }
}
