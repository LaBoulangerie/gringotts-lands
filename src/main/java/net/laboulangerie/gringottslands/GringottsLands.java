package net.laboulangerie.gringottslands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.api.dependency.Dependency;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class GringottsLands extends JavaPlugin {
    private static final String MESSAGES_YML = "messages.yml";

    @Override
    public void onLoad() {
        try {
            Plugin plugin = Gringotts.instance.getDependencies()
                    .hookPlugin("Lands", "me.angeschossen.lands.Lands", "7.9.5");

            if (plugin != null && Gringotts.instance.getDependencies()
                    .registerDependency(new LandsDependency(Gringotts.instance, plugin))) {
                getLogger().warning("Lands plugin is already assigned into the dependencies.");
            }
        } catch (IllegalArgumentException e) {
            getLogger().warning("Looks like Lands plugin is not compatible with Gringotts");
        }

        // load and init configuration
        saveDefaultConfig(); // saves default configuration if no config.yml exists yet
        reloadConfig();

        Gringotts.instance.getDependencies().getDependency("lands").ifPresent(Dependency::onLoad);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    /**
     * Reload config.
     * <p>
     * override to handle custom config logic and language loading
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        LandsConfiguration.CONF.readConfig(getConfig());
        LandsLanguage.LANG.readLanguage(getMessages());
    }

    /**
     * Get the configured player interaction messages.
     *
     * @return the configured player interaction messages
     */
    public FileConfiguration getMessages() {
        String langPath = String.format("i18n/messages_%s.yml", LandsConfiguration.CONF.language);

        // try configured language first
        InputStream langStream = getResource(langPath);
        FileConfiguration conf;

        if (langStream != null) {
            Reader langReader = new InputStreamReader(langStream, StandardCharsets.UTF_8);
            conf = YamlConfiguration.loadConfiguration(langReader);
        } else {
            // use custom/default
            File langFile = new File(getDataFolder(), MESSAGES_YML);
            conf = YamlConfiguration.loadConfiguration(langFile);
        }

        return conf;
    }
}
