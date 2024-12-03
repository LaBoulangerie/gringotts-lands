package net.laboulangerie.gringottslands;

import org.bukkit.configuration.file.FileConfiguration;

public enum LandsConfiguration {
    CONF;

    /**
     * Language to be used for messages. Should be an ISO 639-1 (alpha-2) code.
     * If a language is not supported by Gringotts, use user-configured or default (English) messages.
     */
    public String language           = "custom";
    public String landSignTypeName   = "land";

    public boolean vaultsOnlyInLands  = false;
    public long    landStartBalance   = 0;

    public void readConfig(FileConfiguration savedConfig) {
        CONF.language           = savedConfig.getString("language", "custom");
        CONF.landSignTypeName   = savedConfig.getString("land_sign_type_name", "land");
        CONF.vaultsOnlyInLands  = savedConfig.getBoolean("vaults_only_in_lands", false);
        CONF.landStartBalance   = savedConfig.getLong("land_start_balance", 0);
    }
}
