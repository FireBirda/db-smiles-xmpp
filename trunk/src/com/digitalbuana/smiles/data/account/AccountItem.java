package com.digitalbuana.smiles.data.account;

import java.security.KeyPair;
import java.util.Date;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionState;
import com.digitalbuana.smiles.data.connection.ConnectionThread;

public class AccountItem extends ConnectionItem {

	public static final String UNDEFINED_PASSWORD = "com.digitalbuana.smiles.data.core.AccountItem.UNDEFINED_PASSWORD";
	private Long id;
	private String account = null;
	private boolean enabled;
	private boolean authFailed;
	private boolean invalidCertificate;
	private boolean passwordRequested;
	private int priority;
	private StatusMode statusMode;
	private String statusText;
	private KeyPair keyPair;
	private Date lastSync;
	private ArchiveMode archiveMode;

	public AccountItem(String userName, String password, String serverName,
			int priority, StatusMode statusMode, String statusText,
			boolean enabled, boolean saslEnabled, boolean compression,
			KeyPair keyPair, Date lastSync, ArchiveMode archiveMode) {
		super(userName, password, serverName, saslEnabled, compression);
		this.id = null;
		this.account = userName;
		this.enabled = enabled;
		this.priority = getValidPriority(priority);
		this.statusMode = statusMode;
		this.statusText = statusText;
		this.keyPair = keyPair;
		this.lastSync = lastSync;
		this.archiveMode = archiveMode;
		authFailed = false;
		invalidCertificate = false;
		passwordRequested = false;
	}

	Long getId() {
		return id;
	}

	void setId(long id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public Date getLastSync() {
		return lastSync;
	}

	void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public ArchiveMode getArchiveMode() {
		return archiveMode;
	}

	void setArchiveMode(ArchiveMode archiveMode) {
		this.archiveMode = archiveMode;
	}

	public int getPriority() {
		return priority;
	}

	void setPriority(int priority) {
		this.priority = getValidPriority(priority);
	}

	void setStatus(StatusMode statusMode, String statusText) {
		this.statusMode = statusMode;
		this.statusText = statusText;
	}

	StatusMode getRawStatusMode() {
		return statusMode;
	}

	public String getStatusText() {
		return statusText;
	}

	public StatusMode getDisplayStatusMode() {
		ConnectionState state = getState();
		if (state.isConnected())
			return statusMode;
		else if (state.isConnectable())
			return StatusMode.connection;
		else
			return StatusMode.unavailable;
	}

	public StatusMode getFactualStatusMode() {
		if (getState().isConnected())
			return statusMode;
		else
			return StatusMode.unavailable;
	}

	public Presence getPresence() throws NetworkException {
		StatusMode statusMode = getFactualStatusMode();
		if (statusMode == StatusMode.unsubscribed)
			throw new IllegalStateException();
		if (statusMode == StatusMode.unavailable)
			throw new NetworkException(R.string.NOT_CONNECTED);
		if (statusMode == StatusMode.invisible)
			return new Presence(Type.unavailable);
		else {
			int priority;
			if (statusMode != StatusMode.dnd) {
				statusMode = StatusMode.available;
			}
			if (SettingsManager.connectionAdjustPriority()) {
				if (statusMode == StatusMode.available)
					priority = SettingsManager.connectionPriorityAvailable();
				else if (statusMode == StatusMode.away)
					priority = SettingsManager.connectionPriorityAway();
				else if (statusMode == StatusMode.chat)
					priority = SettingsManager.connectionPriorityChat();
				else if (statusMode == StatusMode.dnd)
					priority = SettingsManager.connectionPriorityDnd();
				else if (statusMode == StatusMode.xa)
					priority = SettingsManager.connectionPriorityXa();
				else
					throw new IllegalStateException();
			} else
				priority = this.priority;
			return new Presence(Type.available, statusText, priority,
					statusMode.getMode());
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	void updateConnectionSettings(boolean custom, String host, int port,
			String password, boolean saslEnabled, boolean compression) {
		getConnectionSettings().update(password, saslEnabled, compression);
		passwordRequested = false;
		AccountManager.getInstance().removePasswordRequest(account);
	}

	@Override
	protected boolean isConnectionAvailable(boolean userRequest) {
		// Check password before go online.
		if (statusMode.isOnline()
				&& enabled
				&& !passwordRequested
				&& UNDEFINED_PASSWORD.equals(getConnectionSettings()
						.getPassword())) {
			passwordRequested = true;
			AccountManager.getInstance().addPasswordRequest(account);
		}
		if (userRequest) {
			authFailed = false;
			invalidCertificate = false;
		}
		return statusMode.isOnline() && enabled && !authFailed
				&& !invalidCertificate && !passwordRequested;
	}

	@Override
	protected void onPasswordChanged(String password) {
		super.onPasswordChanged(password);
		AccountManager.getInstance().requestToWriteAccount(this);
	}

	@Override
	protected void onSRVResolved(ConnectionThread connectionThread) {
		super.onSRVResolved(connectionThread);
		AccountManager.getInstance().onAccountChanged(account);
	}

	@Override
	protected void onInvalidCertificate() {
		super.onInvalidCertificate();
		invalidCertificate = true;
		updateConnection(false);
	}

	@Override
	protected void onConnected(ConnectionThread connectionThread) {
		super.onConnected(connectionThread);
		AccountManager.getInstance().onAccountChanged(account);
	}

	@Override
	protected void onAuthFailed() {
		super.onAuthFailed();
		// Login failed. We don`t want to reconnect.
		authFailed = true;
		updateConnection(false);
		AccountManager.getInstance().addAuthenticationError(account);
	}

	@Override
	protected void onAuthorized(ConnectionThread connectionThread) {
		super.onAuthorized(connectionThread);
		AccountManager.getInstance().onAccountChanged(account);
	}

	@Override
	protected void onClose(ConnectionThread connectionThread) {
		super.onClose(connectionThread);
		AccountManager.getInstance().onAccountChanged(account);
	}

	@Override
	public String toString() {
		return super.toString() + ":" + getAccount();
	}

	static private int getValidPriority(int priority) {
		return Math.min(128, Math.max(-128, priority));
	}

}
