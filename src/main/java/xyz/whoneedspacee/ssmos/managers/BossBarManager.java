package xyz.whoneedspacee.ssmos.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.whoneedspacee.ssmos.Main;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    public static BossBarManager ourInstance;
    private static JavaPlugin plugin = Main.getInstance();
    private static Map<Player, BossBar> bossBars = new ConcurrentHashMap<>();

    public BossBarManager() {
        ourInstance = this;
    }

    public static void setBar(Player player, String text, float healthPercent) {
        removeBar(player);
        BossBar bossBar = Bukkit.createBossBar(text, BarColor.WHITE, BarStyle.SOLID);
        bossBar.setProgress(Math.max(0, Math.min(1, healthPercent / 100.0)));
        bossBar.addPlayer(player);
        bossBars.put(player, bossBar);
    }

    public static void removeBar(Player player) {
        BossBar bossBar = bossBars.remove(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    /** No-op stub — native BossBar API handles positioning automatically. */
    public static void teleportBar(Player player) {
    }

    public static void updateText(Player player, String text) {
        updateBar(player, text, -1);
    }

    public static void updateHealth(Player player, float healthPercent) {
        updateBar(player, null, healthPercent);
    }

    public static void updateBar(Player player, String text, float healthPercent) {
        BossBar bossBar = bossBars.get(player);
        if (bossBar == null) {
            setBar(player, text != null ? text : "", healthPercent != -1 ? healthPercent : 100);
            return;
        }
        if (text != null) {
            bossBar.setTitle(text);
        }
        if (healthPercent != -1) {
            bossBar.setProgress(Math.max(0, Math.min(1, healthPercent / 100.0)));
        }
    }

    public static Set<Player> getPlayers() {
        return bossBars.keySet();
    }

}
