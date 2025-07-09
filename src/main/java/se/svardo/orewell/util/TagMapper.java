package se.svardo.orewell.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.NamespacedKey;
//import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

public class TagMapper {
//    private final JavaPlugin plugin;
//    private final Logger logger;
    private final Map<String, Tag<Material>> tagMap;

    public TagMapper() {
//        this.plugin = plugin;
//        this.logger = plugin.getLogger();
        this.tagMap = new HashMap<>();
        initializeTagMap();
    }

    /**
     * Dynamically initialize the mapping between string names and actual Tag objects
     * Uses reflection to find all static Tag<Material> fields in the Tag class
     */
    private void initializeTagMap() {
        try {
            Field[] fields = Tag.class.getDeclaredFields();
            int tagCount = 0;

            for (Field field : fields) {
                // Check if field is static, final, and of type Tag
                if (Modifier.isStatic(field.getModifiers()) &&
                        Modifier.isFinal(field.getModifiers()) &&
                        field.getType() == Tag.class) {

                    try {
                        field.setAccessible(true);
                        Object tagObject = field.get(null);

                        // Verify it's a Tag<Material> by checking if it can handle Material
                        if (tagObject instanceof Tag<?>) {
                            @SuppressWarnings("unchecked")
                            Tag<Material> tag = (Tag<Material>) tagObject;

                            // Convert field name to lowercase for easier matching
                            String tagName = field.getName().toLowerCase();
                            tagMap.put(tagName, tag);
                            tagCount++;
                        }
                    } catch (IllegalAccessException e) {
//                        logger.warning("Could not access tag field: " + field.getName());
                    } catch (Exception e) {
//                        logger.warning("Error processing tag field " + field.getName() + ": " + e.getMessage());
                    }
                }
            }

//            logger.info("Dynamically initialized tag mapping with " + tagCount + " tags");
        } catch (Exception e) {
//            logger.severe("Failed to initialize tag mapping: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a Tag object from a string name, including registry lookup for custom tags
     * @param tagName The string name of the tag
     * @return The Tag object, or null if not found
     */
    public Tag<Material> getTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }

        String normalizedName = tagName.toLowerCase().trim();

        // First check our static tag mapping
        Tag<Material> staticTag = tagMap.get(normalizedName);
        if (staticTag != null) {
            return staticTag;
        }

        // Try to find custom tags from registry (for datapack tags)
        try {
            // Handle namespaced keys (e.g., "minecraft:logs" or "mypack:custom_tag")
            NamespacedKey key;
            if (normalizedName.contains(":")) {
                String[] parts = normalizedName.split(":", 2);
                key = new NamespacedKey(parts[0], parts[1]);
            } else {
                // Default to minecraft namespace
                key = new NamespacedKey("minecraft", normalizedName);
            }

            // Try to get the tag from Bukkit's registry
            return org.bukkit.Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
        } catch (Exception e) {
            // If registry lookup fails, try items registry as fallback
            try {
                NamespacedKey key;
                if (normalizedName.contains(":")) {
                    String[] parts = normalizedName.split(":", 2);
                    key = new NamespacedKey(parts[0], parts[1]);
                } else {
                    key = new NamespacedKey("minecraft", normalizedName);
                }
                return org.bukkit.Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material.class);
            } catch (Exception ex) {
                // Both lookups failed
                return null;
            }
        }
    }

    /**
     * Get multiple tags from a list of string names
     * @param tagNames List of tag names
     * @return Set of valid Tag objects
     */
    public Set<Tag<Material>> getTags(List<String> tagNames) {
        Set<Tag<Material>> tags = new HashSet<>();

        for (String tagName : tagNames) {
            Tag<Material> tag = getTag(tagName);
            if (tag != null) {
                tags.add(tag);
            } else {
//                logger.warning("Unknown tag: " + tagName);
            }
        }

        return tags;
    }

//    /**
//     * Load tags from config file
//     * @param configPath The path in the config file where tags are stored
//     * @return Set of valid Tag objects
//     */
//    public Set<Tag<Material>> loadTagsFromConfig(String configPath) {
//        FileConfiguration config = plugin.getConfig();
//        List<String> tagNames = config.getStringList(configPath);
//
//        if (tagNames.isEmpty()) {
//            logger.warning("No tags found in config at path: " + configPath);
//            return new HashSet<>();
//        }
//
//        logger.info("Loading " + tagNames.size() + " tags from config");
//        Set<Tag<Material>> tags = getTags(tagNames);
//        logger.info("Successfully loaded " + tags.size() + " valid tags");
//
//        return tags;
//    }

    /**
     * Check if a material is in any of the specified tags
     * @param material The material to check
     * @param tags The tags to check against
     * @return true if the material is in any of the tags
     */
    public boolean isInTags(Material material, Set<Tag<Material>> tags) {
        for (Tag<Material> tag : tags) {
            if (tag.isTagged(material)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all available tag names
     * @return Set of all available tag names
     */
    public Set<String> getAvailableTagNames() {
        return new HashSet<>(tagMap.keySet());
    }

//    /**
//     * Validate tags from config and log any invalid ones
//     * @param configPath The path in the config file where tags are stored
//     * @return List of invalid tag names
//     */
//    public List<String> validateConfigTags(String configPath) {
//        FileConfiguration config = plugin.getConfig();
//        List<String> tagNames = config.getStringList(configPath);
//        List<String> invalidTags = new ArrayList<>();
//
//        for (String tagName : tagNames) {
//            if (getTag(tagName) == null) {
//                invalidTags.add(tagName);
//            }
//        }
//
//        if (!invalidTags.isEmpty()) {
//            logger.warning("Invalid tags found in config: " + String.join(", ", invalidTags));
//            logger.info("Available tags: " + String.join(", ", getAvailableTagNames()));
//        }
//
//        return invalidTags;
//    }

    /**
     * Check if a string represents a valid tag name
     * @param tagName The string to check
     * @return true if the string is a valid tag name
     */
    public boolean isValidTagName(String tagName) {
        return getTag(tagName) != null;
    }

    /**
     * Get suggestions for similar tag names (useful for typos)
     * @param input The input string to find suggestions for
     * @return List of similar tag names
     */
    public List<String> getSuggestions(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedInput = input.toLowerCase().trim();
        List<String> suggestions = new ArrayList<>();

        // Exact match first
        if (tagMap.containsKey(normalizedInput)) {
            suggestions.add(normalizedInput);
            return suggestions;
        }

        // Find tags that contain the input
        for (String tagName : tagMap.keySet()) {
            if (tagName.contains(normalizedInput)) {
                suggestions.add(tagName);
            }
        }

        // If no containing matches, find tags that start with the input
        if (suggestions.isEmpty()) {
            for (String tagName : tagMap.keySet()) {
                if (tagName.startsWith(normalizedInput)) {
                    suggestions.add(tagName);
                }
            }
        }

        // Sort suggestions by length (shorter = more relevant)
        suggestions.sort(Comparator.comparing(String::length));

        return suggestions;
    }

    /**
     * Refresh the tag mapping (useful if tags are added dynamically)
     */
    public void refreshTagMapping() {
        tagMap.clear();
        initializeTagMap();
//        logger.info("Refreshed tag mapping");
    }

//    /**
//     * Create a custom tag from a list of materials
//     * @param materials List of materials to include in the tag
//     * @return A custom tag containing the specified materials
//     */
//    public Tag<Material> createCustomTag(Set<Material> materials) {
//        return new Tag<Material>() {
//            @Override
//            public boolean isTagged(Material material) {
//                return materials.contains(material);
//            }
//
//            @Override
//            public Set<Material> getValues() {
//                return materials;
//            }
//
//            @Override
//            public NamespacedKey getKey() {
//                return new NamespacedKey(plugin, "custom_tag");
//            }
//        };
//    }
}
