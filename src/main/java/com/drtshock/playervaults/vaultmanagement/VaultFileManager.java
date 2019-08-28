package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

public class VaultFileManager {

	private static final String VAULTKEY = "vault%d";

	public void migrateToMySQL() {
		File base64Directory = new File("plugins/PlayerVaults/base64vaults");
		File[] userFiles = base64Directory.listFiles();

		if(userFiles.length > 0) {
			for(File userFile : userFiles) {
				String uuid = userFile.getName().replaceAll(".yml", "");

				for(int vaultNumber : getVaultNumbers(userFile)) {
					YamlConfiguration userFileCfg = YamlConfiguration.loadConfiguration(userFile);

					PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().createVault(userFileCfg.getString(String.format(VAULTKEY, vaultNumber)), uuid, vaultNumber);
				}

				userFile.delete();
			}
		}

		System.out.println("[PlayerVaults] Data successfully migrated to MySQL");
	}

	public Set<Integer> getVaultNumbers(File userFile) {
		Set<Integer> vaults = new HashSet<>();
		YamlConfiguration playerVaultFileCfg = YamlConfiguration.loadConfiguration(userFile);
		if(playerVaultFileCfg == null) {
			return vaults;
		}

		for(String s : playerVaultFileCfg.getKeys(false)) {
			try {
				int number = Integer.valueOf(s.substring(4));
				vaults.add(number);
			} catch(NumberFormatException ex) {
			}
		}

		return vaults;
	}
}
