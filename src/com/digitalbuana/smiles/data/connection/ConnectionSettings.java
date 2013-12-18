package com.digitalbuana.smiles.data.connection;

public class ConnectionSettings {

	private final String userName;
	private String password;
	private boolean saslEnabled;
	private boolean compression;
	private final String serverName;

	public ConnectionSettings(String userName, String password,
			String serverName, boolean saslEnabled, boolean compression) {
		super();
		this.userName = userName;
		this.password = password;
		this.serverName = serverName;
		this.saslEnabled = saslEnabled;
		this.compression = compression;
	}

	public String getServerName() {
		return serverName;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public boolean isSaslEnabled() {
		return saslEnabled;
	}

	public boolean useCompression() {
		return compression;
	}

	public void update(String password, boolean saslEnabled, boolean compression) {
		this.password = password;
		this.saslEnabled = saslEnabled;
		this.compression = compression;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
