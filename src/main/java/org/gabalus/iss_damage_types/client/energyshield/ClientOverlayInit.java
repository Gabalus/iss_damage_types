package org.gabalus.iss_damage_types.client.energyshield;

import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.gabalus.iss_damage_types.Iss_damage_types;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID, value = Dist.CLIENT)
public final class ClientOverlayInit {
    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.PLAYER_HEALTH,
                ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "energy_shield"),
                ESHudOverlay::render // DeltaTracker
        );
    }
}