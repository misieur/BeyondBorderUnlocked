# BeyondBorderUnlocked

A Spigot plugin that enables interactions beyond the Minecraft world border including combat, building, and entity interactions.

Perfect for your **minigame server**!

## Features
- Bypass world border restrictions for:
  - Building and breaking blocks
  - Combat and entity interactions
  - Player movement
- Visual block outline system for blocks beyond border
- Advanced block breaking animations and effects
- Realistic combat mechanics with critical hits & damage calculation based on armor and weapons

## Installation
1. Place the plugin JAR in your server's `plugins/` folder
2. Restart/reload your server
3. Configure `plugins/BeyondBorderUnlocked/config.yml` to your needs

## Configuration (`config.yml`)
```yaml
building: false        # Allow block placement beyond border (left click)
breaking: true        # # Allow block breaking beyond border (left click)
walkthrough: true     # Allow walking through border
hitting: true         # Allow hitting entities beyond border

damage:
  enabled: true       # Enable custom border damage settings below
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
- `building`, `breaking`, `walkthrough`, `hitting` (true/false)
- `damage.enabled`, `damage.buffer`, `damage.amount` (numbers)
- `blockOutline.enabled`, `blockOutline.size`, `blockOutline.block`

**Permissions**:  
`beyondborder.commands` - Access to commands (default: op)

## API Documentation

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

## Requirements
- Spigot/Paper 1.18.2+
- Java 17+
- PacketEvents