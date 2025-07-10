package se.svardo.orewell;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import org.jetbrains.annotations.NotNull;
import se.svardo.orewell.util.TagHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Orewell extends JavaPlugin implements Listener {

    private NamespacedKey placedKey;

    private final Set<Tag<Material>> trackedTags = new HashSet<>();
    private final Set<Material> trackedBlocks = new HashSet<>();

    private TagHelper tagHelper;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        tagHelper = new TagHelper(this);

        loadTrackedTags(config);

        loadTrackedBlocks(config);


        placedKey = new NamespacedKey(this, "player_placed");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void loadTrackedBlocks(FileConfiguration config) {
        List<String> blockNames = config.getStringList("tracked-blocks");
        for (String blockName : blockNames) {
            try {
                Material mat = Material.valueOf(blockName.toUpperCase());
                trackedBlocks.add(mat);
                getLogger().info("Tracking block: " + blockName);

                setupObjective("block_" + blockName.toLowerCase(), "Tracked Block " + blockName);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid block name in config: " + blockName);
            }
        }
        getLogger().info("Tracking " + trackedBlocks.size() + "valid blocks");
    }

    private void loadTrackedTags(FileConfiguration config) {
        List<String> tagNames = config.getStringList("tracked-tags");

        validateTagNames(tagNames);

        for (String tagName : tagNames) {
            try {
                Tag<Material> tag = tagHelper.getTag(tagName);
                trackedTags.add(tag);
                getLogger().info("Tracking tag: " + tagName);

                setupObjective("tag_" + asCommandFriendly(tagName).toLowerCase(), "Tag: " + tagName);
            } catch (IllegalArgumentException e){
                getLogger().warning("Invalid tag name in config: " + tagName);

            }
        }

        getLogger().info("Tracking " + trackedTags.size() + "valid tags");
    }

    private void validateTagNames(List<String> tagNamesToValidate) {

        List<String> invalidTags = new ArrayList<>();

        for (String tagName : tagNamesToValidate) {
            if (tagHelper.getTag(tagName) == null) {
                invalidTags.add(tagName);
            }
        }

        if (!invalidTags.isEmpty()) {
            getLogger().warning("Invalid tags found in config: " + String.join(", ", invalidTags));
        }

    }

    private String asCommandFriendly(String stringToChange) {
        return stringToChange.replace(':', '_');
    }

    private void setupObjective(String name, String displayName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getObjective(name) == null) {
            scoreboard.registerNewObjective(name, Criteria.DUMMY, net.kyori.adventure.text.Component.text(displayName), RenderType.INTEGER);
            getLogger().info("Created scoreboard objective: " + name);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Material type = block.getType();

        if (shouldTrack(type)) {
            block.setMetadata(placedKey.getKey(), new FixedMetadataValue(this, true));

        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();
        if (!shouldTrack(type)) return;


        if (!isPlayerPlaced(block)) {

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String playerName = event.getPlayer().getName();


            for (Tag<Material> tag : trackedTags) {
                if (tag.isTagged(type)) {
                    String objName = "tag_" + tag.getKey().getKey().toLowerCase();
                    Objective obj = scoreboard.getObjective(objName);
                    if (obj != null) increment(obj, playerName);
                }
            }

            if (trackedBlocks.contains(type)) {
                String objName = "block_" + type.name().toLowerCase();
                Objective obj = scoreboard.getObjective(objName);
                if (obj != null) increment(obj, playerName);
            }
        }
    }

    private boolean isPlayerPlaced(Block block) {

        if(!block.hasMetadata(placedKey.getKey())) return false;


        List<MetadataValue> metadataValues = block.getMetadata(placedKey.getKey());
        for (MetadataValue value : metadataValues)
        {
            if(this.equals(value.getOwningPlugin()) && value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldTrack(Material type) {

        if(tagHelper.isInTags(type, trackedTags))
        {
            return true;
        }
        return trackedBlocks.contains(type);
    }

    private void increment(Objective obj, String playerName) {
        int current = obj.getScore(playerName).getScore();
        obj.getScore(playerName).setScore(current + 1);
    }

    //stats command placeholder
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NamedTextColor.RED + "Only players can use this command.");
            return true;
        }

        showStats(player);
        return true;
    }

    private void showStats(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        player.sendMessage(NamedTextColor.GREEN + "==== Your Natural Ore Stats ====");

        for (Tag<Material> tag : trackedTags) {
            String objName = "tag_" + tag.getKey().getKey().toLowerCase();
            Objective obj = scoreboard.getObjective(objName);
            if (obj != null) {
                int score = obj.getScore(player.getName()).getScore();
                player.sendMessage(NamedTextColor.AQUA + tag.getKey().getKey() + ": " + score);
            }
        }

        for (Material mat : trackedBlocks) {
            String objName = "block_" + mat.name().toLowerCase();
            Objective obj = scoreboard.getObjective(objName);
            if (obj != null) {
                int score = obj.getScore(player.getName()).getScore();
                player.sendMessage(NamedTextColor.LIGHT_PURPLE + mat.name() + ": " + score);
            }
        }
    }
}
