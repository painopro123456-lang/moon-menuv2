package com.moonmenu;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class ModuleRuntime {
    private static final Set<String> ENABLED = new HashSet<>();
    private static double savedGamma = 1.0;
    private static boolean gammaCaptured;
    private static boolean savedToggleSprint;
    private static boolean savedToggleSneak;
    private static boolean movementKeyForced;
    private static long sessionTicks;
    private static int lastHealth = -1;
    private static int sessionDeaths;
    private static int lastFood = -1;

    private ModuleRuntime() {}

    public static void initialize() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("moonmenu", "module_hud"),
                (graphics, deltaTracker) -> renderHud(graphics)
        );
    }

    public static boolean isEnabled(String name) {
        return ENABLED.contains(name);
    }

    public static void toggle(String name) {
        Minecraft client = Minecraft.getInstance();
        if (ENABLED.remove(name)) {
            disable(name, client);
        } else {
            enable(name, client);
        }
    }

    private static void enable(String name, Minecraft client) {
        ENABLED.add(name);
        if (name.equals("Fullbright")) {
            savedGamma = client.options.gamma().get();
            gammaCaptured = true;
            client.options.gamma().set(1.0);
        } else if (name.equals("Toggle Sprint")) {
            savedToggleSprint = client.options.toggleSprint().get();
            client.options.toggleSprint().set(true);
        } else if (name.equals("Toggle Sneak")) {
            savedToggleSneak = client.options.toggleCrouch().get();
            client.options.toggleCrouch().set(true);
        }
    }

    private static void disable(String name, Minecraft client) {
        if (name.equals("Fullbright") && gammaCaptured) {
            client.options.gamma().set(savedGamma);
            gammaCaptured = false;
        } else if (name.equals("Toggle Sprint")) {
            client.options.toggleSprint().set(savedToggleSprint);
        } else if (name.equals("Toggle Sneak")) {
            client.options.toggleCrouch().set(savedToggleSneak);
        } else if (name.equals("Auto Sprint") && client.player != null) {
            client.player.setSprinting(false);
        }
    }

    public static void tick(Minecraft client) {
        if (client.player != null) {
            sessionTicks++;
            if (lastHealth >= 0 && lastHealth > 0 && client.player.getHealth() <= 0) sessionDeaths++;
            lastHealth = Math.max(0, (int) client.player.getHealth());
            lastFood = client.player.getFoodData().getFoodLevel();
            if (isEnabled("Auto Respawn") && client.player.isDeadOrDying()) client.player.respawn();
        }
        if (client.player == null) {
            movementKeyForced = false;
            return;
        }

        if (isEnabled("Auto Sprint")) {
            boolean movingForward = client.options.keyUp.isDown();
            boolean canSprint = movingForward && !client.player.isCrouching()
                    && client.player.getFoodData().getFoodLevel() > 6;
            client.player.setSprinting(canSprint);
            movementKeyForced = true;
        } else if (movementKeyForced) {
            client.player.setSprinting(false);
            movementKeyForced = false;
        }

        if (isEnabled("Fullbright")) {
            client.options.gamma().set(1.0);
        }
    }

    private static void renderHud(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int y = 8;
        if (isEnabled("FPS")) {
            y = line(graphics, client, "FPS  " + client.getFps(), y);
        }
        if (isEnabled("Coordinates")) {
            y = line(graphics, client, String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()), y);
        }
        if (isEnabled("Health Display")) {
            y = line(graphics, client, String.format("HEALTH  %.1f", client.player.getHealth()), y);
        }
        if (isEnabled("Food Status")) {
            y = line(graphics, client, "FOOD  " + client.player.getFoodData().getFoodLevel(), y);
        }
        if (isEnabled("Memory Usage")) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L;
            long max = runtime.maxMemory() / 1024L / 1024L;
            y = line(graphics, client, "MEMORY  " + used + " / " + max + " MB", y);
        }
        if (isEnabled("Chunk Coordinates")) {
            int cx = client.player.blockPosition().getX() >> 4;
            int cz = client.player.blockPosition().getZ() >> 4;
            y = line(graphics, client, "CHUNK  " + cx + "  " + cz, y);
        }
        if (isEnabled("Held Item")) {
            String item = client.player.getMainHandItem().isEmpty()
                    ? "Empty" : client.player.getMainHandItem().getHoverName().getString();
            y = line(graphics, client, "HELD  " + item, y);
        }
        if (isEnabled("Durability") || isEnabled("Armor Status")) {
            var stack = client.player.getMainHandItem();
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int remaining = stack.getMaxDamage() - stack.getDamageValue();
                y = line(graphics, client, "DURABILITY  " + remaining + " / " + stack.getMaxDamage(), y);
            }
        }
        if (isEnabled("Armor Points")) {
            y = line(graphics, client, "ARMOR  " + client.player.getArmorValue(), y);
        }
        if (isEnabled("Absorption")) {
            y = line(graphics, client, String.format("ABSORPTION  %.1f", client.player.getAbsorptionAmount()), y);
        }
        if (isEnabled("Air")) {
            y = line(graphics, client, "AIR  " + client.player.getAirSupply()
                    + " / " + client.player.getMaxAirSupply(), y);
        }
        if (isEnabled("Coordinates XYZ")) {
            y = line(graphics, client, String.format("XYZ  %d  %d  %d",
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ()), y);
        }
        if (isEnabled("Chunk Border Info")) {
            var pos = client.player.blockPosition();
            y = line(graphics, client, "CHUNK  " + (pos.getX() >> 4) + "  "
                    + (pos.getZ() >> 4) + "  LOCAL " + (pos.getX() & 15) + " " + (pos.getZ() & 15), y);
        }
        if (isEnabled("Direction") || isEnabled("Compass")) {
            y = line(graphics, client, "FACING  " + client.player.getDirection().toString(), y);
        }
        if (isEnabled("Velocity")) {
            var velocity = client.player.getDeltaMovement();
            y = line(graphics, client, String.format("VELOCITY  %.2f  %.2f  %.2f",
                    velocity.x, velocity.y, velocity.z), y);
        }
        if (isEnabled("Light Level")) {
            y = line(graphics, client, "LIGHT  "
                    + client.level.getMaxLocalRawBrightness(client.player.blockPosition()), y);
        }
        if (isEnabled("Day Counter")) {
            y = line(graphics, client, "DAY  " + (client.level.getGameTime() / 24000L + 1L), y);
        }
        if (client.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity mount) {
            if (isEnabled("Mount Health")) {
                y = line(graphics, client, String.format("MOUNT HP  %.1f / %.1f",
                        mount.getHealth(), mount.getMaxHealth()), y);
            }
            if (isEnabled("Mount Speed")) {
                var motion = mount.getDeltaMovement();
                y = line(graphics, client, String.format("MOUNT SPEED  %.2f",
                        Math.sqrt(motion.x * motion.x + motion.z * motion.z) * 20.0), y);
            }
        }
        if (client.hitResult instanceof net.minecraft.world.phys.EntityHitResult targetHit) {
            var target = targetHit.getEntity();
            if (isEnabled("Target Distance")) {
                y = line(graphics, client, String.format("TARGET DIST  %.1f", client.player.distanceTo(target)), y);
            }
            if (isEnabled("Target Name")) {
                y = line(graphics, client, "TARGET  " + target.getName().getString(), y);
            }
            if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                if (isEnabled("Target Health")) {
                    y = line(graphics, client, String.format("TARGET HP  %.1f / %.1f",
                            livingTarget.getHealth(), livingTarget.getMaxHealth()), y);
                }
                if (isEnabled("Target Armor")) {
                    y = line(graphics, client, "TARGET ARMOR  " + livingTarget.getArmorValue(), y);
                }
            }
        }
        if (isEnabled("Clock") || isEnabled("Clock 24h")) {
            java.time.LocalTime now = java.time.LocalTime.now();
            String clock = isEnabled("Clock 24h")
                    ? now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    : now.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            y = line(graphics, client, "CLOCK  " + clock, y);
        }
        if (isEnabled("Speed")) {
            var motion = client.player.getDeltaMovement();
            double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z) * 20.0;
            y = line(graphics, client, String.format("SPEED  %.2f m/s", speed), y);
        }
        if (isEnabled("Weather") || isEnabled("Weather Visuals")) {
            String weather = client.level.isThundering() ? "Thunder"
                    : client.level.isRaining() ? "Rain" : "Clear";
            y = line(graphics, client, "WEATHER  " + weather, y);
        }
        if (isEnabled("Experience")) {
            y = line(graphics, client, "XP  " + client.player.experienceLevel
                    + "  " + Math.round(client.player.experienceProgress * 100) + "%", y);
        }
        if (isEnabled("Reach Display") && client.hitResult instanceof net.minecraft.world.phys.EntityHitResult reachHit) {
            y = line(graphics, client, String.format("REACH  %.2f blocks",
                    client.player.distanceTo(reachHit.getEntity())), y);
        }
        if (isEnabled("Target Info") && client.hitResult instanceof net.minecraft.world.phys.EntityHitResult infoHit) {
            var target = infoHit.getEntity();
            String info = target.getName().getString();
            if (target instanceof net.minecraft.world.entity.LivingEntity living) {
                info += String.format("  %.1f HP", living.getHealth());
            }
            y = line(graphics, client, "TARGET INFO  " + info, y);
        }
        if (isEnabled("Keystrokes")) {
            String keys = (client.options.keyUp.isDown() ? "W" : "w") + " "
                    + (client.options.keyLeft.isDown() ? "A" : "a") + " "
                    + (client.options.keyDown.isDown() ? "S" : "s") + " "
                    + (client.options.keyRight.isDown() ? "D" : "d")
                    + (client.options.keyJump.isDown() ? "  SPACE" : "");
            y = line(graphics, client, "KEYS  " + keys, y);
        }
        if (isEnabled("Totem Count") || isEnabled("Arrow Count") || isEnabled("Projectile Count")) {
            int totems = 0, arrows = 0, projectiles = 0;
            for (int slot = 0; slot < client.player.getInventory().getContainerSize(); slot++) {
                var stack = client.player.getInventory().getItem(slot);
                if (stack.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)) totems += stack.getCount();
                if (stack.is(net.minecraft.world.item.Items.ARROW)
                        || stack.is(net.minecraft.world.item.Items.SPECTRAL_ARROW)
                        || stack.is(net.minecraft.world.item.Items.TIPPED_ARROW)) arrows += stack.getCount();
                if (stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem) {
                    projectiles += stack.getCount();
                }
            }
            if (isEnabled("Totem Count")) y = line(graphics, client, "TOTEMS  " + totems, y);
            if (isEnabled("Arrow Count")) y = line(graphics, client, "ARROWS  " + arrows, y);
            if (isEnabled("Projectile Count")) y = line(graphics, client, "PROJECTILES  " + projectiles, y);
        }
        if (isEnabled("Armor Durability") || isEnabled("Armor Status")) {
            var inventory = client.player.getInventory();
            String[] labels = {"BOOTS", "LEGS", "CHEST", "HELM"};
            net.minecraft.world.entity.EquipmentSlot[] slots = {
                    net.minecraft.world.entity.EquipmentSlot.FEET,
                    net.minecraft.world.entity.EquipmentSlot.LEGS,
                    net.minecraft.world.entity.EquipmentSlot.CHEST,
                    net.minecraft.world.entity.EquipmentSlot.HEAD
            };
            for (int i = 0; i < slots.length; i++) {
                var stack = client.player.getItemBySlot(slots[i]);
                if (!stack.isEmpty() && stack.isDamageableItem()) {
                    int remaining = stack.getMaxDamage() - stack.getDamageValue();
                    int percent = Math.round(remaining * 100.0f / stack.getMaxDamage());
                    y = line(graphics, client, labels[i] + "  " + percent + "%", y);
                }
            }
        }
        if (isEnabled("Durability Percent")) {
            var stack = client.player.getMainHandItem();
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int percent = Math.round((stack.getMaxDamage() - stack.getDamageValue())
                        * 100.0f / stack.getMaxDamage());
                y = line(graphics, client, "DURABILITY  " + percent + "%", y);
            }
        }
        if (isEnabled("Server Address") && client.getCurrentServer() != null) {
            y = line(graphics, client, "SERVER  " + client.getCurrentServer().ip, y);
        }
        if (isEnabled("World Time")) {
            long dayTime = client.level.getGameTime() % 24000L;
            long hours = (dayTime / 1000L + 6L) % 24L;
            long minutes = (dayTime % 1000L) * 60L / 1000L;
            y = line(graphics, client, String.format("WORLD TIME  %02d:%02d", hours, minutes), y);
        }
        if (isEnabled("Ping")) {
            var connection = client.getConnection();
            var info = connection == null ? null : connection.getPlayerInfo(client.player.getUUID());
            if (info != null) y = line(graphics, client, "PING  " + info.getLatency() + " ms", y);
        }
        if (isEnabled("Biome") || isEnabled("Biome Temperature")) {
            var biome = client.level.getBiome(client.player.blockPosition());
            String biomeName = biome.unwrapKey().map(key -> key.location().toString()).orElse("Unknown");
            y = line(graphics, client, "BIOME  " + biomeName, y);
        }
        if (isEnabled("Session Time")) {
            long seconds = sessionTicks / 20L;
            y = line(graphics, client, String.format("SESSION  %02d:%02d:%02d",
                    seconds / 3600, (seconds / 60) % 60, seconds % 60), y);
        }
        if (isEnabled("Food Status")) {
            y = line(graphics, client, String.format("HUNGER  %d  SAT %.1f",
                    client.player.getFoodData().getFoodLevel(),
                    client.player.getFoodData().getSaturationLevel()), y);
        }
        if (isEnabled("Entity Count") || isEnabled("Hostile Count") || isEnabled("Passive Count")) {
            int nearby = 0, hostile = 0, passive = 0;
            for (var entity : client.level.entitiesForRendering()) {
                if (entity == client.player || entity.distanceToSqr(client.player) > 4096) continue;
                nearby++;
                if (entity instanceof net.minecraft.world.entity.monster.Monster) hostile++;
                if (entity instanceof net.minecraft.world.entity.animal.Animal) passive++;
            }
            if (isEnabled("Entity Count")) y = line(graphics, client, "ENTITIES  " + nearby, y);
            if (isEnabled("Hostile Count")) y = line(graphics, client, "HOSTILES  " + hostile, y);
            if (isEnabled("Passive Count")) y = line(graphics, client, "PASSIVE  " + passive, y);
        }
        if (isEnabled("Deaths")) y = line(graphics, client, "DEATHS  " + sessionDeaths, y);
        if (isEnabled("Local Difficulty")) {
            y = line(graphics, client, "LOCAL DIFFICULTY  "
                    + client.level.getCurrentDifficultyAt(client.player.blockPosition()).getEffectiveDifficulty(), y);
        }
        if (isEnabled("Jump Distance")) {
            var velocity = client.player.getDeltaMovement();
            y = line(graphics, client, String.format("HORIZONTAL MOTION  %.2f",
                    Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)), y);
        }
        if (isEnabled("Armor Enchants")) {
            for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (!slot.isArmor()) continue;
                var stack = client.player.getItemBySlot(slot);
                if (!stack.isEmpty() && !stack.getEnchantments().isEmpty()) {
                    y = line(graphics, client, slot.getName().toUpperCase() + " ENCHANTED", y);
                }
            }
        }
        if (isEnabled("Totem Count") || isEnabled("Arrow Count") || isEnabled("Projectile Count")) {
            // Counts are rendered in the shared inventory counter block above.
        }
    }

    private static int line(net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                            Minecraft client, String value, int y) {
        graphics.text(client.font, value, 8, y, 0xFFEAF0FA, true);
        return y + 11;
    }
}
