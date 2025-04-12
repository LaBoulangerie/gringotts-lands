package net.laboulangerie.gringottslands.tax;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.events.player.economy.PlayerTaxEvent;
import me.angeschossen.lands.api.memberholder.MemberHolder;

import net.laboulangerie.gringottslands.GringottsLands;
import net.laboulangerie.gringottslands.land.LandAccountHolder;
import net.laboulangerie.gringottslands.land.LandHolderProvider;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.accountholder.AccountHolderProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaxHolderProvider implements AccountHolderProvider, Listener {

    private LandsIntegration api;

    public TaxHolderProvider(LandsIntegration api) {
        this.api = api;
    }

    /**
     * Get the AccountHolder object mapped to the given id for this provider.
     *
     * @param uuid id of account holder
     * @return account holder for id
     */
    public @Nullable AccountHolder getAccountHolder(@NotNull UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return getAccountHolder(player);
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
            return getAccountHolder(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get a AccountHolder for the player.
     *
     * @param player player 
     * @return AccountHolder for the player.
     */
    @Override
    public @Nullable AccountHolder getAccountHolder(@NotNull OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        return new TaxAccountHolder(player);
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return TaxAccountHolder.ACCOUNT_TYPE;
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
     * Lands Player Pay Tax Event.
     *
     * @param event the event
     */
    @EventHandler
    public void onPlayerTaxEvent(PlayerTaxEvent event) {
        GringottsLands.debugMsg(event.toString());

        TaxAccountHolder holder = (TaxAccountHolder) this.getAccountHolder(event.getPlayerUUID());
        double taxAccount = Gringotts.instance.getAccounting().getAccount(holder).getBalance();
        double playerAccount = event.getPlayerBalance();

        if (taxAccount - event.getTax() < 1) { // Verify that the player can't pay with his tax vault
            if(taxAccount + playerAccount - event.getTax() < 1){ // Verify that the player can't pay with his tax vault and personal balance
                // Kick the player from the area (if it's the default area it kick him from the land)
                event.getArea().untrustPlayer(event.getPlayerUUID());
    
                event.setCancelled(true);

                return;
            } else { // Empty the player tax vault and then take the rest form his personal balance
                Gringotts.instance.getAccounting().getAccount(holder).add((long)-(taxAccount*10));
                Gringotts.instance.getAccounting().getAccount(this.getAccountHolder(event.getPlayerUUID())).add((long)-((event.getTax()-taxAccount)*10));

                event.setCancelled(true);
            }
        } else { // Take the tax from the tax vault
            Gringotts.instance.getAccounting().getAccount(holder).add((long)-(event.getTax()*10));
        }

        // Give the tax amount to the land's land vault
        Gringotts.instance.getAccounting().getAccount((LandAccountHolder) new LandHolderProvider(api).getAccountHolder(event.getArea().getLand().getULID())).add((long) event.getTax()*10);

        event.setCancelled(true);
    }
}
