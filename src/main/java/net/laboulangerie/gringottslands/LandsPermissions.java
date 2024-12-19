package net.laboulangerie.gringottslands;

import org.bukkit.entity.Player;

/**
 * The Permissions.
 */
public enum LandsPermissions {
    /**
     * Create vault land permissions.
     */
    CREATE_VAULT_LAND("gringotts.createvault.land");

    /**
     * The Node.
     */
    public final String node;

    LandsPermissions(String node) {
        this.node = node;
    }

    /**
     * Check if a player has this permission.
     *
     * @param player player to check
     * @return whether given player has this permission
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAllowed(Player player) {
        return player.hasPermission(this.node);
    }
}
