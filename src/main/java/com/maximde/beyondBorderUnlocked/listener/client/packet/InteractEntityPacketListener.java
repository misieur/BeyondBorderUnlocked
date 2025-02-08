package com.maximde.beyondBorderUnlocked.listener.client.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHurtAnimation;
import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import com.maximde.beyondBorderUnlocked.mechanics.combat.CombatUtils;
import com.maximde.beyondBorderUnlocked.mechanics.combat.DamageCalculator;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.maximde.beyondBorderUnlocked.events.AsyncEntityDamageBorderEvent;
import com.maximde.beyondBorderUnlocked.events.AsyncEntityInteractBorderEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class InteractEntityPacketListener implements PacketListener {
    private static final long HIT_DELAY = 250;
    private static final long DAMAGE_DELAY = 500;
    private static final double BASE_DAMAGE = 3.0;
    private static final double KNOCKBACK_HORIZONTAL = 0.5;
    private static final double KNOCKBACK_VERTICAL = 0.2;
    private static final int PARTICLE_COUNT = 6;
    private static final double PARTICLE_SPREAD = 0.1;
    private static final double VISIBLE_RANGE = 30.0;

    private final BeyondBorderUnlocked plugin;
    private final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isValidInteraction(event)) {
            return;
        }

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        Player player = Bukkit.getPlayer(event.getUser().getName());
        Entity entity = SpigotConversionUtil.getEntityById(player.getWorld(), packet.getEntityId());

        if (!(entity instanceof LivingEntity) || !shouldProcessInteraction(packet, player)) {
            return;
        }

        LivingEntity target = (LivingEntity) entity;
        if (!handleDelays(event, player, target)) {
            return;
        }

        processAttack(player, target, packet.getEntityId());
    }

    private boolean isValidInteraction(PacketReceiveEvent event) {
        return plugin.getPluginConfig().isHitting() &&
                event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY;
    }

    private boolean shouldProcessInteraction(WrapperPlayClientInteractEntity packet, Player player) {
        AsyncEntityInteractBorderEvent interactEvent = new AsyncEntityInteractBorderEvent(
                player,
                SpigotConversionUtil.getEntityById(player.getWorld(), packet.getEntityId()),
                packet.getAction()
        );

        Bukkit.getPluginManager().callEvent(interactEvent);
        return !interactEvent.isCancelled() &&
                packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK &&
                !player.getWorld().getWorldBorder().isInside(player.getLocation());
    }

    private boolean handleDelays(PacketReceiveEvent event, Player player, LivingEntity target) {
        long currentTime = System.currentTimeMillis();

        if (isOnHitCooldown(player, currentTime)) {
            event.setCancelled(true);
            return false;
        }

        lastHitTime.put(player.getUniqueId(), currentTime);

        return !isOnDamageCooldown(target, currentTime);
    }

    private boolean isOnHitCooldown(Player player, long currentTime) {
        return lastHitTime.containsKey(player.getUniqueId()) &&
                currentTime - lastHitTime.get(player.getUniqueId()) < HIT_DELAY;
    }

    private boolean isOnDamageCooldown(LivingEntity entity, long currentTime) {
        return lastDamageTime.containsKey(entity.getUniqueId()) &&
                currentTime - lastDamageTime.get(entity.getUniqueId()) < DAMAGE_DELAY;
    }

    private void processAttack(Player player, LivingEntity target, int entityId) {
        AsyncEntityDamageBorderEvent damageEvent = new AsyncEntityDamageBorderEvent(player, target, BASE_DAMAGE);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) {
            return;
        }

        boolean isCritical = CombatUtils.isCriticalHit(player, target);
        double finalDamage = DamageCalculator.calculateDamage(player, target, damageEvent.getDamage(), isCritical);

        plugin.getServer().getScheduler().runTask(plugin, () ->
                applyDamageEffects(player, target, entityId, finalDamage, isCritical));
    }

    private void applyDamageEffects(Player player, LivingEntity target, int entityId,
                                    double damage, boolean isCritical) {
        playDamageEffects(player, target, isCritical);
        applyKnockback(player, target);
        target.damage(damage, player);
        lastDamageTime.put(target.getUniqueId(), System.currentTimeMillis());
        broadcastHurtAnimation(player, target, entityId, damage);
    }

    private void playDamageEffects(Player player, LivingEntity target, boolean isCritical) {
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

        if (isCritical) {
            player.getWorld().spawnParticle(
                    Particle.CRIT,
                    target.getLocation().add(0, target.getHeight() / 2, 0),
                    PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD
            );
        }
    }

    private void applyKnockback(Player player, LivingEntity target) {
        Vector knockback = player.getLocation().getDirection()
                .multiply(KNOCKBACK_HORIZONTAL)
                .setY(KNOCKBACK_VERTICAL);
        target.setVelocity(knockback);
    }

    private void broadcastHurtAnimation(Player player, Entity target, int entityId, double damage) {
        WrapperPlayServerHurtAnimation hurtAnimation = new WrapperPlayServerHurtAnimation(
                entityId,
                (float) damage
        );

        player.getWorld().getNearbyEntities(target.getLocation(), VISIBLE_RANGE, VISIBLE_RANGE, VISIBLE_RANGE)
                .stream()
                .filter(e -> e instanceof Player)
                .forEach(p -> plugin.getPlayerManager().sendPacket(p, hurtAnimation));
    }
}