package com.arenapvp.kit;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class KitSerializer {

    private KitSerializer() {
    }

    public static String serialize(ItemStack[] items) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             BukkitObjectOutputStream data = new BukkitObjectOutputStream(output)) {
            data.writeInt(items.length);
            for (ItemStack item : items) {
                data.writeObject(item);
            }
            return Base64Coder.encodeLines(output.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize kit items", ex);
        }
    }

    public static ItemStack[] deserialize(String data, int size) {
        try {
            byte[] bytes = Base64Coder.decodeLines(data);
            try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
                 BukkitObjectInputStream stream = new BukkitObjectInputStream(input)) {
                int length = stream.readInt();
                ItemStack[] items = new ItemStack[size];
                for (int i = 0; i < length && i < size; i++) {
                    items[i] = (ItemStack) stream.readObject();
                }
                return items;
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to deserialize kit items", ex);
        }
    }
}
