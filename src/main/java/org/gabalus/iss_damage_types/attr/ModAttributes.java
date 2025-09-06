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

    // Stun Configuration Attributes
    public static final DeferredHolder<Attribute, Attribute> STUN_BASE_THRESHOLD_PCT = ATTRIBUTES.register("stun_base_threshold_pct",
            () -> new RangedAttribute("attribute.name.stun.base_threshold_pct", 0.10D, 0.01D, 1.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_MIN_DURATION = ATTRIBUTES.register("stun_min_duration",
            () -> new RangedAttribute("attribute.name.stun.min_duration", 20, 0, 600).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_MAX_DURATION = ATTRIBUTES.register("stun_max_duration",
            () -> new RangedAttribute("attribute.name.stun.max_duration", 60, 0, 600).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_COOLDOWN = ATTRIBUTES.register("stun_cooldown",
            () -> new RangedAttribute("attribute.name.stun.cooldown", 40, 0, 1200).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_OVERKILL_SCALE = ATTRIBUTES.register("stun_overkill_scale",
            () -> new RangedAttribute("attribute.name.stun.overkill_scale", 0.50D, 0.0D, 5.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_GAUGE_DECAY = ATTRIBUTES.register("stun_gauge_decay",
            () -> new RangedAttribute("attribute.name.stun.gauge_decay", 5.0D, 0.0D, 1000.0D).setSyncable(true));

    // Shield Bash Attributes
    public static final DeferredHolder<Attribute, Attribute> SHIELD_BASH_RANGE = ATTRIBUTES.register("shield_bash_range",
            () -> new RangedAttribute("attribute.name.shield_bash.range", 3.0D, 1.0D, 8.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHIELD_BASH_ARC = ATTRIBUTES.register("shield_bash_arc",
            () -> new RangedAttribute("attribute.name.shield_bash.arc", 60.0D, 10.0D, 180.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHIELD_BASH_DAMAGE = ATTRIBUTES.register("shield_bash_damage",
            () -> new RangedAttribute("attribute.name.shield_bash.damage", 4.0D, 0.0D, 2048.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHIELD_BASH_BLOCKED_PCT = ATTRIBUTES.register("shield_bash_blocked_pct",
            () -> new RangedAttribute("attribute.name.shield_bash.blocked_pct", 0.25D, 0.0D, 5.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHIELD_BASH_WINDOW = ATTRIBUTES.register("shield_bash_window",
            () -> new RangedAttribute("attribute.name.shield_bash.window", 10, 0, 40).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STUN_SHIELD_MULT =
            ATTRIBUTES.register("stun_shield_mult",
                    () -> new RangedAttribute("attribute.name.stun.shield_mult", 1.50D, 0.10D, 10.0D).setSyncable(true));

    // --- Heavy stun (while blocking) ---
    public static final DeferredHolder<Attribute, Attribute> HEAVY_STUN_THRESHOLD_MULT =
            ATTRIBUTES.register("heavy_stun_threshold_mult",
                    () -> new RangedAttribute("attribute.name.stun.heavy.threshold_mult", 1.25D, 0.10D, 10.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> HEAVY_STUN_DURATION =
            ATTRIBUTES.register("heavy_stun_duration",
                    () -> new RangedAttribute("attribute.name.stun.heavy.duration", 80, 0, 20 * 60).setSyncable(true)); // ticks

    // --- Decay delay ---
    public static final DeferredHolder<Attribute, Attribute> STUN_GAUGE_DECAY_DELAY =
            ATTRIBUTES.register("stun_gauge_decay_delay",
                    () -> new RangedAttribute("attribute.name.stun.gauge_decay_delay", 80, 0, 20 * 60 * 10).setSyncable(true)); // ticks
}
