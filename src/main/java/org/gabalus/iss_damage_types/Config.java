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
    
    // Energy Shield Default Values
    public static double defaultESMax;
    public static double defaultESRechargeRate;
    public static int defaultESRechargeDelay;
    public static double defaultESBreakThreshold;
    public static double defaultESOnKillGain;


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

        // Load default values for attributes (used when new players join)
        defaultESMax = DEFAULT_ES_MAX.get();
        defaultESRechargeRate = DEFAULT_ES_RECHARGE_RATE.get();
        defaultESRechargeDelay = DEFAULT_ES_RECHARGE_DELAY.get();
        defaultESBreakThreshold = DEFAULT_ES_BREAK_THRESHOLD.get();
        defaultESOnKillGain = DEFAULT_ES_ON_KILL_GAIN.get();
    }
}
