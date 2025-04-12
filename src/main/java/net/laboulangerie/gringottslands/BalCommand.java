package net.laboulangerie.gringottslands;

import org.bukkit.Bukkit;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public class BalCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack arg0, String[] arg1) {
        if(arg1.length == 0) {
            arg0.getSender().sendMessage(LandsLanguage.LANG.balNoPlayerFound);
            return;
        }
        if(Bukkit.getOfflinePlayer(arg1[0].toString()) == null) {
            arg0.getSender().sendMessage(LandsLanguage.LANG.balNoPlayerFound);
            return;
        }


    }
}
