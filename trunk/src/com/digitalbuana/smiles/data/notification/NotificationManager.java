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
package com.digitalbuana.smiles.data.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.RemoteViews;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.ChatViewActivity;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnCloseListener;
import com.digitalbuana.smiles.data.OnInitializedListener;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.ArchiveMode;
import com.digitalbuana.smiles.data.account.OnAccountArchiveModeChangedListener;
import com.digitalbuana.smiles.data.account.OnAccountChangedListener;
import com.digitalbuana.smiles.data.account.OnAccountRemovedListener;
import com.digitalbuana.smiles.data.connection.ConnectionState;
import com.digitalbuana.smiles.data.extension.avatar.AvatarManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.message.MessageItem;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.chat.ChatManager;
import com.digitalbuana.smiles.data.message.phrase.PhraseManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.ui.ClearNotifications;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.StringUtils;

public class NotificationManager implements OnInitializedListener,
		OnAccountChangedListener, OnCloseListener, OnLoadListener, Runnable,
		OnAccountRemovedListener, OnAccountArchiveModeChangedListener {

	public static final int PERSISTENT_NOTIFICATION_ID = 1;
	private static final int CHAT_NOTIFICATION_ID = 2;
	private static final int BASE_NOTIFICATION_PROVIDER_ID = 0x10;

	private static final long VIBRATION_DURATION = 500;
	private static final int MAX_NOTIFICATION_TEXT = 80;
	private final long startTime;
	private final Application application;
	private final android.app.NotificationManager notificationManager;

	private final PendingIntent clearNotifications;
	private final Handler handler;

	private final Runnable startVibro;

	private final Runnable stopVibro;
	private final List<NotificationProvider<? extends NotificationItem>> providers;

	private final List<MessageNotification> messageNotifications;

	private final static NotificationManager instance;

	private String TAG = getClass().getSimpleName();

	static {
		instance = new NotificationManager();
		Application.getInstance().addManager(instance);
	}

	public static NotificationManager getInstance() {
		return instance;
	}

	private NotificationManager() {
		this.application = Application.getInstance();
		notificationManager = (android.app.NotificationManager) application
				.getSystemService(Context.NOTIFICATION_SERVICE);
		handler = new Handler();
		providers = new ArrayList<NotificationProvider<? extends NotificationItem>>();
		messageNotifications = new ArrayList<MessageNotification>();
		startTime = System.currentTimeMillis();
		clearNotifications = PendingIntent.getActivity(application, 0,
				ClearNotifications.createIntent(application), 0);
		stopVibro = new Runnable() {
			@Override
			public void run() {
				handler.removeCallbacks(startVibro);
				handler.removeCallbacks(stopVibro);
				((Vibrator) NotificationManager.this.application
						.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
			}
		};
		startVibro = new Runnable() {
			@Override
			public void run() {
				handler.removeCallbacks(startVibro);
				handler.removeCallbacks(stopVibro);
				((Vibrator) NotificationManager.this.application
						.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
				((Vibrator) NotificationManager.this.application
						.getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(VIBRATION_DURATION);
				handler.postDelayed(stopVibro, VIBRATION_DURATION);
			}
		};
	}

	@Override
	public void onLoad() {
		final Collection<MessageNotification> messageNotifications = new ArrayList<MessageNotification>();
		Cursor cursor = NotificationTable.getInstance().list();
		try {
			if (cursor.moveToFirst()) {
				do {
					messageNotifications.add(new MessageNotification(
							NotificationTable.getAccount(cursor),
							NotificationTable.getUser(cursor),
							NotificationTable.getText(cursor),
							NotificationTable.getTimeStamp(cursor),
							NotificationTable.getCount(cursor)));
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onLoaded(messageNotifications);
			}
		});
	}

	private void onLoaded(Collection<MessageNotification> messageNotifications) {
		this.messageNotifications.addAll(messageNotifications);
		for (MessageNotification messageNotification : messageNotifications)
			MessageManager.getInstance().openChat(
					messageNotification.getAccount(),
					messageNotification.getUser());
	}

	@Override
	public void onInitialized() {
		application.addUIListener(OnAccountChangedListener.class, this);
		updateMessageNotification(null);
	}

	public void registerNotificationProvider(
			NotificationProvider<? extends NotificationItem> provider) {
		providers.add(provider);
	}

	@SuppressWarnings("deprecation")
	public <T extends NotificationItem> void updateNotifications(
			NotificationProvider<T> provider, T notify) {

		int id = providers.indexOf(provider);

		if (id == -1)
			throw new IllegalStateException(
					"registerNotificationProvider() must be called from onLoaded() method.");
		else
			id += BASE_NOTIFICATION_PROVIDER_ID;

		Iterator<? extends NotificationItem> iterator = provider
				.getNotifications().iterator();

		if (!iterator.hasNext()) {
			notificationManager.cancel(id);
		} else {
			NotificationItem top;
			String ticker;
			if (notify == null) {
				top = iterator.next();
				ticker = null;
			} else {
				top = notify;
				ticker = StringUtils.replaceStringEquals(top.getTitle());
			}

			Intent intent = top.getIntent();
			Notification notification = new Notification(provider.getIcon(),
					ticker, System.currentTimeMillis());
			if (!provider.canClearNotifications()) {
				notification.flags |= Notification.FLAG_NO_CLEAR;
			}
			notification.setLatestEventInfo(application, StringUtils
					.replaceStringEquals(top.getTitle()), StringUtils
					.replaceStringEquals(top.getText()), PendingIntent
					.getActivity(application, 0, intent,
							PendingIntent.FLAG_UPDATE_CURRENT));
			if (ticker != null) {
				setNotificationDefaults(notification,
						SettingsManager.eventsVibro(), provider.getSound(),
						provider.getStreamType());
			}

			notification.deleteIntent = clearNotifications;
			notify(id, notification);
		}

	}

	private void setNotificationDefaults(Notification notification,
			boolean vibro, Uri sound, int streamType) {
		notification.audioStreamType = streamType;
		notification.defaults = 0;
		Context cntx = Application.getInstance().getApplicationContext();
		SharedPreferences mSettings = PreferenceManager
				.getDefaultSharedPreferences(cntx);
		String chatSound = mSettings.getString(
				AppConstants.SOUND_CHAT_NOTIF_TAG, null);

		Uri path = sound;
		if (chatSound != null && !chatSound.equals("default")) {
			String chatAssetPath = "android.resource://"
					+ cntx.getPackageName() + "/raw/" + chatSound;
			path = Uri.parse(chatAssetPath);
		}
		notification.sound = path;

		// notification.sound = sound;

		if (vibro) {
			if (SettingsManager.eventsIgnoreSystemVibro())
				handler.post(startVibro);
			else
				notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (SettingsManager.eventsLightning()) {
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
	}

	private void updateMessageNotification(MessageItem ticker) {
		Collection<String> accountList = AccountManager.getInstance()
				.getAccounts();
		boolean started = application.isInitialized();
		int waiting = 0;
		int connecting = 0;
		int connected = 0;
		for (String account : accountList) {
			ConnectionState state = AccountManager.getInstance()
					.getAccount(account).getState();
			if (RosterManager.getInstance().isRosterReceived(account))
				connected++;
			else if (state == ConnectionState.connecting
					|| state == ConnectionState.authentication)
				connecting++;
			else if (state == ConnectionState.waiting)
				waiting++;
		}

		String accountQuantity;
		String connectionState;
		if (connected > 0) {
			accountQuantity = StringUtils.getQuantityString(
					application.getResources(), R.array.account_quantity, 1);
			String connectionFormat = StringUtils.getQuantityString(
					application.getResources(),
					R.array.connection_state_connected, connected);
			connectionState = String.format(connectionFormat, connected, 1,
					accountQuantity);
		} else if (connecting > 0) {
			accountQuantity = StringUtils.getQuantityString(
					application.getResources(), R.array.account_quantity, 1);
			String connectionFormat = StringUtils.getQuantityString(
					application.getResources(),
					R.array.connection_state_connecting, connecting);
			connectionState = String.format(connectionFormat, connecting, 1,
					accountQuantity);
		} else if (waiting > 0 && started) {
			accountQuantity = StringUtils.getQuantityString(
					application.getResources(), R.array.account_quantity, 1);
			String connectionFormat = StringUtils.getQuantityString(
					application.getResources(),
					R.array.connection_state_waiting, waiting);
			connectionState = String.format(connectionFormat, waiting, 1,
					accountQuantity);
		} else {
			accountQuantity = StringUtils.getQuantityString(
					application.getResources(),
					R.array.account_quantity_offline, 1);
			connectionState = application.getString(
					R.string.connection_state_offline, 1, accountQuantity);
		}

		if (messageNotifications.isEmpty()) {
			notificationManager.cancel(CHAT_NOTIFICATION_ID);
		} else {
			int messageCount = 0;

			for (MessageNotification messageNotification : messageNotifications)
				messageCount += messageNotification.getCount();

			MessageNotification message = messageNotifications
					.get(messageNotifications.size() - 1);
			RemoteViews chatViews = new RemoteViews(
					application.getPackageName(), R.layout.chat_notification);
			Intent chatIntent = ChatViewActivity.createClearTopIntent(
					application, message.getAccount(), message.getUser());
			chatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			if (MUCManager.getInstance().hasRoom(message.getAccount(),
					message.getUser())) {
				chatViews.setImageViewBitmap(R.id.icon, AvatarManager
						.getInstance().getRoomBitmap(message.getUser()));
			} else {
				chatViews.setImageViewBitmap(R.id.icon, AvatarManager
						.getInstance().getUserBitmap(message.getUser()));
			}
			chatViews.setTextViewText(R.id.title, StringUtils
					.replaceStringEquals(RosterManager.getInstance().getName(
							message.getAccount(), message.getUser())));
			String text;
			if (ChatManager.getInstance().isShowText(message.getAccount(),
					message.getUser())) {
				text = trimText(StringUtils.replaceStringEquals(message
						.getText()));
			} else {
				text = "";
			}
			if (message.getText().contains(StringUtils.attention)) {
				chatViews.setTextViewText(
						R.id.text2,
						Emoticons.getSmiledText(
								application,
								Html.fromHtml("<b><font color=#e80f0f>" + text
										+ "<b></font>")));
			} else {
				chatViews.setTextViewText(R.id.text2,
						Emoticons.getSmiledText(application, text));
			}
			chatViews.setTextViewText(R.id.time,
					StringUtils.getSmartTimeText(message.getTimestamp()));

			String messageText = StringUtils.getQuantityString(
					application.getResources(), R.array.chat_message_quantity,
					messageCount);
			String contactText = StringUtils.getQuantityString(
					application.getResources(), R.array.chat_contact_quantity,
					messageNotifications.size());
			String status = application.getString(R.string.chat_status,
					messageCount, messageText, messageNotifications.size(),
					contactText);
			chatViews.setTextViewText(R.id.text, status);

			Notification notification = new Notification();

			updateNotification(notification, ticker);

			if (message.getText().contains(StringUtils.attention)) {

				Context cntx = Application.getInstance()
						.getApplicationContext();
				SharedPreferences mSettings = PreferenceManager
						.getDefaultSharedPreferences(cntx);
				String attentionSound = mSettings.getString(
						AppConstants.SOUND_ATTENTION_TAG, null);

				Uri path;
				if (attentionSound != null && !attentionSound.equals("default")) {
					String attentionAssetPath = "android.resource://"
							+ cntx.getPackageName() + "/raw/" + attentionSound;
					path = Uri.parse(attentionAssetPath);
				} else {
					path = SettingsManager.chatsAttentionSound();
				}
				notification.sound = path;
				notification.audioStreamType = AudioManager.RINGER_MODE_NORMAL;
			}

			notification.icon = connected > 0 ? R.drawable.ic_stat_message
					: R.drawable.ic_stat_message;
			notification.when = System.currentTimeMillis();

			notification.contentView = chatViews;
			notification.contentIntent = PendingIntent.getActivity(application,
					0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.deleteIntent = clearNotifications;

			try {
				notify(CHAT_NOTIFICATION_ID, notification);
			} catch (RuntimeException e) {
				chatViews.setImageViewResource(R.id.icon,
						R.drawable.ic_placeholder);
				notify(CHAT_NOTIFICATION_ID, notification);
			}

		}

	}

	public void notify(int id, Notification notification) {
		try {
			if (notification.tickerText != null)
				notification.tickerText = StringUtils
						.replaceStringEquals(notification.tickerText.toString());
			notificationManager.notify(id, notification);
		} catch (SecurityException e) {

		}
	}

	private void updateNotification(Notification notification,
			MessageItem ticker) {
		if (ticker == null)
			return;
		if (ticker.getChat().getFirstNotification()
				|| !SettingsManager.eventsFirstOnly())
			setNotificationDefaults(
					notification,
					ChatManager.getInstance().isMakeVibro(
							ticker.getChat().getAccount(),
							ticker.getChat().getUser()),
					PhraseManager.getInstance().getSound(
							ticker.getChat().getAccount(),
							ticker.getChat().getUser(),
							StringUtils.replaceStringEquals(ticker.getText())),
					AudioManager.STREAM_NOTIFICATION);
		if (ChatManager.getInstance().isShowText(ticker.getChat().getAccount(),
				ticker.getChat().getUser())) {
			String messageTemp = ticker.getChat().getUser()
					.replace("@" + AppConstants.XMPPServerHost, "")
					+ " : " + StringUtils.replaceStringEquals(ticker.getText());
			if (messageTemp.contains(StringUtils.attention)) {
				notification.tickerText = Html
						.fromHtml("<b><font color=#e80f0f>"
								+ trimText(messageTemp) + "<b></font>");
			} else {
				notification.tickerText = trimText(messageTemp);
			}

		}
	}

	private MessageNotification getMessageNotification(String account,
			String user) {
		for (MessageNotification messageNotification : messageNotifications)
			if (messageNotification.equals(account, user))
				return messageNotification;
		return null;
	}

	// public

	public void onMessageNotification(MessageItem messageItem,
			boolean addNotification) {

		if (addNotification) {

			String resource = messageItem.getResource();

			MessageNotification messageNotification = getMessageNotification(
					messageItem.getChat().getAccount(), messageItem.getChat()
							.getUser());

			if (messageNotification == null) {

				String tmpSender = messageItem.getChat().getUser();

				messageNotification = new MessageNotification(messageItem
						.getChat().getAccount(), tmpSender, null, null, 0);
			} else
				messageNotifications.remove(messageNotification);

			if (messageNotification.getUser().contains(
					AppConstants.XMPPGroupsServer)
					|| messageNotification.getUser().contains(
							AppConstants.XMPPRoomsServer)) {
				messageNotification
						.addMessage(resource
								+ " : "
								+ StringUtils.replaceStringEquals(messageItem
										.getText()));
			} else {
				messageNotification.addMessage(StringUtils
						.replaceStringEquals(messageItem.getText()));
			}
			messageNotifications.add(messageNotification);

			final String account = messageNotification.getAccount();
			final String user = messageNotification.getUser();
			final String text = messageNotification.getText();
			final Date timestamp = messageNotification.getTimestamp();
			final int count = messageNotification.getCount();
			if (AccountManager.getInstance().getArchiveMode(account) != ArchiveMode.dontStore)
				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {
						NotificationTable.getInstance().write(account, user,
								text, timestamp, count);
					}
				});
		}
		updateMessageNotification(messageItem);
	}

	/**
	 * Updates message notification.
	 */
	public void onMessageNotification() {
		updateMessageNotification(null);
	}

	public int getNotificationMessageCount(String account, String user) {
		MessageNotification messageNotification = getMessageNotification(
				account, user);
		if (messageNotification == null)
			return 0;
		return messageNotification.getCount();
	}

	public void removeMessageNotification(final String account,
			final String user) {
		MessageNotification messageNotification = getMessageNotification(
				account, user);
		if (messageNotification == null)
			return;
		messageNotifications.remove(messageNotification);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				NotificationTable.getInstance().remove(account, user);
			}
		});
		updateMessageNotification(null);
	}

	/**
	 * Called when notifications was cleared by user.
	 */
	public void onClearNotifications() {
		for (NotificationProvider<? extends NotificationItem> provider : providers)
			if (provider.canClearNotifications())
				provider.clearNotifications();
		messageNotifications.clear();
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				NotificationTable.getInstance().clear();
			}
		});
		updateMessageNotification(null);
	}

	@Override
	public void onAccountArchiveModeChanged(AccountItem accountItem) {
		final String account = accountItem.getAccount();
		if (AccountManager.getInstance().getArchiveMode(account) != ArchiveMode.dontStore)
			return;
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				NotificationTable.getInstance().removeAccount(account);
			}
		});
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		handler.post(this);
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		for (NotificationProvider<? extends NotificationItem> notificationProvider : providers)
			if (notificationProvider instanceof AccountNotificationProvider) {
				((AccountNotificationProvider<? extends NotificationItem>) notificationProvider)
						.clearAccountNotifications(accountItem.getAccount());
				updateNotifications(notificationProvider, null);
			}
	}

	@Override
	public void run() {
		handler.removeCallbacks(this);
		updateMessageNotification(null);
	}

	@Override
	public void onClose() {
		notificationManager.cancelAll();
	}

	private static String trimText(String text) {
		if (text.length() > MAX_NOTIFICATION_TEXT)
			return text.substring(0, MAX_NOTIFICATION_TEXT - 3) + "...";
		else
			return text;

	}

}
