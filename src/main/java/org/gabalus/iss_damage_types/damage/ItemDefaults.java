package org.gabalus.iss_damage_types.damage;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.gabalus.iss_damage_types.Config;
import org.gabalus.iss_damage_types.Iss_damage_types;

import java.util.List;

@EventBusSubscriber(modid = Iss_damage_types.MOD_ID)
public final class ItemDefaults {
    private ItemDefaults(){}

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
                e.addModifier(AD,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "weapon_base_damage"),
                                m.amount(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND);
            } else if (entry.attribute().equals(AS)) {
                AttributeModifier m = entry.modifier();
                e.addModifier(AS,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(Iss_damage_types.MOD_ID, "weapon_base_attack_speed"),
                                m.amount(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND);
            }
        }
    }
}
