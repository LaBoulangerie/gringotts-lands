package net.laboulangerie.gringottslands.tax;

import org.bukkit.OfflinePlayer;

import org.gestern.gringotts.accountholder.AccountHolder;

public class TaxAccountHolder implements AccountHolder {
    public static final String ACCOUNT_TYPE = "tax";
    private final       OfflinePlayer player;

    /**
     * Instantiates a new Land account holder.
     *
     * @param player the player (OfflinePlayer)
     */
    TaxAccountHolder(OfflinePlayer player) {
        this.player = player;
    }

    /**
     * Return name of the account holder.
     *
     * @return name of the account holder
     */
    @Override
    public String getName() {
        return this.player.getName();
    }

    /**
     * Send message to the account holder.
     *
     * @param message to send
     */
    @Override
    public void sendMessage(String message) {
        this.player.getPlayer().sendMessage(message);
    }

    /**
     * Type of the account holder. For instance "faction" or "player".
     *
     * @return account holder type
     */
    @Override
    public String getType() {
        return ACCOUNT_TYPE;
    }

    /**
     * A unique identifier for the account holder.
     * For players, this is simply the name. For factions, it is their id.
     *
     * @return unique account holder id
     */
    @Override
    public String getId() {
        return ACCOUNT_TYPE + "-" + this.player.getUniqueId().toString();
    }

    /**
     * The player owning this account
     * @return OfflinePlayer object
     */
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean hasPermission(String arg0) {
        return false;
    }
}
