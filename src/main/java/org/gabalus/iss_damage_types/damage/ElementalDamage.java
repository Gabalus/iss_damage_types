package org.gabalus.iss_damage_types.damage;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.attr.ModAttributes;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class ElementalDamage {
    private ElementalDamage() {}

    private static final Set<Integer> REENTRY = ConcurrentHashMap.newKeySet();

    private static float tryGetAttr(LivingEntity le, String idStr) {
        ResourceLocation id = ResourceLocation.parse(idStr);
        Optional<Holder.Reference<Attribute>> holder = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getHolder(id);
        if (holder.isEmpty()) return -1.0F;
        AttributeInstance inst = le.getAttribute(holder.get());
        return inst != null ? (float) inst.getValue() : -1.0F;
    }

    private static Holder<DamageType> resolveElemType(LivingEntity ctx, String elem) {
        var reg = ctx.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", elem + "_magic");
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, id);
        return reg.getHolder(key).orElse(null);
    }

    private static void hurtByElement(LivingEntity target, LivingEntity attacker, Entity direct, String elem, float amount) {
        if (amount <= 0.0F) return;
        Holder<DamageType> type = resolveElemType(target, elem);
        if (type == null) return;
        DamageSource src = new DamageSource(type, attacker, direct);
        int id = target.getId();
        if (!REENTRY.add(id)) return;
        try {
            target.hurt(src, amount);
        } finally {
            REENTRY.remove(id);
        }
    }

    @SubscribeEvent
    public static void attacks(LivingDamageEvent.Pre e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;
        var baseSrc = e.getSource();
        LivingEntity target = e.getEntity();
        if (REENTRY.contains(target.getId())) return;
        Entity attackerEnt = baseSrc.getEntity();
        if (!(attackerEnt instanceof LivingEntity attacker)) return;
        var direct = baseSrc.getDirectEntity();
        if (direct instanceof io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile) return;

        float perElemApplied = 0.0F;
        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(attacker, "iss_damage_types:" + elem + "_attack_damage");
            if (elemDmg != -1.0F) {
                float resist = tryGetAttr(target, "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                float finalAmt = elemDmg * (2.0F - resist);
                if (finalAmt > 0.0F) {
                    hurtByElement(target, attacker, attacker, elem, finalAmt);
                    perElemApplied += finalAmt;
                }
            }
        }
        if (perElemApplied > 0.0F) {
            e.setNewDamage(e.getNewDamage());
        }
    }

    @SubscribeEvent
    public static void spells(SpellDamageEvent e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;
        SpellDamageSource sds = e.getSpellDamageSource();
        String school = sds.spell().getSchoolType().getId().getPath();
        var srcEnt = sds.get().getEntity();
        if (!(srcEnt instanceof LivingEntity caster)) return;

        float original = e.getOriginalAmount();
        float keptBase = original;

        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(caster, "iss_damage_types:" + elem + "_spell_damage");
            if (elemDmg != -1.0F) {
                if (school.equals(elem)) {
                    elemDmg += keptBase;
                    keptBase = 0.0F;
                }
                float resist = tryGetAttr(e.getEntity(), "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                float finalAmt = elemDmg * (2.0F - resist);
                if (finalAmt > 0.0F) {
                    var direct = sds.get().getDirectEntity();
                    hurtByElement(e.getEntity(), caster, direct != null ? direct : caster, elem, finalAmt);
                }
            } else if (caster instanceof Player p) {
                p.displayClientMessage(Component.literal("unable to get attribute"), true);
            }
        }

        e.setAmount(keptBase);
    }
}
