package com.arenapvp.kit;

import org.bukkit.inventory.ItemStack;

public record LocalKit(String name, ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
}
