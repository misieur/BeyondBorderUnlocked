<div align="center">

  [![Discord](https://img.shields.io/badge/Discord_Server-7289DA?style=flat&logo=discord&logoColor=white)](https://discord.gg/2UTkYj26B4)
  [![JitPack](https://jitpack.io/v/max1mde/BeyondBorderUnlocked.svg)](https://jitpack.io/#max1mde/BeyondBorderUnlocked)
  [![GitHub Downloads](https://img.shields.io/github/downloads/max1mde/BeyondBorderUnlocked/total?color=2ECC71)](https://github.com/max1mde/BeyondBorderUnlocked/releases)
  [![Minecraft version](https://img.shields.io/badge/Minecraft%20version-1.19_--_1.21-brightgreen.svg)](https://github.com/max1mde/BeyondBorderUnlocked)

  <img src="https://github.com/user-attachments/assets/012f91e1-d872-4c0c-a9f8-829f5004d262">
  <br><br>
  <p>Leave a :star: if you like this plugin :octocat:</p>
  <p>Break or place blocks & damage entities behind the vanilla worldborder without any client side-mods!</p>
</div>

## Features
- Bypass world border restrictions for:
  - Placing blocks (only possible by left clicking with a block)
  - Breaking blocks
  - Combat and entity interactions
  - Player movement through border
- Visual block outline system for blocks beyond border (using display entities)
- Block breaking animations and effects
- Realistic combat mechanics with critical hits & damage calculation based on armor and weapons

## Installation
1. Place the plugin JAR and [packetevents.jar](https://www.spigotmc.org/resources/80279/) in your server's `plugins/` folder 
2. Restart/reload your server
3. Configure `plugins/BeyondBorderUnlocked/config.yml` to your needs

> [!WARNING]
> These actions beyond the world border are not intended by game design.  
> Unexpected bugs may occur, and the experience will differ from normal gameplay.  
> Test thoroughly before production use.

## Configuration (`config.yml`)
```yaml
building: false        # Allow block placement beyond border (left click)
breaking: true        # # Allow block breaking beyond border (left click)
walkthrough: true     # Allow walking through border
hitting: true         # Allow hitting entities beyond border

damage:
  enabled: true      # Enable custom border damage settings below (if false, the plugin does not modify the values below in your worlds)
  buffer: 5.0        # Distance before damage starts
  amount: 0.2        # Damage per second

blockOutline:
  enabled: true      # Show block outlines beyond border
  size: 0.009        # Size of outline segments
  block: BLACK_STAINED_GLASS  # Block type for outline
```

## Commands
- `/beyondborder reload` - Reload configuration
- `/beyondborder set <setting> <value>` - Change settings

Available settings:
- `building`, `breaking`, `walkthrough`, `blockOutline.enabled`, `hitting` (true/false)
- `damage.enabled`, `damage.buffer`, `blockOutline.size`, `damage.amount` (numbers)
- `blockOutline.block` (text)

**Permissions**:  
`beyondborder.commands` - Access to commands (default: op)

## API Documentation

To use the api add `depends: BeyondBorderUnlocked` to your plugin.yml  
and this plugin as a compile-only dependency:

```groovy
repositories {
	maven { url "https://jitpack.io" }
}
dependencies {
	compileOnly("com.github.max1mde:BeyondBorderUnlocked:1.0.0")
}
```

### Accessing the API
```java
BeyondBorderUnlocked plugin = BeyondBorderUnlocked.getInstance();
Config config = plugin.getPluginConfig();
```

### Events
| Event Class | Async | Cancellable |
|-------------|-------|-------------|
| `BlockBreakBorderEvent` | No | Yes |
| `BlockPlaceBorderEvent` | No | Yes |
| `AsyncEntityDamageBorderEvent` | Yes | Yes |
| `AsyncEntityInteractBorderEvent` | Yes | Yes |

**Example Event Usage**:
```java
@EventHandler
public void onEntityDamage(AsyncEntityDamageBorderEvent event) {
    if (event.getDamager() instanceof Player) {
        event.setDamage(event.getDamage() * 1.5); // 50% more damage for example
    }
}
```

## Known Issues

- Blocks can only be placed using the left mouse button
- Walking through the world border is buggy due to required player teleportation
- Blocks sometimes continue breaking without holding the mouse button, or breaking progress resets even when holding the left mouse button
- The block selection outline box has incorrect sizing for certain blocks
- Mechanics such as block breaking, damage calculations, and other interactions are not entirely accurate

## How does it work?

By default, Minecraft clients don't send certain packets when behind the world border:
- No block break/place packets
- No block break progress packets
- No block selection outline is displayed by the client

This plugin works by:
1. Listening for arm swing animation packets (which are always sent)
2. Calculating which block the player is looking at
3. Simulating the appropriate action server-side:
   - Block breaking with animations and effects
   - Block placement with sound effects

The block selection outline is created using:
- 12 item display entities forming a 3D outline
- Custom packets to spawn and position these entities
- Only visible to the player looking at the block

Because of that, the `building` feature does not work when right-clicking, as the client does not send any packets when right-clicking beyond the world border. The only packet sent is the arm animation packet when the player left-clicks.
