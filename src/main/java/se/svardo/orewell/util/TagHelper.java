package se.svardo.orewell.util;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class TagHelper {

    private static final Map<String, Tag<Material>> MATERIAL_TAGS = new HashMap<>();



//    static {
//        for (Tag<?> tag : Tag.values()) {
//            if (Material.class.isAssignableFrom(tag.getRegistry())) {
//                @SuppressWarnings("unchecked")
//                Tag<Material> materialTag = (Tag<Material>) tag;
//                String key = tag.getKey().toString(); // "minecraft:ores"
//                MATERIAL_TAGS.put(key.toLowerCase(), materialTag);
//            }
//        }
//    }

    /**
     * Tries to find a Bukkit Tag<Material> by namespace:key string.
     * @param namespace e.g. "minecraft"
     * @param key e.g. "ores"
     * @return Tag<Material> or null
     */
    public static Tag<Material> getMaterialTag(String namespace, String key) {
        String combined = (namespace + ":" + key).toLowerCase();
        return MATERIAL_TAGS.get(combined);
    }

    /**
     * Checks if the given Material is in the tag.
     */
    public static boolean isMaterialInTag(Material material, String namespace, String key) {
        Tag<Material> tag = getMaterialTag(namespace, key);
        if (tag == null) return false;
        return tag.isTagged(material);
    }
}
