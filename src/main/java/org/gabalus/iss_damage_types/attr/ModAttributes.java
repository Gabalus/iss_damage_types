package org.gabalus.iss_damage_types.attr;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.gabalus.iss_damage_types.Iss_damage_types;

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

// Attacker-side multiplier for stun duration scaling (0 = none, 1.0 = +100% duration, etc.)
    private static DeferredHolder<Attribute, Attribute> registerAttack(String element) {
        return ATTRIBUTES.register(element + "_attack_damage",
            () -> new RangedAttribute("attack_damage." + element, 0.0D, 0.0D, 2048.0D).setSyncable(true));
    }

    private static DeferredHolder<Attribute, Attribute> registerSpell(String element) {
        return ATTRIBUTES.register(element + "_spell_damage",
            () -> new RangedAttribute("spell_damage." + element, 0.0D, 0.0D, 2048.0D).setSyncable(true));
    }

    public static final DeferredHolder<Attribute, Attribute> ES_MAX = ATTRIBUTES.register("es_max",
            () -> new RangedAttribute("attribute.name.energyshield.max", 0, 0.0D, 1024.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ES_RECHARGE_RATE = ATTRIBUTES.register("es_recharge_rate",
            () -> new RangedAttribute("attribute.name.energyshield.recharge_rate", 1, 0.0D, 1024.0).setSyncable(true)); // per second

    public static final DeferredHolder<Attribute, Attribute> ES_RECHARGE_DELAY = ATTRIBUTES.register("es_recharge_delay",
            () -> new RangedAttribute("attribute.name.energyshield.recharge_delay", 60, 0.0D, 20 * 60 * 60).setSyncable(true)); // ticks

    public static final DeferredHolder<Attribute, Attribute> ES_BREAK_THRESHOLD = ATTRIBUTES.register("es_break_threshold",
            () -> new RangedAttribute("attribute.name.energyshield.break_threshold", 0.1, 0.0D, 1024.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ES_ON_KILL_GAIN = ATTRIBUTES.register("es_on_kill_gain",
            () -> new RangedAttribute("attribute.name.energyshield.on_kill_gain", 0, 0.0D, 1024.0).setSyncable(true));
}
