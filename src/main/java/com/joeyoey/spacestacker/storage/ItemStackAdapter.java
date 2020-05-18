package com.joeyoey.spacestacker.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.spacestacker.SpaceStacker;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.List;

public class ItemStackAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {
    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ItemStack itemStack = new ItemStack(Material.AIR);
        JsonObject jsonObject = json.getAsJsonObject();

        itemStack.setType(Material.getMaterial(jsonObject.get("material").getAsString()));
        itemStack.setAmount(jsonObject.get("amount").getAsInt());
        itemStack.setDurability(jsonObject.get("durability").getAsByte());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!jsonObject.get("name").isJsonNull()) {
            itemMeta.setDisplayName(jsonObject.get("name").getAsString());
        }
        if (!jsonObject.get("lore").isJsonNull()) {
            itemMeta.setLore(SpaceStacker.gson.fromJson(jsonObject.get("lore"), new TypeToken<List<String>>() {
            }.getType()));
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the
     * {@code src} object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).</p>
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("material", src.getType().name());
        jsonObject.addProperty("amount", src.getAmount());
        jsonObject.addProperty("durability", src.getDurability());
        String name = !src.hasItemMeta() ? null : (!src.getItemMeta().hasDisplayName() ? null : src.getItemMeta().getDisplayName());
        jsonObject.addProperty("name", name);
        List<String> lore = !src.hasItemMeta() ? null : (!src.getItemMeta().hasLore() ? null : src.getItemMeta().getLore());
        jsonObject.add("lore", SpaceStacker.gson.toJsonTree(lore, new TypeToken<List<String>>(){}.getType()));

        return jsonObject;
    }
}
