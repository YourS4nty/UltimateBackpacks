package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.util.HashMap;
import java.util.UUID;

public class ViewerContext {

    private static final HashMap<UUID, UUID> viewerToTarget = new HashMap<>();

    // Associates a viewer with a target UUID
    public static void set(UUID viewer, UUID target) {
        viewerToTarget.put(viewer, target);
    }

    // Retrieves the target UUID associated with a viewer
    public static UUID get(UUID viewer) {
        return viewerToTarget.get(viewer);
    }

    // Checks if a viewer has an associated target UUID
    public static void clear(UUID viewer) {
        viewerToTarget.remove(viewer);
    }
}
