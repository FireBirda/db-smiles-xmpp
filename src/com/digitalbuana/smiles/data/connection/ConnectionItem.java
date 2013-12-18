package com.digitalbuana.smiles.data.connection;

import org.jivesoftware.smack.XMPPConnection;

import android.util.Log;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;

public abstract class ConnectionItem {

	private final ConnectionSettings connectionSettings;
	private ConnectionThread connectionThread;
	private boolean connectionRequest;
	private ConnectionState state;
	private boolean disconnectionRequested;
	private String TAG = getClass().getSimpleName();

	public ConnectionItem(
			String userName,
			String password,
			String serverName,
			boolean saslEnabled,
			boolean compression)
	{
		connectionSettings = new ConnectionSettings(
				userName,
				password,
				serverName,
				saslEnabled,
				compression);
		connectionRequest = false;
		disconnectionRequested = false;
		connectionThread = null;
		state = ConnectionState.offline;
	}

	public ConnectionThread getConnectionThread() {
		return connectionThread;
	}

	public ConnectionSettings getConnectionSettings() {
		return connectionSettings;
	}

	public ConnectionState getState() {
		return state;
	}

	public String getRealJid() {
		ConnectionThread connectionThread = getConnectionThread();
		if (connectionThread == null){
			return null;
		}
		XMPPConnection xmppConnection = connectionThread.getXMPPConnection();
		if (xmppConnection == null){
			return null;
		}
		String user = xmppConnection.getUser();
		if (user == null){
			return null;
		}
		return user;
	}

	protected boolean isConnectionAvailable(boolean userRequest) {
		return true;
	}

	public boolean updateConnection(boolean userRequest) {
		boolean available = isConnectionAvailable(userRequest);
		if (NetworkManager.getInstance().getState() != NetworkState.available
				|| !available || disconnectionRequested) {
			ConnectionState target = available ? ConnectionState.waiting
					: ConnectionState.offline;
			if (state == ConnectionState.connected
					|| state == ConnectionState.authentication
					|| state == ConnectionState.connecting) {
				if (userRequest)
					connectionRequest = false;
				if (connectionThread != null) {
					disconnect(connectionThread);
					onClose(connectionThread);
					connectionThread = null;
				}
			} else if (state == target) {
				return false;
			}
			state = target;
			return true;
		} else {
			if (state == ConnectionState.offline
					|| state == ConnectionState.waiting) {
				if (userRequest)
					connectionRequest = true;
				state = ConnectionState.connecting;
				connectionThread = new ConnectionThread(this);
				connectionThread.start(AppConstants.XMPPServerHost,5222, true);
				return true;
			} else {
				return false;
			}
		}
	}

	public void forceReconnect() {
		if (!getState().isConnectable())
			return;
		disconnectionRequested = true;
		boolean request = connectionRequest;
		connectionRequest = false;
		updateConnection(false);
		connectionRequest = request;
		disconnectionRequested = false;
		updateConnection(false);
	}

	protected void disconnect(final ConnectionThread connectionThread) {
		Thread thread = new Thread("Disconnection thread for " + this) {
			@Override
			public void run() {
				XMPPConnection xmppConnection = connectionThread.getXMPPConnection();
				if (xmppConnection != null)
					try {
						xmppConnection.disconnect();
					} catch (RuntimeException e) {
						// connectionClose() in smack can fail.
					}
			};
		};
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	boolean isManaged(ConnectionThread connectionThread) {
		return connectionThread == this.connectionThread;
	}

	protected void onPasswordChanged(String password) {
		connectionSettings.setPassword(password);
	}

	protected void onSRVResolved(ConnectionThread connectionThread) {
	}

	protected void onInvalidCertificate() {
		Log.d(TAG, "onInvalidCertificate");
	}

	protected void onConnected(ConnectionThread connectionThread) {
		if (isManaged(connectionThread))
			state = ConnectionState.authentication;
	}

	protected void onAuthFailed() {
		
	}

	protected void onAuthorized(ConnectionThread connectionThread) {
		if (isManaged(connectionThread))
			state = ConnectionState.connected;
	}

	private boolean onDisconnect(ConnectionThread connectionThread) {
		XMPPConnection xmppConnection = connectionThread.getXMPPConnection();
		boolean acceptable = isManaged(connectionThread);
		if (xmppConnection == null)
			LogManager.i(this, "onClose " + acceptable);
		else
			LogManager.i(this, "onClose " + xmppConnection.hashCode() + " - "+ xmppConnection.connectionCounterValue + ", "+ acceptable);
		ConnectionManager.getInstance().onDisconnect(connectionThread);
		if (acceptable)
			connectionThread.shutdown();
		return acceptable;
	}

	protected void onClose(ConnectionThread connectionThread) {
		if (onDisconnect(connectionThread)) {
			state = ConnectionState.waiting;
			this.connectionThread = null;
			if (connectionRequest)
				Application.getInstance().onError(R.string.CONNECTION_FAILED);
			connectionRequest = false;
		}
	}

	protected void onSeeOtherHost(
			ConnectionThread connectionThread,
			String fqdn, int port, boolean useSrvLookup) {
		if (onDisconnect(connectionThread)) {
			state = ConnectionState.connecting;
			this.connectionThread = new ConnectionThread(this);
			this.connectionThread.start(fqdn, port, useSrvLookup);
		}
	}

}
