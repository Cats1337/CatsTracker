package io.github.cats1337.CatsTracker.utils;

import io.github.cats1337.CatsTracker.CatsTracker;

import java.util.ArrayList;
import java.util.List;

public interface ITabCompleterHelper {
    /**
     * Filters a list of commands based on the provided argument
     * @param arg The argument to filter by
     * @param subCommands The list of commands to filter
     * @return A filtered list of commands that start with the argument
     */
    static List<String> tabComplete(String arg, List<String> subCommands) {
        try {
            if (arg == null || subCommands == null || subCommands.isEmpty()) {
                return new ArrayList<>();
            }

            // Create a new ArrayList to avoid returning an immutable list
            return new ArrayList<>(subCommands.stream()
                .filter(x -> x != null && x.toLowerCase().startsWith(arg.toLowerCase()))
                .toList());
        } catch (Exception e) {
            // Log the error properly instead of using printStackTrace
            CatsTracker.log.warning("Error in tab completion: " + e.getMessage());
            // Return an empty list in case of any error
            return new ArrayList<>();
        }
    }
}