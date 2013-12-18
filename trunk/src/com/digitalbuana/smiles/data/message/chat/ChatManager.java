package com.digitalbuana.smiles.data.message.chat;

import java.util.HashSet;
import java.util.Set;

import android.database.Cursor;
import android.net.Uri;

import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.OnAccountRemovedListener;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.entity.NestedMap;

public class ChatManager implements
OnLoadListener,
OnAccountRemovedListener {

	public static final Uri EMPTY_SOUND = Uri.parse("com.digitalbuana.smiles.data.message.ChatManager.EMPTY_SOUND");

	private static final Object PRIVATE_CHAT = new Object();

	private final NestedMap<ChatInput> chatInputs;

	private final NestedMap<Object> privateChats;

	private final NestedMap<Boolean> notifyVisible;

	private final NestedMap<Boolean> showText;

	private final NestedMap<Boolean> makeVibro;

	private final NestedMap<Uri> sounds;

	private final static ChatManager instance;

	static {
		instance = new ChatManager();
		Application.getInstance().addManager(instance);
	}

	public static ChatManager getInstance() {
		return instance;
	}

	private ChatManager() {
		chatInputs = new NestedMap<ChatInput>();
		privateChats = new NestedMap<Object>();
		sounds = new NestedMap<Uri>();
		showText = new NestedMap<Boolean>();
		makeVibro = new NestedMap<Boolean>();
		notifyVisible = new NestedMap<Boolean>();
	}

	@Override
	public void onLoad() {
			final Set<BaseEntity> privateChats = new HashSet<BaseEntity>();
			final NestedMap<Boolean> notifyVisible = new NestedMap<Boolean>();
			final NestedMap<Boolean> showText = new NestedMap<Boolean>();
			final NestedMap<Boolean> makeVibro = new NestedMap<Boolean>();
			final NestedMap<Uri> sounds = new NestedMap<Uri>();
			Cursor cursor;
			cursor = PrivateChatTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						privateChats.add(new BaseEntity(PrivateChatTable
								.getAccount(cursor), PrivateChatTable
								.getUser(cursor)));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}

			cursor = NotifyVisibleTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						notifyVisible.put(NotifyVisibleTable.getAccount(cursor),
								NotifyVisibleTable.getUser(cursor),
								NotifyVisibleTable.getValue(cursor));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}

			cursor = ShowTextTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						showText.put(ShowTextTable.getAccount(cursor),
								ShowTextTable.getUser(cursor),
								ShowTextTable.getValue(cursor));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}

			cursor = VibroTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						makeVibro.put(VibroTable.getAccount(cursor),
								VibroTable.getUser(cursor),
								VibroTable.getValue(cursor));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}

			cursor = SoundTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						sounds.put(SoundTable.getAccount(cursor),
								SoundTable.getUser(cursor),
								SoundTable.getValue(cursor));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}

			Application.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLoaded(privateChats, notifyVisible, showText, makeVibro, sounds);
				}
			});
	}

	private void onLoaded(Set<BaseEntity> privateChats,
			NestedMap<Boolean> notifyVisible, NestedMap<Boolean> showText,
			NestedMap<Boolean> vibro, NestedMap<Uri> sounds) {
		for (BaseEntity baseEntity : privateChats)
			this.privateChats.put(baseEntity.getAccount(),
					baseEntity.getUser(), PRIVATE_CHAT);
		this.notifyVisible.addAll(notifyVisible);
		this.showText.addAll(showText);
		this.makeVibro.addAll(vibro);
		this.sounds.addAll(sounds);
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		chatInputs.clear(accountItem.getAccount());
		privateChats.clear(accountItem.getAccount());
		sounds.clear(accountItem.getAccount());
		showText.clear(accountItem.getAccount());
		makeVibro.clear(accountItem.getAccount());
		notifyVisible.clear(accountItem.getAccount());
	}

	public boolean isSaveMessages(String account, String user) {
		return privateChats.get(account, user) != PRIVATE_CHAT;
	}

	public void setSaveMessages(final String account, final String user,
			final boolean save) {
		if (save)
			privateChats.remove(account, user);
		else
			privateChats.put(account, user, PRIVATE_CHAT);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				if (save)
					PrivateChatTable.getInstance().remove(account, user);
				else
					PrivateChatTable.getInstance().write(account, user);
			}
		});
	}

	public String getTypedMessage(String account, String user) {
		ChatInput chat = chatInputs.get(account, user);
		if (chat == null)
			return "";
		return chat.getTypedMessage();
	}

	public int getSelectionStart(String account, String user) {
		ChatInput chat = chatInputs.get(account, user);
		if (chat == null)
			return 0;
		return chat.getSelectionStart();
	}

	public int getSelectionEnd(String account, String user) {
		ChatInput chat = chatInputs.get(account, user);
		if (chat == null)
			return 0;
		return chat.getSelectionEnd();
	}

	public void setTyped(String account, String user, String typedMessage,
			int selectionStart, int selectionEnd) {
		ChatInput chat = chatInputs.get(account, user);
		if (chat == null) {
			chat = new ChatInput();
			chatInputs.put(account, user, chat);
		}
		chat.setTyped(typedMessage, selectionStart, selectionEnd);
	}

	public boolean isNotifyVisible(String account, String user) {
		Boolean value = notifyVisible.get(account, user);
		if (value == null)
			return SettingsManager.eventsVisibleChat();
		return value;
	}

	public void setNotifyVisible(final String account, final String user,
			final boolean value) {
		notifyVisible.put(account, user, value);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				NotifyVisibleTable.getInstance().write(account, user, value);
			}
		});
	}

	public boolean isShowText(String account, String user) {
		Boolean value = showText.get(account, user);
		if (value == null)
			return SettingsManager.eventsShowText();
		return value;
	}

	public void setShowText(final String account, final String user,
			final boolean value) {
		showText.put(account, user, value);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				ShowTextTable.getInstance().write(account, user, value);
			}
		});
	}

	public boolean isMakeVibro(String account, String user) {
		Boolean value = makeVibro.get(account, user);
		if (value == null)
			return SettingsManager.eventsVibro();
		return value;
	}

	public void setMakeVibro(final String account, final String user,
			final boolean value) {
		makeVibro.put(account, user, value);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				VibroTable.getInstance().write(account, user, value);
			}
		});
	}

	public Uri getSound(String account, String user) {
		Uri value = sounds.get(account, user);
		if (value == null)
			return SettingsManager.eventsSound();
		if (EMPTY_SOUND.equals(value))
			return null;
		return value;
	}

	public void setSound(final String account, final String user,
			final Uri value) {
		sounds.put(account, user, value == null ? EMPTY_SOUND : value);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				SoundTable.getInstance().write(account, user,
						value == null ? EMPTY_SOUND : value);
			}
		});
	}

}
