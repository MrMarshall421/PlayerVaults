package com.drtshock.playervaults.data;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.Base64Serialization;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class PlayerVaultsDB {

	private PlayerVaultsDB playerVaultsDB = this;

	public void createVault(Inventory inventory, String target, int number) {
		int size = VaultOperations.getMaxVaultSize(target);
		String serialized = Base64Serialization.toBase64(inventory, size);

		if(!vaultExists(target, number)) {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("INSERT INTO PlayerVaults (UUID, Inventory, VaultNumber) VALUES (?, ?, ?)");
				ps.setString(1, target);
				ps.setString(2, serialized);
				ps.setInt(3, number);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			//> save vault instead of creating
			saveVault(inventory, target, number);
		}
	}

	public void createVault(String inventory, String target, int number) {
		int size = VaultOperations.getMaxVaultSize(target);

		if(!vaultExists(target, number)) {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("INSERT INTO PlayerVaults (UUID, Inventory, VaultNumber) VALUES (?, ?, ?)");
				ps.setString(1, target);
				ps.setString(2, inventory);
				ps.setInt(3, number);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			//> save vault instead of creating
			saveVault(inventory, target, number);
		}
	}

	public void saveVault(Inventory inventory, String target, int number) {
		int size = VaultOperations.getMaxVaultSize(target);
		String serialized = Base64Serialization.toBase64(inventory, size);

		try {
			PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("UPDATE PlayerVaults SET Inventory = ? WHERE UUID = ? AND VaultNumber = ?");
			ps.setString(1, serialized);
			ps.setString(2, target);
			ps.setInt(3, number);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveVault(String inventory, String target, int number) {
		int size = VaultOperations.getMaxVaultSize(target);

		try {
			PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("UPDATE PlayerVaults SET Inventory = ? WHERE UUID = ? AND VaultNumber = ?");
			ps.setString(1, inventory);
			ps.setString(2, target);
			ps.setInt(3, number);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deletePlayerVaults(String target) {
		if(playerVaultsExist(target)) {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("DELETE FROM PlayerVaults WHERE UUID = ?");
				ps.setString(1, target);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteVault(String target, int number) {
		if(playerVaultsExist(target)) {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("DELETE FROM PlayerVaults WHERE UUID = ? AND VaultNumber = ?");
				ps.setString(1, target);
				ps.setInt(2, number);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean playerVaultsExist(String holder) {
		try {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("SELECT * FROM PlayerVaults WHERE UUID = ?");
				ps.setString(1, holder);
				ResultSet rs = ps.executeQuery();
				boolean b = rs.next();
				rs.close();
				return b;
			} catch(NullPointerException ex) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean vaultExists(String holder, int number) {
		boolean exists = false;

		try {
			try {
				PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("SELECT * FROM PlayerVaults WHERE UUID = '" + holder  + "' AND VaultNumber = ?");
				ps.setInt(1, number);
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					exists = true;
				}
				rs.close();
			} catch(NullPointerException ex) {
				ex.printStackTrace();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return exists;
	}

	public String getInventory(String holder, int number) {
		try {
			PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("SELECT * FROM PlayerVaults WHERE UUID = ? AND VaultNumber = ?");
			ps.setString(1, holder);
			ps.setInt(2, number);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				return rs.getString("Inventory");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Set<Integer> getVaultNumbers(String holder) {
		Set<Integer> vaultNumbers = null;

		try {
			PreparedStatement ps = PlayerVaults.getInstance().getMySQL().connection.prepareStatement("SELECT * FROM PlayerVaults WHERE UUID = ?");
			ps.setString(1, holder);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				vaultNumbers.add(rs.getInt("VaultNumber"));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return vaultNumbers;
	}

	public PlayerVaultsDB getPlayerVaultsDB() {
		return playerVaultsDB;
	}
}
