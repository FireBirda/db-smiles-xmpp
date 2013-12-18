package com.digitalbuana.smiles.data.extension.ping;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.Ping;

import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.connection.OnPacketListener;

public class PingManager implements OnPacketListener {

	private final static PingManager instance;

	static {
		instance = new PingManager();
		Application.getInstance().addManager(instance);
		Connection.addConnectionCreationListener(new ConnectionCreationListener() {
			@Override
			public void connectionCreated(final Connection connection) {
				ServiceDiscoveryManager.getInstanceFor(connection).addFeature("urn:xmpp:ping");
			}
		});
	}

	public static PingManager getInstance() {
		return instance;
	}

	private PingManager() {
	}

	@Override
	public void onPacket(ConnectionItem connection, final String bareAddress,Packet packet)
	{
		if (!(connection instanceof AccountItem))
			return;
		final String account = ((AccountItem) connection).getAccount();
		if (!(packet instanceof Ping))
			return;
		final Ping ping = (Ping) packet;
		if (ping.getType() != IQ.Type.GET)
			return;
		try {
			ConnectionManager.getInstance().sendPacket(account,IQ.createResultIQ(ping));
		} catch (NetworkException e) {
			LogManager.exception(this, e);
		}
	}

}
