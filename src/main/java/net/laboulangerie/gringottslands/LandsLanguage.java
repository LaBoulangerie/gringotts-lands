package net.laboulangerie.gringottslands;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

import static org.gestern.gringotts.Util.translateColors;

public enum LandsLanguage {
        LANG;

        public String noLandVaultPerm;
        public String noLandResident;
        public String noNationVaultPerm;
        public String notInNation;
        public String vaultNotInLand;

        public void readLanguage(FileConfiguration savedLanguage) {
                BiFunction<String, String, String> translator = (path,
                                def) -> translateColors(savedLanguage.getString(path, def));

                LANG.noLandVaultPerm = translator.apply("noLandPerm",
                                "You do not have permission to create land vaults here.");
                LANG.noLandResident = translator.apply("noLandResident",
                                "Cannot create land vault: You are not resident of a land.");
                LANG.noNationVaultPerm = translator.apply("NoNationVaultPerm",
                                "You do not have permission to create nation vaults here.");
                LANG.notInNation = translator.apply("notInNation",
                                "Cannot create nation vault: You do not belong to a nation.");
                LANG.vaultNotInLand = translator.apply("vaultNotInLand", "You cannot create vaults outside of lands.");

        }
}
