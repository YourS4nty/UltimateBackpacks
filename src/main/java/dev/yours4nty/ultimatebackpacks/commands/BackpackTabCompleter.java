package dev.yours4nty.ultimatebackpacks.commands;

// Java imports
import java.util.*;
import java.util.stream.Collectors;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackStorage;

public class BackpackTabCompleter implements TabCompleter {

    // List of subcommands for the /backpack command
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "newshared", "adduser", "removeuser", "delshared", "transferowner", "leave"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        String sub = args.length > 0 ? args[0].toLowerCase() : "";

        if (args.length == 1) {
            // Autocomplete the first argument with subcommands
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(sub))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // This block handles the second argument based on the subcommand
            if (sender instanceof Player player) {
                if (sub.equals("adduser") || sub.equals("removeuser") || sub.equals("delshared") || sub.equals("transferowner")) {
                    List<String> backpacks = SharedBackpackStorage.getSharedBackpacks(player.getUniqueId());
                    return backpacks.stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        } else if (sub.equals("leave") && sender instanceof Player player) {
            List<String> memberOf = SharedBackpackStorage.getSharedBackpacksAsMember(player.getUniqueId());
            return memberOf.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            // Player suggestions for specific subcommands
            if (sub.equals("adduser") || sub.equals("removeuser") || sub.equals("transferowner")) {
                List<String> suggestions = new ArrayList<>();

                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    String name = player.getName();
                    if (name != null && name.toLowerCase().startsWith(args[2].toLowerCase())) {
                        suggestions.add(name);
                    }
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!suggestions.contains(online.getName()) &&
                            online.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        suggestions.add(online.getName());
                    }
                }

                return suggestions;
            }
        }

        return Collections.emptyList();
    }

}
