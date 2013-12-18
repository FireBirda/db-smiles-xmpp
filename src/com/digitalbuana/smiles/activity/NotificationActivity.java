package com.digitalbuana.smiles.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.AdminUpdateAdapter;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.friends.AdminModel;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.OnAdminUpdateChangeListener;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class NotificationActivity extends ManagedActivity implements
		OnClickListener, OnAdminUpdateChangeListener, OnItemClickListener {

	private static final String ACTION_ATTENTION = "com.digitalbuana.smiles.data.ATTENTION";

	private FrameLayout btnBack;
	private ListView adminNotifList;
	private AdminUpdateAdapter aum;
	private FrameLayout notificationButtonClear;

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();

		setContentView(R.layout.activity_notification);

		FriendsManager.getInstance().getAdminUpdateManager().markAsRead();

		notificationButtonClear = (FrameLayout) findViewById(R.id.notificationButtonClear);
		notificationButtonClear.setOnClickListener(this);

		aum = new AdminUpdateAdapter(this);

		adminNotifList = (ListView) findViewById(R.id.adminNotifList);
		adminNotifList.setAdapter(aum);

		rootView = (FrameLayout) findViewById(R.id.notificationRootView);
		btnBack = (FrameLayout) findViewById(R.id.notificationbtnBack);

		btnBack.setOnClickListener(this);

		FontUtils.setRobotoFont(context, rootView);

		adminNotifList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				AdminModel am = (AdminModel) aum.getItem(arg2);
				Toast.makeText(context, am.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		});

		settingsEditor.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, false);
		settingsEditor.commit();

		if (aum.getCount() < 1)
			notificationButtonClear.setVisibility(View.GONE);

		String account = mSettings.getString(AppConstants.USERNAME_KEY, "");

		FriendsManager.getInstance().getAdminUpdateManager()
				.removeAdminNotifications(account, AppConstants.XMPPServerHost);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Application.getInstance().addUIListener(
				OnAdminUpdateChangeListener.class, this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Application.getInstance().removeUIListener(
				OnAdminUpdateChangeListener.class, this);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, NotificationActivity.class);
	}

	@Override
	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == notificationButtonClear) {
			FriendsManager.getInstance().getAdminUpdateManager()
					.deleteAllNotification();
			aum.onChange();
			notificationButtonClear.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAdminUpdateChanged(ArrayList<AdminModel> listVisitor) {
		// TODO Auto-generated method stub
		aum.onChange();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new Intent(context, NotificationActivity.class);
	}

	public static Intent createClearTopIntent(Context context, String account,
			String user) {
		Intent intent = createIntent(context, account, user);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}
