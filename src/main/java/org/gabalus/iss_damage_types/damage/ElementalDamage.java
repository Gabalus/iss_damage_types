package org.gabalus.iss_damage_types.damage;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
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
    private ElementalDamage(){}

    private static final Set<Integer> REENTRY = ConcurrentHashMap.newKeySet();

    private static float tryGetAttr(LivingEntity le, String idStr) {
        ResourceLocation id = ResourceLocation.parse(idStr);
        Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id);
        if (holder.isEmpty()) return -1.0F;
        AttributeInstance inst = le.getAttribute(holder.get());
        return inst != null ? (float) inst.getValue() : -1.0F;
    }

    @SubscribeEvent
    public static void attacks(LivingDamageEvent.Pre e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;
        var src = e.getSource();
        if (src.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        LivingEntity target = e.getEntity();
        if (REENTRY.contains(target.getId())) return;
        Entity attackerEnt = src.getEntity();
        if (!(attackerEnt instanceof LivingEntity attacker)) return;
        var direct = src.getDirectEntity();
        if (direct instanceof io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile) return;

        float bonus = 0.0F;
        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(attacker, "iss_damage_types:" + elem + "_attack_damage");
            if (elemDmg != -1.0F) {
                float resist = tryGetAttr(target, "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                elemDmg *= (2.0F - resist);
                bonus += elemDmg;
            }
        }
        if (bonus > 0.0F) {
            e.setNewDamage(e.getNewDamage() + bonus);
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
        float totalElemental = 0.0F;

        for (String elem : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            float elemDmg = tryGetAttr(caster, "iss_damage_types:" + elem + "_spell_damage");
            if (elemDmg != -1.0F) {
                if (school.equals(elem)) { elemDmg += original; original = 0.0F; }
                float resist = tryGetAttr(e.getEntity(), "irons_spellbooks:" + elem + "_magic_resist");
                if (resist == -1.0F) resist = 1.0F;
                elemDmg *= (2.0F - resist);
                totalElemental += elemDmg;
            } else if (caster instanceof Player p) {
                p.displayClientMessage(Component.literal("unable to get attribute"), true);
            }
        }

        e.setAmount(original);

        if (totalElemental > 0.0F) {
            var direct = sds.get().getDirectEntity();
            DamageSource trueSrc;
            if (direct != null) {
                trueSrc = e.getEntity().damageSources().indirectMagic(direct, caster);
            } else {
                trueSrc = e.getEntity().damageSources().indirectMagic(caster, caster);
            }
            int id = e.getEntity().getId();
            if (!REENTRY.add(id)) return;
            try {
                e.getEntity().hurt(trueSrc, totalElemental);
            } finally {
                REENTRY.remove(id);
            }
        }
    }
}
