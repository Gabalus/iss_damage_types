package org.gabalus.iss_damage_types;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS = BUILDER
        .comment("In vanilla, when adding a new attribute to a weapon with only base attributes (such as attack damage and attack speed), it can overwrite them.\n"
               + "If you attempt to add 10 fire damage to a diamond sword that deals 7 damage, you may expect 17 damage.\n"
               + "But the +6 damage the sword gives can be overwritten, so it only deals 11 damage.\n"
               + "If this setting is true, a weapon's base damage and attack speed will be kept when adding a new attribute.")
        .define("copyWeaponsDefaultAttributesToNewWeapons", true);

    private static final ModConfigSpec.BooleanValue REPLACE_SHIELD_BLOCK = BUILDER
            .comment("If true, disables vanilla right-click shield blocking and uses stun-threshold-as-shield instead.")
            .define("stun.replaceShieldBlock", true);

    private static final ModConfigSpec.DoubleValue SHIELD_THRESHOLD_MULTIPLIER = BUILDER
            .comment("Multiplier applied to player's stun threshold while a shield is equipped (PoE2-like).")
            .defineInRange("stun.shieldThresholdMultiplier", 1.50D, 0.1D, 10.0D);
    private static final ModConfigSpec.DoubleValue BASE_STUN_THRESHOLD_PCT = BUILDER
            .comment("Base fraction of Max HP used as Stun Threshold (before attribute scaling).")
            .defineInRange("stun.baseThresholdPct", 0.10D, 0.01D, 1.0D); // default 10% of max HP

    private static final ModConfigSpec.IntValue STUN_MIN_DURATION_TICKS = BUILDER
            .comment("Minimum stun duration in ticks for a threshold-breaking hit.")
            .defineInRange("stun.minDurationTicks", 20, 0, 20 * 30); // default 1s

    private static final ModConfigSpec.IntValue STUN_MAX_DURATION_TICKS = BUILDER
            .comment("Maximum clamp for stun duration in ticks.")
            .defineInRange("stun.maxDurationTicks", 60, 0, 20 * 30); // default 3s

    private static final ModConfigSpec.IntValue STUN_COOLDOWN_TICKS = BUILDER
            .comment("Per-target cooldown after being stunned.")
            .defineInRange("stun.cooldownTicks", 40, 0, 20 * 60); // default 2s

    private static final ModConfigSpec.DoubleValue STUN_OVERKILL_SCALE = BUILDER
            .comment("Extra duration scale when damage exceeds threshold. " +
                    "Duration = minDur * (1 + overkill * this) before resist/potency.")
            .defineInRange("stun.overkillScale", 0.50D, 0.0D, 5.0D);

    private static final ModConfigSpec.DoubleValue STUN_GAUGE_DECAY_PER_SECOND = BUILDER
            .comment("How fast the stun gauge decays when not taking damage (damage per second).")
            .defineInRange("stun.gaugeDecayPerSecond", 5.0D, 0.0D, 1000.0D);

    private static final ModConfigSpec.IntValue STUN_GAUGE_SYNC_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) the server syncs stun gauge values to clients.")
            .defineInRange("stun.gaugeSyncIntervalTicks", 5, 1, 200);

    private static final ModConfigSpec.BooleanValue PREVENT_AXE_SHIELD_COOLDOWN = BUILDER
            .comment("Removes vanilla axe-induced shield cooldown so you can re-raise immediately.")
            .define("stun.preventAxeShieldCooldown", true);

    private static final ModConfigSpec.DoubleValue HEAVY_STUN_THRESHOLD_MULT = BUILDER
            .comment("Heavy Stun threshold = (normal threshold) * this when blocking.")
            .defineInRange("stun.heavyThresholdMult", 1.25D, 0.5D, 5.0D);

    private static final ModConfigSpec.IntValue HEAVY_STUN_DURATION_TICKS = BUILDER
            .comment("Duration of Heavy Stun when the heavy gauge fills.")
            .defineInRange("stun.heavyStunDurationTicks", 80, 0, 20 * 30); // default 4s

    private static final ModConfigSpec.IntValue SHIELD_BASH_WINDOW_TICKS = BUILDER
            .comment("Release shield within this many ticks after a block to perform a bash.")
            .defineInRange("stun.bashWindowTicks", 10, 0, 40);

    private static final ModConfigSpec.DoubleValue SHIELD_BASH_RANGE = BUILDER
            .comment("Max range of shield bash.")
            .defineInRange("stun.bashRange", 3.0D, 1.0D, 8.0D);

    private static final ModConfigSpec.IntValue SHIELD_BASH_ARC_DEGREES = BUILDER
            .comment("Half-angle of the bash cone (degrees). 90 means 180° wide.")
            .defineInRange("stun.bashArcDegrees", 60, 10, 180);

    private static final ModConfigSpec.DoubleValue SHIELD_BASH_BASE_DAMAGE = BUILDER
            .comment("Base damage of shield bash.")
            .defineInRange("stun.bashBaseDamage", 4.0D, 0.0D, 2048.0D);

    // how long to wait (ticks) after last damage before gauge starts decaying
    private static final ModConfigSpec.IntValue STUN_GAUGE_DECAY_DELAY_TICKS = BUILDER
            .comment("Ticks to wait after last damage before stun gauge begins to decay.")
            .defineInRange("stun.gaugeDecayDelayTicks", 80, 0, 20 * 60); // default 4s

    private static final ModConfigSpec.DoubleValue SHIELD_BASH_BLOCKED_PCT = BUILDER
            .comment("Extra Shield Bash damage as a fraction of the last blocked damage.")
            .defineInRange("stun.bashBlockedPct", 0.25D, 0.0D, 5.0D);


    // Energy Shield Defaults - These are now attributes and can be modified in-game
    // The values here are just defaults for new players
    private static final ModConfigSpec.DoubleValue DEFAULT_ES_MAX = BUILDER
            .comment("Default maximum energy shield value for new players. Can be modified in-game with attributes.")
            .defineInRange("energyshield.defaultMax", 0.0D, 0.0D, 1024.0D);

    private static final ModConfigSpec.DoubleValue DEFAULT_ES_RECHARGE_RATE = BUILDER
            .comment("Default energy shield recharge rate for new players. Can be modified in-game with attributes.")
            .defineInRange("energyshield.defaultRechargeRate", 1.0D, 0.0D, 1024.0D);

    private static final ModConfigSpec.IntValue DEFAULT_ES_RECHARGE_DELAY = BUILDER
            .comment("Default delay in ticks for new players. Can be modified in-game with attributes.")
            .defineInRange("energyshield.defaultRechargeDelay", 60, 0, 20 * 60);

    private static final ModConfigSpec.DoubleValue DEFAULT_ES_BREAK_THRESHOLD = BUILDER
            .comment("Default break threshold for new players. Can be modified in-game with attributes.")
            .defineInRange("energyshield.defaultBreakThreshold", 0.1D, 0.0D, 1024.0D);

    private static final ModConfigSpec.DoubleValue DEFAULT_ES_ON_KILL_GAIN = BUILDER
            .comment("Default on-kill gain for new players. Can be modified in-game with attributes.")
            .defineInRange("energyshield.defaultOnKillGain", 0.0D, 0.0D, 1024.0D);




    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean copyWeaponsDefaultAttributesToNewWeapons;

    public static double baseStunThresholdPct;
    public static int stunMinDurationTicks;
    public static int stunMaxDurationTicks;
    public static int stunCooldownTicks;
    public static double stunOverkillScale;
    public static boolean replaceShieldBlock;
    public static double shieldThresholdMultiplier;
    public static double stunGaugeDecayPerSecond;
    public static int stunGaugeSyncIntervalTicks;

    public static boolean preventAxeShieldCooldown;
    public static double heavyStunThresholdMult;
    public static int    heavyStunDurationTicks;
    public static int    shieldBashWindowTicks;
    public static double shieldBashRange;
    public static int    shieldBashArcDegrees;
    public static double shieldBashBaseDamage;
    public static double shieldBashBlockedPct;
    
    // Energy Shield Default Values
    public static double defaultESMax;
    public static double defaultESRechargeRate;
    public static int defaultESRechargeDelay;
    public static double defaultESBreakThreshold;
    public static double defaultESOnKillGain;
    
    public static int stunGaugeDecayDelayTicks;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading e) {
        if (e.getConfig().getSpec() == SPEC) load();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading e) {
        if (e.getConfig().getSpec() == SPEC) load();
    }

    private static void load() {
        // Load configuration values that are not attributes
        copyWeaponsDefaultAttributesToNewWeapons = COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS.get();
        replaceShieldBlock = REPLACE_SHIELD_BLOCK.get();
        preventAxeShieldCooldown = PREVENT_AXE_SHIELD_COOLDOWN.get();
        
        // Load default values for attributes (used when new players join)
        defaultESMax = DEFAULT_ES_MAX.get();
        defaultESRechargeRate = DEFAULT_ES_RECHARGE_RATE.get();
        defaultESRechargeDelay = DEFAULT_ES_RECHARGE_DELAY.get();
        defaultESBreakThreshold = DEFAULT_ES_BREAK_THRESHOLD.get();
        defaultESOnKillGain = DEFAULT_ES_ON_KILL_GAIN.get();

        baseStunThresholdPct = BASE_STUN_THRESHOLD_PCT.get();
        stunMinDurationTicks = STUN_MIN_DURATION_TICKS.get();
        stunMaxDurationTicks = STUN_MAX_DURATION_TICKS.get();
        stunCooldownTicks = STUN_COOLDOWN_TICKS.get();
        stunOverkillScale = STUN_OVERKILL_SCALE.get();
        stunGaugeDecayPerSecond = STUN_GAUGE_DECAY_PER_SECOND.get();
        stunGaugeSyncIntervalTicks = STUN_GAUGE_SYNC_INTERVAL_TICKS.get();
        shieldThresholdMultiplier = SHIELD_THRESHOLD_MULTIPLIER.get();
        heavyStunThresholdMult = HEAVY_STUN_THRESHOLD_MULT.get();
        heavyStunDurationTicks = HEAVY_STUN_DURATION_TICKS.get();
        shieldBashWindowTicks = SHIELD_BASH_WINDOW_TICKS.get();
        shieldBashRange = SHIELD_BASH_RANGE.get();
        shieldBashArcDegrees = SHIELD_BASH_ARC_DEGREES.get();
        shieldBashBaseDamage = SHIELD_BASH_BASE_DAMAGE.get();
        shieldBashBlockedPct = SHIELD_BASH_BLOCKED_PCT.get();
        stunGaugeDecayDelayTicks = STUN_GAUGE_DECAY_DELAY_TICKS.get();
    }
}
