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

        event.add(EntityType.PLAYER, ModAttributes.STUN_THRESHOLD_REDUCTION);
        event.add(EntityType.PLAYER, ModAttributes.STUN_RESISTANCE);
        event.add(EntityType.PLAYER, ModAttributes.STUN_POTENCY);

    }
}
