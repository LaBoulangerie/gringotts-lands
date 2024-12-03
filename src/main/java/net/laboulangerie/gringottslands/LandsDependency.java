package net.laboulangerie.gringottslands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.api.dependency.Dependency;
import org.gestern.gringotts.event.PlayerVaultCreationEvent;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import net.laboulangerie.gringottslands.land.LandAccountHolder;
import net.laboulangerie.gringottslands.land.LandHolderProvider;

public class LandsDependency implements Dependency, Listener {
    private final LandHolderProvider landHolderProvider;
    private final Gringotts gringotts;
    private final Plugin plugin;
    private final String id;
    private final LandsIntegration api;

    /**
     * Instantiates a new Lands dependency.
     *
     * @param gringotts the gringotts
     * @param plugin    the plugin
     */
    public LandsDependency(Gringotts gringotts, Plugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("'plugin' is null");
        }

        this.gringotts = gringotts;
        this.plugin = plugin;
        this.id = "lands";

        this.api = LandsIntegration.of(plugin);
        this.landHolderProvider = new LandHolderProvider(this.gringotts, this.api);
        
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
        return this.plugin;
    }

    /**
     * On enable.
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this.gringotts);
        Bukkit.getPluginManager().registerEvents(this.landHolderProvider, this.gringotts);

        Gringotts.instance.registerAccountHolderProvider(LandAccountHolder.ACCOUNT_TYPE, this.landHolderProvider);
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

        AccountHolder owner;

        if (event.getType().equals(LandsConfiguration.CONF.landSignTypeName)) {
            if (!LandsPermissions.CREATE_VAULT_LAND.isAllowed(player)) {
                player.sendMessage(LandsLanguage.LANG.noLandVaultPerm);

                return;
            }

            Land land = this.api.getLandByName(line2String);
            if (land == null) {
                player.sendMessage(LandsLanguage.LANG.noLandFound);
                return;
            }

            owner = this.landHolderProvider.getAccountHolder(land);
        } else {
            return;
        }

        if (owner == null) {
            return;
        }

        if (LandsConfiguration.CONF.vaultsOnlyInLands && this.api.getArea(event.getCause().getBlock().getLocation()) == null) {
            event.getCause().getPlayer().sendMessage(LandsLanguage.LANG.vaultNotInLand);
            return;
        }

        event.setOwner(owner);
        event.setValid(true);
    }
}
