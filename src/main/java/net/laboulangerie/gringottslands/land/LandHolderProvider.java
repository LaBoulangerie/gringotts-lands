package net.laboulangerie.gringottslands.land;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.applicationframework.util.ULID;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import me.angeschossen.lands.api.events.LandRenameEvent;
import me.angeschossen.lands.api.events.land.bank.LandBankBalanceChangedEvent;
import me.angeschossen.lands.api.events.land.bank.LandBankWithdrawEvent;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.memberholder.MemberHolder;
import net.laboulangerie.gringottslands.GringottsLands;
import net.laboulangerie.gringottslands.LandsConfiguration;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.gestern.gringotts.AccountChest;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.accountholder.AccountHolderProvider;
import org.gestern.gringotts.api.Account;
import org.gestern.gringotts.api.TransactionResult;
import org.gestern.gringotts.event.AccountBalanceChangeEvent;
import org.gestern.gringotts.event.CalculateStartBalanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LandHolderProvider implements AccountHolderProvider, Listener {

    private Gringotts gringotts;
    private LandsIntegration api;

    public LandHolderProvider(Gringotts gringotts, LandsIntegration api) {
        this.gringotts = gringotts;
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
            return getAccountHolder(ULID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get the AccountHolder object mapped to the given id for this provider.
     *
     * @param uuid id of account holder
     * @return account holder for id
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull UUID uuid) {
        throw new UnsupportedOperationException("Unimplemented method 'getAccountHolder'");
    }

    /**
     * Get a AccountHolder for the land of which player is a resident, if any.
     *
     * @param player player to get land for
     * @return AccountHolder for the land of which player is a resident, if
     *         any. null otherwise.
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull OfflinePlayer player) {
        throw new UnsupportedOperationException("Unimplemented method 'getAccountHolder'");
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
     * @param Land the land
     * @return the account holder
     */
    public @Nullable AccountHolder getAccountHolder(@Nullable Land land) {
        if (land == null) {
            return null;
        }

        return new LandAccountHolder(land);
    }

    /**
     * Rename land.
     *
     * @param event the event
     */
    @EventHandler
    public void renameLand(LandRenameEvent event) {
        ULID id = event.getLand().getULID();

        new BukkitRunnable() {
            @Override
            public void run() {
                LandAccountHolder holder = (LandAccountHolder) getAccountHolder(id);
                if (holder == null)
                    return;

                GringottsAccount account = Gringotts.instance.getAccounting().getAccount(holder);
                if (account == null)
                    return;

                Gringotts.instance.getDao().retrieveChests(account).forEach(AccountChest::updateSign);
            }
        }.runTask(this.gringotts);
    }

    /**
     * Calculate start balance.
     *
     * @param event the event
     */
    @EventHandler
    public void calculateStartBalance(CalculateStartBalanceEvent event) {
        if (!event.holder.getType().equals(getType())) {
            return;
        }

        event.startValue = Configuration.CONF.getCurrency().getCentValue(LandsConfiguration.CONF.landStartBalance);
    }

    /**
     * Delete land.
     *
     * @param event the event
     */
    @EventHandler
    public void deleteLand(LandDeleteEvent event) {
        Land land = event.getLand();
        AccountHolder holder = getAccountHolder(land);

        if (holder == null) {
            return;
        }

        GringottsAccount account = Gringotts.instance.getAccounting().getAccount(holder);

        if (account == null) {
            return;
        }

        Gringotts.instance.getDao().deleteAccount(account);
    }

    @EventHandler
    public void onBalanceChange(AccountBalanceChangeEvent event) {
        if (!event.holder.getType().equals(this.getType()))
            return;

        double balance = Configuration.CONF.getCurrency().getDisplayValue(event.balance);
        LandAccountHolder holder = (LandAccountHolder) this.getAccountHolder(event.holder.getId());

        if (holder == null) {
            return;
        }

        if (holder.getLand().getBalance() == balance)
            return;

        GringottsLands.LOGGER.fine("Replicate Gringotts account balance change detected on Land " + holder.getId() + " - " + balance);

        holder.getLand().setBalance(balance);
    }

    @EventHandler
    public void onLandBankBalanceChanged(LandBankBalanceChangedEvent event) {
        Land land = event.getLand();

        AccountHolder holder = getAccountHolder(land);

        if (holder == null) {
            return;
        }

        GringottsAccount account = Gringotts.instance.getAccounting().getAccount(holder);
        if (account.getBalance() != event.getNow()) {
            GringottsLands.LOGGER.fine("Replicate Land Balance change (from " + event.getPrevious() + " to " + event.getNow() + ") on account " + land.getULID());
            long update = Configuration.CONF.getCurrency().getCentValue(event.getNow() - event.getPrevious());
            TransactionResult result;
            if (update > 0) {
                result = account.add(update);
            } else {
                result = account.remove(Math.abs(update));
            }

            if (result != TransactionResult.SUCCESS) {
                throw new IllegalStateException(account.owner.getId() + " account transaction error for "
                        + (event.getNow() - event.getPrevious()) + " " + result);
            }
        }
    }

    @EventHandler
    public void onLandBankWithdraw(LandBankWithdrawEvent event) {
        Land land = event.getLand();

        AccountHolder holder = getAccountHolder(land);

        if (holder == null) {
            return;
        }

        Account account = Gringotts.instance.getEco().getAccount(holder.getId());
        long value = Configuration.CONF.getCurrency().getCentValue(event.getValue());
        if (!account.has(value)) {
            event.setCancelled(true);
        }
    }

}
