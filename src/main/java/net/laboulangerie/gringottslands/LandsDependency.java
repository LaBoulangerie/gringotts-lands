package net.laboulangerie.gringottslands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.api.dependency.Dependency;
import org.gestern.gringotts.event.PlayerVaultCreationEvent;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import net.laboulangerie.gringottslands.land.LandAccountHolder;
import net.laboulangerie.gringottslands.land.LandHolderProvider;

public class LandsDependency implements Dependency, Listener {
    private final LandHolderProvider landHolderProvider;
    private final Plugin lands;
    private final String id;
    private final LandsIntegration api;

    /**
     * Instantiates a new Lands dependency.
     *
     * @param lands    the plugin
     */
    public LandsDependency(Plugin lands) {
        if (lands == null) {
            throw new NullPointerException("'lands' is null");
        }

        this.lands = lands;
        this.id = "lands";

        this.api = LandsIntegration.of(lands);
        this.landHolderProvider = new LandHolderProvider(this.api);
        
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets plugin.
     *
     * @return the plugin
     */
    @Override
    public Plugin getPlugin() {
        return this.lands;
    }

    /**
     * On enable.
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, Gringotts.instance);
        Bukkit.getPluginManager().registerEvents(this.landHolderProvider, Gringotts.instance);

        Gringotts.instance.registerAccountHolderProvider(LandAccountHolder.ACCOUNT_TYPE, this.landHolderProvider);
    }

    public void checkLandBalanceConsistency() {
        // Check gringotts/lands balance consistency
        for (Land land : this.api.getLands()) {
            GringottsLands.debugMsg("Check Land " + land.getULID() + " balance consistency.");
            AccountHolder holder = this.landHolderProvider.getAccountHolder(land);
            GringottsAccount account = Gringotts.instance.getAccounting().getAccount(holder);
            double landBalance = land.getBalance();
            double balance = Configuration.CONF.getCurrency().getDisplayValue(account.getBalance());
            if (landBalance != balance) {
                GringottsLands.LOGGER.severe("Update Land " + land.getULID() + " balance to resolve inconsistency. (current: " + landBalance + " gringotts: " + balance + ")" );
                land.setBalance(balance);
            }
        }
    }

    /**
     * Vault created.
     *
     * @param event the event
     */
    @EventHandler
    public void vaultCreated(PlayerVaultCreationEvent event) {
        // some listener already claimed this event
        if (event.isValid() || !this.isEnabled()) {
            return;
        }

        String line2String = event.getCause().getLine(2);

        if (line2String == null) {
            return;
        }

        Player player = event.getCause().getPlayer();

        if (!event.getType().equals(LandsConfiguration.CONF.landSignTypeName)) {
            return;
            
        }

        if (!LandsPermissions.CREATE_VAULT_LAND.isAllowed(player)) {
            player.sendMessage(LandsLanguage.LANG.noLandVaultPerm);
            return;
        }

        Land land = this.api.getLandByName(line2String);
        if (land == null) {
            player.sendMessage(LandsLanguage.LANG.noLandFound);
            return;
        }

        AccountHolder owner = this.landHolderProvider.getAccountHolder(land);

        if (LandsConfiguration.CONF.vaultsOnlyInLands && this.api.getArea(event.getCause().getBlock().getLocation()) == null) {
            event.getCause().getPlayer().sendMessage(LandsLanguage.LANG.vaultNotInLand);
            return;
        }

        if (LandsConfiguration.CONF.maxLandVaults != -1 && ((int) Gringotts.instance.getDao().retrieveChests().stream().filter(c -> c.account.owner.getId().equals(land.getULID().toString())).count() + 1) > LandsConfiguration.CONF.maxLandVaults) {
            event.getCause().getPlayer().sendMessage(LandsLanguage.LANG.tooManyVaults);
            return;
        }

        event.setOwner(owner);
        event.setValid(true);
    }
}
