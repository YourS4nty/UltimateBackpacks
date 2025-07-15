package dev.yours4nty.ultimatebackpacks.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionLogger {

    private static final File logFile = new File("plugins/UltimateBackpacks/logs/Activity-Day-" +
            java.time.LocalDate.now() + ".log");

    public static void log(String message) {
        try {
            if (!logFile.getParentFile().exists()) logFile.getParentFile().mkdirs();
            if (!logFile.exists()) logFile.createNewFile();

            try (FileWriter writer = new FileWriter(logFile, true)) {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write("[" + timeStamp + "] " + message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
