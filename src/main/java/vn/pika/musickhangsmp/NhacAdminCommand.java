package vn.pika.musickhangsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class NhacAdminCommand implements CommandExecutor, TabCompleter {
    private final MusicKhangSMP plugin;
    private final MusicManager manager;

    public NhacAdminCommand(MusicKhangSMP plugin, MusicManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("musickhangsmp.admin")) {
            sender.sendMessage(ChatColor.RED + "Ban khong co quyen su dung lenh quan tri nay (musickhangsmp.admin)!");
            return true;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&d[&e♫ KhangSMP&d] &7"));

        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[♫ QUẢN TRỊ NHẠC SERVER] " + ChatColor.DARK_PURPLE + "═══════════════");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad on     " + ChatColor.GRAY + "→ Bat he thong phat nhac toan server");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad off    " + ChatColor.GRAY + "→ Tat he thong phat nhac toan server");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad next   " + ChatColor.GRAY + "→ Bo qua va chuyen sang bai tiep theo");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad check  " + ChatColor.GRAY + "→ Kiem tra thong tin bai dang phat");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad list   " + ChatColor.GRAY + "→ Xem danh sach tat ca bai hat");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad reload " + ChatColor.GRAY + "→ Nap lai config.yml");
            sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on":
                if (manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.YELLOW + "He thong nhac server da dang BAT roi!");
                } else {
                    manager.start();
                    sender.sendMessage(prefix + ChatColor.GREEN + "Da bat he thong phat nhac toan server thanh cong!");
                    Bukkit.broadcastMessage(prefix + ChatColor.GREEN + "He thong nhac toan server da duoc kich hoat boi Admin!");
                }
                break;

            case "off":
                if (!manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.YELLOW + "He thong nhac server da dang TAT roi!");
                } else {
                    manager.stop();
                    sender.sendMessage(prefix + ChatColor.RED + "Da tat he thong phat nhac toan server va dung am thanh!");
                    Bukkit.broadcastMessage(prefix + ChatColor.RED + "He thong nhac toan server da duoc tam dung boi Admin.");
                }
                break;

            case "next":
            case "skip":
                if (!manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.RED + "Khong the chuyen bai vi he thong dang tat! Hay go /nhacad on truoc.");
                    return true;
                }
                manager.nextSong();
                Song next = manager.getCurrentSong();
                String name = (next != null) ? next.getName() : "Khong xac dinh";
                sender.sendMessage(prefix + ChatColor.GREEN + "Da chuyen sang bai hat ke tiep: " + ChatColor.GOLD + name);
                break;

            case "check":
                Song current = manager.getCurrentSong();
                if (current == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Chua co bai hat nao trong danh sach phat!");
                    return true;
                }
                int elapsed = manager.getElapsedSeconds();
                int total = current.getDurationSeconds();
                int remaining = Math.max(0, total - elapsed);

                sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[♫ KIEM TRA NHAC SERVER] " + ChatColor.DARK_PURPLE + "═══════════════");
                sender.sendMessage(ChatColor.WHITE + "• Trang thai server: " + (manager.isServerEnabled() ? ChatColor.GREEN + "Dang chay [ON]" : ChatColor.RED + "Dang tat [OFF]"));
                sender.sendMessage(ChatColor.WHITE + "• Bai dang phat (" + (manager.getCurrentIndex() + 1) + "/" + manager.getPlaylist().size() + "): " + ChatColor.GOLD + current.getName());
                sender.sendMessage(ChatColor.WHITE + "• Tien do thoi gian: " + ChatColor.AQUA + Song.formatTime(elapsed) + ChatColor.GRAY + " / " + ChatColor.AQUA + Song.formatTime(total) + " " + current.getProgressBar(elapsed));
                sender.sendMessage(ChatColor.WHITE + "• Con lai: " + ChatColor.YELLOW + remaining + " giay" + ChatColor.GRAY + " truoc khi tu dong sang bai ke");
                sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "list":
                List<Song> playlist = manager.getPlaylist();
                sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[DANH SACH 15 BAI HAT] " + ChatColor.DARK_PURPLE + "═══════════════");
                for (int i = 0; i < playlist.size(); i++) {
                    Song s = playlist.get(i);
                    boolean isPlaying = (manager.getCurrentIndex() == i && manager.isServerEnabled());
                    String marker = isPlaying ? ChatColor.GREEN + " ► " : ChatColor.DARK_GRAY + " • ";
                    String songColor = isPlaying ? (ChatColor.GOLD + "" + ChatColor.BOLD) : ChatColor.YELLOW.toString();
                    sender.sendMessage(marker + ChatColor.WHITE + String.format("%02d. ", i + 1) + songColor + s.getName() + ChatColor.GRAY + " [" + s.getFormattedDuration() + "]");
                }
                sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "reload":
                plugin.reloadConfig();
                manager.loadPlaylist();
                sender.sendMessage(prefix + ChatColor.GREEN + "Da nap lai cau hinh config.yml cua MusicKhangSMP thanh cong!");
                break;

            default:
                sender.sendMessage(prefix + ChatColor.RED + "Lenh quan tri khong hop le! Su dung: " + ChatColor.GOLD + "/nhacad on | off | next | check | list | reload");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("musickhangsmp.admin")) return new ArrayList<>();
        if (args.length == 1) {
            List<String> list = Arrays.asList("on", "off", "next", "check", "list", "reload");
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