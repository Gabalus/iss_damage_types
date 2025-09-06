package org.gabalus.iss_damage_types;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.gabalus.iss_damage_types.network.Network;
import org.gabalus.iss_damage_types.network.StunGaugeS2C;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public class EventHandler {

    private static final String NBT_GAUGE = "iss_stun_gauge";
    private static final String NBT_LAST  = "iss_stun_last_update";

    private static final String NBT_LAST_ELEM_ADD      = "iss_last_elem_add";
    private static final String NBT_LAST_ELEM_TICK     = "iss_last_elem_tick";
    private static final String NBT_LAST_ELEM_ATTACKER = "iss_last_elem_attacker";

    private static final String NBT_HGAUGE = "iss_heavy_stun_gauge";
    private static final String NBT_HLAST  = "iss_heavy_stun_last";

    private static final String NBT_BLOCK_TICK   = "iss_block_last_tick";
    private static final String NBT_BLOCK_AMOUNT = "iss_block_last_amount";

    private static final String NBT_LAST_HIT = "iss_stun_last_hit";


    private static void markPlayerElementalAdd(LivingEntity target, Entity attacker, double elemAdded) {
        if (elemAdded <= 0 || !(attacker instanceof Player)) return;
        var nbt = target.getPersistentData();
        nbt.putDouble(NBT_LAST_ELEM_ADD, elemAdded);
        nbt.putLong(NBT_LAST_ELEM_TICK, target.level().getGameTime());
        nbt.putInt(NBT_LAST_ELEM_ATTACKER, attacker.getId());
    }

    private static double consumeRecentPlayerElementalAdd(LivingEntity target, Entity attacker, long now) {
        if (!(attacker instanceof Player)) return 0.0D;
        var nbt = target.getPersistentData();
        long when = nbt.getLong(NBT_LAST_ELEM_TICK);
        if (when != now) return 0.0D; // same-tick guard
        if (nbt.getInt(NBT_LAST_ELEM_ATTACKER) != attacker.getId()) return 0.0D;
        double v = nbt.getDouble(NBT_LAST_ELEM_ADD);
        // consume so we don't subtract twice
        nbt.putDouble(NBT_LAST_ELEM_ADD, 0.0D);
        return Math.max(0.0D, v);
    }



    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void calculateElementalDamageForAttacks(LivingDamageEvent.Pre e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        DamageSource src = e.getSource();
        Entity attackerEnt = src.getEntity();
        if (!(attackerEnt instanceof LivingEntity attacker)) return;

        Entity direct = src.getDirectEntity();
        if (direct instanceof io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile) return;

        float totalElemental = 0.0F;
        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(attacker, "iss_damage_types:" + elem + "_attack_damage");
            if (elemDmg != -1.0F) {
                float resist = tryGetAttr(e.getEntity(), "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                elemDmg *= (2.0F - resist);
                totalElemental += elemDmg;
            }
        }

        float elemAdded = 0.0F;

        if (direct instanceof Arrow arrow) {
            double base = arrow.getBaseDamage();
            double newTotal = base + totalElemental;
            elemAdded = (float) (newTotal - base); // == totalElemental
            e.setNewDamage((float) newTotal);
        } else if (attackerEnt == direct || direct instanceof net.minecraft.world.entity.projectile.AbstractArrow) {
            float before = e.getNewDamage();
            e.setNewDamage(before + totalElemental);
            elemAdded = totalElemental;
        }

        if (elemAdded > 0) {
            markPlayerElementalAdd(e.getEntity(), attacker, elemAdded);
        }
    }

    @SubscribeEvent
    public static void calculateElementalDamageForSpells(SpellDamageEvent e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        String school = e.getSpellDamageSource().spell().getSchoolType().getId().getPath();
        SpellDamageSource sds = e.getSpellDamageSource();
        Entity srcEnt = sds.get().getEntity();
        if (!(srcEnt instanceof LivingEntity caster)) return;

        float original = e.getOriginalAmount();
        float totalElemental = 0.0F;

        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(caster, "iss_damage_types:" + elem + "_spell_damage");
            if (elemDmg != -1.0F) {
                if (school.equals(elem)) {
                    elemDmg += original;
                    original = 0.0F;
                }
                float resist = tryGetAttr(e.getEntity(), "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                elemDmg *= (2.0F - resist);
                totalElemental += elemDmg;
            } else if (caster instanceof net.minecraft.world.entity.player.Player p) {
                p.displayClientMessage(Component.literal("unable to get attribute"), true);
            }
        }
        float before = original;
        float after  = before + totalElemental;
        float elemAdded = after - before;

        if (elemAdded > 0 && srcEnt instanceof Player) {
            markPlayerElementalAdd(e.getEntity(), srcEnt, elemAdded);
        }

        e.setAmount(after);
    }

    @SubscribeEvent
    public static void copyDefaultItemAttributes(ItemAttributeModifierEvent e) {
        if (!Config.copyWeaponsDefaultAttributesToNewWeapons) return;
        if (e.getItemStack().getItem().getEquipmentSlot(e.getItemStack()) != EquipmentSlot.MAINHAND) return;

        ItemStack stack = e.getItemStack();
        if (stack.isEmpty()) return;

        Holder<Attribute> AD = Attributes.ATTACK_DAMAGE;
        Holder<Attribute> AS = Attributes.ATTACK_SPEED;

        List<ItemAttributeModifiers.Entry> entries = e.getModifiers();

        for (ItemAttributeModifiers.Entry entry : entries) {
            if (entry.attribute().equals(AD)) {
                AttributeModifier m = entry.modifier();
                e.addModifier(
                        AD,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "weapon_base_damage"),
                                m.amount(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
            } else if (entry.attribute().equals(AS)) {
                AttributeModifier m = entry.modifier();
                e.addModifier(
                        AS,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "weapon_base_attack_speed"),
                                m.amount(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
            }
        }



    }

    private static float tryGetAttr(LivingEntity le, String idStr) {
        ResourceLocation id = ResourceLocation.parse(idStr);
        Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id);
        if (holder.isEmpty()) return -1.0F;

        AttributeInstance inst = le.getAttribute(holder.get());
        return inst != null ? (float) inst.getValue() : -1.0F;
    }

    private static final TagKey<DamageType> PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("iss_damage_types", "physical"));

    private static boolean isMinecraftPhysical(DamageSource src) {
        return src.is(PHYSICAL);
    }

    @SubscribeEvent
    public static void onStopUsingItem(final net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop e) {
        if (!(e.getEntity() instanceof Player p)) return;
        ItemStack st = e.getItem();
        if (!(st.getItem() instanceof ShieldItem)) return;

        long now = p.level().getGameTime();
        long lastBlock = p.getPersistentData().getLong(NBT_BLOCK_TICK);
        if (lastBlock == 0L || now - lastBlock > Config.shieldBashWindowTicks) return;

        double lastBlocked = p.getPersistentData().getDouble(NBT_BLOCK_AMOUNT);
        double dmg = Config.shieldBashBaseDamage + Config.shieldBashBlockedPct * lastBlocked;
        if (dmg <= 0) return;

        double range = Config.shieldBashRange;
        double cosHalf = Math.cos(Math.toRadians(Config.shieldBashArcDegrees));
        var look = p.getLookAngle().normalize();

        var aabb = p.getBoundingBox().inflate(range, 1.0, range);
        var list = p.level().getEntitiesOfClass(LivingEntity.class, aabb,
                le -> le.isAlive() && le != p && le.distanceTo(p) <= range &&
                        look.dot(le.position().subtract(p.position()).normalize()) >= cosHalf);

        if (list.isEmpty()) return;

        for (var le : list) {
            // damage
            le.hurt(p.damageSources().playerAttack(p), (float)dmg);

            int stunTicks = Math.max(10, Math.min(Config.stunMaxDurationTicks, 40));
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stunTicks, 10, false, true, true));
            le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     stunTicks, 10, false, true, true));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         stunTicks,  2, false, true, true));
        }
    }

    @SubscribeEvent
    public static void onLivingTickHeavy(final EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le.level().isClientSide) return;

        double g = getHeavyGauge(le);
        if (g <= 0.0D) return;

        long now = le.level().getGameTime();
        long last = lastHeavyUpdate(le);
        long dt = Math.max(1, now - last);

        double decayPerTick = Config.stunGaugeDecayPerSecond / 20.0D;
        double ng = Math.max(0.0D, g - decayPerTick * dt);

        if (ng != g) {
            setHeavyGauge(le, ng);
        }
    }


    @SubscribeEvent
    public static void clearShieldCooldown(final EntityTickEvent.Post e) {
        if (!Config.preventAxeShieldCooldown) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide) return;
        if (p.getCooldowns().isOnCooldown(net.minecraft.world.item.Items.SHIELD)) {
            p.getCooldowns().removeCooldown(net.minecraft.world.item.Items.SHIELD);
        }
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyStunOnBigHits(LivingDamageEvent.Post e) {
        LivingEntity target = e.getEntity();
        DamageSource source = e.getSource();
        if (target == null || target.level().isClientSide) return;

        long now = target.level().getGameTime();

        if (target instanceof Player) {
            if (target.getPersistentData().getLong(NBT_BLOCK_TICK) == now) {
                syncGauge(target, getGauge(target), computeThreshold(target));
            }
            return;
        }

        if (!isMinecraftPhysical(source)) return;

        final String TAG = "iss_stun_cd";
        if (now < target.getPersistentData().getLong(TAG)) return;

        double dmg = e.getNewDamage();
        if (dmg <= 0.0D) return;

        Entity attackerEnt = source.getEntity();
        double subtractElem = attackerEnt != null ? consumeRecentPlayerElementalAdd(target, attackerEnt, now) : 0.0D;
        double dmgForGauge = Math.max(0.0D, dmg - subtractElem);
        if (dmgForGauge <= 0.0D) return;

        double threshold = computeThreshold(target);
        if (threshold <= 0.0D) return;

        double gauge = Math.min(threshold, getGauge(target) + dmgForGauge);

        if (gauge >= threshold) {
            LivingEntity attacker = (attackerEnt instanceof LivingEntity le) ? le : null;
            double potency = attacker != null ? safeValue(attacker, ModAttributes.STUN_POTENCY, 0.0D) : 0.0D;
            double resist  = safeValue(target, ModAttributes.STUN_RESISTANCE, 0.0D);
            double duration = Config.stunMinDurationTicks * (1.0D + Config.stunOverkillScale);
            duration *= (1.0D + potency);
            duration *= (1.0D - resist);

            int ticks = (int)Math.max(0, Math.min(Config.stunMaxDurationTicks, Math.round(duration)));
            if (ticks > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }

            setGauge(target, 0.0D);
            target.getPersistentData().putLong(TAG, now + Config.stunCooldownTicks);
            syncGauge(target, 0.0D, threshold);
        } else {
            markHitNow(target);
            setGauge(target, gauge);
            syncGauge(target, gauge, threshold);
        }
    }


    private static void syncGauge(LivingEntity target, double gauge, double threshold) {
        if (target.level().isClientSide) return;

        var pkt = new org.gabalus.iss_damage_types.network.StunGaugeS2C(
                target.getId(), gauge, threshold);

        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            org.gabalus.iss_damage_types.network.Network.sendTo(sp, pkt);
        }
        org.gabalus.iss_damage_types.network.Network.sendTracking(target, pkt);
    }


    @SubscribeEvent
    public static void onLivingTick(final net.neoforged.neoforge.event.tick.EntityTickEvent.Post e) {
        var ent = e.getEntity();
        if (!(ent instanceof net.minecraft.world.entity.LivingEntity le)) return;
        if (le.level().isClientSide) return;

        long now = le.level().getGameTime();
        double gauge = getGauge(le);

        boolean shouldPeriodicSync = (now % Config.stunGaugeSyncIntervalTicks == 0);

        if (gauge > 0.0D) {
            long sinceHit = now - lastHit(le);

            if (sinceHit < Config.stunGaugeDecayDelayTicks) {
                le.getPersistentData().putLong(NBT_LAST, now);
                if (shouldPeriodicSync) {
                    double threshold = computeThreshold(le);
                    syncGauge(le, gauge, threshold);
                }
                return;
            }
        }

        long last = lastUpdate(le);
        long dt = Math.max(1, now - last);

        double decayPerTick = Config.stunGaugeDecayPerSecond / 20.0D;
        double newGauge = Math.max(0.0D, gauge - decayPerTick * dt);

        boolean changed = (newGauge != gauge);
        if (changed) setGauge(le, newGauge);

        if (changed || shouldPeriodicSync) {
            double threshold = computeThreshold(le);
            syncGauge(le, getGauge(le), threshold);
        }
    }





    private static double computeThreshold(LivingEntity target) {
        double basePct = Config.baseStunThresholdPct;
        double reduce  = safeValue(target, ModAttributes.STUN_THRESHOLD_REDUCTION, 0.0D);
        double threshold = target.getMaxHealth() * basePct * (1.0D - reduce);

        if (target instanceof Player p) {
            if (!hasShieldEquipped(p)) return 0.0D;
            threshold *= Config.shieldThresholdMultiplier;
            threshold *= safeValue(p, ModAttributes.STUN_SHIELD_ITEM_MULT, 1.0D);
        }
        return Math.max(0.0D, threshold);
    }



    private static double safeValue(LivingEntity ent,
                                    DeferredHolder<Attribute, Attribute> attr,
                                    double def) {

        if (attr ==null) return def;
        var inst = ent.getAttribute(attr);
        return inst != null ? inst.getValue() : def;
    }

    private static double getHeavyGauge(LivingEntity e) {
        return e.getPersistentData().getDouble(NBT_HGAUGE);
    }
    private static void setHeavyGauge(LivingEntity e, double v) {
        e.getPersistentData().putDouble(NBT_HGAUGE, Math.max(0.0D, v));
        e.getPersistentData().putLong(NBT_HLAST, e.level().getGameTime());
    }
    private static long lastHeavyUpdate(LivingEntity e) {
        return e.getPersistentData().getLong(NBT_HLAST);
    }

    private static double computeHeavyThreshold(LivingEntity target) {
        double th = computeThreshold(target);
        return th > 0 ? th * Config.heavyStunThresholdMult : 0.0D;
    }

    private static boolean hasShieldEquipped(LivingEntity ent) {
        ItemStack main = ent.getMainHandItem();
        ItemStack off  = ent.getOffhandItem();
        return (main.getItem() instanceof ShieldItem) || (off.getItem() instanceof ShieldItem);
    }

    @SubscribeEvent
    public static void onShieldBlock(net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent e) {
        LivingEntity tgt = e.getEntity();
        if (tgt.level().isClientSide) return;

        long now = tgt.level().getGameTime();
        var nbt = tgt.getPersistentData();
        nbt.putLong(NBT_BLOCK_TICK, now);
        nbt.putDouble(NBT_BLOCK_AMOUNT, Math.max(0.0D, e.getBlockedDamage()));

        if (!(tgt instanceof Player p) || !p.isBlocking()) return;

        double add = Math.max(0.0D, e.getBlockedDamage());
        if (add <= 0.0D) return;

        double threshold = computeThreshold(tgt);
        if (threshold <= 0.0D) return;

        double g = Math.min(threshold, getGauge(tgt) + add);

        markHitNow(tgt);

        if (g >= threshold) {
            int ticks = Math.max(0, Math.min(Config.stunMaxDurationTicks, Config.heavyStunDurationTicks));
            if (ticks > 0) {
                tgt.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,     ticks, 10, false, true, true));
                tgt.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,         ticks,  2, false, true, true));
            }
            setGauge(tgt, 0.0D);
            tgt.getPersistentData().putLong("iss_stun_cd", now + Config.stunCooldownTicks);
            syncGauge(tgt, 0.0D, threshold);
        } else {
            setGauge(tgt, g);
            syncGauge(tgt, g, threshold);
        }
    }



    @SubscribeEvent
    public static void onUseItemStart(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem e) {
        if (!Config.replaceShieldBlock) return;
        ItemStack stack = e.getItemStack();
        if (stack.getItem() instanceof ShieldItem) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static double getGauge(LivingEntity e) {
        return e.getPersistentData().getDouble(NBT_GAUGE);
    }
    private static void setGauge(LivingEntity e, double v) {
        e.getPersistentData().putDouble(NBT_GAUGE, Math.max(0.0D, v));
        e.getPersistentData().putLong(NBT_LAST, e.level().getGameTime());
    }
    private static long lastUpdate(LivingEntity e) {
        return e.getPersistentData().getLong(NBT_LAST);
    }

    private static void markHitNow(LivingEntity e) {
        e.getPersistentData().putLong(NBT_LAST_HIT, e.level().getGameTime());
    }
    private static long lastHit(LivingEntity e) {
        return e.getPersistentData().getLong(NBT_LAST_HIT);
    }
}
