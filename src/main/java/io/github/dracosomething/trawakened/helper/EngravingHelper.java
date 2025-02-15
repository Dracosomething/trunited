package io.github.dracosomething.trawakened.helper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class EngravingHelper {
    /**
     * removes 1 enchantment from an item.
     * @param itemStack - item with the enchant.
     * @param toClear - enchantment that should be removed.
     * @return - returns the item with the enchantment removed
     */
    public static ItemStack RemoveEnchantments(ItemStack itemStack, Enchantment toClear) {

        Map<Enchantment, Integer> enchantments = itemStack.getAllEnchantments();
        enchantments.remove(toClear);
        EnchantmentHelper.setEnchantments(enchantments, itemStack);

        return itemStack;
    }

    /**
     * removes all enchantment from an item
     * @param itemStack - the item of which the enchantments should be cleared
     * @return - the ItemStack with the enchantments removed
     */
    public static ItemStack RemoveAllEnchantments(ItemStack itemStack) {

        Map<Enchantment, Integer> enchantments = itemStack.getAllEnchantments();
        enchantments.clear();
        EnchantmentHelper.setEnchantments(enchantments, itemStack);

        return itemStack;
    }
}
