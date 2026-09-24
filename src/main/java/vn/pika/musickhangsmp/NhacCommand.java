package vn.pika.musickhangsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class NhacCommand implements CommandExecutor, TabCompleter {
    private final MusicKhangSMP plugin;
    private final MusicManager manager;

    public NhacCommand(MusicKhangSMP plugin, MusicManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Lenh nay chi danh cho nguoi choi trong game!");
            return true;
        }

        Player player = (Player) sender;
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&d[&e♫ KhangSMP&d] &7"));

        if (args.length == 0) {
            player.sendMessage(ChatColor.DARK_PURPLE + "════════════════ " + ChatColor.LIGHT_PURPLE + "[♫ KHANGSMP MUSIC] " + ChatColor.DARK_PURPLE + "════════════════");
            player.sendMessage(ChatColor.YELLOW + "  /nhac on    " + ChatColor.GRAY + "→ Bat nghe nhac cho ban than");
            player.sendMessage(ChatColor.YELLOW + "  /nhac off   " + ChatColor.GRAY + "→ Tat nhac cho ban than (im lang)");
            player.sendMessage(ChatColor.YELLOW + "  /nhac check " + ChatColor.GRAY + "→ Xem bai hat dang phat va tien do");
            player.sendMessage(ChatColor.YELLOW + "  /nhac list  " + ChatColor.GRAY + "→ Xem danh sach 15 bai hat he thong");
            player.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on":
                if (manager.isMusicEnabled(player.getUniqueId())) {
                    player.sendMessage(prefix + ChatColor.YELLOW + "Ban da dang bat nhac nen roi!");
                } else {
                    manager.setMusicEnabled(player.getUniqueId(), true);
                    manager.markPlayerVerified(player.getUniqueId());
                    player.sendMessage(prefix + ChatColor.GREEN + "Da bat nhac nen! He thong dang phat nhac cho ban.");
                    Song current = manager.getCurrentSong();
                    if (current != null && manager.isServerEnabled()) {
                        manager.playSongToPlayer(player, current);
                        manager.sendNotificationToPlayer(player, current);
                    }
                }
                break;

            case "off":
                if (!manager.isMusicEnabled(player.getUniqueId())) {
                    player.sendMessage(prefix + ChatColor.YELLOW + "Ban da dang tat nhac nen roi!");
                } else {
                    manager.setMusicEnabled(player.getUniqueId(), false);
                    manager.stopPlayerSound(player);
                    player.sendMessage(prefix + ChatColor.RED + "Da tat nhac nen cho rieng ban. Go " + ChatColor.WHITE + "/nhac on" + ChatColor.RED + " de bat lai bat cu luc nao.");
                }
                break;

            case "check":
                boolean isEnabled = manager.isMusicEnabled(player.getUniqueId());
                Song current = manager.getCurrentSong();
                player.sendMessage(ChatColor.DARK_PURPLE + "════════════════ " + ChatColor.LIGHT_PURPLE + "[♫ KHANGSMP MUSIC] " + ChatColor.DARK_PURPLE + "════════════════");
                player.sendMessage(ChatColor.WHITE + "• Trang thai ca nhan: " + (isEnabled ? ChatColor.GREEN + "Dang Bat [ON]" : ChatColor.RED + "Dang Tat [OFF]"));

                if (!manager.isServerEnabled()) {
                    player.sendMessage(ChatColor.YELLOW + "• He thong nhac toan server dang tam dung boi Admin.");
                } else if (current == null) {
                    player.sendMessage(ChatColor.YELLOW + "• Hien tai chua co bai hat nao trong danh sach phat.");
                } else {
                    int elapsed = manager.getElapsedSeconds();
                    int total = current.getDurationSeconds();
                    int remaining = Math.max(0, total - elapsed);
                    player.sendMessage(ChatColor.WHITE + "• Bai dang phat: " + ChatColor.GOLD + current.getName());
                    player.sendMessage(ChatColor.WHITE + "• Tien do thoi gian: " + ChatColor.AQUA + current.formatTime(elapsed) + " / " + current.getFormattedDuration()
                            + " " + current.getProgressBar(elapsed));
                    player.sendMessage(ChatColor.GRAY + "• Bai tiep theo sau: " + ChatColor.WHITE + remaining + " giay");
                }
                player.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "list":
                List<Song> songs = manager.getPlaylist();
                player.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[DANH SACH 15 BAI HAT] " + ChatColor.DARK_PURPLE + "═══════════════");
                for (int i = 0; i < songs.size(); i++) {
                    Song s = songs.get(i);
                    boolean isPlaying = (i == manager.getCurrentIndex()) && manager.isServerEnabled();
                    String line = String.format(" %s %02d. %s [%s]",
                            (isPlaying ? ChatColor.GOLD + "►" : ChatColor.GRAY + "•"),
                            (i + 1),
                            (isPlaying ? ChatColor.YELLOW + s.getName() : ChatColor.WHITE + s.getName()),
                            ChatColor.AQUA + s.getFormattedDuration() + (isPlaying ? ChatColor.YELLOW : ChatColor.WHITE));
                    player.sendMessage(line);
                }
                player.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "next":
            case "skip":
                player.sendMessage(prefix + ChatColor.RED + "Ban khong co quyen chuyen bai hat! He thong phat tu dong toan server.");
                break;

            default:
                player.sendMessage(prefix + ChatColor.RED + "Lenh khong hop le. Go " + ChatColor.WHITE + "/nhac" + ChatColor.RED + " de xem huong dan.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("on", "off", "check", "list");
            List<String> sub = new ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    sub.add(s);
                }
            }
            return sub;
        }
        return new ArrayList<>();
    }
}
