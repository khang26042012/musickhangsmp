package vn.pika.musickhangsmp;

public class Song {
    private final String id;
    private final String name;
    private final int durationSeconds;
    private final String javaSound;
    private final String bedrockSound;

    public Song(String id, String name, int durationSeconds, String javaSound, String bedrockSound) {
        this.id = id;
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.javaSound = javaSound;
        this.bedrockSound = bedrockSound;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getJavaSound() {
        return javaSound;
    }

    public String getBedrockSound() {
        return bedrockSound;
    }

    public String getFormattedDuration() {
        return formatTime(durationSeconds);
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getProgressBar(int elapsed) {
        int totalBars = 20;
        float percent = (float) Math.min(elapsed, durationSeconds) / (float) Math.max(1, durationSeconds);
        int progressBars = (int) (totalBars * percent);

        StringBuilder sb = new StringBuilder("§a[");
        for (int i = 0; i < totalBars; i++) {
            if (i < progressBars) {
                sb.append("■");
            } else if (i == progressBars) {
                sb.append("§e►§7");
            } else {
                sb.append("□");
            }
        }
        sb.append("§a] §e").append((int) (percent * 100)).append("%");
        return sb.toString();
    }
}
