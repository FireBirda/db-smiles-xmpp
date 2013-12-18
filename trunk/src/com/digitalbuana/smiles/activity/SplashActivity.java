package com.digitalbuana.smiles.activity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.LocationHelper;
import com.digitalbuana.smiles.data.ActivityManager;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.OnAccountChangedListener;
import com.digitalbuana.smiles.data.friends.AdminManager;
import com.digitalbuana.smiles.data.message.MessageTable;
import com.digitalbuana.smiles.dialog.SmilesDefaultDialog;
import com.digitalbuana.smiles.service.SmilesService;
import com.digitalbuana.smiles.ui.helper.SingleActivity;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;

public class SplashActivity extends SingleActivity implements
		OnAccountChangedListener, LocationListener {

	// Location
	private LocationManager locationManager;
	private String provider;
	private String TAG = getClass().getSimpleName();
	private Location location = null;
	private AQuery aq;
	private SharedPreferences mSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		aq = new AQuery(this);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_splash);
		// Casting
		rootView = (FrameLayout) findViewById(R.id.splashRootView);
		FontUtils.setNormalRobotoFont(context, rootView);
		final ImageView imgAnim = (ImageView) findViewById(R.id.splashLogo);
		imgAnim.setBackgroundResource(R.drawable.kakatua_logo_kedip_anim);

		rootView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) imgAnim
						.getBackground();
				anim.start();
			}
		});

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
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
			Log.v(TAG, " >>>>> failed to get location >>>>>>>>> ");
		}
		setSplashAction();
	}

	private void setSplashAction() {
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MessageTable.getInstance().setUpdateMessageTable();
				AdminManager.getInstance().setUpdateTable();
				if (location != null
						&& (location.getLongitude() == 0 || location
								.getLatitude() == 0))
					LocationHelper.getLongLat(SplashActivity.this,
							SplashActivity.this);
			}
		});
	}

	private void loadAppConfig() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username",
				mSettings.getString(AppConstants.USERNAME_KEY, ""));
		params.put("long", "" + AppConfiguration.getInstance().getLongitude());
		params.put("lat", "" + AppConfiguration.getInstance().getLatitude());
		params.put("carrier", RecentUtils.getCarrier());
		aq.ajax(AppConstants.APIConfiguration, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject object,
							AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, object, status);
						if (object != null) {
							AppConfiguration.getInstance().setTempJSON(
									object.toString());
							if (AppConfiguration.getInstance().isActive()) {
								finisToMain();
							} else {
								appNotActive();
							}
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									SplashActivity.this);
							builder.setMessage(
									getString(R.string.CONNECTION_FAILED))
									.setPositiveButton(
											R.string.formGlobalOk,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													finish();
												}
											});
							AlertDialog failedDialog = builder.create();
							failedDialog.show();
						}
					}
				});
	}

	private void appNotActive() {
		SmilesDefaultDialog dialog = new SmilesDefaultDialog(context, "SORRY",
				"APPLICATION IS TEMPORARY NOT ACTIVE");
		dialog.show();
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				Application.getInstance().closeApplication();
			}
		});
	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if (urlKu == AppConstants.APIConfiguration) {
			AppConfiguration.getInstance().setTempJSON(result);
			if (AppConfiguration.getInstance().isActive()) {
				finisToMain();
			} else {
				appNotActive();
			}
		}
	}

	private void finisToMain() {
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
		Application.getInstance().addUIListener(OnAccountChangedListener.class,
				this);
		if (Application.getInstance().isClosing()) {
			((TextView) findViewById(R.id.text))
					.setText(R.string.application_state_closing);
		} else {
			startService(SmilesService.createIntent(this));
			update();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		Application.getInstance().removeUIListener(
				OnAccountChangedListener.class, this);
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		update();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			cancel();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void update() {
		if (Application.getInstance().isInitialized()
				&& !Application.getInstance().isClosing() && !isFinishing()) {
			loadAppConfig();
		}
	}

	private void cancel() {
		finish();
		ActivityManager.getInstance().cancelTask(this);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, SplashActivity.class);
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

	@Override
	public void onLocationChanged(Location location) {
		AppConfiguration.getInstance().setLongitude(location.getLongitude());
		AppConfiguration.getInstance().setLatitude(location.getLatitude());
	}

}
