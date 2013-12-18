/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.data.connection;

import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.StreamError;
import org.xbill.DNS.Record;

import android.util.Log;
import android.widget.Toast;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;

public class ConnectionThread implements
		org.jivesoftware.smack.ConnectionListener,
		org.jivesoftware.smack.PacketListener {

	private static Pattern ADDRESS_AND_PORT = Pattern.compile("^(.*):(\\d+)$");
	private final AcceptAll ACCEPT_ALL = new AcceptAll();
	private final ConnectionItem connectionItem;
	private XMPPConnection xmppConnection;
	private final ExecutorService executorService;
	private final String login;
	private final String token;
	private final boolean saslEnabled;
	private final boolean compression;
	private boolean started;

	private static String TAG = "ConnectionThread";

	public ConnectionThread(final ConnectionItem connectionItem) {
		this.connectionItem = connectionItem;
		executorService = Executors
				.newSingleThreadExecutor(new ThreadFactory() {
					@Override
					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(
								runnable,
								"Connection thread for "
										+ (connectionItem instanceof AccountItem ? ((AccountItem) connectionItem)
												.getAccount() : connectionItem));
						thread.setPriority(Thread.MIN_PRIORITY);
						thread.setDaemon(true);
						return thread;
					}
				});
		ConnectionManager.getInstance().onConnection(this);
		ConnectionSettings connectionSettings = connectionItem
				.getConnectionSettings();
		token = connectionSettings.getPassword();
		saslEnabled = connectionSettings.isSaslEnabled();
		compression = false;
		login = connectionSettings.getUserName();
		started = false;
	}

	public XMPPConnection getXMPPConnection() {
		return xmppConnection;
	}

	public ConnectionItem getConnectionItem() {
		return connectionItem;
	}

	private void srvResolve(final String fqdn, final String defaultHost,
			final int defaultPort) {
		final Record[] records = DNSManager.getInstance().fetchRecords(fqdn);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onSRVResolved(fqdn, defaultHost, defaultPort, records);
			}
		});
	}

	private void onSRVResolved(final String fqdn, final String defaultHost,
			final int defaultPort, Record[] records) {
		DNSManager.getInstance().onRecordsReceived(fqdn, records);
		final Target target = DNSManager.getInstance().getCurrentTarget(fqdn);
		if (target == null) {
			runOnConnectionThread(new Runnable() {
				@Override
				public void run() {
					addressResolve(null, defaultHost, defaultPort, true);
				}
			});
		} else {
			runOnConnectionThread(new Runnable() {
				@Override
				public void run() {
					addressResolve(fqdn, target.getHost(), target.getPort(),
							true);
				}
			});
		}
	}

	private void addressResolve(final String fqdn, final String host,
			final int port, final boolean firstRequest) {
		final InetAddress[] addresses = DNSManager.getInstance()
				.fetchAddresses(host);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onAddressResolved(fqdn, host, port, firstRequest, addresses);
			}
		});
	}

	private void onAddressResolved(final String fqdn, final String host,
			final int port, boolean firstRequest, final InetAddress[] addresses) {
		DNSManager.getInstance().onAddressesReceived(host, addresses);
		InetAddress address = DNSManager.getInstance().getNextAddress(host);
		if (address != null) {
			onReady(address, port);
			return;
		}
		if (fqdn == null) {
			if (firstRequest) {
				onAddressResolved(null, host, port, false, addresses);
				return;
			}
		} else {
			DNSManager.getInstance().getNextTarget(fqdn);
			if (DNSManager.getInstance().getCurrentTarget(fqdn) == null
					&& firstRequest)
				DNSManager.getInstance().getNextTarget(fqdn);
			final Target target = DNSManager.getInstance().getCurrentTarget(
					fqdn);
			if (target != null) {
				runOnConnectionThread(new Runnable() {
					@Override
					public void run() {
						addressResolve(fqdn, target.getHost(),
								target.getPort(), false);
					}
				});
				return;
			}
		}
		XMPPException exception = new XMPPException(
				"There is no available address.");
		LogManager.exception(this, exception);
		connectionClosedOnError(exception);
	}

	private void onReady(final InetAddress address, final int port) {

		LogManager.i(this, "Use " + address);

		// initProvider();

		ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(
				address.getHostAddress(), port, AppConstants.XMPPServerHost);
		if (Application.SDK_INT >= 14) {
			connectionConfiguration.setTruststoreType("AndroidCAStore");
			connectionConfiguration.setTruststorePassword(null);
			connectionConfiguration.setTruststorePath(null);
		} else {
			connectionConfiguration.setTruststoreType("BKS");
			connectionConfiguration
					.setTruststorePath(ConnectionManager.TRUST_STORE_PATH);
		}
		// Disable smack`s reconnection.
		connectionConfiguration.setReconnectionAllowed(false);
		// We will send custom presence.
		connectionConfiguration.setSendPresence(true);
		// We use own roster management.
		connectionConfiguration.setRosterLoadedAtLogin(true);

		if (SettingsManager.securityCheckCertificate()) {
			connectionConfiguration.setExpiredCertificatesCheckEnabled(true);
			connectionConfiguration.setNotMatchingDomainCheckEnabled(true);
			connectionConfiguration.setSelfSignedCertificateEnabled(false);
			connectionConfiguration.setVerifyChainEnabled(true);
			connectionConfiguration.setVerifyRootCAEnabled(true);
			connectionConfiguration.setCertificateListener(CertificateManager
					.getInstance().createCertificateListener(connectionItem));
		} else {
			connectionConfiguration.setExpiredCertificatesCheckEnabled(false);
			connectionConfiguration.setNotMatchingDomainCheckEnabled(false);
			connectionConfiguration.setSelfSignedCertificateEnabled(true);
			connectionConfiguration.setVerifyChainEnabled(false);
			connectionConfiguration.setVerifyRootCAEnabled(false);
		}

		connectionConfiguration.setSASLAuthenticationEnabled(saslEnabled);
		connectionConfiguration.setSecurityMode(SecurityMode.enabled);
		connectionConfiguration.setCompressionEnabled(compression);

		xmppConnection = new XMPPConnection(connectionConfiguration);
		xmppConnection.addPacketListener(this, ACCEPT_ALL);
		xmppConnection.forceAddConnectionListener(this);
		connectionItem.onSRVResolved(this);
		runOnConnectionThread(new Runnable() {
			@Override
			public void run() {
				connect(token);
			}
		});
	}

	private void seeOtherHost(String target) {
		Matcher matcher = ADDRESS_AND_PORT.matcher(target);
		int defaultPort = 5222;
		if (matcher.matches()) {
			String value = matcher.group(2);
			try {
				defaultPort = Integer.valueOf(value);
				target = matcher.group(1);
			} catch (NumberFormatException e2) {
			}
		}
		final String fqdn = target;
		final int port = defaultPort;
		// TODO: Check for the same address.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectionItem.onSeeOtherHost(ConnectionThread.this, fqdn,
						port, true);
			}
		});
	}

	private void connect(final String password) {
		try {
			xmppConnection.connect();
			Log.i(TAG, " :: >> :: Connecting to Server :: << :: ");
		} catch (XMPPException e) {
			checkForCertificateError(e);
			if (!checkForSeeOtherHost(e)) {
				throw new RuntimeException(e);
			}
		} catch (IllegalStateException e) {
			throw new RuntimeException(e);
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onConnected(password);
			}
		});
	}

	private void checkForCertificateError(Exception e) {
		if (!(e instanceof XMPPException))
			return;
		Throwable e1 = ((XMPPException) e).getWrappedThrowable();
		if (!(e1 instanceof SSLException))
			return;
		Throwable e2 = ((SSLException) e1).getCause();
		if (!(e2 instanceof CertificateException))
			return;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectionItem.onInvalidCertificate();
			}
		});
	}

	private boolean checkForSeeOtherHost(Exception e) {
		if (!(e instanceof XMPPException))
			return false;
		StreamError streamError = ((XMPPException) e).getStreamError();
		if (streamError == null
				|| streamError.getType() != StreamError.Type.seeOtherHost)
			return false;
		String target = streamError.getBody();
		if (target == null || "".equals(target))
			return false;
		LogManager.i(this, "See other host: " + target);
		seeOtherHost(target);
		return true;
	}

	private void onConnected(final String password) {
		// FileTransferKuManager.getInstance().setFileTransferManager(xmppConnection);
		connectionItem.onConnected(this);
		ConnectionManager.getInstance().onConnected(this);
		runOnConnectionThread(new Runnable() {
			@Override
			public void run() {
				authorization(password);
			}
		});
	}

	private void authorization(String password) {
		try {
			xmppConnection
					.login(login, password, AppConstants.XMPPConfResource);
		} catch (IllegalStateException e) {
			// onClose must be called from reader thread.
			Log.e(TAG, "IllegalStateException.." + e.getMessage());
			return;
		} catch (XMPPException e) {
			Log.e(TAG, "XMPPException.." + e.getMessage());
			LogManager.exception(connectionItem, e);
			// SASLAuthentication#authenticate(String,String,String)
			boolean SASLfailed = e.getMessage() != null
					&& e.getMessage().startsWith("SASL authentication ")
					&& !e.getMessage().endsWith("temporary-auth-failure");
			// NonSASLAuthentication#authenticate(String,String,String)
			// Authentication request failed (doesn't supported),
			// error was returned after negotiation or
			// authentication failed.
			boolean NonSASLfailed = e.getXMPPError() != null
					&& "Authentication failed.".equals(e.getMessage());
			if (SASLfailed || NonSASLfailed) {
				// connectionClosed can be called before from reader thread,
				// so don't check whether connection is managed.
				Application.getInstance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Login failed. We don`t want to reconnect.
						connectionItem.onAuthFailed();
						// Added by DHei
						AccountManager.getInstance().removeAccount(login);
						Toast.makeText(
								Application.getInstance()
										.getApplicationContext(),
								"Failed Login with user : " + login,
								Toast.LENGTH_SHORT).show();
					}
				});
				connectionClosed();
			} else
				connectionClosedOnError(e);
			// Server will destroy connection, but we can speedup
			// it.
			xmppConnection.disconnect();
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onAuthorized();
			}
		});
	}

	/**
	 * Authorization passed.
	 */
	private void onAuthorized() {
		connectionItem.onAuthorized(this);
		ConnectionManager.getInstance().onAuthorized(this);
		if (connectionItem instanceof AccountItem)
			AccountManager.getInstance().removeAuthorizationError(
					((AccountItem) connectionItem).getAccount());
		shutdown();
	}

	@Override
	public void connectionClosed() {
		// Can be called on error, e.g. XMPPConnection#initConnection().
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectionItem.onClose(ConnectionThread.this);
				Log.e(TAG, ">> [Smiles] connection closed...");
			}
		});
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		checkForCertificateError(e);
		if (checkForSeeOtherHost(e))
			return;
		connectionClosed();
	}

	@Override
	public void reconnectingIn(int seconds) {
	}

	@Override
	public void reconnectionSuccessful() {
	}

	@Override
	public void reconnectionFailed(Exception e) {
	}

	@Override
	public void processPacket(final Packet packet) {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ConnectionManager.getInstance().processPacket(
						ConnectionThread.this, packet);
			}
		});
	}

	static class AcceptAll implements PacketFilter {
		@Override
		public boolean accept(Packet packet) {
			return true;
		}
	}

	synchronized void start(final String fqdn, final int port,
			final boolean useSRVLookup) {
		if (started)
			throw new IllegalStateException();
		started = true;
		runOnConnectionThread(new Runnable() {
			@Override
			public void run() {
				if (useSRVLookup)
					srvResolve(fqdn, fqdn, port);
				else
					addressResolve(null, fqdn, port, true);
			}
		});
	}

	void shutdown() {
		executorService.shutdownNow();
	}

	private void runOnConnectionThread(final Runnable runnable) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				if (!connectionItem.isManaged(ConnectionThread.this))
					return;
				try {
					runnable.run();
				} catch (RuntimeException e) {
					LogManager.exception(connectionItem, e);
					connectionClosedOnError(e);
				}
			}
		});
	}

	private void runOnUiThread(final Runnable runnable) {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!connectionItem.isManaged(ConnectionThread.this))
					return;
				runnable.run();
			}
		});
	}

	/*
	 * private void initProvider() {
	 * 
	 * Log.e(TAG, " <<< initProvider >> : initiate");
	 * 
	 * ProviderManager pm = ProviderManager.getInstance(); //
	 * pm.addIQProvider("query","http://jabber.org/protocol/disco#items", // new
	 * DiscoverItemsProvider()); //
	 * pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new //
	 * DiscoverInfoProvider()); try { pm.addIQProvider("query",
	 * "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
	 * } catch (ClassNotFoundException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } pm.addExtensionProvider("x", "jabber:x:roster",
	 * new RosterExchangeProvider()); pm.addExtensionProvider("x",
	 * "jabber:x:event", new MessageEventProvider());
	 * pm.addExtensionProvider("active",
	 * "http://jabber.org/protocol/chatstates", new
	 * ChatStateExtension.Provider()); pm.addExtensionProvider("composing",
	 * "http://jabber.org/protocol/chatstates", new
	 * ChatStateExtension.Provider()); pm.addExtensionProvider("paused",
	 * "http://jabber.org/protocol/chatstates", new
	 * ChatStateExtension.Provider()); pm.addExtensionProvider("inactive",
	 * "http://jabber.org/protocol/chatstates", new
	 * ChatStateExtension.Provider()); pm.addExtensionProvider("gone",
	 * "http://jabber.org/protocol/chatstates", new
	 * ChatStateExtension.Provider());
	 * 
	 * // XHTML pm.addExtensionProvider("html",
	 * "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider()); //
	 * Group Chat Invitations pm.addExtensionProvider("x",
	 * "jabber:x:conference", new GroupChatInvitation.Provider()); // Service
	 * Discovery # Items pm.addIQProvider("query",
	 * "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
	 * // Service Discovery # Info pm.addIQProvider("query",
	 * "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider()); //
	 * Data Forms pm.addExtensionProvider("x", "jabber:x:data", new
	 * DataFormProvider()); // MUC User pm.addExtensionProvider("x",
	 * "http://jabber.org/protocol/muc#user", new MUCUserProvider()); // MUC
	 * Admin pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
	 * new MUCAdminProvider()); // MUC Owner pm.addIQProvider("query",
	 * "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider()); //
	 * Delayed Delivery pm.addExtensionProvider("x", "jabber:x:delay", new
	 * DelayInformationProvider()); // Version try { pm.addIQProvider("query",
	 * "jabber:iq:version",
	 * Class.forName("org.jivesoftware.smackx.packet.Version")); } catch
	 * (ClassNotFoundException e) { // Not sure what's happening here. } //
	 * VCard // pm.addIQProvider("vCard","vcard-temp", new VCardProvider()); //
	 * Offline Message Requests pm.addIQProvider("offline",
	 * "http://jabber.org/protocol/offline", new
	 * OfflineMessageRequest.Provider()); // Offline Message Indicator
	 * pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline",
	 * new OfflineMessageInfo.Provider()); // Last Activity
	 * pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
	 * // User Search pm.addIQProvider("query", "jabber:iq:search", new
	 * UserSearch.Provider()); // SharedGroupsInfo
	 * pm.addIQProvider("sharedgroup",
	 * "http://www.jivesoftware.org/protocol/sharedgroup", new
	 * SharedGroupsInfo.Provider()); // JEP-33: Extended Stanza Addressing
	 * pm.addExtensionProvider("addresses",
	 * "http://jabber.org/protocol/address", new MultipleAddressesProvider());
	 * // FileTransfer pm.addIQProvider("si", "http://jabber.org/protocol/si",
	 * new StreamInitiationProvider()); pm.addIQProvider("query",
	 * "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
	 * pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new
	 * IBBProviders.Open()); pm.addIQProvider("close",
	 * "http://jabber.org/protocol/ibb", new IBBProviders.Close());
	 * pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new
	 * IBBProviders.Data()); pm.addIQProvider("open",
	 * "http://jabber.org/protocol/ibb", new OpenIQProvider());
	 * pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new
	 * CloseIQProvider()); pm.addExtensionProvider("data",
	 * "http://jabber.org/protocol/ibb", new DataPacketProvider()); // Privacy
	 * pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
	 * pm.addIQProvider("command", "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider());
	 * 
	 * pm.addExtensionProvider("malformed-action",
	 * "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider.MalformedActionError());
	 * pm.addExtensionProvider("bad-locale",
	 * "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider.BadLocaleError());
	 * pm.addExtensionProvider("bad-payload",
	 * "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider.BadPayloadError());
	 * pm.addExtensionProvider("bad-sessionid",
	 * "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider.BadSessionIDError());
	 * pm.addExtensionProvider("session-expired",
	 * "http://jabber.org/protocol/commands", new
	 * AdHocCommandDataProvider.SessionExpiredError()); }
	 */

}
