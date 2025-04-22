package net.laboulangerie.gringottslands;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

import static org.gestern.gringotts.Util.translateColors;

public enum LandsLanguage {
        LANG;

        public String noLandVaultPerm;
        public String noLandFound;
        public String vaultNotInLand;
        public String tooManyVaults;
        public String balNoPlayerFound;
        public String balOfPlayer;
        public String perBalOfPlayer;
        public String taxBalOfPlayer;
        public String baltopIntro;
        public String baltopPlayer;

        public void readLanguage(FileConfiguration savedLanguage) {
                BiFunction<String, String, String> translator = (path, def) -> translateColors(savedLanguage.getString(path, def));

                LANG.noLandVaultPerm = translator.apply("noLandPerm", "You do not have permission to create land vaults here.");
                LANG.noLandFound = translator.apply("noLandFound", "Cannot create land vault: Land not found.");
                LANG.vaultNotInLand = translator.apply("vaultNotInLand", "You cannot create vaults outside of lands.");
                LANG.tooManyVaults = translator.apply("tooManyVaults", "You cannot create more vaults! Max: %max");
                LANG.balNoPlayerFound = translator.apply("balNoPlayerFound", "Player not found.");
                LANG.balOfPlayer = translator.apply("balOfPlayer", "Balances of %player :");
                LANG.perBalOfPlayer = translator.apply("perBalOfPlayer", " Personal account : %balance:pb:");
                LANG.taxBalOfPlayer = translator.apply("taxBalOfPlayer", " Tax account : %balance:pb:");
                LANG.baltopIntro = translator.apply("baltopIntro", "Balance leaderboard :");
                LANG.baltopPlayer = translator.apply("baltopPlayer", " n°%rank %player : %balance:pb:");
        }
}