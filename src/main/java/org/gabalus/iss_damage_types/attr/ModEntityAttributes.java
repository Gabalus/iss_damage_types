package org.gabalus.iss_damage_types.attr;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.gabalus.iss_damage_types.Iss_damage_types;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class ModEntityAttributes {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_FIRE);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_ICE);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_LIGHTNING);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_HOLY);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_ENDER);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_BLOOD);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_EVOCATION);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_NATURE);
        event.add(EntityType.PLAYER, ModAttributes.ATTACK_ELDRITCH);

        event.add(EntityType.PLAYER, ModAttributes.SPELL_FIRE);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_ICE);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_LIGHTNING);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_HOLY);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_ENDER);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_BLOOD);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_EVOCATION);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_NATURE);
        event.add(EntityType.PLAYER, ModAttributes.SPELL_ELDRITCH);

        // Stun Attributes
        event.add(EntityType.PLAYER, ModAttributes.STUN_THRESHOLD_REDUCTION);
        event.add(EntityType.PLAYER, ModAttributes.STUN_RESISTANCE);
        event.add(EntityType.PLAYER, ModAttributes.STUN_POTENCY);
        event.add(EntityType.PLAYER, ModAttributes.STUN_BASE_THRESHOLD_PCT);
        event.add(EntityType.PLAYER, ModAttributes.STUN_MIN_DURATION);
        event.add(EntityType.PLAYER, ModAttributes.STUN_MAX_DURATION);
        event.add(EntityType.PLAYER, ModAttributes.STUN_COOLDOWN);
        event.add(EntityType.PLAYER, ModAttributes.STUN_OVERKILL_SCALE);
        event.add(EntityType.PLAYER, ModAttributes.STUN_GAUGE_DECAY);

        event.add(EntityType.PLAYER, ModAttributes.ES_MAX);
        event.add(EntityType.PLAYER, ModAttributes.ES_RECHARGE_RATE);
        event.add(EntityType.PLAYER, ModAttributes.ES_RECHARGE_DELAY);
        event.add(EntityType.PLAYER, ModAttributes.ES_BREAK_THRESHOLD);
        event.add(EntityType.PLAYER, ModAttributes.ES_ON_KILL_GAIN);

        // Shield Bash Attributes
        event.add(EntityType.PLAYER, ModAttributes.SHIELD_BASH_RANGE);
        event.add(EntityType.PLAYER, ModAttributes.SHIELD_BASH_ARC);
        event.add(EntityType.PLAYER, ModAttributes.SHIELD_BASH_DAMAGE);
        event.add(EntityType.PLAYER, ModAttributes.SHIELD_BASH_BLOCKED_PCT);
        event.add(EntityType.PLAYER, ModAttributes.SHIELD_BASH_WINDOW);

        event.add(EntityType.PLAYER, ModAttributes.STUN_SHIELD_MULT);
        event.add(EntityType.PLAYER, ModAttributes.HEAVY_STUN_THRESHOLD_MULT);
        event.add(EntityType.PLAYER, ModAttributes.HEAVY_STUN_DURATION);
        event.add(EntityType.PLAYER, ModAttributes.STUN_GAUGE_DECAY_DELAY);
    }
}
