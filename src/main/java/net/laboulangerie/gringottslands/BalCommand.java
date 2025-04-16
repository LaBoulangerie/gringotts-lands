package net.laboulangerie.gringottslands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.accountholder.PlayerAccountHolder;

import org.jetbrains.annotations.NotNull;

import me.angeschossen.lands.api.LandsIntegration;

import net.laboulangerie.gringottslands.tax.TaxHolderProvider;

public class BalCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        TaxHolderProvider taxHolderProvider = new TaxHolderProvider(LandsIntegration.of(GringottsLands.instance));
        
        if(args.length == 0) {
            sender.sendMessage(LandsLanguage.LANG.balOfPlayer.replace("%player", sender.getName()));
            sender.sendMessage(LandsLanguage.LANG.perBalOfPlayer.replace("%balance", "" + (Gringotts.instance.getAccounting().getAccount(new PlayerAccountHolder(Bukkit.getOfflinePlayer(sender.getName()))).getBalance())));
            sender.sendMessage(LandsLanguage.LANG.taxBalOfPlayer.replace("%balance", "" + (Gringotts.instance.getAccounting().getAccount(taxHolderProvider.getAccountHolder(Bukkit.getOfflinePlayer(sender.getName()))).getBalance())));
        } else if(Bukkit.getOfflinePlayer(args[0].toString()) == null) {
            sender.sendMessage(LandsLanguage.LANG.balNoPlayerFound);
            return false;
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0].toString());

            sender.sendMessage(LandsLanguage.LANG.balOfPlayer.replace("%player", player.getName()));
            sender.sendMessage(LandsLanguage.LANG.perBalOfPlayer.replace("%balance", "" + (Gringotts.instance.getAccounting().getAccount(new PlayerAccountHolder(player)).getBalance())));
            sender.sendMessage(LandsLanguage.LANG.taxBalOfPlayer.replace("%balance", "" + (Gringotts.instance.getAccounting().getAccount(taxHolderProvider.getAccountHolder(player)).getBalance())));
        }
        sender.sendMessage("test");

        return true;
    }
}
