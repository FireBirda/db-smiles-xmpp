package com.digitalbuana.smiles.ui.helper;

import android.content.Intent;
import android.os.Bundle;

import com.digitalbuana.smiles.data.ActivityManager;

public abstract class ManagedListActivity extends ListActivityConnection {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityManager.getInstance().onCreate(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		ActivityManager.getInstance().onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		ActivityManager.getInstance().onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		ActivityManager.getInstance().onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		ActivityManager.getInstance().onNewIntent(this, intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ActivityManager.getInstance().onActivityResult(this, requestCode,
				resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void startActivity(Intent intent) {
		ActivityManager.getInstance().updateIntent(this, intent);
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		ActivityManager.getInstance().updateIntent(this, intent);
		super.startActivityForResult(intent, requestCode);
	}

}
