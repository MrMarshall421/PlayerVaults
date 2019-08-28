package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultFileManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MigrateCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (VaultOperations.isLocked()) {
			sender.sendMessage(Lang.TITLE + Lang.LOCKED.toString());
			return true;
		}
		switch (args.length) {
			case 0:
				if (sender instanceof Player) {
					Player p = (Player) sender;

					if(p.hasPermission("playervaults.migrate")) {
						PlayerVaults.getInstance().getVaultFileManager().migrateToMySQL();
						p.sendMessage(Lang.TITLE + "§aData-Migration started.. please wait!");
					} else {
						sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
					}
				} else {
					sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
				}
				break;
			default:
				sender.sendMessage(Lang.TITLE + "/" + label);
		}

		return true;
	}
}
