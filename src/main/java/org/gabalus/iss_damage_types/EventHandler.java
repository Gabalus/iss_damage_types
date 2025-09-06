package org.gabalus.iss_damage_types;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public class EventHandler {

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

        if (direct instanceof Arrow arrow) {
            double base = arrow.getBaseDamage();
            double newTotal = base + totalElemental;
            e.setNewDamage((float) newTotal);
        } else if (attackerEnt == direct || direct instanceof net.minecraft.world.entity.projectile.AbstractArrow) {
            e.setNewDamage(e.getNewDamage() + totalElemental);
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

        e.setAmount(original + totalElemental);
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
}
