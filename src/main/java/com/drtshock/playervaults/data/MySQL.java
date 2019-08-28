package com.drtshock.playervaults.data;

import com.drtshock.playervaults.PlayerVaults;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

	private MySQL instance = this;

	private String host = "";
	private String port = "";
	private String database = "";
	private String username = "";
	private String password = "";

	public Connection connection;
	private PlayerVaultsDB playerVaultsDB = new PlayerVaultsDB();

	private boolean loadLoginData() {
		if(PlayerVaults.getInstance().getConfig().getString("mysql.username") != "") {
			host = PlayerVaults.getInstance().getConfig().getString("mysql.host");
			port = PlayerVaults.getInstance().getConfig().getString("mysql.port");
			database = PlayerVaults.getInstance().getConfig().getString("mysql.database");
			username = PlayerVaults.getInstance().getConfig().getString("mysql.username");
			password = PlayerVaults.getInstance().getConfig().getString("mysql.password");
			return true;
		}
		return false;
	}

	private boolean isConnected() {
		return connection != null;
	}

	private void createTables() {
		try {
			connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS PlayerVaults (UUID VARCHAR(100), Inventory VARCHAR(10000), VaultNumber INT(100))");
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void connect() {
		if(loadLoginData()) {
			if(!isConnected()) {
				try {
					connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
					createTables();
					System.out.println("[PlayerVaults] Connected to MySQL");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			System.out.println("[PlayerVaults] stopped due to no mysql connection");
		}
	}

	public void disconnect() {
		if(isConnected()) {
			try {
				connection.close();
				System.out.println("[PlayerVaults] MySQL Connection closed");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public MySQL getInstance() {
		return instance;
	}

	public PlayerVaultsDB getPlayerVaultsDB() {
		return playerVaultsDB;
	}
}