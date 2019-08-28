/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VaultManager {

    private static final String VAULTKEY = "vault%d";
    private static VaultManager instance;
    private final Map<String, YamlConfiguration> cachedVaultFiles = new ConcurrentHashMap<>();

    public VaultManager() {
        instance = this;
    }

    /**
     * Get the instance of this class.
     *
     * @return - instance of this class.
     */
    public static VaultManager getInstance() {
        return instance;
    }

    /**
     * Saves the inventory to the specified player and vault number.
     *
     * @param inventory The inventory to be saved.
     * @param target    The player of whose file to save to.
     * @param number    The vault number.
     */
    public void saveVault(Inventory inventory, String target, int number) {
        if(PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().vaultExists(target, number)) {
            PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().saveVault(inventory, target, number);
        } else {
            PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().createVault(inventory, target, number);
        }
    }

    /**
     * Load the player's vault and return it.
     *
     * @param player The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOwnVault(Player player, int number, int size) {
        if (size % 9 != 0) {
            size = 54;
        }

        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", player.getName());
        VaultViewInfo info = new VaultViewInfo(player.getUniqueId().toString(), number);
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            return PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        }

        Inventory inv;
        VaultHolder vaultHolder = new VaultHolder(number);
        if (!PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().vaultExists(player.getUniqueId().toString(), number)) {
            inv = Bukkit.createInventory(vaultHolder, size, title);
            vaultHolder.setInventory(inv);
        } else {
            Inventory i = getInventory(vaultHolder, player.getUniqueId().toString(), size, number, title);
            if (i == null) {
                return null;
            } else {
                inv = i;
            }
        }

        return inv;
    }

    /**
     * Load the player's vault and return it.
     *
     * @param name   The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOtherVault(String name, int number, int size) {
        if (size % 9 != 0) {
            size = 54;
        }

        String holder = name;

        try {
            UUID uuid = UUID.fromString(name);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            holder = offlinePlayer.getUniqueId().toString();
        } catch (Exception e) {
            // Not a player
        }

        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", holder);
        VaultViewInfo info = new VaultViewInfo(name, number);
        Inventory inv;
        VaultHolder vaultHolder = new VaultHolder(number);
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            Inventory i = getInventory(vaultHolder, holder, size, number, title);
            if (i == null) {
                return null;
            } else {
                inv = i;
            }
            PlayerVaults.getInstance().getOpenInventories().put(info.toString(), inv);
        }
        return inv;
    }

    /**
     * Get an inventory from file. Returns null if the inventory doesn't exist. SHOULD ONLY BE USED INTERNALLY
     *
     * @param uuid       the UUID of the player.
     * @param size       the size of the vault.
     * @param number     the vault number.
     * @return inventory if exists, otherwise null.
     */
    private Inventory getInventory(InventoryHolder owner, String uuid, int size, int number, String title) {
        Inventory inventory = Bukkit.createInventory(owner, size, title);

        String data = PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().getInventory(uuid, number);
        Inventory deserialized = Base64Serialization.fromBase64(data);
        if (deserialized == null) {
            return inventory;
        }

        // Check if deserialized has more used slots than the limit here.
        // Happens on change of permission or if people used the broken version.
        // In this case, players will lose items.
        if (deserialized.getContents().length > size) {
            for (ItemStack stack : deserialized.getContents()) {
                if (stack != null) {
                    inventory.addItem(stack);
                }
            }
        } else {
            inventory.setContents(deserialized.getContents());
        }

        return inventory;
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     * @return The inventory of the specified holder and vault number. Can be null.
     */
    public Inventory getVault(String holder, int number) {
        String serialized = PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().getInventory(holder, number);
        return Base64Serialization.fromBase64(serialized);
    }

    /**
     * Checks if a vault exists.
     *
     * @param holder holder of the vault.
     * @param number vault number.
     * @return true if the vault file and vault number exist in that file, otherwise false.
     */
    public boolean vaultExists(String holder, int number) {
        return PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().vaultExists(holder, number);
    }

    /**
     * Gets the numbers belonging to all their vaults.
     *
     * @param holder
     * @return a set of Integers, which are player's vaults' numbers (fuck grammar).
     */
    public Set<Integer> getVaultNumbers(String holder) {
        return PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().getVaultNumbers(holder);
    }

    public void deleteAllVaults(String holder) {
        PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().deletePlayerVaults(holder);
    }

    /**
     * Deletes a players vault.
     *
     * @param sender The sender of whom to send messages to.
     * @param holder The vault holder.
     * @param number The vault number.
     * @throws IOException Uh oh!
     */
    public void deleteVault(CommandSender sender, final String holder, final int number) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().vaultExists(holder, number)) {
                    return;
                }

                PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().deleteVault(holder, number);
            }
        }.runTaskAsynchronously(PlayerVaults.getInstance());

        OfflinePlayer player = Bukkit.getPlayer(holder);
        if (player != null) {
            if (sender.getName().equalsIgnoreCase(player.getName())) {
                sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replaceAll("%p", player.getName()));
            }
        }

        String vaultName = sender instanceof Player ? ((Player) sender).getUniqueId().toString() : holder;
        PlayerVaults.getInstance().getOpenInventories().remove(new VaultViewInfo(vaultName, number).toString());
    }

    /**
     * Attempt to delete a vault file.
     *
     * @param holder UUID of the holder.
     * @return true if successful, otherwise false.
     */
    public void deletePlayerVaultFile(String holder) {
        PlayerVaults.getInstance().getMySQL().getPlayerVaultsDB().deletePlayerVaults(holder);
    }
}
