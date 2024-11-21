package net.laboulangerie.gringottslands.land;

import me.angeschossen.lands.api.land.Land;

import org.gestern.gringotts.accountholder.AccountHolder;

public class LandAccountHolder implements AccountHolder {
    public static final String ACCOUNT_TYPE = "land";
    private final       Land   land;

    /**
     * Instantiates a new Land account holder.
     *
     * @param land the land
     */
    LandAccountHolder(Land land) {
        this.land = land;
    }

    /**
     * Return name of the account holder.
     *
     * @return name of the account holder
     */
    @Override
    public String getName() {
        return this.land.getName();
    }

    /**
     * Send message to the account holder.
     *
     * @param message to send
     */
    @Override
    public void sendMessage(String message) {
        this.land.getOnlinePlayers().forEach(player -> player.sendMessage(message));
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
        return this.land.getULID().toString();
    }

    /**
     * The town onwing this account
     * @return town object
     */
    public Land getLand() {
        return this.land;
    }

    @Override
    public boolean hasPermission(String arg0) {
        return false;
    }
}
