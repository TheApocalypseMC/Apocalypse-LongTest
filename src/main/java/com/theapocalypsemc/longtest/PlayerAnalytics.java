package com.theapocalypsemc.longtest;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Collects data about players throughout the day and prints them to a log file.
 *
 * @author SirFaizdat
 */
public class PlayerAnalytics implements Listener {

    private static final long HOUR_IN_TICKS = (long) 60 * 60 * 20;

    LongTest plugin;
    int hour = 0;
    int amountJoinedInHour = 0, amountLeftInHour = 0;
    List<String> playersJoinedInLastHour = new ArrayList<String>();
    List<DataHour> dataHours = new ArrayList<DataHour>();

    public PlayerAnalytics(LongTest plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            public void run() {
                logAnalytics();
            }
        }, HOUR_IN_TICKS, HOUR_IN_TICKS); // Wait an hour (3rd argument) until first run, and run each hour (4th argument)
    }

    private void logAnalytics() {
        // Line seperators added in logToAnalyticsFile method.
        plugin.logToAnalyticsFile("Hour " + hour + ":");
        plugin.logToAnalyticsFile("\tPlayers joined: " + amountJoinedInHour);
        plugin.logToAnalyticsFile("\tPlayers left: " + amountLeftInHour);
        plugin.logToAnalyticsFile("\tPlayers online: " + Bukkit.getServer().getOnlinePlayers().size());
        plugin.logToAnalyticsFile("\tPlayers joined:");
        for (String playerName : playersJoinedInLastHour) plugin.logToAnalyticsFile("\t\t- " + playerName);

        dataHours.add(new DataHour(hour, amountJoinedInHour, amountLeftInHour, Bukkit.getServer().getOnlinePlayers().size(), playersJoinedInLastHour));

        amountJoinedInHour = 0;
        amountLeftInHour = 0;
        playersJoinedInLastHour.clear();
        hour++;
    }

    public void logConclusion() {
        int playerCountPerHour[] = new int[dataHours.size()];
        int peakPlayerCount = 0;
        String peakPlayerCountHour = "";
        for (DataHour hour : dataHours) {
            playerCountPerHour[hour.hour] = hour.playerCountAtHour;
            if(hour.playerCountAtHour > peakPlayerCount) {
                peakPlayerCount = hour.playerCountAtHour;
                peakPlayerCountHour = hour.properHourString;
            }
        }
        double averagePlayerCount = average(playerCountPerHour);

        plugin.logToAnalyticsFile("Analytics Conclusion:");
        plugin.logToAnalyticsFile("\tTest run for " + dataHours.size() + " hours.");
        plugin.logToAnalyticsFile("\tAverage player count: " + averagePlayerCount);
        plugin.logToAnalyticsFile("\tPeak player count: " + peakPlayerCount + " at " + peakPlayerCountHour);
        plugin.logToAnalyticsFile("\tSee above for hour-by-hour measurements.");
    }

    private double average(int[] data) {
        int sum = 0;
        for (int d : data) sum += d;
        return 1.0d * sum / data.length;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        amountJoinedInHour++;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        amountLeftInHour++;
    }

    class DataHour {
        int hour;
        int amountJoinedInHour = 0, amountLeftInHour = 0;
        int playerCountAtHour;
        String properHourString = "";
        List<String> playersJoinedInLastHour = new ArrayList<String>();

        public DataHour(int hour, int amountJoinedInHour, int amountLeftInHour, int playerCountAtHour, List<String> playersJoinedInLastHour) {
            this.hour = hour;
            this.amountJoinedInHour = amountJoinedInHour;
            this.amountLeftInHour = amountLeftInHour;
            this.playerCountAtHour = playerCountAtHour;
            this.playersJoinedInLastHour = playersJoinedInLastHour;
            this.properHourString = new SimpleDateFormat("hh a").format(Calendar.getInstance().getTime());
        }
    }

}
