package io.github.cats1337.CatsTracker.utils;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import io.github.cats1337.CatsTracker.CatsTracker;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PointLogger {
    private static PointLogger instance;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("hh:mm a");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
    protected final static String dataLayerFolderPath = "plugins" + File.separator + "CatsTracker";
    private final String logFolderPath = dataLayerFolderPath + File.separator + "logs";
    public static boolean fileLoggerRunning = false;
    private final int secondsBetweenWrites = CatsTracker.getInstance().getConfig().getInt("logSaveSeconds");
    private final StringBuilder queuedEntries = new StringBuilder(); // Use StringBuilder for better performance.

    public static PointLogger getInstance() {
        if (instance == null) {
            instance = new PointLogger();
        }
        return instance;
    }

    public void fileLogger() {
        File logFolder = new File(this.logFolderPath);
        logFolder.mkdirs();

        this.deleteExpiredLogs();

        if (!CatsTracker.getInstance().getConfig().getBoolean("logMessages")) return;

        if (fileLoggerRunning) return;

        int logExpirationDays = CatsTracker.getInstance().getConfig().getInt("logDays");
        if (logExpirationDays >= 0) {
            BukkitScheduler scheduler = CatsTracker.getInstance().getServer().getScheduler();
            final long ticksPerSecond = 20L;
            final long ticksPerDay = ticksPerSecond * 60 * 60 * 24;
            scheduler.runTaskTimerAsynchronously(CatsTracker.getInstance(), new EntryWriter(), this.secondsBetweenWrites * ticksPerSecond, this.secondsBetweenWrites * ticksPerSecond);
            scheduler.runTaskTimerAsynchronously(CatsTracker.getInstance(), new ExpiredLogRemover(), ticksPerDay, ticksPerDay);
            fileLoggerRunning = true;
        } else if (logExpirationDays == -1) {
            CatsTracker.log.info("Logging is disabled in config.yml");
        }
    }

    public void addEntry(String entry) {
        fileLogger();

        if (!CatsTracker.getInstance().getConfig().getBoolean("logMessages")) return;

        int logExpirationDays = CatsTracker.getInstance().getConfig().getInt("logDays");
        if (logExpirationDays < 0) {
            CatsTracker.getInstance().getConfig().set("logMessages", false);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -5);
        String timestamp = this.timestampFormat.format(cal.getTime());
        String datestamp = this.dateFormat.format(new Date());
        datestamp = datestamp.replaceAll("(\\d{2})_(\\d{2})_(\\d{4})", "$2/$1/$3");

        entry = entry + " @ " + timestamp + " " + datestamp;

        this.queuedEntries.append(entry).append('\n');
    }

    public void writeEntries() {
        try {
            if (this.queuedEntries.isEmpty()) return;

            String filename = this.dateFormat.format(new Date()) + ".log";
            String filepath = this.logFolderPath + File.separator + filename;
            File logFile = new File(filepath);

            Files.asCharSink(logFile, StandardCharsets.UTF_8, FileWriteMode.APPEND).write(this.queuedEntries.toString());

            this.queuedEntries.setLength(0); // Clear the buffer after writing.
        } catch (Exception e) {
            CatsTracker.log.severe("Failed to write log entries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteExpiredLogs() {
        try {
            File logFolder = new File(this.logFolderPath);
            File[] files = logFolder.listFiles();

            if (files == null) return;

            int expirationDays = CatsTracker.getInstance().getConfig().getInt("logDays");
            Calendar expirationDay = Calendar.getInstance();
            expirationDay.add(Calendar.DATE, -expirationDays);

            for (File file : files) {
                if (file.isDirectory() || !file.getName().endsWith(".log")) continue;

                String filename = file.getName().replace(".log", "");
                String[] dateParts = filename.split("_");
                if (dateParts.length != 3) continue;

                try {
                    int month = Integer.parseInt(dateParts[0]) - 1;
                    int day = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    Calendar filedate = Calendar.getInstance();
                    filedate.set(year, month, day);
                    if (filedate.before(expirationDay)) {
                        if (!file.delete()) {
                            CatsTracker.log.warning("Failed to delete expired log file: " + file.getName());
                        }
                    }
                } catch (NumberFormatException e) {
                    CatsTracker.log.info("Ignoring invalid log file: " + file.getName());
                }
            }
        } catch (Exception e) {
            CatsTracker.log.severe("Failed to delete expired log files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class EntryWriter implements Runnable {
        @Override
        public void run() {
            writeEntries();
        }
    }

    private class ExpiredLogRemover implements Runnable {
        @Override
        public void run() {
            deleteExpiredLogs();
        }
    }

}
