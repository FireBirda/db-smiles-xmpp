package com.digitalbuana.smiles.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.LeftMenuAdapter;
import com.digitalbuana.smiles.adapter.RequestFriendsAdapter.OnFriendsReqListener;
import com.digitalbuana.smiles.awan.adapters.RightMenuMultimediaAdapter;
import com.digitalbuana.smiles.awan.helper.ImageHelper;
import com.digitalbuana.smiles.awan.helper.PhoneContactHelper;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.awan.model.AdsApiModel;
import com.digitalbuana.smiles.awan.model.RightMenuMultimediaModel;
import com.digitalbuana.smiles.awan.stores.AdsApiStore;
import com.digitalbuana.smiles.awan.stores.RightMenuMultimediaStores;
import com.digitalbuana.smiles.awan.stores.RightMenuStores;
import com.digitalbuana.smiles.data.ActivityManager;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.StatusMode;
import com.digitalbuana.smiles.data.connection.ConnectionState;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.extension.avatar.AvatarManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.friends.AdminModel;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.friends.OnAdminUpdateChangeListener;
import com.digitalbuana.smiles.data.friends.OnFriendsUpdateChangeListener;
import com.digitalbuana.smiles.data.friends.OnVisitorChangeListener;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.OnChatChangedListener;
import com.digitalbuana.smiles.data.notification.NotificationManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.GroupManager;
import com.digitalbuana.smiles.data.roster.OnContactChangedListener;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.data.roster.ShowOfflineMode;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog.OnSmilesDialogClose;
import com.digitalbuana.smiles.service.SmilesService;
import com.digitalbuana.smiles.ui.AccountEditor;
import com.digitalbuana.smiles.ui.ContactAdd;
import com.digitalbuana.smiles.ui.ContactEditor;
import com.digitalbuana.smiles.ui.ContactViewer;
import com.digitalbuana.smiles.ui.MUCEditor;
import com.digitalbuana.smiles.ui.StatusEditor;
import com.digitalbuana.smiles.ui.adapter.AccountConfiguration;
import com.digitalbuana.smiles.ui.adapter.AccountToggleAdapter;
import com.digitalbuana.smiles.ui.adapter.ContactListAdapter;
import com.digitalbuana.smiles.ui.adapter.GroupConfiguration;
import com.digitalbuana.smiles.ui.dialog.AccountChooseDialogBuilder;
import com.digitalbuana.smiles.ui.dialog.ConfirmDialogBuilder;
import com.digitalbuana.smiles.ui.dialog.ConfirmDialogListener;
import com.digitalbuana.smiles.ui.dialog.DialogBuilder;
import com.digitalbuana.smiles.ui.dialog.GroupRenameDialogBuilder;
import com.digitalbuana.smiles.ui.helper.BaseUIListActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.android.gcm.GCMRegistrar;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;

public class HomeActivity extends BaseUIListActivity implements
		OnContactChangedListener, /* OnAccountChangedListener, */
		OnChatChangedListener, View.OnClickListener, ConfirmDialogListener,
		OnItemClickListener, OnLongClickListener, /* OnVCardListener, */
		OnFriendsReqListener, OnFriendsUpdateChangeListener,
		OnVisitorChangeListener, OnAdminUpdateChangeListener {

	private String TAG = getClass().getSimpleName();

	// HomeContainer
	// Title
	private FrameLayout btnShowMenu;
	private FrameLayout btnShowEntertaint;
	private ImageView homeLeftMenuIcon;

	// Content
	LinearLayout homeContainer;
	SlidingMenu slidingMenu;
	ListView listView;

	// Start Container
	private FrameLayout startContainer;
	private ImageView imgTopLogo;
	private ImageView imgCewe;
	private FrameLayout btnRegister;
	private FrameLayout btnLogin;

	// LeftMenuCotainer
	private LinearLayout leftMenuTitle;
	private ImageView leftMenuAvatar;
	private FrameLayout pitaKananImageView;
	private FrameLayout pitaKiriImageView;
	private TextView leftMenuUser;
	private TextView leftMenuStatus;
	private ListView leftMenuList;
	private LeftMenuAdapter leftMenuAdapter;

	// LeftMenuCotainer
	private GridViewKu rightMenuMultimedia;
	private GridViewKu rightMenuContent;
	private FrameLayout rightMenuAdsConteiner;
	private FrameLayout rightMenuShopConteiner;

	private ContactListAdapter contactListAdapter;

	private static final String ACTION_ROOM_INVITE = "com.digitalbuana.smiles.activity.HomeActivity.ACTION_ROOM_INVITE";

	private static final String SAVED_ACTION = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_ACTION";
	private static final String SAVED_ACTION_WITH_ACCOUNT = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_ACTION_WITH_ACCOUNT";
	private static final String SAVED_ACTION_WITH_GROUP = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_ACTION_WITH_GROUP";
	private static final String SAVED_ACTION_WITH_USER = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_ACTION_WITH_USER";
	private static final String SAVED_SEND_TEXT = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_SEND_TEXT";
	private static final String SAVED_OPEN_DIALOG_USER = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_OPEN_DIALOG_USER";
	private static final String SAVED_OPEN_DIALOG_TEXT = "com.digitalbuana.smiles.activity.HomeActivity.SAVED_OPEN_DIALOG_TEXT";

	private String action, actionWithAccount, actionWithGroup, actionWithUser,
			sendText, openDialogUser, openDialogText;

	private static final int CONTEXT_MENU_VIEW_CHAT_ID = 0x12;
	private static final int CONTEXT_MENU_EDIT_CONTACT_ID = 0x13;
	private static final int CONTEXT_MENU_DELETE_CONTACT_ID = 0x14;
	private static final int CONTEXT_MENU_CLOSE_CHAT_ID = 0x15;
	private static final int CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID = 0x16;
	private static final int CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID = 0x17;
	private static final int CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID = 0x18;
	private static final int CONTEXT_MENU_LEAVE_ROOM_ID = 0x19;
	private static final int CONTEXT_MENU_JOIN_ROOM_ID = 0x1A;
	private static final int CONTEXT_MENU_EDIT_ROOM_ID = 0x1B;
	private static final int CONTEXT_MENU_VIEW_CONTACT_ID = 0x1C;

	private static final int CONTEXT_MENU_GROUP_RENAME_ID = 0x31;
	private static final int CONTEXT_MENU_GROUP_DELETE_ID = 0x32;

	private static final int CONTEXT_MENU_ACCOUNT_EDITOR_ID = 0x33;
	private static final int CONTEXT_MENU_ACCOUNT_STATUS_ID = 0x34;
	private static final int CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID = 0x35;
	private static final int CONTEXT_MENU_ACCOUNT_RECONNECT_ID = 0x39;
	private static final int CONTEXT_MENU_ACCOUNT_VCARD_ID = 0x3A;

	private static final int CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID = 0x40;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID = 0x41;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID = 0x42;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID = 0x43;

	private AccountToggleAdapter accountToggleAdapter;

	private static final int DIALOG_DELETE_CONTACT_ID = 0x50;
	private static final int DIALOG_DELETE_GROUP_ID = 0x51;
	private static final int DIALOG_RENAME_GROUP_ID = 0x52;
	private static final int DIALOG_START_AT_BOOT_ID = 0x53;
	private static final int DIALOG_CONTACT_INTEGRATION_ID = 0x54;
	private static final int DIALOG_OPEN_WITH_ACCOUNT_ID = 0x55;
	private static final int DIALOG_CLOSE_APPLICATION_ID = 0x57;

	private AQuery aq;
	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private String jsonCache = null;

	private Animation blinkAnimation;

	private String regId = null;
	private String imei = null;
	private String tmpUserName;

	public static boolean isHomeScreenShowing = false;

	public void onCreate(Bundle savedInstanceState) {

		if (Intent.ACTION_VIEW.equals(getIntent().getAction())
				|| Intent.ACTION_SEND.equals(getIntent().getAction())
				|| Intent.ACTION_SENDTO.equals(getIntent().getAction())
				|| Intent.ACTION_CREATE_SHORTCUT
						.equals(getIntent().getAction()))

			ActivityManager.getInstance().startNewTask(this);

		super.onCreate(savedInstanceState);

		isHomeScreenShowing = true;

		regId = RecentUtils.getRegID();
		imei = RecentUtils.getImei();

		if (isFinishing()) {
			return;
		}
		;

		ImageHelper.setAqueryCachePath();

		blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);

		// Casting
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();

		boolean firstNotification = mSettings.getBoolean(
				AppConstants.FIRST_NOTIF_TAG, false);

		if (!firstNotification) {
			settingsEditor.putBoolean(AppConstants.FIRST_NOTIF_TAG, true);
			settingsEditor.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, true);
			settingsEditor.commit();
		}

		setContentView(R.layout.activity_home);

		rootView = (FrameLayout) findViewById(R.id.homeRootView);

		homeLeftMenuIcon = (ImageView) findViewById(R.id.homeLeftMenuIcon);

		// Casting
		// HomeContainer
		// title
		btnShowMenu = (FrameLayout) findViewById(R.id.homeTitleBtnShowMenu);
		btnShowEntertaint = (FrameLayout) findViewById(R.id.homeTitleBtnShowEntertaint);

		homeContainer = (LinearLayout) findViewById(R.id.homeContainer);
		listView = getListView();

		// StartContainer
		startContainer = (FrameLayout) findViewById(R.id.homeStartContainer);
		imgTopLogo = (ImageView) findViewById(R.id.homeStartImgTopLogo);
		imgCewe = (ImageView) findViewById(R.id.homeStartCewe);
		btnRegister = (FrameLayout) findViewById(R.id.homeStartBtnRegister);
		btnLogin = (FrameLayout) findViewById(R.id.homeStartBtnLogin);

		registerForContextMenu(listView);
		contactListAdapter = new ContactListAdapter(this);
		setListAdapter(contactListAdapter);

		accountToggleAdapter = new AccountToggleAdapter(this,
				(LinearLayout) findViewById(R.id.account_list));

		settingSlidingMenu();
		// SettingListenr
		btnShowMenu.setOnClickListener(this);
		btnShowEntertaint.setOnClickListener(this);
		leftMenuTitle.setOnClickListener(this);
		listView.setOnItemClickListener(this);
		listView.setItemsCanFocus(true);
		// StartView
		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);

		// LeftMenu
		leftMenuList.setOnItemClickListener(this);
		registerForContextMenu(listView);

		FontUtils.setRobotoFont(context, rootView);

		if (savedInstanceState != null) {
			actionWithAccount = savedInstanceState
					.getString(SAVED_ACTION_WITH_ACCOUNT);
			actionWithGroup = savedInstanceState
					.getString(SAVED_ACTION_WITH_GROUP);
			actionWithUser = savedInstanceState
					.getString(SAVED_ACTION_WITH_USER);
			sendText = savedInstanceState.getString(SAVED_SEND_TEXT);
			openDialogUser = savedInstanceState
					.getString(SAVED_OPEN_DIALOG_USER);
			openDialogText = savedInstanceState
					.getString(SAVED_OPEN_DIALOG_TEXT);
			action = savedInstanceState.getString(SAVED_ACTION);
		} else {
			actionWithAccount = null;
			actionWithGroup = null;
			actionWithUser = null;
			sendText = null;
			openDialogUser = null;
			openDialogText = null;
			action = getIntent().getAction();
		}
		getIntent().setAction(null);
		rightMenuInitiator();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		action = getIntent().getAction();
		getIntent().setAction(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACTION, action);
		outState.putString(SAVED_ACTION_WITH_ACCOUNT, actionWithAccount);
		outState.putString(SAVED_ACTION_WITH_GROUP, actionWithGroup);
		outState.putString(SAVED_ACTION_WITH_USER, actionWithUser);
		outState.putString(SAVED_SEND_TEXT, sendText);
		outState.putString(SAVED_OPEN_DIALOG_USER, openDialogUser);
		outState.putString(SAVED_OPEN_DIALOG_TEXT, openDialogText);
	}

	private void settingAds(JSONObject json) {

		AdsApiStore aas = new AdsApiStore(json);
		ArrayList<AdsApiModel> adsList = aas.getList();

		try {
			String whichAdsActive = json.getString("adsactive");
			AdsApiModel aamActive = new AdsApiModel();
			for (AdsApiModel aam : adsList) {
				if (whichAdsActive.trim().equals(aam.getType())) {
					aamActive.setClientId(aam.getClientId());
				}
			}
			if (whichAdsActive.equals(getString(R.string.admob_key))) {

				AdView adView = new AdView(this, AdSize.SMART_BANNER,
						aamActive.getClientId());
				rightMenuAdsConteiner.addView(adView);
				AdRequest request = new AdRequest();
				adView.loadAd(request);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void settingSlidingMenu() {

		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		slidingMenu.setMenu(R.layout.activity_home_leftmenu);
		slidingMenu.setSecondaryMenu(R.layout.activity_home_rightmenu);
		slidingMenu.setFadeDegree(0);

		slidingMenu.setBehindScrollScale(0.0f);
		// slidingMenu.setBehindWidth(ViewUtilities.GetInstance().convertDPtoPX(320));

		slidingMenu.setBehindWidth(ScreenHelper.getScreenWidth(this));

		leftMenuTitle = (LinearLayout) slidingMenu.getMenu().findViewById(
				R.id.homeLeftMenuTitle);
		leftMenuAvatar = (ImageView) slidingMenu.getMenu().findViewById(
				R.id.homeLeftMenuAvatar);
		leftMenuUser = (TextView) slidingMenu.getMenu().findViewById(
				R.id.homeLeftMenuTitleName);
		leftMenuStatus = (TextView) slidingMenu.getMenu().findViewById(
				R.id.homeLeftMenuTitleStatus);
		leftMenuList = (ListView) slidingMenu.getMenu().findViewById(
				R.id.homeLeftMenuList);
		pitaKananImageView = (FrameLayout) slidingMenu.getMenu().findViewById(
				R.id.pitaKananImageView);
		pitaKananImageView.setOnClickListener(this);

		// RightMenu
		rightMenuMultimedia = (GridViewKu) slidingMenu.getSecondaryMenu()
				.findViewById(R.id.homeRightMenuMultimediaGrid);
		rightMenuContent = (GridViewKu) slidingMenu.getSecondaryMenu()
				.findViewById(R.id.homeRightMenuContentGrid);
		rightMenuAdsConteiner = (FrameLayout) slidingMenu.getSecondaryMenu()
				.findViewById(R.id.homeRightMenuAdsContainer);
		rightMenuShopConteiner = (FrameLayout) slidingMenu.getSecondaryMenu()
				.findViewById(R.id.homeRightMenuShopContainer);
		pitaKiriImageView = (FrameLayout) slidingMenu.getSecondaryMenu()
				.findViewById(R.id.pitaKiriImageView);
		pitaKiriImageView.setOnClickListener(this);

		rightMenuShopConteiner.setOnClickListener(this);
		leftMenuAdapter = new LeftMenuAdapter(this);
		leftMenuList.setAdapter(leftMenuAdapter);

		FontUtils.setRobotoFont(context, slidingMenu.getMenu());
		FontUtils.setRobotoFont(context, slidingMenu.getSecondaryMenu());

		slidingMenu.setOnOpenListener(new OnOpenListener() {
			@Override
			public void onOpen() {
				new Thread() {
					public void run() {
						try {
							runOnUiThread(new Runnable() {
								public void run() {
									resetLeftHeaderNotification();
									leftMenuAdapter.notifyDataSetChanged();
								}
							});
						} finally {
							this.interrupt();
						}
					}
				}.start();
			}
		});
	}

	private void rightMenuInitiator() {
		aq = new AQuery(this);
		jsonCache = mSettings.getString(
				getString(R.string.streaming_cache_key), null);
		if (jsonCache != null) {
			try {
				generateRightList(new JSONObject(jsonCache));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.i(TAG, e.getMessage());
			}
		}
		String homeConfig = AppConstants.APIUserGetRightMenu;
		aq.ajax(homeConfig, JSONObject.class, this, "homeCallback");
	}

	public void homeCallback(String url, JSONObject json, AjaxStatus status) {
		if (json != null) {
			if (jsonCache == null)
				generateRightList(json);
			settingsEditor.putString(getString(R.string.streaming_cache_key),
					json.toString());
			settingsEditor.commit();
			settingAds(json);
		} else {
			Log.i(TAG, "homeCallback ajax error : " + url);
		}
		// AdminManager.getInstance().setUpdateTable();
	}

	private void generateRightList(JSONObject json) {
		RightMenuMultimediaStores hs = new RightMenuMultimediaStores(json);
		final ArrayList<RightMenuMultimediaModel> homeList = hs.getList();
		RightMenuMultimediaAdapter rmma = new RightMenuMultimediaAdapter(this,
				this, R.layout.item_rightmenu_multimedia, homeList);
		rightMenuMultimedia.setNumColumns(2);
		rightMenuMultimedia.setAdapter(rmma);
		rightMenuMultimedia.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				RightMenuMultimediaModel hm = homeList.get(arg2);
				String type = hm.getType();
				String detail = hm.getDetail();
				String parm = hm.getParm();
				if (type.equals("package")) {
					PackageManager pm = getPackageManager();
					Intent appStartIntent = pm
							.getLaunchIntentForPackage(detail);
					if (null != appStartIntent) {
						appStartIntent.putExtra("parm", parm);
						startActivity(appStartIntent);
					} else {
						Intent browserIntent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse(getString(R.string.play_url) + detail));
						startActivity(browserIntent);
					}
				} else {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(detail));
					startActivity(browserIntent);
				}
			}
		});
		RightMenuStores rms = new RightMenuStores(json);
		final ArrayList<RightMenuMultimediaModel> storeList = rms.getList();
		RightMenuMultimediaAdapter rmsa = new RightMenuMultimediaAdapter(this,
				this, R.layout.item_rightmenu_multimedia, storeList);
		rightMenuContent.setNumColumns(2);
		rightMenuContent.setAdapter(rmsa);
		rightMenuContent.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				RightMenuMultimediaModel hm = storeList.get(arg2);
				String type = hm.getType();
				String detail = hm.getDetail();
				String parm = hm.getParm();
				if (type.equals("package")) {
					PackageManager pm = getPackageManager();
					Intent appStartIntent = pm
							.getLaunchIntentForPackage(detail);
					if (null != appStartIntent) {
						appStartIntent.putExtra("parm", parm);
						startActivity(appStartIntent);
					} else {
						Intent browserIntent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse(getString(R.string.play_url) + detail));
						startActivity(browserIntent);
					}
				} else {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(detail));
					startActivity(browserIntent);
				}
			}
		});
		PhoneContactHelper.getSetLocalContact(this);
	}

	@Override
	protected void onResume() {

		super.onResume();
		tmpUserName = mSettings.getString(AppConstants.USERNAME_KEY, null);
		compareRegId();

		resettingView();
		rebuildAccountToggler();
		leftMenuAdapter.notifyDataSetChanged();

		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnChatChangedListener.class,
				this);
		Application.getInstance().addUIListener(
				OnFriendsUpdateChangeListener.class, this);
		Application.getInstance().addUIListener(OnVisitorChangeListener.class,
				this);
		Application.getInstance().addUIListener(
				OnAdminUpdateChangeListener.class, this);

		contactListAdapter.refreshRequest();
		contactListAdapter.getBookmarkItem();

		boolean hasLeftNotification = mSettings.getBoolean(
				AppConstants.LEFTMENU_NOTIF_TAG, false);

		if (hasLeftNotification)
			setLeftNotification();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		if (v == getListView()) {

			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

			BaseEntity baseEntity = (BaseEntity) getListView()
					.getItemAtPosition(info.position);

			if (baseEntity == null)
				// Account toggler
				return;
			if (baseEntity instanceof AbstractContact) {
				// Contact
				actionWithAccount = baseEntity.getAccount();
				actionWithGroup = null;
				actionWithUser = baseEntity.getUser();
				AbstractContact abstractContact = (AbstractContact) baseEntity;
				menu.setHeaderTitle(com.digitalbuana.smiles.utils.StringUtils
						.replaceStringEquals(abstractContact.getName()));
				menu.add(0, CONTEXT_MENU_VIEW_CHAT_ID, 0, getResources()
						.getText(R.string.chat_viewer));

				if (MUCManager.getInstance().hasRoom(actionWithAccount,
						actionWithUser)) {
					if (!MUCManager.getInstance().inUse(actionWithAccount,
							actionWithUser))
						menu.add(0, CONTEXT_MENU_EDIT_ROOM_ID, 0,
								getResources().getText(R.string.muc_edit));
					menu.add(0, CONTEXT_MENU_DELETE_CONTACT_ID, 0,
							getResources().getText(R.string.muc_delete));
					if (MUCManager.getInstance().isDisabled(actionWithAccount,
							actionWithUser))
						menu.add(0, CONTEXT_MENU_JOIN_ROOM_ID, 0,
								getResources().getText(R.string.muc_join));
					else
						menu.add(0, CONTEXT_MENU_LEAVE_ROOM_ID, 0,
								getResources().getText(R.string.muc_leave));
				} else {
					menu.add(0, CONTEXT_MENU_VIEW_CONTACT_ID, 0, getResources()
							.getText(R.string.contact_viewer));
					menu.add(0, CONTEXT_MENU_EDIT_CONTACT_ID, 0, getResources()
							.getText(R.string.contact_editor));
					menu.add(0, CONTEXT_MENU_DELETE_CONTACT_ID, 0,
							getResources().getText(R.string.contact_delete));
					if (MessageManager.getInstance().hasActiveChat(
							actionWithAccount, actionWithUser))
						menu.add(0, CONTEXT_MENU_CLOSE_CHAT_ID, 0,
								getResources().getText(R.string.close_chat));
					if (abstractContact.getStatusMode() == StatusMode.unsubscribed)
						menu.add(0, CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID, 0,
								getText(R.string.request_subscription));
				}
				if (PresenceManager.getInstance().hasSubscriptionRequest(
						actionWithAccount, actionWithUser)) {
					menu.add(0, CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID, 0,
							getResources()
									.getText(R.string.accept_subscription));
					menu.add(0, CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID, 0,
							getText(R.string.discard_subscription));
				}

				return;

			} else if (baseEntity instanceof GroupConfiguration) {
				// Group or account in contact list
				actionWithAccount = baseEntity.getAccount();
				actionWithGroup = baseEntity.getUser();
				actionWithUser = null;

				if (baseEntity instanceof AccountConfiguration) {
					actionWithGroup = null;
				} else {
					// Group
					menu.setHeaderTitle(com.digitalbuana.smiles.utils.StringUtils
							.replaceStringEquals(GroupManager.getInstance()
									.getGroupName(actionWithAccount,
											actionWithGroup)));
					if (actionWithGroup != GroupManager.ACTIVE_CHATS
							&& actionWithGroup != GroupManager.IS_ROOM) {
						menu.add(0, CONTEXT_MENU_GROUP_RENAME_ID, 0,
								getText(R.string.group_rename));
						if (actionWithGroup != GroupManager.NO_GROUP)
							menu.add(0, CONTEXT_MENU_GROUP_DELETE_ID, 0,
									getText(R.string.group_remove));
					}
				}
			} else {
				return;
			}
		} else {
			// Account panel
			actionWithAccount = (String) accountToggleAdapter.getItemForView(v);
			actionWithGroup = null;
			actionWithUser = null;
		}

		// Group or account
		if (actionWithGroup == null) {
			// Account
			menu.setHeaderTitle(com.digitalbuana.smiles.utils.StringUtils
					.replaceStringEquals(AccountManager.getInstance()
							.getVerboseName(actionWithAccount)));
			AccountItem accountItem = AccountManager.getInstance().getAccount(
					actionWithAccount);
			ConnectionState state = accountItem.getState();
			if (state == ConnectionState.waiting)
				menu.add(0, CONTEXT_MENU_ACCOUNT_RECONNECT_ID, 0,
						getText(R.string.account_reconnect));
			menu.add(0, CONTEXT_MENU_ACCOUNT_STATUS_ID, 0,
					getText(R.string.status_editor));
			menu.add(0, CONTEXT_MENU_ACCOUNT_EDITOR_ID, 0,
					getText(R.string.account_editor));
			if (state.isConnected()) {
				menu.add(0, CONTEXT_MENU_ACCOUNT_VCARD_ID, 0,
						getText(R.string.contact_viewer));
				menu.add(0, CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID, 0,
						getText(R.string.contact_add));
			}
		}

		if (actionWithGroup != null || SettingsManager.contactsShowAccounts()) {
			SubMenu mapMode = menu.addSubMenu(getResources().getText(
					R.string.show_offline_settings));
			mapMode.setHeaderTitle(R.string.show_offline_settings);
			MenuItem always = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID, 0, getResources()
							.getText(R.string.show_offline_always));
			MenuItem normal = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID, 0, getResources()
							.getText(R.string.show_offline_normal));
			MenuItem never = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID, 0, getResources()
							.getText(R.string.show_offline_never));
			mapMode.setGroupCheckable(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID, true,
					true);
			ShowOfflineMode showOfflineMode = GroupManager.getInstance()
					.getShowOfflineMode(
							actionWithAccount,
							actionWithGroup == null ? GroupManager.IS_ACCOUNT
									: actionWithGroup);
			if (showOfflineMode == ShowOfflineMode.always)
				always.setChecked(true);
			else if (showOfflineMode == ShowOfflineMode.normal)
				normal.setChecked(true);
			else if (showOfflineMode == ShowOfflineMode.never)
				never.setChecked(true);
			else
				throw new IllegalStateException();
		}

		new Thread("get contact list...") {
			public void run() {
				try {
					PhoneContactHelper.getContactList(HomeActivity.this);
				} finally {
					this.interrupt();
				}
			}
		}.start();

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		// Contact
		case CONTEXT_MENU_VIEW_CHAT_ID:
			MessageManager.getInstance().openChat(actionWithAccount,
					actionWithUser);
			startActivity(ChatViewActivity.createIntent(this,
					actionWithAccount, actionWithUser));
			return true;
		case CONTEXT_MENU_VIEW_CONTACT_ID:
			startActivity(ProfileDetailActivity.createIntent(this,
					actionWithAccount, actionWithUser));
			return true;
		case CONTEXT_MENU_EDIT_CONTACT_ID:
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_DELETE_CONTACT_ID:
			showDialog(DIALOG_DELETE_CONTACT_ID);
			return true;
		case CONTEXT_MENU_EDIT_ROOM_ID:
			startActivity(MUCEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_JOIN_ROOM_ID:
			MUCManager.getInstance().joinRoom(actionWithAccount,
					actionWithUser, true);
			return true;
		case CONTEXT_MENU_LEAVE_ROOM_ID:
			MUCManager.getInstance().leaveRoom(actionWithAccount,
					actionWithUser);
			MessageManager.getInstance().closeChat(actionWithAccount,
					actionWithUser);
			NotificationManager.getInstance().removeMessageNotification(
					actionWithAccount, actionWithUser);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_CLOSE_CHAT_ID:
			MessageManager.getInstance().clearHistory(actionWithAccount,
					actionWithUser);
			MessageManager.getInstance().closeChat(actionWithAccount,
					actionWithUser);
			NotificationManager.getInstance().removeMessageNotification(
					actionWithAccount, actionWithUser);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().requestSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().acceptSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().discardSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;

			// Group
		case CONTEXT_MENU_GROUP_RENAME_ID:
			showDialog(DIALOG_RENAME_GROUP_ID);
			return true;
		case CONTEXT_MENU_GROUP_DELETE_ID:
			showDialog(DIALOG_DELETE_GROUP_ID);
			return true;

			// Account
		case CONTEXT_MENU_ACCOUNT_RECONNECT_ID:
			if (AccountManager.getInstance().getAccount(actionWithAccount)
					.updateConnection(true))
				AccountManager.getInstance()
						.onAccountChanged(actionWithAccount);
			return true;
		case CONTEXT_MENU_ACCOUNT_VCARD_ID:
			String user = AccountManager.getInstance()
					.getAccount(actionWithAccount).getRealJid();
			if (user == null)
				Application.getInstance().onError(R.string.NOT_CONNECTED);
			else {
				startActivity(ContactViewer.createIntent(this,
						actionWithAccount, user));
			}
			return true;
		case CONTEXT_MENU_ACCOUNT_EDITOR_ID:
			startActivity(AccountEditor.createIntent(this, actionWithAccount));
			return true;
		case CONTEXT_MENU_ACCOUNT_STATUS_ID:
			startActivity(StatusEditor.createIntent(this, actionWithAccount));
			return true;
		case CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID:
			startActivity(ContactAdd.createIntent(this, actionWithAccount));
			return true;
			// Groups or account
		case CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.always);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.normal);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.never);
			contactListAdapter.onChange();
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_DELETE_CONTACT_ID:
			int resource;
			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser))
				resource = R.string.muc_delete_confirm;
			else
				resource = R.string.contact_delete_confirm;
			return new ConfirmDialogBuilder(this, DIALOG_DELETE_CONTACT_ID,
					this)
					.setMessage(
							com.digitalbuana.smiles.utils.StringUtils
									.replaceStringEquals(getString(
											resource,
											RosterManager.getInstance()
													.getName(actionWithAccount,
															actionWithUser),
											AccountManager.getInstance()
													.getVerboseName(
															actionWithAccount))))
					.create();
		case DIALOG_DELETE_GROUP_ID:
			return new ConfirmDialogBuilder(this, DIALOG_DELETE_GROUP_ID, this)
					.setMessage(
							getString(R.string.group_remove_confirm,
									actionWithGroup)).create();
		case DIALOG_RENAME_GROUP_ID:
			return new GroupRenameDialogBuilder(this, DIALOG_RENAME_GROUP_ID,
					this, actionWithGroup == GroupManager.NO_GROUP ? ""
							: actionWithGroup).create();
		case DIALOG_START_AT_BOOT_ID:
			return new ConfirmDialogBuilder(this, DIALOG_START_AT_BOOT_ID, this)
					.setMessage(getString(R.string.start_at_boot_suggest))
					.create();
		case DIALOG_CONTACT_INTEGRATION_ID:
			return new ConfirmDialogBuilder(this,
					DIALOG_CONTACT_INTEGRATION_ID, this).setMessage(
					getString(R.string.contact_integration_suggest)).create();
		case DIALOG_OPEN_WITH_ACCOUNT_ID:
			return new AccountChooseDialogBuilder(this,
					DIALOG_OPEN_WITH_ACCOUNT_ID, this, openDialogUser).create();
		case DIALOG_CLOSE_APPLICATION_ID:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog
					.setMessage(getString(R.string.application_state_closing));
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			progressDialog.setIndeterminate(true);
			return progressDialog;
		default:
			return null;
		}
	}

	/**
	 * Show search dialog.
	 */
	private void search() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null)
			inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
					0);
	}

	private void rebuildAccountToggler() {

		AccountItem activeAccount = AccountManager.getInstance()
				.getActiveAccount();

		if (activeAccount != null) {

			leftMenuUser
					.setText(activeAccount.getAccount().replace("%40", "@"));
			settingsEditor.putString(AppConstants.USERNAME_KEY,
					activeAccount.getAccount());
			settingsEditor.commit();

			leftMenuStatus
					.setText(
							Emoticons.getSmiledText(this,
									SettingsManager.statusText()),
							BufferType.SPANNABLE);

			File fileAvatar = new File(
					Environment.getExternalStorageDirectory() + File.separator
							+ "smilesAvatar.jpg");
			if (fileAvatar.exists()) {
				Bitmap myBitmap = BitmapFactory.decodeFile(fileAvatar
						.getAbsolutePath());
				leftMenuAvatar.setImageBitmap(myBitmap);
			} else {
				Drawable serverAvatar = AvatarManager.getInstance()
						.getAccountAvatar(activeAccount.getAccount());
				leftMenuAvatar.setImageDrawable(serverAvatar);
			}

			/*
			 * new Thread("get bookmark list...") { public void run() { try {
			 * BookmarkHelper bh = new BookmarkHelper(); bh.getBookmark(); }
			 * catch (NullPointerException e1) { Bookmarks bookmarks = new
			 * Bookmarks(); List<BookmarkedConference> bl = bookmarks
			 * .getBookmarkedConferences(); Log.e(TAG, "bookmarked list : " +
			 * bl.size()); if (bl.size() > 0) { for (BookmarkedConference bi :
			 * bl) { Log.e(TAG, "bookmark jid : " + bi.getJid()); } } } catch
			 * (XMPPException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } finally { this.interrupt(); }
			 * 
			 * } }.start();
			 */

			/*
			 * new Thread("get bookmark list") { public void run() { try {
			 * BookmarkHelper bh = new BookmarkHelper();
			 * Collection<BookmarkedConference> bookmarkList = bh
			 * .getBookmark(); Log.e(TAG, "bookmarkList ..... " +
			 * bookmarkList.size()); for (BookmarkedConference bc :
			 * bookmarkList) { Log.e(TAG, "BookmarkedConference :: " +
			 * bc.getJid()); }
			 * 
			 * } catch (NullPointerException e1) { Log.e(TAG,
			 * "null while get bookmark list : " + e1.getMessage()); } catch
			 * (XMPPException e) { // TODO Auto-generated catch block //
			 * e.printStackTrace(); Log.e(TAG, e.getMessage()); } finally {
			 * this.interrupt(); } } }.start();
			 */

		}
		if (SettingsManager.contactsShowPanel()
				&& AccountManager.getInstance().getAccounts().size() >= 1) {
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			homeContainer.setVisibility(View.VISIBLE);
		} else {
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			homeContainer.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterListeners();
	}

	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		if (isApplicationBroughtToBackground()) {
			finish();
		}
	}

	private boolean isApplicationBroughtToBackground() {
		android.app.ActivityManager am = (android.app.ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	private void unregisterListeners() {

		// Application.getInstance().removeUIListener(OnAccountChangedListener.class,
		// this);
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);
		Application.getInstance().removeUIListener(OnChatChangedListener.class,
				this);

		Application.getInstance().removeUIListener(
				OnFriendsUpdateChangeListener.class, this);
		Application.getInstance().removeUIListener(
				OnVisitorChangeListener.class, this);

		Application.getInstance().removeUIListener(
				OnAdminUpdateChangeListener.class, this);

		contactListAdapter.removeRefreshRequests();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isHomeScreenShowing = false;
		destroyService();

	}

	@Override
	protected void resettingView() {
		super.resettingView();
		if (homeContainer.getVisibility() == View.VISIBLE) {
			int widthku = ViewUtilities.GetInstance().convertDPtoPX(320);
			if (viewWidth <= widthku) {
				slidingMenu.setBehindWidth(viewWidth);
			}
		}
		if (startContainer.getVisibility() == View.VISIBLE) {

			String tmpPassword = mSettings.getString(AppConstants.PASSWORD_KEY,
					null);

			if (tmpUserName != null && tmpPassword != null
					&& !Application.getInstance().isClosing()
					&& AccountManager.getInstance().getAccounts().size() < 1
					&& !SettingsManager.contactsShowPanel()) {
				startActivity(LoginActivity.createIntent(this));
				startContainer.setVisibility(View.GONE);
			} else {
				int tempHeight = viewHeight - 90;
				imgTopLogo.getLayoutParams().height = (tempHeight * 17) / 100;
				imgCewe.getLayoutParams().height = (tempHeight * 66) / 100;
				if (ViewUtilities.GetInstance().isLandscape()) {
					imgCewe.setImageResource(R.drawable.img_start_cewe_landscape);
				} else {
					imgCewe.setImageResource(R.drawable.img_start_cewe);
				}
			}
		}
	}

	private void destroyService() {

		aq.clear();
		aq.ajaxCancel();

		AQUtility.cleanCacheAsync(context);

		AccountItem activeAccount = AccountManager.getInstance()
				.getActiveAccount();
		if (activeAccount != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						stopService(SmilesService.createIntent(context));
					} catch (RuntimeException e1) {
						Log.e(TAG, e1.getMessage());
					}
				}
			});
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {

			destroyService();

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (slidingMenu.isMenuShowing() || slidingMenu.isSecondaryMenuShowing()) {
			slidingMenu.showContent();
		} else {
			finish();
			// moveTaskToBack(true);
		}
	}

	public static Intent createPersistentIntent(Context context) {
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, HomeActivity.class);
	}

	public static Intent createRoomInviteIntent(Context context,
			String account, String room) {
		Intent intent = new EntityIntentBuilder(context, HomeActivity.class)
				.setAccount(account).setUser(room).build();
		intent.setAction(ACTION_ROOM_INVITE);
		return intent;
	}

	@Override
	public void onCancel(DialogBuilder dialogBuilder) {
	}

	@Override
	public void onDecline(DialogBuilder dialogBuilder) {
	}

	private void doPostUnFriends() {
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager
				.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("rel_code", StringUtils
				.replaceStringEquals(actionWithUser)));
		doPostAsync(context, AppConstants.APIDeleteFriends, postDatanya, null,
				true);
	}

	private void delBookmark(String value) {
		Map<String, String> parms = new HashMap<String, String>();
		parms.put("r", AppConstants.ofSecret);
		parms.put("bv", value);
		parms.put("bu", actionWithAccount);
		aq.ajax(AppConstants.APIDelBookmark, parms, String.class,
				new AjaxCallback<String>() {
					@Override
					public void callback(String url, String object,
							AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, object, status);
						if (object != null) {
							Log.i(TAG, object);
						}
					}
				});
	}

	@Override
	public void onAccept(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_DELETE_CONTACT_ID:
			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser)) {
				delBookmark(actionWithUser);
				MUCManager.getInstance().removeRoom(actionWithAccount,
						actionWithUser);
				MessageManager.getInstance().closeChat(actionWithAccount,
						actionWithUser);
				NotificationManager.getInstance().removeMessageNotification(
						actionWithAccount, actionWithUser);
			} else {
				try {
					RosterManager.getInstance().removeContact(
							actionWithAccount, actionWithUser);
					FriendsManager.getInstance().getFriendsListManager()
							.removeFriendsJID(actionWithUser);
				} catch (NetworkException e) {
					Application.getInstance().onError(e);
					Log.e(TAG, e.getMessage());
				} finally {
					doPostUnFriends();
				}
			}
			break;
		case DIALOG_DELETE_GROUP_ID:
			try {
				if (actionWithAccount == GroupManager.NO_ACCOUNT)
					RosterManager.getInstance().removeGroup(actionWithGroup);
				else
					RosterManager.getInstance().removeGroup(actionWithAccount,
							actionWithGroup);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			break;
		case DIALOG_RENAME_GROUP_ID:
			String name = ((GroupRenameDialogBuilder) dialogBuilder).getName();
			String source = actionWithGroup == GroupManager.NO_GROUP ? null
					: actionWithGroup;
			try {
				if (actionWithAccount == GroupManager.NO_ACCOUNT)
					RosterManager.getInstance().renameGroup(source, name);
				else
					RosterManager.getInstance().renameGroup(actionWithAccount,
							source, name);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			break;
		case DIALOG_START_AT_BOOT_ID:
			SettingsManager.setStartAtBootSuggested();
			SettingsManager.setConnectionStartAtBoot(true);
			break;
		/*
		 * case DIALOG_CONTACT_INTEGRATION_ID:
		 * SettingsManager.setContactIntegrationSuggested(); for (String account
		 * : AccountManager.getInstance().getAllAccounts())
		 * AccountManager.getInstance().setSyncable(account, true); break;
		 */
		case DIALOG_OPEN_WITH_ACCOUNT_ID:
			BaseEntity baseEntity = new BaseEntity(
					((AccountChooseDialogBuilder) dialogBuilder).getSelected(),
					openDialogUser);
			openChat(baseEntity, openDialogText);
			break;
		}
	}

	private void openChat(BaseEntity baseEntity, String text) {
		if (text == null)
			startActivity(ChatViewActivity.createSendIntent(this,
					baseEntity.getAccount(), baseEntity.getUser(), null));
		else
			startActivity(ChatViewActivity.createSendIntent(this,
					baseEntity.getAccount(), baseEntity.getUser(), text));
		finish();
	}

	@Override
	public boolean onLongClick(View arg0) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == leftMenuList) {
			if (((LeftMenuAdapter.ViewHolder) view.getTag()).isSelectable()) {
				int pos = ((LeftMenuAdapter.ViewHolder) view.getTag())
						.getIndex();
				switch (pos) {
				case 1:
					startActivity(AddFriendsActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 2:
					startActivity(FriendsRequestActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 3:
					startActivity(CreateGroupActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 4:
					startActivity(BlockedFriendsActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 5:
					startActivity(FriendsUpdateActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 6:
					startActivity(VisitorActivity.createClearTopIntent(this,
							actionWithAccount, actionWithUser));
					slidingMenu.showContent();
					break;
				case 7:
					startActivity(NotificationActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 8:
					startActivity(RoomsActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 9:
					startActivity(BroadcastMesssageActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 10:
					startActivity(SettingsActivity.createIntent(this));
					slidingMenu.showContent();
					break;
				case 11:
					slidingMenu.showContent();
					search();
					break;
				case 12:
					doLogout();
					break;
				}
				// LeftMenuAdapter.indexLeftMenuPos= position;
				// eftMenuAdapter.notifyDataSetChanged();
			}
		} else {
			Object object = parent.getAdapter().getItem(position);
			if (object == null) {
				// Account toggler
			} else if (object instanceof AbstractContact) {
				AbstractContact abstractContact = (AbstractContact) object;
				if (ACTION_ROOM_INVITE.equals(action)) {
					action = null;
					Intent intent = getIntent();
					String account = getRoomInviteAccount(intent);
					String user = getRoomInviteUser(intent);
					if (account != null && user != null)
						try {
							MUCManager.getInstance().invite(account, user,
									abstractContact.getUser());
						} catch (NetworkException e) {
							Application.getInstance().onError(e);
						}
					finish();
				} else if (Intent.ACTION_SEND.equals(action)) {
					action = null;
					startActivity(ChatViewActivity.createSendIntent(this,
							abstractContact.getAccount(),
							abstractContact.getUser(), sendText));
					finish();
				} else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
					Intent intent = new Intent();
					intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
							ChatViewActivity.createClearTopIntent(this,
									abstractContact.getAccount(),
									abstractContact.getUser()));
					intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
							abstractContact.getName());
					Bitmap bitmap;
					if (MUCManager.getInstance().hasRoom(
							abstractContact.getAccount(),
							abstractContact.getUser()))
						bitmap = AvatarManager.getInstance().getRoomBitmap(
								abstractContact.getUser());
					else
						bitmap = AvatarManager.getInstance().getUserBitmap(
								abstractContact.getUser());
					intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, AvatarManager
							.getInstance().createShortcutBitmap(bitmap));
					setResult(RESULT_OK, intent);
					finish();
				} else {
					startActivity(ChatViewActivity.createIntent(this,
							abstractContact.getAccount(),
							abstractContact.getUser()));
				}
			} else if (object instanceof GroupConfiguration) {
				GroupConfiguration groupConfiguration = (GroupConfiguration) object;
				contactListAdapter.setExpanded(groupConfiguration.getAccount(),
						groupConfiguration.getUser(),
						!groupConfiguration.isExpanded());
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (SettingsManager.contactsShowPanel()) {
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.showContent(true);
			} else if (slidingMenu.isSecondaryMenuShowing()) {
				slidingMenu.showMenu(true);
			} else {
				slidingMenu.showMenu(true);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void doLogout() {
		SmilesConfirmDialog dialog = new SmilesConfirmDialog(context,
				new OnSmilesDialogClose() {
					@Override
					public void onSmilesDialogClose(boolean isConfirm) {
						if (isConfirm) {
							settingsEditor.clear();
							settingsEditor.commit();
							doPostLogout();
						}
					}
				}, "LOG OUT", "Are You Sure to Log Out ? \n "/*
															 * + AccountManager.
															 * getInstance
															 * ().getAccountKu()
															 */);
		dialog.show();
	}

	private void doPostLogout() {

		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "smilesAvatar.jpg");
		boolean deleted = file.delete();
		if (deleted)
			Log.i(TAG, "avatar successfully deleted");
		else
			Log.i(TAG, "avatar failed deleted");

		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("username", AccountManager
				.getInstance().getAccountKu()));
		doPostAsync(context, AppConstants.APILogOut, postData, null, true);
	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if (result.length() >= 10) {
			if (urlKu == AppConstants.APILogOut) {
				successLogOut();
			}
			if (urlKu == AppConstants.APIConfiguration) {
				AppConfiguration.getInstance().setTempJSON(result);
				if (!AppConfiguration.getInstance().isActive()) {
					Application.getInstance().closeApplication();
					ActivityManager.getInstance().finishAll();
				}
			}
		}
	}

	private void successLogOut() {
		AccountManager.getInstance().doLogout();
		slidingMenu.showContent();
		GCMRegistrar.unregister(context);
		finish();
	}

	private void resetLeftHeaderNotification() {
		homeLeftMenuIcon.setImageResource(R.drawable.img_title_left_btn);
		if (blinkAnimation.isInitialized()) {
			homeLeftMenuIcon.clearAnimation();
			blinkAnimation.cancel();
			blinkAnimation.reset();
		}
		settingsEditor.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, false);
		settingsEditor.commit();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.homeTitleBtnShowMenu:
			resetLeftHeaderNotification();
			slidingMenu.showMenu(true);
			break;
		case R.id.homeTitleBtnShowEntertaint:
			slidingMenu.showSecondaryMenu(true);
			break;
		case R.id.homeLeftMenuTitle:
			startActivity(ProfileSettingsActivity.createIntent(this));
			break;
		case R.id.homeStartBtnLogin:
			startActivity(LoginActivity.createIntent(this));
			break;
		case R.id.homeStartBtnRegister:
			startActivity(RegisterActivity.createIntent(this));
			break;
		case R.id.homeRightMenuShopContainer:
			startActivity(StickerSelectActivity.createIntent(this));
			break;
		case R.id.pitaKiriImageView:
			slidingMenu.showContent();
			break;
		case R.id.pitaKananImageView:
			slidingMenu.showContent();
			break;
		}
	}

	@Override
	public void onChatChanged(String account, String user, boolean incoming) {
		contactListAdapter.refreshRequest();
	}

	/*
	 * @Override public void onAccountsChanged(Collection<String> accounts) {
	 * contactListAdapter.refreshRequest(); rebuildAccountToggler(); }
	 */

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {

		contactListAdapter.refreshRequest();

	}

	private static String getRoomInviteAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getRoomInviteUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

	/*
	 * @Override public void onVCardReceived(String account, String bareAddress,
	 * VCard vCard) { // TODO Auto-generated method stub
	 * contactListAdapter.refreshRequest(); }
	 * 
	 * 
	 * @Override public void onVCardFailed(String account, String bareAddress) {
	 * // TODO Auto-generated method stub
	 * 
	 * }
	 */

	@Override
	public void onReqFriendsListener(int type, String jid) {
		// TODO Auto-generated method stub
		setLeftNotification();
	}

	@Override
	public void onFriendsUpdateChanged(ArrayList<FriendsModel> listVisitor) {
		// TODO Auto-generated method stub
		setLeftNotification();
	}

	@Override
	public void onVisitorChanged(ArrayList<FriendsModel> listVisitor) {
		// TODO Auto-generated method stub
		setLeftNotification();
	}

	@Override
	public void onAdminUpdateChanged(ArrayList<AdminModel> listVisitor) {
		// TODO Auto-generated method stub
		setLeftNotification();
	}

	private void setLeftNotification() {
		homeLeftMenuIcon
				.setImageResource(R.drawable.img_title_left_btn_notification);
		homeLeftMenuIcon.startAnimation(blinkAnimation);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		slidingMenu.setBehindWidth(ScreenHelper.getScreenWidth(this));
		rightMenuInitiator();
	}

	/**
	 * this method used for sso if the GCM failed to push, and apps try to
	 * compare [regid] when [regid] "beda", force to logout :D
	 * */
	private void compareRegId() {

		AccountItem activeAccount = AccountManager.getInstance()
				.getActiveAccount();

		if (activeAccount != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", tmpUserName);
			params.put("targetname", tmpUserName);
			aq.ajax(AppConstants.APIGetProfile, params, JSONObject.class,
					new AjaxCallback<JSONObject>() {
						@Override
						public void callback(String url, JSONObject object,
								AjaxStatus status) {
							// TODO Auto-generated method stub
							super.callback(url, object, status);
							if (object != null) {
								try {
									JSONObject data = object
											.getJSONObject("DATA");
									String currentRegId = data
											.getString("regid");
									String currentImei = data.getString("imei");
									if (!currentRegId.trim().equals(regId)
											&& !currentImei.trim().equals(
													imei.trim())) {
										killTheApps();
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									// e.printStackTrace();
									// Log.e(TAG, e.getMessage());
									if (e.getMessage().equals(
											"No value for DATA")) {
										killTheApps();
									}
								}
							}
						}
					});
		}
	}

	private void killTheApps() {

		String actionWith = mSettings
				.getString(AppConstants.USERNAME_KEY, null);

		if (actionWith != null)
			AccountManager.getInstance().removeAccount(actionWith);

		settingsEditor.putString(AppConstants.USERNAME_KEY, null);
		settingsEditor.putString(AppConstants.PASSWORD_KEY, null);

		settingsEditor.clear();
		settingsEditor.commit();

		AccountManager.getInstance().doLogout();
		finish();
	}
}
