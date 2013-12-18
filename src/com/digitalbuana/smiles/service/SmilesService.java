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
package com.digitalbuana.smiles.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.notification.NotificationManager;

public class SmilesService extends Service {

	private Method startForeground;
	private Method stopForeground;

	private static SmilesService instance;

	private MyPhoneStateListener MyListener;
	private TelephonyManager Tel;
	private Context cntx;
	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private String MY_CONNECTION_MODE = "connection_mode";
	private String MY_IP_KEY = "my_ip_key";
	private static String TAG = "SmilesService";// getClass().getSimpleName();

	// private TimerTask myTimerTask = null;

	public static boolean isInstanceCreated() {
		return instance != null;
	}

	public static SmilesService getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		// Try to get methods supported in API Level 5+
		try {
			startForeground = getClass().getMethod("startForeground",
					new Class[] { int.class, Notification.class });
			stopForeground = getClass().getMethod("stopForeground",
					new Class[] { boolean.class });
		} catch (NoSuchMethodException e) {
			startForeground = stopForeground = null;
		}

		changeForeground();
		// setTimerTask();
	}

	public void changeForeground() {
		stopForegroundWrapper();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Application.getInstance().onServiceStarted();
		cntx = Application.getInstance().getApplicationContext();
		mSettings = PreferenceManager.getDefaultSharedPreferences(cntx);
		settingsEditor = mSettings.edit();
		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		tryToTriggerHeartBeatConnection();
		// setTimerTask();
	}

	/*
	 * private void setTimerTask() { if (myTimerTask == null) { myTimerTask =
	 * new TimerTask() {
	 * 
	 * @Override public void run() { // retry to send undelivered message
	 * Application.getInstance().runInBackground(new Runnable() {
	 * 
	 * @SuppressWarnings("static-access")
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub Cursor
	 * unSendList = MessageTable.getInstance() .unSent(); try { if
	 * (unSendList.moveToFirst()) { do { boolean isDeliverered = MessageTable
	 * .isDelivered(unSendList); boolean isIncomming = MessageTable
	 * .isIncoming(unSendList); boolean isReadByFriend = MessageTable
	 * .getReadByFriend(unSendList); boolean isError = MessageTable
	 * .hasError(unSendList); boolean isSent = MessageTable .isSent(unSendList);
	 * Date dateIs = MessageTable .getTimeStamp(unSendList); ChatAction action =
	 * MessageTable .getAction(unSendList); Date delayTimeStamp = MessageTable
	 * .getDelayTimeStamp(unSendList);
	 * 
	 * String resource = MessageTable .getUser(unSendList);
	 * 
	 * String defaultUserSender = mSettings .getString(
	 * AppConstants.USERNAME_KEY, null);
	 * 
	 * boolean isOnline = PresenceManager .getInstance() .getStatusMode(
	 * defaultUserSender, resource).isOnline();
	 * 
	 * if (isOnline && resource != null && !resource.equals("") && isSent &&
	 * !isError && !isReadByFriend && !isIncomming && !isDeliverered &&
	 * !resource .contains(AppConstants.XMPPGroupsServer) && !resource
	 * .contains(AppConstants.XMPPRoomsServer)) {
	 * 
	 * if (defaultUserSender != null) {
	 * 
	 * String textIs = MessageTable .getText(unSendList);
	 * 
	 * String packetId = MessageTable .getPacketId(unSendList);
	 * 
	 * Message message = new Message(); message.setTo(resource);
	 * message.setType(Message.Type.chat); message.setBody(textIs);
	 * message.setThread(packetId); message.setPacketID(packetId);
	 * 
	 * Log.d(TAG, "retry : " + packetId);
	 * 
	 * AbstractChat chat = new RegularChat( defaultUserSender,
	 * Jid.getBareAddress(resource));
	 * 
	 * MessageItem messageItem = new MessageItem( chat, "", resource, textIs,
	 * action, dateIs, delayTimeStamp, isIncomming, true, isSent, isError,
	 * isDeliverered, false, false, packetId, isReadByFriend);
	 * 
	 * ChatStateManager.getInstance() .updateOutgoingMessage( chat, message);
	 * 
	 * ReceiptManager.getInstance() .updateOutgoingMessage( chat, message,
	 * messageItem);
	 * 
	 * try { ConnectionManager .getInstance() .sendPacket( defaultUserSender,
	 * message); } catch (NetworkException e1) { }
	 * 
	 * try { new Thread().sleep(1000); } catch (InterruptedException e) {
	 * Log.e(TAG, e.getMessage()); }
	 * 
	 * }
	 * 
	 * } } while (unSendList.moveToNext()); } } finally { unSendList.close(); }
	 * } }); } }; // new timer Timer timer = new Timer(); // schedule timer
	 * timer.scheduleAtFixedRate(myTimerTask, 45000, 45000); } }
	 */

	@Override
	public void onDestroy() {
		super.onDestroy();

		stopForegroundWrapper();
		Application.getInstance().onServiceDestroy();

		try {
			if (mSettings.getString(AppConstants.PASSWORD_KEY, null) != null) {
				startService(SmilesService.createIntent(this));
			}

		} catch (NullPointerException e) {
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	void startForegroundWrapper(Notification notification) {
		if (startForeground != null) {
			Object[] startForegroundArgs = new Object[] {
					Integer.valueOf(NotificationManager.PERSISTENT_NOTIFICATION_ID),
					notification };
			try {
				startForeground.invoke(this, startForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				LogManager.w(this, "Unable to invoke startForeground" + e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				LogManager.w(this, "Unable to invoke startForeground" + e);
			}
		} else {
			try {
				((android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
						.notify(NotificationManager.PERSISTENT_NOTIFICATION_ID,
								notification);
			} catch (SecurityException e) {
			}
		}
	}

	void stopForegroundWrapper() {
		if (stopForeground != null) {
			try {
				stopForeground.invoke(this, new Object[] { Boolean.TRUE });
				// We don't want to clear notification bar.
			} catch (InvocationTargetException e) {
				// Should not happen.
				LogManager.w(this, "Unable to invoke stopForeground" + e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				LogManager.w(this, "Unable to invoke stopForeground" + e);
			}
		} else {
			// setForeground(false);
		}
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, SmilesService.class);
	}

	private class MyPhoneStateListener extends PhoneStateListener {
		@SuppressLint("NewApi")
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);

			ConnectivityManager cm = (ConnectivityManager) cntx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo[] netInfo = cm.getAllNetworkInfo();
				for (NetworkInfo ni : netInfo) {
					String myConnectionMode = ni.getTypeName();
					if ((myConnectionMode.equalsIgnoreCase("WIFI") || myConnectionMode
							.equalsIgnoreCase("MOBILE"))
							&& ni.isConnected()
							&& ni.isAvailable()) {
						String myCurrentConnectionMode = mSettings.getString(
								MY_CONNECTION_MODE, null);
						String existingConnectionMode = null;
						if (myCurrentConnectionMode == null) {
							if (myConnectionMode.equalsIgnoreCase("WIFI")) {
								settingsEditor.putString(MY_CONNECTION_MODE,
										myConnectionMode);
								existingConnectionMode = myConnectionMode;
							} else {
								settingsEditor.putString(MY_CONNECTION_MODE,
										ni.getSubtypeName());
								existingConnectionMode = ni.getSubtypeName();
							}
							settingsEditor.commit();
						} else {
							if (myConnectionMode.equalsIgnoreCase("WIFI")) {
								existingConnectionMode = myConnectionMode;
							} else {
								existingConnectionMode = ni.getSubtypeName();
							}
						}
						String myIp = getLocalIpAddress();
						String myLocalIp = mSettings.getString(MY_IP_KEY, null);
						if (myLocalIp == null) {
							settingsEditor.putString(MY_IP_KEY, myIp);
							settingsEditor.commit();
						} else if (!myLocalIp.equalsIgnoreCase(myIp)) {
							settingsEditor.putString(MY_IP_KEY, myIp);
							settingsEditor.commit();
							tryToTriggerHeartBeatConnection();
						} else {
							if (!myCurrentConnectionMode
									.equals(existingConnectionMode)) {
								settingsEditor.putString(MY_CONNECTION_MODE,
										existingConnectionMode);
								settingsEditor.commit();
								tryToTriggerHeartBeatConnection();
							}
						}
					}
				}
			}

		}
	}

	private void tryToTriggerHeartBeatConnection() {
		ConnectionManager.getInstance().checkOnTimer();
		/*
		 * Log.v(TAG,
		 * " >> heartbeat :: tryToTriggerHeartBeatConnection :: send connection request to connection manager :: "
		 * ); new Thread("tryToTriggerHeartBeatConnection......") { public void
		 * run() { try { ConnectionManager.getInstance().checkOnTimer(); }
		 * finally { this.interrupt(); } } }.start();
		 */
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}
}
