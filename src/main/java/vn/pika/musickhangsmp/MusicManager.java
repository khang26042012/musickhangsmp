package vn.pika.musickhangsmp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MusicManager {
    private final MusicKhangSMP plugin;
    private final List<Song> playlist = new ArrayList<>();
    private final Set<UUID> disabledPlayers = new HashSet<>();
    private final Set<UUID> verifiedPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Location> joinLocations = new ConcurrentHashMap<>();

    private int currentIndex = 0;
    private int elapsedSeconds = 0;
    private boolean serverEnabled = true;
    private BukkitTask tickerTask = null;
    private File disabledFile;
    private YamlConfiguration disabledConfig;

    public MusicManager(MusicKhangSMP plugin) {
        this.plugin = plugin;
        loadDisabledPlayers();
        loadPlaylist();
    }

    public void loadPlaylist() {
        playlist.clear();
        List<Map<?, ?>> list = plugin.getConfig().getMapList("playlist");
        if (list != null) {
            for (Map<?, ?> map : list) {
                String id = String.valueOf(map.get("id"));
                String name = String.valueOf(map.get("name"));
                int duration = 0;
                try {
                    duration = Integer.parseInt(String.valueOf(map.get("duration")));
                } catch (NumberFormatException ignored) {}
                String javaSound = String.valueOf(map.get("java-sound"));
                String bedrockSound = String.valueOf(map.get("bedrock-sound"));

                if (id != null && !id.isEmpty() && duration > 0) {
                    playlist.add(new Song(id, name, duration, javaSound, bedrockSound));
                }
            }
        }
        plugin.getLogger().info("Da nap " + playlist.size() + " bai hat vao he thong MusicKhangSMP.");
    }

    private void loadDisabledPlayers() {
        disabledFile = new File(plugin.getDataFolder(), "disabled_players.yml");
        if (!disabledFile.exists()) {
            try {
                if (disabledFile.getParentFile() != null) disabledFile.getParentFile().mkdirs();
                disabledFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Khong the tao file disabled_players.yml: " + e.getMessage());
            }
        }
        disabledConfig = YamlConfiguration.loadConfiguration(disabledFile);
        List<String> list = disabledConfig.getStringList("disabled");
        disabledPlayers.clear();
        for (String s : list) {
            try {
                disabledPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveDisabledPlayers() {
        List<String> list = new ArrayList<>();
        for (UUID u : disabledPlayers) {
            list.add(u.toString());
        }
        disabledConfig.set("disabled", list);
        try {
            disabledConfig.save(disabledFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Khong the luu disabled_players.yml: " + e.getMessage());
        }
    }

    public void start() {
        if (tickerTask != null) {
            tickerTask.cancel();
        }
        serverEnabled = true;
        if (!playlist.isEmpty()) {
            playCurrentSong();
            startTicker();
        }
    }

    public void stop() {
        serverEnabled = false;
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
        stopAllSounds();
    }

    private void startTicker() {
        tickerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!serverEnabled || playlist.isEmpty()) return;

            elapsedSeconds++;
            Song current = getCurrentSong();
            if (current != null && elapsedSeconds >= current.getDurationSeconds()) {
                nextSong();
            }
        }, 20L, 20L); // chay moi 1 giay (20 ticks)
    }

    public void nextSong() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        elapsedSeconds = 0;
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (!serverEnabled || playlist.isEmpty()) return;

        Song song = getCurrentSong();
        if (song == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (disabledPlayers.contains(p.getUniqueId())) {
                continue;
            }
            // Chi phat cho nguoi choi da di chuyen >= 2 block (co trong server)
            if (!verifiedPlayers.contains(p.getUniqueId())) {
                continue;
            }

            playSongToPlayer(p, song);
            sendNotificationToPlayer(p, song);
        }
    }

    public void sendNotificationToPlayer(Player p, Song song) {
        if (p == null || !p.isOnline() || song == null) return;
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&d[&e♫ KhangSMP&d] &7"));
        String notifyType = plugin.getConfig().getString("notify-type", "BOTH").toUpperCase();
        String rawMsg = plugin.getConfig().getString("notify-message", "&fDang phat: &e{song_name} &7[{duration}]");
        String formattedMsg = ChatColor.translateAlternateColorCodes('&', rawMsg
                .replace("{song_name}", song.getName())
                .replace("{duration}", song.getFormattedDuration()));

        if (notifyType.equals("BOTH") || notifyType.equals("CHAT")) {
            p.sendMessage(prefix + formattedMsg);
        }
        if (notifyType.equals("BOTH") || notifyType.equals("ACTIONBAR")) {
            sendActionBar(p, prefix + formattedMsg);
        }
    }

    public void sendActionBar(Player player, String message) {
        try {
            // Paper / Adventure API
            Method sendActionBarMethod = player.getClass().getMethod("sendActionBar", net.kyori.adventure.text.Component.class);
            Object comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            sendActionBarMethod.invoke(player, comp);
        } catch (Throwable ignored) {
            try {
                // Fallback title actionbar command
                String clean = message.replace('"', '\'');
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " actionbar {\"text\":\"" + clean + "\"}");
            } catch (Throwable ignored2) {}
        }
    }

    public void playSongToPlayer(Player p, Song song) {
        if (p == null || !p.isOnline() || song == null) return;
        try {
            p.stopSound(SoundCategory.RECORDS);
        } catch (Throwable ignored) {}

        boolean isBedrock = isBedrockPlayer(p);
        String soundKey = isBedrock ? song.getBedrockSound() : song.getJavaSound();

        try {
            p.playSound(p.getLocation(), soundKey, SoundCategory.RECORDS, 1000000.0f, 1.0f);
        } catch (Throwable ignored) {}

        try {
            String cmd = String.format("execute at %s run playsound %s record %s ~ ~ ~ 1000000 1", p.getName(), soundKey, p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } catch (Throwable ignored) {}
    }

    public void stopPlayerSound(Player p) {
        if (p != null && p.isOnline()) {
            try {
                p.stopSound(SoundCategory.RECORDS);
            } catch (Throwable ignored) {}
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound " + p.getName() + " record");
            } catch (Throwable ignored) {}
        }
    }

    public void stopAllSounds() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            stopPlayerSound(p);
        }
    }

    public boolean isMusicEnabled(UUID uuid) {
        return !disabledPlayers.contains(uuid);
    }

    public void setMusicEnabled(UUID uuid, boolean enabled) {
        if (enabled) {
            disabledPlayers.remove(uuid);
        } else {
            disabledPlayers.add(uuid);
        }
        saveDisabledPlayers();
    }

    // --- LOGIC DI CHUYEN 2 BLOCK ---
    public void markPlayerJoined(Player player) {
        UUID uuid = player.getUniqueId();
        verifiedPlayers.remove(uuid);
        joinLocations.put(uuid, player.getLocation().clone());
    }

    public void markPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        verifiedPlayers.remove(uuid);
        joinLocations.remove(uuid);
        stopPlayerSound(player);
    }

    public boolean isPlayerVerified(UUID uuid) {
        return verifiedPlayers.contains(uuid);
    }

    public void markPlayerVerified(UUID uuid) {
        verifiedPlayers.add(uuid);
        joinLocations.remove(uuid);
    }

    public void handlePlayerMove(Player player, Location to) {
        UUID uuid = player.getUniqueId();
        if (verifiedPlayers.contains(uuid)) {
            return;
        }

        Location joinLoc = joinLocations.get(uuid);
        if (joinLoc == null) {
            joinLocations.put(uuid, to.clone());
            return;
        }

        if (to.getWorld() != null && !to.getWorld().equals(joinLoc.getWorld())) {
            joinLocations.put(uuid, to.clone());
            return;
        }

        double dx = to.getX() - joinLoc.getX();
        double dy = to.getY() - joinLoc.getY();
        double dz = to.getZ() - joinLoc.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        // Di chuyen >= 2 block (2^2 = 4) -> da thuc su load xong trong the gioi!
        if (distSq >= 4.0) {
            verifiedPlayers.add(uuid);
            joinLocations.remove(uuid);
            plugin.getLogger().info("Nguoi choi " + player.getName() + " da di chuyen >= 2 block -> Xac thuc co trong server!");

            if (serverEnabled && !disabledPlayers.contains(uuid)) {
                Song current = getCurrentSong();
                if (current != null) {
                    playSongToPlayer(player, current);
                    sendNotificationToPlayer(player, current);
                }
            }
        }
    }

    public boolean isBedrockPlayer(Player player) {
        if (player == null) return false;
        String name = player.getName();
        if (name.startsWith("PE_") || name.startsWith(".")) {
            return true;
        }
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstanceMethod = floodgateApiClass.getMethod("getInstance");
            Object apiInstance = getInstanceMethod.invoke(null);
            Method isFloodgateMethod = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
            return (boolean) isFloodgateMethod.invoke(apiInstance, player.getUniqueId());
        } catch (Throwable ignored) {
            return false;
        }
    }

    public Song getCurrentSong() {
        if (playlist.isEmpty()) return null;
        if (currentIndex < 0 || currentIndex >= playlist.size()) {
            currentIndex = 0;
        }
        return playlist.get(currentIndex);
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isServerEnabled() {
        return serverEnabled;
    }

    public List<Song> getPlaylist() {
        return Collections.unmodifiableList(playlist);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}
