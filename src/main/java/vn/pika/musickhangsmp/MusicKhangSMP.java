package vn.pika.musickhangsmp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MusicKhangSMP extends JavaPlugin {
    private static MusicKhangSMP instance;
    private MusicManager musicManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.musicManager = new MusicManager(this);

        // Dang ky lenh
        if (getCommand("nhac") != null) {
            NhacCommand nhacCmd = new NhacCommand(this, musicManager);
            getCommand("nhac").setExecutor(nhacCmd);
            getCommand("nhac").setTabCompleter(nhacCmd);
        }

        if (getCommand("nhacad") != null) {
            NhacAdminCommand nhacadCmd = new NhacAdminCommand(this, musicManager);
            getCommand("nhacad").setExecutor(nhacadCmd);
            getCommand("nhacad").setTabCompleter(nhacadCmd);
        }

        // Dang ky listener
        Bukkit.getPluginManager().registerEvents(new MusicListener(this, musicManager), this);

        // Tu dong bat neu config auto-start: true
        if (getConfig().getBoolean("auto-start", true)) {
            musicManager.start();
        }

        getLogger().info("=================================================");
        getLogger().info("  MusicKhangSMP v1.0.0 da kich hoat thanh cong!");
        getLogger().info("  Tong so bai hat: " + musicManager.getPlaylist().size());
        getLogger().info("  He thong phat nhac: " + (musicManager.isServerEnabled() ? "DANG BAT [ON]" : "DANG TAT [OFF]"));
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (musicManager != null) {
            musicManager.stop();
            musicManager.saveDisabledPlayers();
        }
        getLogger().info("MusicKhangSMP da tat va luu tru du lieu thanh cong.");
    }

    public static MusicKhangSMP getInstance() {
        return instance;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
