package com.digitalbuana.smiles.data.friends;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.digitalbuana.smiles.activity.NotificationActivity;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.notification.EntityNotificationItem;

public class AdminNotification extends BaseEntity implements
		EntityNotificationItem {

	private AdminModel admin;
	private String TAG = getClass().getSimpleName();

	public AdminNotification(String account, String user, AdminModel am) {
		super(account, user);
		Log.e(TAG, "account : " + account + ", user : " + user);
		this.admin = am;
	}

	@Override
	public Intent getIntent() {
		return NotificationActivity.createClearTopIntent(
				Application.getInstance(), account, user);
	}

	@Override
	public String getTitle() {
		return "admin";
	}

	@Override
	public String getText() {
		return Uri.decode(admin.getMessage());
	}

}
