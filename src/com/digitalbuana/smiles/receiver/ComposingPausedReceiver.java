package com.digitalbuana.smiles.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.digitalbuana.smiles.data.extension.cs.ChatStateManager;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;

public class ComposingPausedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ChatStateManager.getInstance().onPaused(intent, getAccount(intent),getUser(intent));
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new EntityIntentBuilder(context, ComposingPausedReceiver.class).setAccount(account).setUser(user).build();
	}

	private static String getAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

}
