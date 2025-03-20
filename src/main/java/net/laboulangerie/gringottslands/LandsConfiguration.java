package net.laboulangerie.gringottslands;

import org.bukkit.configuration.file.FileConfiguration;

public enum LandsConfiguration {
    CONF;

    /**
     * Language to be used for messages. Should be an ISO 639-1 (alpha-2) code.
     * If a language is not supported by Gringotts, use user-configured or default
     * (English) messages.
     */
    public String language = "custom";
    public String landSignTypeName = "land";

    public int maxLandVaults = -1;
    public int maxCapitalLandVaultsAdditional = 0;

    public boolean vaultsOnlyInLands = false;
    public long landStartBalance = 0;

    public boolean debug = false;

    public void readConfig(FileConfiguration savedConfig) {
        CONF.language = savedConfig.getString("language", "custom");
        CONF.landSignTypeName = savedConfig.getString("land_sign_type_name", "land");
        CONF.maxLandVaults = savedConfig.getInt("max_land_vaults", -1);
        CONF.maxCapitalLandVaultsAdditional = savedConfig.getInt("max_capital_land_vaults_additional", 0);
        CONF.vaultsOnlyInLands = savedConfig.getBoolean("vaults_only_in_lands", false);
        CONF.landStartBalance = savedConfig.getLong("land_start_balance", 0);
        CONF.debug = savedConfig.getBoolean("debug", false);
    }
}
