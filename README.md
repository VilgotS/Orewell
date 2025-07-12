# Orewell
A Minecraft server plugin that tracks how many naturally generated blocks players have mined,
excluding player placed blocks. Works with block IDs and tags!

I made this plugin to track how many diamonds a player has found
while preventing inflation of score from using both silk touch and fortune pickaxes.

Made by Vilgot <dev.vilgot@gmail.com>

## Features
* Only counts blocks not placed by players
* Track categories of blocks using Minecraft tags (for example both regular and deepslate diamond ore). Full list here.
* Uses Minecraft´s built in scoreboard system
* Easy YAML configuration


## Requirements

* Minecraft Server: Paper server (tested) or compatible
* Java Version: Java 21 or higher
* Minecraft Version: 1.21+ (tested on 1.21.7)

## Building from Source
### Prerequisites

* Java Development Kit (JDK) 21 or higher
* Gradle 7.0 or higher
* Git

### Build Steps

1. Clone the repository:
    ```bash
    git clone https://github.com/VilgotS/orewell.git
    cd orewell
    ```

2. Build with Gradle:
    ```bash
   ./gradlew build
   ```

3. Locate the JAR file:
The compiled plugin will be in build/libs/Orewell-1.X.jar

## Installation

1. Download the latest release ([GitHub](), [Hangar](), [Modrinth]()) or build from source
2. Copy the Orewell.jar file to your server's plugins/ directory
3. Start your server
4. Configure the plugin (see [Configuration](#configuration) section below)
5. Restart your server to apply configuration changes

## Configuration
The plugin creates a config.yml file in plugins/Orewell/ on first run.
### Default Configuration
```yaml
# Blocks to track individually by material name
tracked-blocks:
- NETHER_QUARTZ_ORE
- ANCIENT_DEBRIS

# Tags to track (tracks all blocks in the tag)
tracked-tags:
- diamond_ores
- gold_ores
- iron_ores
- coal_ores
- copper_ores
- emerald_ores
- lapis_ores
- redstone_ores
```
### Configuration Options
Capitalization does not matter.

#### tracked-blocks
  List of individual block types to track. Use the exact Material enum names from Bukkit/Spigot. [Full list here.](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)
  
**Examples:**
- DIAMOND_ORE
- DEEPSLATE_DIAMOND_ORE
- ANCIENT_DEBRIS
- BIRCH_PRESSURE_PLATE

#### tracked-tags
List of Minecraft tags to track. This allows you to track entire categories of blocks. [List here.](https://mcreator.net/wiki/minecraft-block-tags-list)
Currently, tags containing slashes "/" (e.g "mineable/axe") do not work.
**Common ore tags:**

diamond_ores - All diamond ore variants (deepslate and regular)
gold_ores - All gold ore variants
iron_ores - All iron ore variants
coal_ores - All coal ore variants
copper_ores - All copper ore variants
emerald_ores - All emerald ore variants
lapis_ores - All lapis lazuli ore variants
redstone_ores - All redstone ore variants

**Other examples:**

logs - All kinds of logs
enderman_holdable


**Custom tags:** 

You can also(in theory, not tested) use custom tags from datapacks:
mypack:custom_ores - Custom ore tag from datapack

## Usage
### Server Administration

Administrators can view and modify player statistics using standard Minecraft scoreboard commands:

**Examples:**
View a player's diamond ore count
```
/scoreboard players get PlayerName tag_diamond_ores
```

Set a player's diamond ore count
```
/scoreboard players set PlayerName tag_diamond_ores 50
```

Display score for everyone below their name
```
/scoreboard objectives setdisplay below_name tag_diamond_ores
```

View all objectives
```
/scoreboard objectives list
```

## License
Orewell is provided under the terms of the GNU Lesser General Public License Version 3 or
(at your option) any later version. See LICENSE.md and LICENSE.LESSER.md for the full license text.