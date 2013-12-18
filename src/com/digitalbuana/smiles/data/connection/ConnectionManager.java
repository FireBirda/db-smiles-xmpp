package com.digitalbuana.smiles.data.connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.OnCloseListener;
import com.digitalbuana.smiles.data.OnInitializedListener;
import com.digitalbuana.smiles.data.OnTimerListener;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.entity.NestedMap;
import com.digitalbuana.smiles.xmpp.address.Jid;

public class ConnectionManager implements OnInitializedListener,
		OnCloseListener, OnTimerListener {

	public final static int PACKET_REPLY_TIMEOUT = 5000;
	public final static String TRUST_STORE_PATH;

	private final Collection<ConnectionThread> managedConnections;

	private final NestedMap<RequestHolder> requests;

	private final static ConnectionManager instance;

	static {
		instance = new ConnectionManager();
		Application.getInstance().addManager(instance);

		SmackConfiguration.setPacketReplyTimeout(PACKET_REPLY_TIMEOUT);

		ServiceDiscoveryManager.setIdentityType("handheld");
		ServiceDiscoveryManager.setIdentityName(Application.getInstance()
				.getString(R.string.client_name));
		String path = System.getProperty("javax.net.ssl.trustStore");
		if (path == null)
			TRUST_STORE_PATH = System.getProperty("java.home") + File.separator
					+ "etc" + File.separator + "security" + File.separator
					+ "cacerts.bks";
		else
			TRUST_STORE_PATH = path;

		Connection
				.addConnectionCreationListener(new ConnectionCreationListener() {
					@Override
					public void connectionCreated(final Connection connection) {
						ServiceDiscoveryManager.getInstanceFor(connection)
								.addFeature("sslc2s");
					}
				});

	}

	public static ConnectionManager getInstance() {
		return instance;
	}

	private ConnectionManager() {
		managedConnections = new ArrayList<ConnectionThread>();
		requests = new NestedMap<RequestHolder>();
	}

	@Override
	public void onInitialized() {
		updateConnections(false);
		AccountManager.getInstance().onAccountsChanged(
				new ArrayList<String>(AccountManager.getInstance()
						.getAllAccounts()));
	}

	@Override
	public void onClose() {
		ArrayList<ConnectionThread> connections = new ArrayList<ConnectionThread>(
				managedConnections);
		managedConnections.clear();
		for (ConnectionThread connectionThread : connections)
			connectionThread.getConnectionItem().disconnect(connectionThread);
	}

	public void updateConnections(boolean userRequest) {
		AccountManager accountManager = AccountManager.getInstance();
		for (String account : accountManager.getAccounts()) {
			if (accountManager.getAccount(account)
					.updateConnection(userRequest))
				AccountManager.getInstance().onAccountChanged(account);
		}

	}

	public void forceReconnect() {
		AccountManager accountManager = AccountManager.getInstance();
		for (String account : accountManager.getAccounts()) {
			accountManager.getAccount(account).forceReconnect();
			AccountManager.getInstance().onAccountChanged(account);
		}
	}

	public void sendPacket(String account, Packet packet)
			throws NetworkException {
		ConnectionThread connectionThread = null;
		for (ConnectionThread check : managedConnections)
			if (check.getConnectionItem() instanceof AccountItem
					&& ((AccountItem) check.getConnectionItem()).getAccount()
							.equals(account)) {
				connectionThread = check;
				break;
			}
		if (connectionThread == null
				|| !connectionThread.getConnectionItem().getState()
						.isConnected()) {
			throw new NetworkException(R.string.NOT_CONNECTED);
		}

		XMPPConnection xmppConnection = connectionThread.getXMPPConnection();
		try {
			xmppConnection.sendPacket(packet);
		} catch (IllegalStateException e) {
			throw new NetworkException(R.string.XMPP_EXCEPTION);
		}
	}

	public void sendRequest(String account, IQ iq, OnResponseListener listener)
			throws NetworkException {
		String packetId = iq.getPacketID();
		RequestHolder holder = new RequestHolder(listener);
		sendPacket(account, iq);
		requests.put(account, packetId, holder);
	}

	public void onConnection(ConnectionThread connectionThread) {
		managedConnections.add(connectionThread);
		for (OnConnectionListener listener : Application.getInstance()
				.getManagers(OnConnectionListener.class))
			listener.onConnection(connectionThread.getConnectionItem());
	}

	public void onConnected(ConnectionThread connectionThread) {
		if (!managedConnections.contains(connectionThread))
			return;
		for (OnConnectedListener listener : Application.getInstance()
				.getManagers(OnConnectedListener.class))
			listener.onConnected(connectionThread.getConnectionItem());
	}

	public void onAuthorized(ConnectionThread connectionThread) {
		if (!managedConnections.contains(connectionThread))
			return;
		for (OnAuthorizedListener listener : Application.getInstance()
				.getManagers(OnAuthorizedListener.class))
			listener.onAuthorized(connectionThread.getConnectionItem());
	}

	public void onDisconnect(ConnectionThread connectionThread) {
		if (!managedConnections.remove(connectionThread))
			return;
		ConnectionItem connectionItem = connectionThread.getConnectionItem();
		if (connectionItem instanceof AccountItem) {
			String account = ((AccountItem) connectionItem).getAccount();
			for (Entry<String, RequestHolder> entry : requests.getNested(
					account).entrySet())
				entry.getValue().getListener()
						.onDisconnect(account, entry.getKey());
			requests.clear(account);
		}
		for (OnDisconnectListener listener : Application.getInstance()
				.getManagers(OnDisconnectListener.class))
			listener.onDisconnect(connectionThread.getConnectionItem());
	}

	public void processPacket(ConnectionThread connectionThread, Packet packet) {
		if (!managedConnections.contains(connectionThread))
			return;
		ConnectionItem connectionItem = connectionThread.getConnectionItem();
		if (packet instanceof IQ && connectionItem instanceof AccountItem) {
			IQ iq = (IQ) packet;
			String packetId = iq.getPacketID();
			if (packetId != null
					&& (iq.getType() == Type.RESULT || iq.getType() == Type.ERROR)) {
				String account = ((AccountItem) connectionItem).getAccount();
				RequestHolder requestHolder = requests
						.remove(account, packetId);
				if (requestHolder != null) {
					if (iq.getType() == Type.RESULT)
						requestHolder.getListener().onReceived(account,
								packetId, iq);
					else
						requestHolder.getListener().onError(account, packetId,
								iq);
				}
			}
		}

		for (OnPacketListener listener : Application.getInstance().getManagers(
				OnPacketListener.class))
			listener.onPacket(connectionItem,
					Jid.getBareAddress(packet.getFrom()), packet);
	}

	@Override
	public void onTimer() {
		if (NetworkManager.getInstance().getState() != NetworkState.suspended) {
			Collection<ConnectionItem> reconnect = new ArrayList<ConnectionItem>();
			for (ConnectionThread connectionThread : managedConnections)
				if (connectionThread.getConnectionItem().getState()
						.isConnected()
						&& !connectionThread.getXMPPConnection().isAlive()) {
					reconnect.add(connectionThread.getConnectionItem());
				}
			for (ConnectionItem connection : reconnect)
				connection.forceReconnect();
		}
		long now = new Date().getTime();
		Iterator<NestedMap.Entry<RequestHolder>> iterator = requests.iterator();
		while (iterator.hasNext()) {
			NestedMap.Entry<RequestHolder> entry = iterator.next();
			if (entry.getValue().isExpired(now)) {
				entry.getValue().getListener()
						.onTimeout(entry.getFirst(), entry.getSecond());
				iterator.remove();
			}
		}
	}

	public void checkOnTimer() {
		// mencoba untuk handle beratnya saat ingin meminta proses rekonek
		if (NetworkManager.getInstance().getState() != NetworkState.suspended) {
			Collection<ConnectionItem> reconnect = new ArrayList<ConnectionItem>();
			for (ConnectionThread connectionThread : managedConnections)
				if (connectionThread.getConnectionItem().getState()
						.isConnected()
						&& !connectionThread.getXMPPConnection().isAlive()) {
					reconnect.add(connectionThread.getConnectionItem());
				}
			for (ConnectionItem connection : reconnect) {
				if (!connection.getState().isConnected()) {
					connection.forceReconnect();
				}
			}
		}

		long now = new Date().getTime();
		Iterator<NestedMap.Entry<RequestHolder>> iterator = requests.iterator();
		while (iterator.hasNext()) {
			NestedMap.Entry<RequestHolder> entry = iterator.next();
			if (entry.getValue().isExpired(now)) {
				entry.getValue().getListener()
						.onTimeout(entry.getFirst(), entry.getSecond());
				iterator.remove();
			}
		}
	}
}
