package com.theapocalypsemc.longtest;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Main class for the plugin.
 *
 * @author SirFaizdat
 */
public class LongTest extends JavaPlugin {

    File dataLogFile;

    File analyticsLogFile;
    FileWriter analyticsFileWriter;

    PlayerAnalytics analytics;

    List<String> pendingLogLines = new ArrayList<String>();

    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        initDataLog();
        initAnalyticsLog();

        analytics = new PlayerAnalytics(this);

        getLogger().info("Enabled the long test by SirFaizdat.");

        // 5 minutes after the server starts, collect the server data.
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                collectServerData();
            }
        }, (long) 5 * 60 * 20);
    }

    private void initAnalyticsLog() {
        analyticsLogFile = new File(getDataFolder(), "playerAnalytics.log");
        if (!analyticsLogFile.exists()) {
            try {
                if (!analyticsLogFile.createNewFile())
                    throw new IOException(); // IntelliJ forced me to handle the return boolean.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initDataLog() {
        dataLogFile = new File(getDataFolder(), "serverData.log");
        if (!dataLogFile.exists()) {
            try {
                if (!dataLogFile.createNewFile())
                    throw new IOException("createNewFile() returned false."); // IntelliJ forced me to handle the return boolean.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void collectServerData() {
        try {
            PrintWriter writer = new PrintWriter(dataLogFile);
            Set<String> propertyNames = System.getProperties().stringPropertyNames();
            writer.append("All system properties:\n");
            for (String propertyName : propertyNames)
                writer.append("\t").append(propertyName).append(": ").append(System.getProperty(propertyName)).append("\n");
            writer.append("Memory information:\n\t" + collectMemoryData());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String collectMemoryData() {
        return ("CPU cores: " + Runtime.getRuntime().availableProcessors() + "\n\t") + "Maximum memory available to JVM: " + (Runtime.getRuntime().maxMemory() / 1048576) + " MB" + "\n\t" + "Allocated memory: " + (Runtime.getRuntime().totalMemory() / 1048576) + " MB" + "\n\t" + "Used memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MB" + "\n\t" + "Free memory: " + (Runtime.getRuntime().freeMemory() / 1048576) + " MB" + "\n";
    }

    public void onDisable() {
        analytics.logConclusion();
        try {
            analyticsFileWriter = new FileWriter(analyticsLogFile);
            for (String textToLog : pendingLogLines)
                analyticsFileWriter.append(textToLog).append(System.lineSeparator());
            analyticsFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logToAnalyticsFile(String textToLog) {
        pendingLogLines.add(textToLog);
    }

}
