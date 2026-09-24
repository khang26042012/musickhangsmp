package vn.pika.musickhangsmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MusicListener implements Listener {
    private final MusicKhangSMP plugin;
    private final MusicManager manager;

    public MusicListener(MusicKhangSMP plugin, MusicManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&d[&e♫ KhangSMP&d] &7"));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if (manager.isMusicEnabled(player.getUniqueId()) && manager.isServerEnabled()) {
                Song current = manager.getCurrentSong();
                if (current != null) {
                    int remaining = Math.max(0, current.getDurationSeconds() - manager.getElapsedSeconds());
                    player.sendMessage(prefix + ChatColor.GRAY + "He thong dang phat: " + ChatColor.GOLD + current.getName()
                            + ChatColor.GRAY + " (Con " + ChatColor.WHITE + remaining + "s" + ChatColor.GRAY + " se sang bai moi).");
                    player.sendMessage(prefix + ChatColor.DARK_GRAY + "Go " + ChatColor.WHITE + "/nhac off" + ChatColor.DARK_GRAY + " neu ban khong muon nghe.");
                }
            }
        }, 60L); // 3 giay sau khi join
    }
}
