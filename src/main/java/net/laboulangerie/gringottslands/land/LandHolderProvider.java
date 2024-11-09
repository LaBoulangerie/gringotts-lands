package net.laboulangerie.gringottslands.land;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.applicationframework.util.ULID;
import me.angeschossen.lands.api.events.LandRenameEvent;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.memberholder.MemberHolder;
import me.angeschossen.lands.api.player.LandPlayer;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.gestern.gringotts.AccountChest;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.accountholder.AccountHolderProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LandHolderProvider implements AccountHolderProvider, Listener {

    private LandsIntegration api;
    
    public LandHolderProvider(LandsIntegration api) {
        this.api = api;
    }

    /**
     * Get the AccountHolder object mapped to the given id for this provider.
     *
     * @param ulid id of account holder
     * @return account holder for id
     */
    public @Nullable AccountHolder getAccountHolder(@NotNull ULID ulid) {
        Land land = this.api.getLandByULID(ulid);

        return getAccountHolder(land);
    }

    /**
     * Get the AccountHolder object mapped to the given id for this provider.
     *
     * @param id id of account holder
     * @return account holder for id
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull String id) {
        try {
            ULID targetUlid = ULID.fromString(id);

            return getAccountHolder(targetUlid);
        } catch (IllegalArgumentException ignored) {}

        String vaultPrefix = LandAccountHolder.ACCOUNT_TYPE + "-";
        String validId = id.startsWith(vaultPrefix) ? id.substring(vaultPrefix.length()) : id;

        return getAccountHolder(ULID.fromString(validId));
    }

    /**
     * Get the AccountHolder object mapped to the given id for this provider.
     *
     * @param uuid id of account holder
     * @return account holder for id
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull UUID uuid) {
        return null;
    }

    /**
     * Get a AccountHolder for the land of which player is a resident, if any.
     *
     * @param player player to get town for
     * @return AccountHolder for the land of which player is a resident, if
     * any. null otherwise.
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull OfflinePlayer player) {
        LandPlayer resident = this.api.getLandPlayer(player.getUniqueId());
        if (resident == null) return null;


        // resident.getLands().stream().f

        return null;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return LandAccountHolder.ACCOUNT_TYPE;
    }

    /**
     * Gets account names.
     *
     * @return the account names
     */
    @Override
    public @NotNull Set<String> getAccountNames() {
        return this.api.getLands().stream().map(MemberHolder::getName).collect(Collectors.toSet());
    }

    /**
     * Gets account holder.
     *
     * @param town the town
     * @return the account holder
     */
    public @Nullable AccountHolder getAccountHolder(@Nullable Land land) {
        if (land == null) {
            return null;
        }

        return new LandAccountHolder(land);
    }

    /**
     * Rename town.
     *
     * @param event the event
     */
    @EventHandler
    public void renameLand(LandRenameEvent event) {
        Land land = event.getLand();

        AccountHolder holder = getAccountHolder(land);

        if (holder == null) {
            return;
        }

        GringottsAccount account = Gringotts.instance.getAccounting().getAccount(holder);

        if (account == null) {
            return;
        }

        Gringotts.instance.getDao().retrieveChests(account).forEach(AccountChest::updateSign);
    }

    /**
     * Calculate start balance.
     *
     * @param event the event
     */
    // @EventHandler
    // public void calculateStartBalance(CalculateStartBalanceEvent event) {
    //     if (!event.holder.getType().equals(getType())) {
    //         return;
    //     }

    //     event.startValue = Configuration.CONF.getCurrency().getCentValue(LandsConfiguration.CONF.townStartBalance);
    // }
}
