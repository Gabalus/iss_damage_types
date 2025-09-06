package org.gabalus.iss_damage_types;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod("iss_damage_types")
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS = BUILDER
        .comment("In vanilla, when adding a new attribute to a weapon with only base attributes (such as attack damage and attack speed), it can overwrite them.\n"
               + "If you attempt to add 10 fire damage to a diamond sword that deals 7 damage, you may expect 17 damage.\n"
               + "But the +6 damage the sword gives can be overwritten, so it only deals 11 damage.\n"
               + "If this setting is true, a weapon's base damage and attack speed will be kept when adding a new attribute.")
        .define("copyWeaponsDefaultAttributesToNewWeapons", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean copyWeaponsDefaultAttributesToNewWeapons;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        copyWeaponsDefaultAttributesToNewWeapons = COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS.get();
    }
}
