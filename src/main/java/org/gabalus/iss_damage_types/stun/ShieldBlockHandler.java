package org.gabalus.iss_damage_types.stun;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.StunGaugeS2C;

import static org.gabalus.iss_damage_types.stun.GaugeStore.*;
import static org.gabalus.iss_damage_types.stun.Thresholds.*;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class ShieldBlockHandler {
    private ShieldBlockHandler(){}

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent e) {
        LivingEntity tgt = e.getEntity();
        if (tgt.level().isClientSide) return;

        long now = tgt.level().getGameTime();
        var nbt = tgt.getPersistentData();
        nbt.putLong(NBTKeys.BLOCK_TICK, now);
        nbt.putDouble(NBTKeys.BLOCK_AMOUNT, Math.max(0.0D, e.getBlockedDamage()));

        if (!(tgt instanceof Player p) || !p.isBlocking()) return;

        double add = Math.max(0.0D, e.getBlockedDamage());
        if (add <= 0.0D) return;

        double threshold = computeThreshold(tgt);
        if (threshold <= 0.0D) return;

        double g = Math.min(threshold, getGauge(tgt) + add);
        markHitNow(tgt); // for decay grace

        if (g >= threshold) {
            int ticks = Math.max(0, Math.min(Config.stunMaxDurationTicks, Config.heavyStunDurationTicks));
            if (ticks > 0) {
                tgt.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }
            setGauge(tgt, 0.0D);
            tgt.getPersistentData().putLong(NBTKeys.STUN_CD, now + Config.stunCooldownTicks);
            sync(tgt, 0.0D, threshold);
        } else {
            setGauge(tgt, g);
            sync(tgt, g, threshold);
        }
    }

    // Optional bash (if you want to keep it)
    @SubscribeEvent
    public static void onStopUsingItem(final LivingEntityUseItemEvent.Stop e) {
        if (!(e.getEntity() instanceof Player p)) return;
        ItemStack st = e.getItem();
        if (!(st.getItem() instanceof ShieldItem)) return;

        long now = p.level().getGameTime();
        long lastBlock = p.getPersistentData().getLong(NBTKeys.BLOCK_TICK);
        if (lastBlock == 0L || now - lastBlock > Config.shieldBashWindowTicks) return;

        // Implement bash here if desired (omitted to keep this file focused)
    }

    @SubscribeEvent
    public static void clearShieldCooldown(final EntityTickEvent.Post e) {
        if (!Config.preventAxeShieldCooldown) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide) return;
        if (p.getCooldowns().isOnCooldown(Items.SHIELD)) {
            p.getCooldowns().removeCooldown(Items.SHIELD);
        }
    }

    @SubscribeEvent
    public static void onUseItemStart(PlayerInteractEvent.RightClickItem e) {
        if (!Config.replaceShieldBlock) return;
        ItemStack stack = e.getItemStack();
        if (stack.getItem() instanceof ShieldItem) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static void sync(LivingEntity target, double gauge, double threshold) {
        if (target.level().isClientSide) return;
        var pkt = new StunGaugeS2C(target.getId(), gauge, threshold);
        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            Network.sendTo(sp, pkt);
        }
        Network.sendTracking(target, pkt);
    }
}
