package net.laboulangerie.gringottslands;

import java.util.ArrayList;
import java.util.List;
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
        List<GringottsAccount> playerAccounts = new ArrayList<>();
        Integer rank = 1;

        for(OfflinePlayer player : Bukkit.getOfflinePlayers()){ // Get all offline players accounts
            GringottsAccount account = Gringotts.instance.getAccounting().getAccount(taxHolderProvider.getAccountHolder(player));

            // Ignore players with no balance
            if(account.getBalance() >= 1) playerAccounts.add(account);
        }

        playerAccounts = playerAccounts.stream() // Sort the accounts to get the top 10
                .sorted((a, b) -> Long.compare(b.getBalance(), a.getBalance()))
                .limit(10)
                .collect(Collectors.toList());

        sender.sendMessage(LandsLanguage.LANG.baltopIntro);
        for(GringottsAccount playerAccount : playerAccounts){
            sender.sendMessage(LandsLanguage.LANG.baltopPlayer
                .replace("%rank", rank.toString())
                .replace("%player", playerAccount.owner.getName())
                .replace("%balance", "" + (playerAccount.getBalance())));
            rank++;
        }

        return true;
    }
}
