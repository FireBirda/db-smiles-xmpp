package com.digitalbuana.smiles.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smackx.ChatState;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.EmoticonPagerAdapterRes;
import com.digitalbuana.smiles.adapter.StickerPagerAdapter;
import com.digitalbuana.smiles.adapter.StickerPagerAdapter.OnGridPagerClickListener;
import com.digitalbuana.smiles.adapter.StickerPagerAdapterRes;
import com.digitalbuana.smiles.adapter.chat.ChatMessageAdapter;
import com.digitalbuana.smiles.adapter.chat.MenuPopAdapter;
import com.digitalbuana.smiles.awan.activity.GroupInviteActivity;
import com.digitalbuana.smiles.data.ActivityManager;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.StickerManager;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.OnAccountChangedListener;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.extension.archive.MessageArchiveManager;
import com.digitalbuana.smiles.data.extension.attention.AttentionManager;
import com.digitalbuana.smiles.data.extension.cs.ChatStateManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.message.MessageItem;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.OnChatChangedListener;
import com.digitalbuana.smiles.data.notification.NotificationManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.OnContactChangedListener;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.ui.ChatEditor;
import com.digitalbuana.smiles.ui.ContactEditor;
import com.digitalbuana.smiles.ui.OccupantList;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

@SuppressLint("NewApi")
public class ChatViewActivity extends ManagedActivity implements
		View.OnClickListener, View.OnKeyListener, OnContactChangedListener,
		OnAccountChangedListener, OnEditorActionListener,
		OnGridPagerClickListener, LocationListener, OnTouchListener,
		OnChatChangedListener, OnPageChangeListener, OnItemClickListener {

	private String TAG = getClass().getSimpleName();

	private static final String ACTION_ATTENTION = "com.digitalbuana.smiles.data.ATTENTION";
	private static final int MINIMUM_MESSAGES_TO_LOAD = 10;
	private static final String SAVED_ACCOUNT = "com.digitalbuana.smiles.ui.ChatViewer.SAVED_ACCOUNT";
	private static final String SAVED_USER = "com.digitalbuana.smiles.ui.ChatViewer.SAVED_USER";
	// private static final String SAVED_EXIT_ON_SEND =
	// "com.digitalbuana.smiles.ui.ChatViewer.EXIT_ON_SEND";

	private final static int RESULT_CODE_STICKER_SELECT = 11048899;
	private final static int RESULT_CODE_UPLOADFILE = 11049999;

	private String actionWithAccount;
	private String actionWithUser;
	private View actionWithView;
	// private MessageItem actionWithMessage;
	private AbstractContact abstractContact;
	// private boolean isVisible;

	// Title
	private ImageView avatar;
	private TextView txtUser;
	private TextView txtStatus;
	private FrameLayout btnBack;

	// ListView
	private ListView listChat;
	private ChatMessageAdapter chatMessageAdapter;

	// BottomView
	private EditText editMessage;
	private FrameLayout btnSend;
	private FrameLayout btnShowSticker;

	// Sticker
	private int stickerHeight = 0;
	private int indexSelect = 0;
	private boolean isStickerShow = false;
	private boolean isStickerShowDetail = false;
	private StickerPagerAdapter pagerStickerAdapter;
	private ArrayList<String> listStickerForAdapter;
	private StickerPagerAdapterRes adapterAttachment;
	private FrameLayout stickerContainer;
	private ViewPager pagerStricker;
	private FrameLayout btnAttachment;
	private FrameLayout btnEmotion;
	private FrameLayout btnSticker;
	private FrameLayout btnIkonia;
	private FrameLayout btnAdd;
	private TextView txtAttachemnt;
	private TextView txtEmotion;
	private TextView txtSticker;
	private TextView txtIkonia;
	// private TextView txtAdd;

	// Location
	private LocationManager locationManager;
	private String provider;

	// Popup
	private FrameLayout btnPop;
	private boolean isPopShow = false;
	private FrameLayout popContainer;
	private ListView popList;
	private MenuPopAdapter popAdapter;

	private AQuery aq;
	// private int rowVisisbleOnList = 0;
	private boolean isWakeupPushSendAlready = false;

	public static boolean isChatScreenShowing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (isFinishing())
			return;

		isChatScreenShowing = true;

		aq = new AQuery(this);

		Intent intent = getIntent();
		String account = getAccount(intent);
		String user = getUser(intent);

		if (account == null || user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			finish();
			return;
		}

		if (hasAttention(intent))
			AttentionManager.getInstance().removeAccountNotifications(account,
					user);

		actionWithAccount = null;
		actionWithUser = null;
		actionWithView = null;
		// actionWithMessage = null;

		setContentView(R.layout.activity_chat);

		// Casting
		rootView = (FrameLayout) findViewById(R.id.chatViewRootView);
		FontUtils.setRobotoFont(context, rootView);

		// Tiitle
		avatar = (ImageView) findViewById(R.id.chatActivityAvatar);
		txtUser = (TextView) findViewById(R.id.chatActivityTxtName);
		txtStatus = (TextView) findViewById(R.id.chatActivityTxtStatus);
		btnBack = (FrameLayout) findViewById(R.id.chatActivityBtnBack);

		// ListView
		listChat = (ListView) findViewById(R.id.chatActivityListView);

		// BottomVIew
		btnSend = (FrameLayout) findViewById(R.id.chatActivityBtnSend);
		editMessage = (EditText) findViewById(R.id.chatActivityEditMessage);
		btnShowSticker = (FrameLayout) findViewById(R.id.chatActivityBtnShowSticker);

		// Sticker
		stickerContainer = (FrameLayout) findViewById(R.id.chatViewContainerSticker);
		pagerStricker = (ViewPager) findViewById(R.id.chatViewPagerSticker);

		btnAttachment = (FrameLayout) findViewById(R.id.chatViewBtnAttachemt);
		btnEmotion = (FrameLayout) findViewById(R.id.chatViewBtnEmotion);
		btnSticker = (FrameLayout) findViewById(R.id.chatViewBtnSticker);
		btnIkonia = (FrameLayout) findViewById(R.id.chatViewBtnIkonia);
		btnAdd = (FrameLayout) findViewById(R.id.chatViewBtnAddSticker);

		txtAttachemnt = (TextView) findViewById(R.id.chatViewTxtAtachment);
		txtEmotion = (TextView) findViewById(R.id.chatViewTxtEmotion);
		txtSticker = (TextView) findViewById(R.id.chatViewTxtSticker);
		txtIkonia = (TextView) findViewById(R.id.chatViewTxtIkonia);
		// txtAdd = (TextView) findViewById(R.id.chatViewTxtAddSticker);

		// Popup
		btnPop = (FrameLayout) findViewById(R.id.chatActivityBtnMenu);
		popContainer = (FrameLayout) findViewById(R.id.chatActivityPopContainer);
		popList = (ListView) findViewById(R.id.chatActivityPopList);

		btnBack.setOnClickListener(this);
		btnShowSticker.setOnClickListener(this);
		btnPop.setOnClickListener(this);

		// StickerShow
		btnAttachment.setOnClickListener(this);
		btnEmotion.setOnClickListener(this);
		btnSticker.setOnClickListener(this);
		btnIkonia.setOnClickListener(this);
		btnAdd.setOnClickListener(this);
		pagerStricker.setOnPageChangeListener(this);
		editMessage.setOnTouchListener(this);

		/*
		 * editMessage.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
		 * android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
		 * android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		 */

		FontUtils.setRobotoFont(context, editMessage);

		// SettingListener
		btnSend.setOnClickListener(this);
		editMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				ChatStateManager.getInstance().onComposing(actionWithAccount,
						actionWithUser, s);
			}
		});

		abstractContact = RosterManager.getInstance().getBestContact(account,
				user);
		avatar.setImageDrawable(abstractContact.getAvatar());

		chatMessageAdapter = new ChatMessageAdapter(this);
		chatMessageAdapter.setChat(account, user);

		listChat.setAdapter(chatMessageAdapter);

		// PopMenu
		popAdapter = new MenuPopAdapter(context);
		popList.setAdapter(popAdapter);
		popAdapter.setChangeMUC(MUCManager.getInstance().hasRoom(
				actionWithAccount, actionWithUser));
		popList.setOnItemClickListener(this);

		// Sticker
		listStickerForAdapter = new ArrayList<String>();
		listStickerForAdapter.clear();
		for (int i = 0; i < StickerManager.getInstance().getStickerListAll()
				.size(); i++) {
			if (StickerManager.getInstance().getStickerListAll().get(i)
					.isDownlaoded()) {
				listStickerForAdapter.add(StickerManager.getInstance()
						.getStickerListAll().get(i).getThumbnail());
			}
		}
		stickerHeight = ViewUtilities.GetInstance().convertDPtoPX(57);
		pagerStickerAdapter = new StickerPagerAdapter(context,
				listStickerForAdapter, this, 4, 3, stickerHeight);
		// int numOfGridC=4*3;
		// float asalC =
		// ((float)listStickerForAdapter.size()/(float)numOfGridC)+0.45f;
		// int numOfPageC = (int)Math.round(asalC);
		adapterAttachment = new StickerPagerAdapterRes(context,
				AppConstants.listAttachment, this, 4, 3, stickerHeight);
		pagerStricker.setAdapter(adapterAttachment);

		if (savedInstanceState != null) {
			actionWithAccount = savedInstanceState.getString(SAVED_ACCOUNT);
			actionWithUser = savedInstanceState.getString(SAVED_USER);
		}
		if (actionWithAccount == null)
			actionWithAccount = account;
		if (actionWithUser == null)
			actionWithUser = user;

		scrollMyListViewToBottom();
		locationInitiator();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isChatScreenShowing = false;
		// MessageHelper.getArchieveMessage(actionWithAccount, actionWithUser);
	}

	private static boolean hasAttention(Intent intent) {
		return ACTION_ATTENTION.equals(intent.getAction());
	}

	private void locationInitiator() {
		// Location
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		// Initialize the location fields
		if (location != null) {
			AppConfiguration.getInstance()
					.setLongitude(location.getLongitude());
			AppConfiguration.getInstance().setLatitude(location.getLatitude());
			onLocationChanged(location);
		} else {
			// Intent myIntent = new
			// Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			// context.startActivity(myIntent);
		}

		// listChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		silentUpdateLocation();
	}

	private void refreshView() {
		if (isStickerShow || isStickerShowDetail) {
			hideSticker();
		}
		if (isPopShow) {
			hidePop();
		}
		hidingKeyboard();
		chatMessageAdapter.setChat(actionWithAccount, actionWithUser);
		abstractContact = RosterManager.getInstance().getBestContact(
				actionWithAccount, actionWithUser);
		avatar.setImageDrawable(abstractContact.getAvatar());
		changeStatus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
		Application.getInstance().addUIListener(OnChatChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnAccountChangedListener.class,
				this);
		popAdapter.setChangeMUC(MUCManager.getInstance().hasRoom(
				actionWithAccount, actionWithUser));
		Intent intent = getIntent();
		// if (actionWithView != null)
		// chatMessageAdapter.onChange();

		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			String additional = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (additional != null) {
				intent.removeExtra(Intent.EXTRA_TEXT);
				if (actionWithView != null) {
					editMessage.setText(additional);
				}
			}
		}

		MessageManager.getInstance().requestToLoadLocalHistory(
				actionWithAccount, actionWithUser);

		chatMessageAdapter.setChat(actionWithAccount, actionWithUser);
		// chatMessageAdapter.setRequestFromDatabase(true);
		chatMessageAdapter.onChange();

		// isVisible = true;
		changeStatus();

		MessageManager.getInstance().requestToLoadLocalHistory(
				actionWithAccount, actionWithUser);

		MessageManager.getInstance().setVisibleChat(actionWithAccount,
				actionWithUser);

		NotificationManager.getInstance().removeMessageNotification(
				actionWithAccount, actionWithUser);

		listChat.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				MessageItem mi = (MessageItem) chatMessageAdapter.getItem(arg2);
				int msgType = chatMessageAdapter.getItemViewTypeMessage(mi);
				if (msgType == ChatMessageAdapter.TYPE_MESSAGE) {
					showMessageDialogOption(mi);
				}
				return true;
			}
		});

	}

	private String[] sentOption = { "Copy", "Delete", "Re-Send" };
	private String[] sentOption2 = { "Copy", "Delete" };
	private String[] incomingOption = { "Copy", "Delete" };

	private void showMessageDialogOption(final MessageItem mi) {

		final boolean isIncoming = mi.isIncoming();
		final boolean isDelivered = mi.isDelivered();
		final String text = mi.getText();
		String[] finalOption;

		if (isIncoming) { // incoming message
			finalOption = incomingOption;
		} else { // sent message
			if (isDelivered) {
				finalOption = sentOption2;
			} else {
				finalOption = sentOption;
			}
		}

		final Dialog dlg = new Dialog(context);
		dlg.setCancelable(true);
		dlg.setTitle("Option:");
		dlg.setContentView(R.layout.list_general);
		ListView gelist = (ListView) dlg.findViewById(R.id.gelist);

		gelist.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, finalOption));

		gelist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				if (isIncoming) { // incoming message
					switch (position) {
					case 0: // copy
						coptTextToClipboard("SMiles", text);
						break;
					case 1: // delete
						MessageManager.getInstance().removeMessage(mi);
						onChangeListener();
						break;
					}
				} else { // sent message
					if (isDelivered) {
						switch (position) {
						case 0: // copy
							coptTextToClipboard("SMiles", text);
							break;
						case 1: // delete
							MessageManager.getInstance().removeMessage(mi);
							onChangeListener();
							break;
						}
					} else {
						switch (position) {
						case 0: // copy
							coptTextToClipboard("SMiles", text);
							break;
						case 1: // delete
							MessageManager.getInstance().removeMessage(mi);
							onChangeListener();
							break;
						case 2: // re-send
							MessageManager.getInstance().removeMessage(mi);
							onChangeListener();
							sendMessage(text);
							break;
						}
					}
				}
				dlg.dismiss();
			}
		});
		dlg.show();
	}

	@SuppressLint("NewApi")
	private void coptTextToClipboard(String label, String message) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(message);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText(label, message);
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(context, "text copied to clipboard", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT, actionWithAccount);
		outState.putString(SAVED_USER, actionWithUser);
	}

	private void showFileUpload(int type) {
		Intent i = UploadFileActivity.createIntent(this);
		i.putExtra("type", type);
		i.putExtra("user", actionWithUser);
		startActivityForResult(i, RESULT_CODE_UPLOADFILE);
		if (isStickerShow) {
			hideSticker();
		}
	}

	private void toggleSelect() {
		isStickerShowDetail = false;
		btnAttachment.setBackgroundResource(R.color.AbuTuaVeryLow);
		btnEmotion.setBackgroundResource(R.color.AbuTuaVeryLow);
		btnSticker.setBackgroundResource(R.color.AbuTuaVeryLow);
		btnIkonia.setBackgroundResource(R.color.AbuTuaVeryLow);
		txtAttachemnt.setTextColor(getResources().getColor(
				R.color.TextSecondaryLight));
		txtEmotion.setTextColor(getResources().getColor(
				R.color.TextSecondaryLight));
		txtSticker.setTextColor(getResources().getColor(
				R.color.TextSecondaryLight));
		txtIkonia.setTextColor(getResources().getColor(
				R.color.TextSecondaryLight));
		if (indexSelect == 0) {
			btnAttachment.setBackgroundResource(R.color.BiruNdogAsin);
			txtAttachemnt.setTextColor(getResources().getColor(R.color.Putih));
		} else if (indexSelect == 1) {
			btnEmotion.setBackgroundResource(R.color.BiruNdogAsin);
			txtEmotion.setTextColor(getResources().getColor(R.color.Putih));
		} else if (indexSelect == 2) {
			btnSticker.setBackgroundResource(R.color.BiruNdogAsin);
			txtSticker.setTextColor(getResources().getColor(R.color.Putih));
		} else if (indexSelect == 3) {
			btnIkonia.setBackgroundResource(R.color.BiruNdogAsin);
			txtIkonia.setTextColor(getResources().getColor(R.color.Putih));
		}
		resetViewSticker();
	}

	private void resetViewSticker() {
		int stickerHeightC = (viewHeight * 40) / 100;
		int numVer = viewWidth / ViewUtilities.GetInstance().convertDPtoPX(90);
		int numHor = stickerHeightC
				/ ViewUtilities.GetInstance().convertDPtoPX(90);
		if (numVer <= 3) {
			numVer = 3;
		}
		;
		if (numHor <= 1) {
			numHor = 1;
		}
		;
		stickerHeight = (numHor * ViewUtilities.GetInstance().convertDPtoPX(90))
				+ ViewUtilities.GetInstance().convertDPtoPX(50);
		listStickerForAdapter.clear();
		if (indexSelect == 0) {
			adapterAttachment = new StickerPagerAdapterRes(context,
					AppConstants.listAttachment, this, numVer, numHor,
					ViewUtilities.GetInstance().convertDPtoPX(57));
			pagerStricker.setAdapter(adapterAttachment);
		} else if (indexSelect == 1) {
			EmoticonPagerAdapterRes epar = new EmoticonPagerAdapterRes(this,
					this);
			pagerStricker.setAdapter(epar);
		} else {
			if (indexSelect == 2) {
				for (int i = 0; i < StickerManager.getInstance()
						.getStickerListAll().size(); i++) {
					if (StickerManager.getInstance().getStickerListAll().get(i)
							.isDownlaoded()) {
						listStickerForAdapter.add(StickerManager.getInstance()
								.getStickerListAll().get(i).getThumbnail());
					}
				}
			} else if (indexSelect == 3) {
				for (int i = 0; i < StickerManager.getInstance()
						.getIkoniaListAll().size(); i++) {
					if (StickerManager.getInstance().getIkoniaListAll().get(i)
							.isDownlaoded()) {
						listStickerForAdapter.add(StickerManager.getInstance()
								.getIkoniaListAll().get(i).getThumbnail());
					}
				}
			}
			pagerStickerAdapter = new StickerPagerAdapter(context,
					listStickerForAdapter, this, numVer, numHor, ViewUtilities
							.GetInstance().convertDPtoPX(57));
			pagerStricker.setAdapter(pagerStickerAdapter);
		}

		stickerContainer.getLayoutParams().height = stickerHeight;
		stickerContainer.getLayoutParams().width = viewWidth;
		stickerContainer.requestLayout();
	}

	private void changeStatus() {
		ChatState chatState = ChatStateManager.getInstance().getChatState(
				abstractContact.getAccount(), abstractContact.getUser());
		final CharSequence statusText;
		if (chatState == ChatState.composing) {
			statusText = context.getString(R.string.chat_state_composing);
		} else if (chatState == ChatState.paused) {
			statusText = context.getString(R.string.chat_state_paused);
		} else {
			statusText = Emoticons.getSmiledText(context,
					abstractContact.getStatusText());
		}
		txtStatus.setText(statusText, BufferType.SPANNABLE);
		String name = StringUtils
				.replaceStringEquals(abstractContact.getName());
		txtUser.setText(name);
	}

	private void sendMessage(String text) {

		int start = 0;
		int end = text.length();

		while (start < end
				&& (text.charAt(start) == ' ' || text.charAt(start) == '\n'))
			start += 1;

		while (start < end
				&& (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '\n'))
			end -= 1;

		text = text.substring(start, end);

		if ("".equals(text))
			return;

		boolean isOnline = PresenceManager.getInstance()
				.getStatusMode(actionWithAccount, actionWithUser).isOnline();
		boolean isMUC = MUCManager.getInstance().hasRoom(actionWithAccount,
				actionWithUser);

		/*
		 * if (!isMUC) setPing();
		 */

		if (isOnline) {
			/*
			 * send chat state send is success
			 */
			ChatStateManager.getInstance().onComposing(actionWithAccount,
					actionWithUser, "");
		}

		if (!isOnline && !isMUC && !isWakeupPushSendAlready) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", actionWithAccount);
			params.put("targetname",
					StringUtils.replaceStringEquals(actionWithUser));
			aq.ajax(AppConstants.APIPushWakeup, params, JSONObject.class,
					new AjaxCallback<JSONObject>() {
						@Override
						public void callback(String url, JSONObject object,
								AjaxStatus status) {
							// TODO Auto-generated method stub
							super.callback(url, object, status);
							if (object != null)
								isWakeupPushSendAlready = true;
						}
					});
		}

		MessageManager.getInstance().sendMessage(actionWithAccount,
				actionWithUser, text);

		chatMessageAdapter.onChange();
		editMessage.setText("");
		changeStatus();

		scrollMyListViewToBottom();
	}

	/**
	 * kirim ping ke penerima pesan
	 * */
	/*
	 * private void setPing() { final Ping ping = new Ping(); IQ iq = new IQ() {
	 * 
	 * @Override public String getChildElementXML() { // TODO Auto-generated
	 * method stub return ping.getChildElementXML(); } };
	 * iq.setFrom(actionWithAccount + "@" + AppConstants.XMPPServerHost + "/" +
	 * AppConstants.XMPPConfResource); iq.setType(Type.GET);
	 * iq.setTo(actionWithUser);
	 * 
	 * try { ConnectionManager.getInstance().sendPacket(
	 * AccountManager.getInstance().getVerboseName( actionWithAccount), iq); }
	 * catch (NetworkException e) { } }
	 */

	// private static String lastWakeupName = "";

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		Application.getInstance().removeUIListener(OnChatChangedListener.class,
				this);
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);
		Application.getInstance().removeUIListener(
				OnAccountChangedListener.class, this);
		MessageManager.getInstance().removeVisibleChat();
		// isVisible = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isFinishing())
			return;
		String account = getAccount(intent);
		String user = getUser(intent);
		if (account == null || user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			return;
		}
		actionWithAccount = account;
		actionWithUser = user;
		refreshView();
		if (hasAttention(intent))
			AttentionManager.getInstance().removeAccountNotifications(account,
					user);
	}

	private void sendSticker(String urlSticker) {
		String stickerToSend = "5T1CK3RK03@" + urlSticker;
		sendMessage(stickerToSend);
		toggleSelect();
		hideSticker();
	}

	private void sendLocation() {

		if (AppConfiguration.getInstance().getLongitude() == 0
				|| AppConfiguration.getInstance().getLatitude() == 0) {
			// Intent myIntent = new
			// Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			// context.startActivity(myIntent);
		} else {
			String locationToSend = "L0C4T10NK03@LAT:"
					+ AppConfiguration.getInstance().getLatitude() + "/LONG:"
					+ AppConfiguration.getInstance().getLongitude();
			sendMessage(locationToSend);
			hideSticker();
		}
	}

	private void sendAttention() {
		sendMessage(AppConstants.UniqueKeyAttention);
		toggleSelect();
		hideSticker();
	}

	private static String getAccount(Intent intent) {
		String value = EntityIntentBuilder.getAccount(intent);
		if (value != null)
			return value;
		return intent.getStringExtra("com.digitalbuana.smiles.data.account");
	}

	private static String getUser(Intent intent) {
		String value = EntityIntentBuilder.getUser(intent);
		if (value != null)
			return value;
		return intent.getStringExtra("com.digitalbuana.smiles.data.user");
	}

	@Override
	public boolean onTouch(View view, MotionEvent motion) {
		if (view == editMessage) {
			if (isStickerShow) {
				hideSticker();
			}
			if (isPopShow) {
				hidePop();
			}
		}
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		AppConfiguration.getInstance().setLongitude(location.getLongitude());
		AppConfiguration.getInstance().setLatitude(location.getLatitude());
		silentUpdateLocation();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		this.provider = provider;
		locationManager.requestLocationUpdates(provider, 1, 1, this);
	}

	@Override
	public void onStatusChanged(String provider, int arg1, Bundle arg2) {
		this.provider = provider;
		locationManager.requestLocationUpdates(provider, 1, 1, this);
	}

	public static int GET_MAP_CODE = 666;

	@Override
	public void gridPagerClicked(int pos) {
		if (indexSelect == 0) {
			switch (pos) {
			case 0:
				sendAttention();
				break;
			case 1:
				showFileUpload(UploadFileActivity.TYPE_GALLERY_IMAGE);
				break;
			case 2:
				showFileUpload(UploadFileActivity.TYPE_GALLERY_VIDEO);
				break;
			case 3:
				showFileUpload(UploadFileActivity.TYPE_CAMERA_IMAGE);
				break;
			case 4:
				showFileUpload(UploadFileActivity.TYPE_GALLERY_AUDIO);
				break;
			case 5:
				showFileUpload(UploadFileActivity.TYPE_CONTACT);
				break;
			case 6:
				sendLocation();
				break;
			default:
				Toast.makeText(context, "These Features under constuction",
						Toast.LENGTH_SHORT).show();
				break;
			}
		} else if (indexSelect == 1) {
			ArrayList<Integer> emoList = Emoticons.getEmoList();
			String tmpText = editMessage.getText().toString()
					.replace("\\Q", "").replace("\\E", "");
			editMessage.setText("");
			editMessage.setText(
					Emoticons.getSmiledText(
							this,
							tmpText
									+ " "
									+ Emoticons.getEmoPatern(emoList.get(pos))
											.replace("\\Q", "")
											.replace("\\E", "")),
					BufferType.SPANNABLE);
		} else {
			if (!isStickerShowDetail) {
				isStickerShowDetail = true;
				listStickerForAdapter.clear();
				if (indexSelect == 2) {
					for (int i = 0; i < StickerManager.getInstance()
							.getStickerListAllDownloaded().get(pos)
							.getSticker().size(); i++) {
						listStickerForAdapter.add(StickerManager.getInstance()
								.getStickerListAllDownloaded().get(pos)
								.getSticker().get(i));
					}
				} else if (indexSelect == 3) {
					for (int i = 0; i < StickerManager.getInstance()
							.getIkoniaListAllDownloaded().get(pos).getSticker()
							.size(); i++) {
						listStickerForAdapter.add(StickerManager.getInstance()
								.getIkoniaListAllDownloaded().get(pos)
								.getSticker().get(i));
					}
				}
				int stickerHeightC = (viewHeight * 40) / 100;
				int numVer = viewWidth
						/ ViewUtilities.GetInstance().convertDPtoPX(80);
				int numHor = stickerHeightC
						/ ViewUtilities.GetInstance().convertDPtoPX(80);
				if (numVer <= 3) {
					numVer = 3;
				}
				;
				if (numHor <= 1) {
					numHor = 1;
				}
				;
				pagerStickerAdapter = new StickerPagerAdapter(context,
						listStickerForAdapter, this, numVer, numHor,
						ViewUtilities.GetInstance().convertDPtoPX(57));
				pagerStricker.setAdapter(pagerStickerAdapter);
				// int numOfGridC = numVer * numHor;
				// float asalC = ((float) listStickerForAdapter.size() / (float)
				// numOfGridC) + 0.5f;
				// int numOfPageC = (int) Math.round(asalC);
				// Log.e(AppConstants.TAG,
				// "Page Count : "+pageControl.getPageCount());
			} else {
				sendSticker(listStickerForAdapter.get(pos));
			}
		}
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEND) {
			sendMessage(editMessage.getText().toString());
			return true;
		}
		return false;
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		onChangeListener();
		changeStatus();
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {
		onChangeListener();
		changeStatus();

	}

	private void onChangeListener() {
		chatMessageAdapter.onChange();
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_ENTER
				&& SettingsManager.chatsSendByEnter()) {
			sendMessage(editMessage.getText().toString());
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		if (view == btnSend) {
			sendMessage(editMessage.getText().toString());
		} else if (view == btnShowSticker) {
			toogleSticker();
		} else if (view == btnBack) {
			close();
		} else if (view == btnAttachment) {
			indexSelect = 0;
			toggleSelect();
		} else if (view == btnEmotion) {
			indexSelect = 1;
			toggleSelect();
		} else if (view == btnSticker) {
			indexSelect = 2;
			toggleSelect();
		} else if (view == btnIkonia) {
			indexSelect = 3;
			toggleSelect();
		} else if (view == btnAdd) {
			startActivityForResult(StickerSelectActivity.createIntent(this),
					RESULT_CODE_STICKER_SELECT);
		} else if (view == btnPop) {
			togglePop();
		}
	}

	private void togglePop() {
		if (isPopShow) {
			hidePop();
		} else {
			showPopup();
		}
	}

	private void showPopup() {
		resettingView();
		hidingKeyboard();
		if (isStickerShow) {
			hideSticker();
		}
		// Log.i(AppConstants.TAG, "IS POP SHOW: "+viewWidth);
		int tempWidthPop = (viewWidth * 80) / 100;
		if (tempWidthPop >= ViewUtilities.GetInstance().convertDPtoPX(300)) {
			tempWidthPop = ViewUtilities.GetInstance().convertDPtoPX(300);
		}
		popContainer.getLayoutParams().width = tempWidthPop;
		int tempHeight = viewHeight
				- ViewUtilities.GetInstance().convertDPtoPX(100);
		int tempHeight2 = popAdapter.getCount()
				* ViewUtilities.GetInstance().convertDPtoPX(55);
		if (tempHeight2 >= tempHeight) {
			popContainer.getLayoutParams().height = (viewHeight * 75) / 100;
		} else {
			popContainer.getLayoutParams().height = tempHeight2;
		}

		Animation anim = new ScaleAnimation(1, 1, 0, 1);
		anim.setDuration(250);
		popContainer.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				popContainer.setVisibility(View.VISIBLE);
				isPopShow = true;
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
			}
		});
	}

	private void hidePop() {
		Animation anim = new ScaleAnimation(1, 1, 1, 0);
		anim.setDuration(100);
		popContainer.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				popContainer.setVisibility(View.GONE);
				isPopShow = false;
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		togglePop();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == RESULT_CODE_STICKER_SELECT) {
				int result = data.getIntExtra("result", 0);
				if (result == 0) {
					indexSelect = 2;
				} else {
					indexSelect = 3;
				}
				toggleSelect();
			} else if (resultCode == GET_MAP_CODE) {
				String longitude = data.getStringExtra("long");
				String latitude = data.getStringExtra("lati");
				String locationToSend = "L0C4T10NK03@LAT:" + latitude
						+ "/LONG:" + longitude;
				sendMessage(locationToSend);
			}
		}
	}

	private void close() {
		finish();
		ActivityManager.getInstance().clearStack(false);
		if (!ActivityManager.getInstance().hasContactList(this))
			startActivity(HomeActivity.createIntent(this));
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new EntityIntentBuilder(context, ChatViewActivity.class)
				.setAccount(account).setUser(user).build();
	}

	public static Intent createClearTopIntent(Context context, String account,
			String user) {
		Intent intent = createIntent(context, account, user);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	public static Intent createAttentionRequestIntent(Context context,
			String account, String user) {
		Intent intent = ChatViewActivity.createClearTopIntent(context, account,
				user);
		intent.setAction(ACTION_ATTENTION);
		return intent;
	}

	public static Intent createSendIntent(Context context, String account,
			String user, String text) {
		Intent intent = ChatViewActivity.createIntent(context, account, user);
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		return intent;
	}

	@Override
	public void onChatChanged(String account, String user, boolean incoming) {

		NotificationManager.getInstance().removeMessageNotification(
				actionWithAccount, actionWithUser);

		if (incoming && user.equals(actionWithUser)) {
			txtUser.startAnimation(shake);
			onChangeListener();
		}

	}

	private void scrollMyListViewToBottom() {
		listChat.postDelayed(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...

				listChat.setSelection(chatMessageAdapter.getCount());
			}
		}, 1000);
	}

	@Override
	protected void resettingView() {
		super.resettingView();
		onChangeListener();
		if (isStickerShow) {
			showSticker();
		} else {
			hideSticker();
		}
	}

	private void showSticker() {
		hidingKeyboard();
		if (isPopShow) {
			hidePop();
		}
		isStickerShow = true;
		resetViewSticker();
	}

	private void hidingKeyboard() {
		InputMethodManager imm = (InputMethodManager) context
				.getApplicationContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
		}
	}

	private void hideSticker() {
		isStickerShow = false;
		stickerContainer.getLayoutParams().height = 0;
		stickerContainer.requestLayout();
	}

	private void toogleSticker() {
		if (isStickerShow) {
			hideSticker();
		} else {
			showSticker();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
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
		if (isStickerShowDetail && isStickerShow) {
			toggleSelect();
		} else if (!isStickerShowDetail && isStickerShow) {
			hideSticker();
		} else if (isPopShow) {
			hidePop();
		} else {
			close();
			finish();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
		if (parent == popList) {
			hidePop();
			if (popAdapter.getISMUC()) {
				switch (pos) {
				case 0:
					/*
					 * startActivity(HomeActivity.createRoomInviteIntent(this,
					 * actionWithAccount, actionWithUser));
					 */
					Intent i = new Intent(this, GroupInviteActivity.class);
					i.putExtra("group", actionWithUser);
					startActivity(i);
					break;
				case 1:
					startActivity(OccupantList.createIntent(this,
							actionWithAccount, actionWithUser));
					break;
				case 2:
					MUCManager.getInstance().leaveRoom(actionWithAccount,
							actionWithUser);
					MessageManager.getInstance().closeChat(actionWithAccount,
							actionWithUser);
					NotificationManager.getInstance()
							.removeMessageNotification(actionWithAccount,
									actionWithUser);
					close();
					break;
				case 4:
					MessageManager.getInstance().requestToLoadLocalHistory(
							actionWithAccount, actionWithUser);
					Log.i(TAG, "getISMUC show message history > > > > >");
					MessageArchiveManager.getInstance().requestHistory(
							actionWithAccount, actionWithUser,
							MINIMUM_MESSAGES_TO_LOAD, 0);
					chatMessageAdapter.onChange();
					break;
				case 3:
					MessageManager.getInstance().clearHistory(
							actionWithAccount, actionWithUser);
					chatMessageAdapter.onChange();
					break;
				// case 5:exportChat();break;
				case 5:
					startActivity(ChatEditor.createIntent(this,
							actionWithAccount, actionWithUser));
					break;
				}
			} else {
				switch (pos) {
				case 0:
					startActivity(ProfileDetailActivity.createIntent(this,
							actionWithAccount, actionWithUser));
					break;
				case 1:
					startActivity(ContactEditor.createIntent(this,
							actionWithAccount, actionWithUser));
					break;
				case 3:
					MessageManager.getInstance().requestToLoadLocalHistory(
							actionWithAccount, actionWithUser);
					MessageArchiveManager.getInstance().requestHistory(
							actionWithAccount, actionWithUser,
							MINIMUM_MESSAGES_TO_LOAD, 0);
					chatMessageAdapter.onChange();
					break;
				case 2:
					MessageManager.getInstance().clearHistory(
							actionWithAccount, actionWithUser);
					chatMessageAdapter.onChange();
					break;
				// case 4:exportChat();break;
				case 4:
					startActivity(ChatEditor.createIntent(this,
							actionWithAccount, actionWithUser));
					break;
				}
			}
		}
	}

	private void exportChat() {
		new ChatExportAsyncTask(StringUtils.replaceStringEquals(actionWithUser))
				.execute();
	}

	private class ChatExportAsyncTask extends AsyncTask<Void, Void, File> {
		private String builder;

		public ChatExportAsyncTask(String builder) {
			this.builder = "smilesKu/chat/CHAT_" + builder + ".html";
		}

		@Override
		protected File doInBackground(Void... params) {
			File file = null;
			try {
				file = MessageManager.getInstance().exportChat(
						actionWithAccount, actionWithUser, builder);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return file;
		}

		@Override
		public void onPostExecute(File result) {
			if (result != null) {
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				Uri uri = Uri.fromFile(result);
				intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent,
						getString(R.string.export_chat)));
			}
		}

	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if (result.length() >= 10) {
			if (urlKu == AppConstants.APIConfiguration) {
				AppConfiguration.getInstance().setTempJSON(result);
				if (!AppConfiguration.getInstance().isActive()) {
					Application.getInstance().closeApplication();
					ActivityManager.getInstance().finishAll();
				}
			}
		}
	}

	private void silentUpdateLocation() {
		long distance = (long) Math.floor(RecentUtils.distFrom(AppConfiguration
				.getInstance().getLastLatitude(), AppConfiguration
				.getInstance().getLastLongitude(), AppConfiguration
				.getInstance().getLatitude(), AppConfiguration.getInstance()
				.getLongitude()) + 0.5d);
		if (distance > 20) {
			List<NameValuePair> postDataa = new ArrayList<NameValuePair>();
			if (AccountManager.getInstance().getAccounts().size() >= 1) {
				postDataa.add(new BasicNameValuePair("username", AccountManager
						.getInstance().getActiveAccount().getAccount()));
				postDataa.add(new BasicNameValuePair("long", ""
						+ AppConfiguration.getInstance().getLongitude()));
				postDataa.add(new BasicNameValuePair("lat", ""
						+ AppConfiguration.getInstance().getLatitude()));
				postDataa.add(new BasicNameValuePair("carrier", RecentUtils
						.getCarrier()));
			}
			doPostAsync(context, AppConstants.APIConfiguration, postDataa,
					null, false);
			AppConfiguration.getInstance().setLastLatitude(
					AppConfiguration.getInstance().getLatitude());
			AppConfiguration.getInstance().setLastLongitude(
					AppConfiguration.getInstance().getLongitude());
		}
	}

}
