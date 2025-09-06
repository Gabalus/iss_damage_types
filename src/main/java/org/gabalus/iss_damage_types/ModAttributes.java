package org.gabalus.iss_damage_types;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Iss_damage_types.MOD_ID);

    public static final List<String> ELEMENTAL_ATTRIBUTE_NAMES = List.of(
        "fire","ice","lightning","holy","ender","blood","evocation","nature","eldritch"
    );

    public static final DeferredHolder<Attribute, Attribute> ATTACK_FIRE       = registerAttack("fire");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_ICE        = registerAttack("ice");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_LIGHTNING  = registerAttack("lightning");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_HOLY       = registerAttack("holy");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_ENDER      = registerAttack("ender");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_BLOOD      = registerAttack("blood");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_EVOCATION  = registerAttack("evocation");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_NATURE     = registerAttack("nature");
    public static final DeferredHolder<Attribute, Attribute> ATTACK_ELDRITCH   = registerAttack("eldritch");

    public static final DeferredHolder<Attribute, Attribute> SPELL_FIRE        = registerSpell("fire");
    public static final DeferredHolder<Attribute, Attribute> SPELL_ICE         = registerSpell("ice");
    public static final DeferredHolder<Attribute, Attribute> SPELL_LIGHTNING   = registerSpell("lightning");
    public static final DeferredHolder<Attribute, Attribute> SPELL_HOLY        = registerSpell("holy");
    public static final DeferredHolder<Attribute, Attribute> SPELL_ENDER       = registerSpell("ender");
    public static final DeferredHolder<Attribute, Attribute> SPELL_BLOOD       = registerSpell("blood");
    public static final DeferredHolder<Attribute, Attribute> SPELL_EVOCATION   = registerSpell("evocation");
    public static final DeferredHolder<Attribute, Attribute> SPELL_NATURE      = registerSpell("nature");
    public static final DeferredHolder<Attribute, Attribute> SPELL_ELDRITCH    = registerSpell("eldritch");

    public static final DeferredHolder<Attribute, Attribute> STUN_SHIELD_ITEM_MULT =
            ATTRIBUTES.register("stun_shield_item_mult",
                    () -> new RangedAttribute("stun.shield_item_mult", 1.00D, 0.10D, 5.00D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_THRESHOLD_REDUCTION =
            ATTRIBUTES.register("stun_threshold_reduction",
                    () -> new RangedAttribute("stun.threshold_reduction", 0.0D, 0.0D, 0.9D).setSyncable(true));
// Reduces target’s threshold by up to 90%. 0.20 = 20% reduction.

    public static final DeferredHolder<Attribute, Attribute> STUN_RESISTANCE =
            ATTRIBUTES.register("stun_resistance",
                    () -> new RangedAttribute("stun.resistance", 0.0D, 0.0D, 0.9D).setSyncable(true));
// Reduces received stun duration by up to 90%.

    public static final DeferredHolder<Attribute, Attribute> STUN_POTENCY =
            ATTRIBUTES.register("stun_potency",
                    () -> new RangedAttribute("stun.potency", 0.0D, 0.0D, 5.0D).setSyncable(true));
// Attacker-side multiplier for stun duration scaling (0 = none, 1.0 = +100% duration, etc.)

    private static DeferredHolder<Attribute, Attribute> registerAttack(String element) {
        return ATTRIBUTES.register(element + "_attack_damage",
            () -> new RangedAttribute("attack_damage." + element, 0.0D, 0.0D, 2048.0D).setSyncable(true));
    }

    private static DeferredHolder<Attribute, Attribute> registerSpell(String element) {
        return ATTRIBUTES.register(element + "_spell_damage",
            () -> new RangedAttribute("spell_damage." + element, 0.0D, 0.0D, 2048.0D).setSyncable(true));
    }
}
