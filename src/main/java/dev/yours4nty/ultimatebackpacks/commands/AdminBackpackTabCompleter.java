package dev.yours4nty.ultimatebackpacks.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class AdminBackpackTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimatebackpacks.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("reload", "view", "viewshared", "log").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // --- view ---
        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("view")) {
            return IntStream.rangeClosed(1, 10)
                    .mapToObj(String::valueOf)
                    .filter(num -> num.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        // --- viewshared ---
        if (args.length == 2 && args[0].equalsIgnoreCase("viewshared")) {
            File dir = new File("plugins/UltimateBackpacks/sharedBackpacks");
            if (!dir.exists()) return Collections.emptyList();

            return Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".yml")))
                    .map(f -> f.getName().replace(".yml", ""))
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // --- log ---
        if (args.length == 2 && args[0].equalsIgnoreCase("log")) {
            return Arrays.asList("list", "view").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("log") && args[1].equalsIgnoreCase("view")) {
            File dir = new File("plugins/UltimateBackpacks/logs");
            if (!dir.exists()) return Collections.emptyList();

            return Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".log")))
                    .map(f -> f.getName().replace(".log", ""))
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("log") && args[1].equalsIgnoreCase("view")) {
            // Pagina sugerida: 1 - 10
            return IntStream.rangeClosed(1, 10)
                    .mapToObj(String::valueOf)
                    .filter(p -> p.startsWith(args[4]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
