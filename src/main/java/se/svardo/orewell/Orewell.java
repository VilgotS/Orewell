package se.svardo.orewell;

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
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import org.jetbrains.annotations.NotNull;
import se.svardo.orewell.util.TagMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Orewell extends JavaPlugin implements Listener {

    private NamespacedKey placedKey;

    private Set<Tag<Material>> trackedTags = new HashSet<>();
    private final Set<Material> trackedBlocks = new HashSet<>();

    private  TagMapper tagMapper;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        tagMapper = new TagMapper(this);

        // Load tracked tags
        trackedTags = tagMapper.loadTagsFromConfig("tracked-tags");

        if(trackedTags.isEmpty()){
            getLogger().warning("No valid tags tracked");
        }

        tagMapper.validateConfigTags("tracked-tags");

        for(Tag<Material> tag : trackedTags)
        {
            setupObjective("tag_" + tag.getKey(), "Tag: " + tag.getValues());
        }

        // Load tracked blocks
        List<String> blockNames = config.getStringList("tracked-blocks");
        for (String blockName : blockNames) {
            try {
                Material mat = Material.valueOf(blockName.toUpperCase());
                trackedBlocks.add(mat);
                getLogger().info("Tracking block: " + blockName);

                setupObjective("block_" + blockName.toLowerCase(), "Block: " + blockName);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid block name in config: " + blockName);
            }
        }

        placedKey = new NamespacedKey(this, "player_placed");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void setupObjective(String name, String displayName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getObjective(name) == null) {
            scoreboard.registerNewObjective(name, "dummy", displayName);
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
//        for (Tag<Material> tag : trackedTags) {
//            if (tag.isTagged(type)) return true;
//        }
        if(tagMapper.isInTags(type, trackedTags))
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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("stats")) {
            showStats(player);
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /orewell stats");
        return true;
    }

    private void showStats(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        player.sendMessage(ChatColor.GREEN + "==== Your Natural Ore Stats ====");

        for (Tag<Material> tag : trackedTags) {
            String objName = "tag_" + tag.getKey().getKey().toLowerCase();
            Objective obj = scoreboard.getObjective(objName);
            if (obj != null) {
                int score = obj.getScore(player.getName()).getScore();
                player.sendMessage(ChatColor.AQUA + tag.getKey().getKey() + ": " + score);
            }
        }

        for (Material mat : trackedBlocks) {
            String objName = "block_" + mat.name().toLowerCase();
            Objective obj = scoreboard.getObjective(objName);
            if (obj != null) {
                int score = obj.getScore(player.getName()).getScore();
                player.sendMessage(ChatColor.LIGHT_PURPLE + mat.name() + ": " + score);
            }
        }
    }
}
