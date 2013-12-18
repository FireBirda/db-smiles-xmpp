package com.digitalbuana.smiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.digitalbuana.smiles.data.ActivityManager;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.OnAdminUpdateChangeListener;
import com.digitalbuana.smiles.data.friends.OnFriendsUpdateChangeListener;
import com.digitalbuana.smiles.data.friends.OnVisitorChangeListener;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.MessageTable;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	public static final int MAX_ATTEMPTS = 5;
	public static final int BACKOFF_MILLI_SECONDS = 2000;

	public GCMIntentService() {
		super(AppConstants.GCMRegID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		// ServerUtilities.register(context, registrationId);
		// Log.i(TAG, "----------- onRegistered -----------");
		// Log.i(TAG, GCMRegistrar.getRegistrationId(context));
		GCMRegistrar.setRegisteredOnServer(context, true);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		// ServerUtilities.unregister(context, registrationId);
		// Log.i(TAG, "----------- onUnregistered -----------");
		GCMRegistrar.setRegisteredOnServer(context, false);
	}

	private static final String typeWakeup = "wakeup";
	private static final String typeLogOut = "logout";
	private static final String typeVisitor = "visitor";
	private static final String typeMyUpdate = "myupdate";
	private static final String typeAdmin = "admin";
	private static final String msgstatus = "msgstatus";

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;

	@Override
	protected void onMessage(final Context context, Intent intent) {
		String message = intent.getExtras().getString("price");
		message = Uri.decode(Uri.decode(message));

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();

		String actionWith = mSettings
				.getString(AppConstants.USERNAME_KEY, null);

		Log.v(TAG, " [GCM] >> " + message + " ");

		// generateNotification(context, message);

		try {

			JSONObject jsonku = new JSONObject(message);
			String type = jsonku.getString("type");

			boolean setLeftMenuNotification = false;

			if (type.equals(msgstatus)) {

				JSONObject jObj = jsonku.getJSONObject("content");
				JSONArray jDelArr = jObj.getJSONArray("delivered");
				JSONArray jReadArr = jObj.getJSONArray("read");
				String sender = jObj.getString("username");

				if (jDelArr.length() > 0) {
					// update delivered message status
					for (int a = 0; a < jDelArr.length(); a++) {
						String deliveredId = jDelArr.getString(a);
						MessageTable.getInstance().markAsDelivered(deliveredId);
					}
				}

				if (jReadArr.length() > 0) {
					// update read message status
					for (int b = 0; b < jReadArr.length(); b++) {
						String readId = jReadArr.getString(b);
						MessageTable.getInstance().markAsReadByFriend(readId);
					}
				}

				MessageManager.getInstance().onChatChanged(actionWith,
						sender + "@" + AppConstants.XMPPServerHost, true);

			} else if (type.equals(typeWakeup)) {
				ConnectionManager.getInstance().checkOnTimer();
				// startService(SmilesService.createIntent(this));
			} else if (type.equals(typeLogOut)) {

				JSONObject content = jsonku.getJSONObject("content");

				String pushRegId = content.getString("regid");
				String pushImei = content.getString("imei");

				String regId = RecentUtils.getRegID();
				String imei = RecentUtils.getImei();

				if (!pushRegId.trim().equals(regId.trim())
						&& !pushImei.trim().equals(imei.trim())) {

					if (actionWith != null)
						AccountManager.getInstance().removeAccount(actionWith);

					settingsEditor.clear();
					settingsEditor.commit();

					AccountManager.getInstance().doLogout();
					Application.getInstance().closeApplication();
					ActivityManager.getInstance().finishAll();
				}

			} else if (type.equals(typeVisitor)) {

				String name = jsonku.getJSONObject("content").getString(
						"username");
				String time = jsonku.getJSONObject("content").getString("dt");
				FriendsManager.getInstance().getVisitorManager()
						.addVisitorByName(name, time);

				/*
				 * trigger the listeners
				 */
				onVisitorChanged();
				setLeftMenuNotification = true;

			} else if (type.equals(typeMyUpdate)) {

				String name = jsonku.getJSONObject("content").getString(
						"username");
				String time = jsonku.getJSONObject("content").getString("dt");
				String messageUpdate = jsonku.getJSONObject("content")
						.getString("msg");
				FriendsManager
						.getInstance()
						.getFriendsUpdateManager()
						.addVisitorByName(name, time,
								messageUpdate.replace("+", " "));

				/*
				 * trigger the listeners
				 */
				onFriendsUpdateChanged();
				setLeftMenuNotification = true;

			} else if (type.equals(typeAdmin)) {

				String time = jsonku.getJSONObject("content").getString("dt");
				String msg = jsonku.getJSONObject("content").getString("msg")
						.replace("+", " ");
				FriendsManager.getInstance().getAdminUpdateManager()
						.addNotification(msg, time);

				onAdminUpdateChanged();
				setLeftMenuNotification = true;

			}
			if (setLeftMenuNotification) {
				settingsEditor
						.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, true);
				settingsEditor.commit();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void onVisitorChanged() {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (OnVisitorChangeListener visitorListener : Application
						.getInstance().getUIListeners(
								OnVisitorChangeListener.class)) {
					visitorListener.onVisitorChanged(FriendsManager
							.getInstance().getVisitorManager().getAllVisitor());
				}
			}
		});
	}

	private void onFriendsUpdateChanged() {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (OnFriendsUpdateChangeListener visitorListener : Application
						.getInstance().getUIListeners(
								OnFriendsUpdateChangeListener.class)) {
					visitorListener.onFriendsUpdateChanged(FriendsManager
							.getInstance().getFriendsUpdateManager()
							.getFriendsUpdate());
				}
			}
		});
	}

	private void onAdminUpdateChanged() {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (OnAdminUpdateChangeListener adminListener : Application
						.getInstance().getUIListeners(
								OnAdminUpdateChangeListener.class)) {
					adminListener.onAdminUpdateChanged(FriendsManager
							.getInstance().getAdminUpdateManager()
							.getAllNotification());
				}
			}
		});
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		// String message = getString(R.string.gcm_deleted, total);
		// notifies user
		// generateNotification(context, "error");
		// Log.i(TAG, "----------- onDeletedMessages -----------");
	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	// private static void generateNotification(Context context, String message)
	// {
	// Log.e(AppConstants.TAG, "----------- new Notification -----------");
	// int icon = R.drawable.ic_launcher;
	// long when = System.currentTimeMillis();
	// NotificationManager notificationManager =
	// (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	// Notification notification = new Notification(icon, message, when);
	// String title = context.getString(R.string.app_name);
	// Intent notificationIntent = new Intent(context,
	// SplashActivity.class);
	// // set intent so it does not start a new activity
	// notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
	// PendingIntent intent =PendingIntent.getActivity(context, 0,
	// notificationIntent, 0);
	// notification.setLatestEventInfo(context, title, message, intent);
	// notification.flags |= Notification.FLAG_AUTO_CANCEL;
	// // Play default notification sound
	// notification.defaults |= Notification.DEFAULT_SOUND;
	// // Vibrate if vibrate is enabled
	// notification.defaults |= Notification.DEFAULT_VIBRATE;
	// notificationManager.notify(0, notification);
	// }

}
