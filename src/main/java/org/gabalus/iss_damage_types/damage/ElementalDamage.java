package org.gabalus.iss_damage_types.damage;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.gabalus.iss_damage_types.Iss_damage_types;
import org.gabalus.iss_damage_types.attr.ModAttributes;
import org.gabalus.iss_damage_types.stun.NBTKeys;

import java.util.Optional;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class ElementalDamage {
    private ElementalDamage(){}

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
        var attackerEnt = src.getEntity();
        if (!(attackerEnt instanceof LivingEntity attacker)) return;

        var direct = src.getDirectEntity();
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
            elemAdded = (float)(newTotal - base);
            e.setNewDamage((float)newTotal);
        } else if (attackerEnt == direct || direct instanceof net.minecraft.world.entity.projectile.AbstractArrow) {
            float before = e.getNewDamage();
            e.setNewDamage(before + totalElemental);
            elemAdded = totalElemental;
        }

        if (elemAdded > 0) {
            // mark on target (same tick) for subtraction in stun logic

            var nbt = e.getEntity().getPersistentData();
            nbt.putDouble(NBTKeys.ELEM_ADD, elemAdded);
            nbt.putLong  (NBTKeys.ELEM_TICK, e.getEntity().level().getGameTime());
            nbt.putInt   (NBTKeys.ELEM_ATTACKER, attackerEnt.getId());
        }
    }

    @SubscribeEvent
    public static void spells(SpellDamageEvent e) {
        if (Iss_damage_types.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        String school = e.getSpellDamageSource().spell().getSchoolType().getId().getPath();
        SpellDamageSource sds = e.getSpellDamageSource();
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

        float after = original + totalElemental;
        float elemAdded = after - original;
        if (elemAdded > 0 && srcEnt instanceof Player) {

            var nbt = e.getEntity().getPersistentData();
            nbt.putDouble(NBTKeys.ELEM_ADD, elemAdded);
            nbt.putLong  (NBTKeys.ELEM_TICK, e.getEntity().level().getGameTime());
            nbt.putInt   (NBTKeys.ELEM_ATTACKER, srcEnt.getId());
        }

        e.setAmount(after);
    }
}
