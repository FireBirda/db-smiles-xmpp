package com.digitalbuana.smiles.data.account;

import android.content.Intent;
import android.content.IntentFilter;

import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnCloseListener;
import com.digitalbuana.smiles.data.OnInitializedListener;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.receiver.ScreenReceiver;

public class ScreenManager implements OnInitializedListener, OnCloseListener {

	private final ScreenReceiver screenReceiver;
	private final static ScreenManager instance;

	static {
		instance = new ScreenManager();
		Application.getInstance().addManager(instance);
	}

	public static ScreenManager getInstance() {
		return instance;
	}

	private ScreenManager() {
		screenReceiver = new ScreenReceiver();
	}

	@Override
	public void onInitialized() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		Application.getInstance().registerReceiver(screenReceiver, filter);
	}

	@Override
	public void onClose() {
		Application.getInstance().unregisterReceiver(screenReceiver);
	}

	public void onScreen(Intent intent) {
		if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
			ConnectionManager.getInstance().updateConnections(false);
			AccountManager.getInstance().wakeUp();
		} else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
			
		}
	}

}
