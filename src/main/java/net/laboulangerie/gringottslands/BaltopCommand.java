package net.laboulangerie.gringottslands;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;

import org.jetbrains.annotations.NotNull;

import me.angeschossen.lands.api.LandsIntegration;

import net.laboulangerie.gringottslands.tax.TaxHolderProvider;

public class BaltopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        TaxHolderProvider taxHolderProvider = new TaxHolderProvider(LandsIntegration.of(GringottsLands.instance));
        Set<GringottsAccount> playerAccounts = new HashSet();
        Integer rank = 1;

        for(OfflinePlayer player : Bukkit.getOfflinePlayers()){ // Get all offline players accounts
            playerAccounts.add(Gringotts.instance.getAccounting().getAccount(taxHolderProvider.getAccountHolder(player)));
        }

        playerAccounts = playerAccounts.stream() // Sort the accounts to get the top 10
                .sorted((a, b) -> Long.compare(b.getBalance(), a.getBalance()))
                .limit(10)
                .collect(Collectors.toSet());

        sender.sendMessage(LandsLanguage.LANG.baltopIntro);
        for(GringottsAccount playerAccount : playerAccounts){
            sender.sendMessage(LandsLanguage.LANG.baltopPlayer
                .replace("%rank", rank.toString())
                .replace("%player", playerAccount.owner.getName())
                .replace("%balance", "" + (playerAccount.getBalance() / 100)));
            rank++;
        }

        return true;
    }
}
