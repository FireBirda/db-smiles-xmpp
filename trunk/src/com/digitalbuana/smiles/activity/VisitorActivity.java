package com.digitalbuana.smiles.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.visitor.VisitorAdapter;
import com.digitalbuana.smiles.awan.stores.FriendVisitsStore;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.friends.OnVisitorChangeListener;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog.OnSmilesDialogClose;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

public class VisitorActivity extends ManagedActivity implements
		OnClickListener, OnVisitorChangeListener, OnItemClickListener {

	private static final String SAVED_ACCOUNT = "com.digitalbuana.smiles.ui.Visitor.SAVED_ACCOUNT";
	private static final String SAVED_USER = "com.digitalbuana.smiles.ui.Visitor.SAVED_USER";

	private Geocoder geoCoder;
	private FrameLayout btnBack;
	private GridViewKu gridVisitor;
	private VisitorAdapter visitorAdapter;
	private FrameLayout btnClear;

	// PopMenu
	private boolean isPopShow = false;
	private FrameLayout popContainer;
	private FrameLayout popBtnClear;
	private FrameLayout popBtnCancel;
	private ImageView popAvatar;
	private TextView popTxtName;
	private TextView popTxtTime;
	private TextView popTxtDistance;
	private FrameLayout popGender;
	private LinearLayout popBtnViewProfile;

	private String TAG = getClass().getSimpleName();

	private ProgressBar progressBar1;
	private AQuery aq;// = new AQuery(this);

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;

	public static final String FRIEND_VISITS_TAG = "friendvisits";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (isFinishing())
			return;

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();
		settingsEditor.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, false);
		settingsEditor.commit();

		aq = new AQuery(this);

		Intent intent = getIntent();
		String account = getAccount(intent);
		String user = getUser(intent);

		if (savedInstanceState != null) {
			actionWithAccount = savedInstanceState.getString(SAVED_ACCOUNT);
			actionWithUser = savedInstanceState.getString(SAVED_USER);
		}
		if (actionWithAccount == null)
			actionWithAccount = account;
		if (actionWithUser == null)
			actionWithUser = user;

		FriendsManager.getInstance().getVisitorManager()
				.removeVisitorNotifications(account, user);

		setContentView(R.layout.activity_visitor);

		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

		rootView = (FrameLayout) findViewById(R.id.visitorsRootView);
		btnBack = (FrameLayout) findViewById(R.id.visitorsBtnBack);

		gridVisitor = (GridViewKu) findViewById(R.id.visitorsGridVisitor);
		btnClear = (FrameLayout) findViewById(R.id.VIsitorBtnClear);
		btnClear.setVisibility(View.GONE);

		// PopMenu
		popContainer = (FrameLayout) findViewById(R.id.VisitorPopConteiner);
		popTxtName = (TextView) findViewById(R.id.VisitorPopTxtname);
		popTxtTime = (TextView) findViewById(R.id.VisitorPopTxtTime);
		popTxtDistance = (TextView) findViewById(R.id.VisitorPopTxtDistance);
		popBtnCancel = (FrameLayout) findViewById(R.id.VisitorPopBtnCancel);
		popBtnClear = (FrameLayout) findViewById(R.id.VisitorPopBtnClear);
		popAvatar = (ImageView) findViewById(R.id.VisitorPopAvatar);
		popGender = (FrameLayout) findViewById(R.id.VisitorPopFrameGender);
		popBtnViewProfile = (LinearLayout) findViewById(R.id.VisitorPopBtnViewProfile);

		popContainer.setVisibility(View.GONE);

		popBtnCancel.setOnClickListener(this);

		btnBack.setOnClickListener(this);
		btnClear.setOnClickListener(this);
		popContainer.setOnClickListener(this);

		ArrayList<FriendsModel> listVisitor = FriendsManager.getInstance()
				.getVisitorManager().getAllVisitor();
		if (listVisitor.isEmpty()) {
			String lastGetData = mSettings.getString(FRIEND_VISITS_TAG, null);
			if (lastGetData != null) {
				try {
					generateAPIList(new JSONObject(lastGetData));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		visitorAdapter = new VisitorAdapter(context, listVisitor);
		gridVisitor.setAdapter(visitorAdapter);

		gridVisitor.setOnItemClickListener(this);

		geoCoder = new Geocoder(context, Locale.getDefault());

		FontUtils.setRobotoFont(context, rootView);
		gridVisitor.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (isPopShow) {
					hidePop();
				}
				return false;
			}
		});
		getFriendUpdatesFromAPI();
	}

	private void getFriendUpdatesFromAPI() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", AccountManager.getInstance().getActiveAccount()
				.getAccount());
		aq.ajax(AppConstants.APIFriendVisits, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject object,
							AjaxStatus status) {
						if (object != null) {
							settingsEditor.putString(FRIEND_VISITS_TAG,
									object.toString());
							settingsEditor.commit();
							generateAPIList(object);
						} else {
							String lastGetData = mSettings.getString(
									FRIEND_VISITS_TAG, null);
							if (lastGetData != null) {
								try {
									generateAPIList(new JSONObject(lastGetData));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						progressBar1.setVisibility(View.GONE);
					}
				});
	}

	private void generateAPIList(JSONObject object) {
		FriendVisitsStore fus = new FriendVisitsStore(object);
		ArrayList<FriendsModel> listVisitor = fus.getList();
		if (listVisitor != null && !listVisitor.isEmpty()) {
			visitorAdapter = new VisitorAdapter(context, listVisitor);
			gridVisitor.setAdapter(visitorAdapter);
			visitorAdapter.notifyDataSetChanged();
			FriendsManager.getInstance().getVisitorManager().deleteAllVisitor();
		}
	}

	private String actionWithAccount;
	private String actionWithUser;

	@Override
	protected void onResume() {
		super.onResume();
		Application.getInstance().addUIListener(OnVisitorChangeListener.class,
				this);
		/*
		 * btnClear.setVisibility(View.GONE);
		 * if(FriendsManager.getInstance().getVisitorManager
		 * ().getAllVisitor().size()>=1){ btnClear.setVisibility(View.VISIBLE);
		 * } visitorAdapter.onChange();
		 */
	}

	private void showPop(final FriendsModel friend) {
		if (RecentUtils.checkNetwork()) {
			resettingView();
			popContainer.setVisibility(View.VISIBLE);
			AbstractContact abstractContact = RosterManager.getInstance()
					.getBestContact(
							actionWithAccount,
							friend.getName() + "@"
									+ AppConstants.XMPPServerHost);
			popAvatar.setImageDrawable(abstractContact.getAvatar());
			popTxtName.setText(StringUtils.replaceStringEquals(abstractContact
					.getName()));
			popTxtTime.setText(StringUtils.parseTimeVisitor(friend.getTime()));
			popBtnClear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// VisitorManager.getInstance().removeVisitor(friend);
					/*
					 * btnClear.setVisibility(View.GONE);
					 * if(FriendsManager.getInstance
					 * ().getVisitorManager().getAllVisitor().size()>=1){
					 * btnClear.setVisibility(View.VISIBLE); }
					 * visitorAdapter.onChange();
					 */
					hidePop();
				}
			});

			Animation anim = new ScaleAnimation(1, 1, 0, 1, 1, ViewUtilities
					.GetInstance().convertDPtoPX(230));
			anim.setDuration(300);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
					gridVisitor.setOnItemClickListener(null);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
					isPopShow = true;
					progressBar1.setVisibility(View.VISIBLE);
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("username", AccountManager.getInstance()
							.getActiveAccount().getAccount());
					params.put("targetname", friend.getName());
					params.put("push", "N");
					aq.ajax(AppConstants.APIGetProfile, params,
							JSONObject.class, new AjaxCallback<JSONObject>() {
								@SuppressLint("DefaultLocale")
								@Override
								public void callback(String url,
										JSONObject json, AjaxStatus status) {
									progressBar1.setVisibility(View.GONE);
									try {
										String statusa = json
												.getString("STATUS");
										if (statusa.toUpperCase().equals(
												"SUCCESS")) {
											JSONObject dataUser = json
													.getJSONObject("DATA");
											boolean userIsMale = !dataUser
													.getString("gender")
													.equals("wanita");
											if (userIsMale) {
												popGender
														.setBackgroundResource(R.color.BiruNdogAsin);
											} else {
												popGender
														.setBackgroundResource(R.color.Pink);
											}
											String statusUser = dataUser
													.getString("status");
											popTxtTime.setText(
													Emoticons
															.getSmiledText(
																	VisitorActivity.this,
																	statusUser),
													BufferType.SPANNABLE);
											// Location
											String longitueS = dataUser
													.getString("longitude");
											String latitudeS = dataUser
													.getString("latitude");
											double longitude = Double
													.parseDouble(longitueS);
											double latitude = Double
													.parseDouble(latitudeS);
											double distance = RecentUtils
													.distFrom(
															AppConfiguration
																	.getInstance()
																	.getLatitude(),
															AppConfiguration
																	.getInstance()
																	.getLongitude(),
															latitude, longitude);
											String distanceS = distance + " M";
											if (distance > 1000) {
												distanceS = (long) Math
														.floor((distance / 1000) + 0.5d)
														+ " KM";
											} else {
												distanceS = (long) Math
														.floor(distance + 0.5d)
														+ " M";
											}
											popTxtDistance
													.setVisibility(View.VISIBLE);
											popTxtDistance.setText(distanceS);
											String fullnameUser = dataUser
													.getString("fullname");
											popTxtName.setText(fullnameUser);
											try {
												List<Address> addresses = geoCoder
														.getFromLocation(
																latitude,
																longitude, 1);

												if (addresses.size() > 0) {
													popTxtDistance.setText(Html
															.fromHtml("<b>"
																	+ distanceS
																	+ "</b><br>"
																	+ addresses
																			.get(0)
																			.getCountryName()
																	+ " - "
																	+ addresses
																			.get(0)
																			.getLocality()));
												}
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});

				}
			});
			popBtnViewProfile.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.e(TAG, "View Profile : " + friend.getName());
					context.startActivity(ProfileDetailActivity.createIntent(
							context, AccountManager.getInstance()
									.getAccountKu(), friend.getName() + "@"
									+ AppConstants.XMPPServerHost));
					hidePop();
				}
			});
			popContainer.startAnimation(anim);
		}
	}

	private void hidePop() {
		popBtnClear.setOnClickListener(null);
		popBtnViewProfile.setOnClickListener(null);
		Animation anim = new ScaleAnimation(1, 1, 1, 0, 1, ViewUtilities
				.GetInstance().convertDPtoPX(230));
		anim.setDuration(200);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				isPopShow = false;
				popContainer.setVisibility(View.GONE);
				popTxtDistance.setVisibility(View.GONE);
				popGender.setBackgroundResource(R.color.AbuTuaMedium);
				gridVisitor.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int arg2, long arg3) {
						FriendsModel friend = ((VisitorAdapter.ViewHolder) view
								.getTag()).getFriend();
						showPop(friend);
					}

				});
			}
		});
		popContainer.startAnimation(anim);
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
		if (isPopShow) {
			hidePop();
		} else {
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT, actionWithAccount);
		outState.putString(SAVED_USER, actionWithUser);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Application.getInstance().removeUIListener(
				OnVisitorChangeListener.class, this);
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new Intent(context, VisitorActivity.class);
	}

	public static Intent createClearTopIntent(Context context, String account,
			String user) {
		Intent intent = createIntent(context, account, user);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	@Override
	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnClear) {
			SmilesConfirmDialog dialog = new SmilesConfirmDialog(
					context,
					new OnSmilesDialogClose() {
						@Override
						public void onSmilesDialogClose(boolean isConfirm) {
							if (isConfirm) {
								btnClear.setVisibility(View.GONE);
								FriendsManager.getInstance()
										.getVisitorManager().deleteAllVisitor();
								visitorAdapter.onChange();
							}
						}
					}, "Clear All Visitor",
					"Are you Sure to delete all History?");
			dialog.show();

		} else if (v == popBtnCancel) {
			hidePop();
		} else if (v == popContainer) {
			hidePop();
		}
	}

	@Override
	public void onVisitorChanged(ArrayList<FriendsModel> listVisitor) {
		getFriendUpdatesFromAPI();
		/*
		 * btnClear.setVisibility(View.GONE);
		 * if(FriendsManager.getInstance().getVisitorManager
		 * ().getAllVisitor().size()>=1){ btnClear.setVisibility(View.VISIBLE);
		 * } visitorAdapter.onChange();
		 */
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
	public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
		if (parent == gridVisitor) {
			FriendsModel friend = ((VisitorAdapter.ViewHolder) view.getTag())
					.getFriend();
			showPop(friend);
		}
	}
}
