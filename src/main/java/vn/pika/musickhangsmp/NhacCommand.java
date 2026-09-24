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
                    player.sendMessage(prefix + ChatColor.GREEN + "Da bat nhac nen! Ban se nghe bai hat khi he thong phat.");
                    Song current = manager.getCurrentSong();
                    if (current != null && manager.isServerEnabled()) {
                        int remaining = Math.max(0, current.getDurationSeconds() - manager.getElapsedSeconds());
                        player.sendMessage(prefix + ChatColor.GRAY + "Hien tai dang phat: " + ChatColor.GOLD + current.getName()
                                + ChatColor.GRAY + " (Con " + ChatColor.WHITE + remaining + "s" + ChatColor.GRAY + " cho bai ke tiep).");
                    }
                }
                break;

            case "off":
                manager.setMusicEnabled(player.getUniqueId(), false);
                manager.stopPlayerSound(player);
                player.sendMessage(prefix + ChatColor.RED + "Da tat nhac nen cho ban than. Go " + ChatColor.WHITE + "/nhac on" + ChatColor.RED + " bat cu luc nao de nghe lai!");
                break;

            case "check":
                if (!manager.isServerEnabled()) {
                    player.sendMessage(prefix + ChatColor.RED + "He thong nhac toan server hien dang tam tat boi Admin.");
                    return true;
                }
                Song current = manager.getCurrentSong();
                if (current == null) {
                    player.sendMessage(prefix + ChatColor.RED + "Chua co bai hat nao trong danh sach phat!");
                    return true;
                }

                int elapsed = manager.getElapsedSeconds();
                int total = current.getDurationSeconds();
                int remaining = Math.max(0, total - elapsed);
                boolean isEnabled = manager.isMusicEnabled(player.getUniqueId());

                player.sendMessage(ChatColor.DARK_PURPLE + "════════════════ " + ChatColor.LIGHT_PURPLE + "[♫ KHANGSMP MUSIC] " + ChatColor.DARK_PURPLE + "════════════════");
                player.sendMessage(ChatColor.WHITE + "• Trang thai ca nhan: " + (isEnabled ? ChatColor.GREEN + "Dang Bat [ON]" : ChatColor.RED + "Dang Tat [OFF]"));
                player.sendMessage(ChatColor.WHITE + "• Bai dang phat: " + ChatColor.GOLD + current.getName());
                player.sendMessage(ChatColor.WHITE + "• Tien do thoi gian: " + ChatColor.AQUA + Song.formatTime(elapsed) + ChatColor.GRAY + " / " + ChatColor.AQUA + Song.formatTime(total) + " " + current.getProgressBar(elapsed));
                player.sendMessage(ChatColor.WHITE + "• Bai tiep theo sau: " + ChatColor.YELLOW + remaining + " giay");
                player.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "list":
                List<Song> playlist = manager.getPlaylist();
                player.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[DANH SACH 15 BAI HAT] " + ChatColor.DARK_PURPLE + "═══════════════");
                for (int i = 0; i < playlist.size(); i++) {
                    Song s = playlist.get(i);
                    boolean isPlaying = (manager.getCurrentIndex() == i && manager.isServerEnabled());
                    String marker = isPlaying ? ChatColor.GREEN + " ► " : ChatColor.DARK_GRAY + " • ";
                    String songColor = isPlaying ? (ChatColor.GOLD + "" + ChatColor.BOLD) : ChatColor.YELLOW.toString();
                    player.sendMessage(marker + ChatColor.WHITE + String.format("%02d. ", i + 1) + songColor + s.getName() + ChatColor.GRAY + " [" + s.getFormattedDuration() + "]");
                }
                player.sendMessage(ChatColor.GRAY + "Go " + ChatColor.WHITE + "/nhac on" + ChatColor.GRAY + " hoac " + ChatColor.WHITE + "/nhac off" + ChatColor.GRAY + " de bat/tat am thanh.");
                player.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "next":
            case "skip":
                player.sendMessage(prefix + ChatColor.RED + "Ban khong co quyen chuyen bai! He thong phat nhac tu dong toan server khong cho phep thanh vien bo qua bai hat.");
                break;

            default:
                player.sendMessage(prefix + ChatColor.RED + "Lenh khong hop le! Su dung: " + ChatColor.YELLOW + "/nhac on | off | check | list");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("on", "off", "check", "list");
            List<String> result = new ArrayList<>();
            for (String s : list) {
                if (s.startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}