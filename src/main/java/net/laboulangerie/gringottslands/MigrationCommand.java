package net.laboulangerie.gringottslands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.angeschossen.lands.api.land.Land;

import java.util.Collection;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.gestern.gringotts.AccountChest;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.api.Account;
import org.jspecify.annotations.NullMarked;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;

@NullMarked
public class MigrationCommand implements BasicCommand {

    private final TownyUniverse towny;
    private final LandsDependency lands;

    public MigrationCommand(TownyUniverse towny, LandsDependency lands) {
        this.towny = towny;
        this.lands = lands;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (sender instanceof Player) {
            return;
        }

        sender.sendMessage("Begin vault migration...");

        for (Land land : this.lands.getLandsApi().getLands()) {
            sender.sendMessage(land.getName() + " Is imported from Towny ?");
            Town town = towny.getTown(land.getName());
            if (town == null) {
                sender.sendMessage(land.getName() + " not found in Towny.");
                continue;
            }
            sender.sendMessage(land.getName() + " found in Towny.");
            Account account = Gringotts.instance.getEco().account(town.getUUID().toString());
            sender.sendMessage(land.getName() + " create new gringotts account.");
            AccountHolder owner = this.lands.getLandHolderProvider().getAccountHolder(land);
            GringottsAccount landAccount = new GringottsAccount(owner);
            Gringotts.instance.getDao().storeAccount(landAccount);
            Collection<AccountChest> chests = account.getVaultChests();
            sender.sendMessage(land.getName() + " moving " + chests.size() + " chest(s) vault to new account.");
            for (AccountChest chest : chests) {
                sender.sendMessage(land.getName() + " moving " + chest.chestLocation());
                Gringotts.instance.getDao().deleteAccountChest(chest);
                AccountChest accountChest = new AccountChest(chest.sign, landAccount, chest.balance(true));
                Gringotts.instance.getDao().storeAccountChest(accountChest);
                sender.sendMessage(land.getName() + " updating sign " + accountChest.chestLocation());
                accountChest.sign.setLine(0, "§l[" + LandsConfiguration.CONF.landSignTypeName + " vault]");
                accountChest.updateSign();
            }

            if (town.isCapital()) {
                try {
                    Account nationAccount = Gringotts.instance.getEco().account(town.getNation().getUUID().toString());
                    Collection<AccountChest> nationChests = nationAccount.getVaultChests();
                    sender.sendMessage(land.getName() + " moving " + nationChests.size() + " nation chest(s) vault to capital account.");
                    for (AccountChest chest : nationChests) {
                        sender.sendMessage(land.getName() + " moving " + chest.chestLocation());
                        Gringotts.instance.getDao().deleteAccountChest(chest);
                        AccountChest accountChest = new AccountChest(chest.sign, landAccount, chest.balance(true));
                        Gringotts.instance.getDao().storeAccountChest(accountChest);
                        sender.sendMessage(land.getName() + " updating sign " + accountChest.chestLocation());
                        accountChest.sign.setLine(0, "§l[" + LandsConfiguration.CONF.landSignTypeName + " vault]");
                        accountChest.updateSign();
                    }
                } catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            }

            sender.sendMessage("-------------");
        }
        sender.sendMessage("Migration done, check consistency...");

        this.lands.checkLandBalanceConsistency();

    }

    @Override
    public boolean canUse(CommandSender sender) {
        return !(sender instanceof Player);
    }
}
