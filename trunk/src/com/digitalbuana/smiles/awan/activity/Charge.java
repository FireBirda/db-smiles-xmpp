package com.digitalbuana.smiles.awan.activity;

import static com.digitalbuana.smiles.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.digitalbuana.smiles.CommonUtilities.EXTRA_MESSAGE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.WakeLocker;
import com.digitalbuana.smiles.activity.StickerSelectActivity;
import com.digitalbuana.smiles.data.AppConstants;
import com.google.android.gcm.GCMRegistrar;

public class Charge extends Activity {

	private WebView webView;
	private String contentCharge = null;
	private String TAG = getClass().getSimpleName();
	private String carrier;// = General.getCarrier(this);
	private String packageID;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		carrier = AppConstants.getCarrier(this);
		setContentView(R.layout.charge_popup);

		Intent sender = getIntent();

		String ticketId = sender.getExtras().getString("ticketId");
		String amount = sender.getExtras().getString("amount");
		packageID = sender.getExtras().getString("packageId");

		contentCharge = sender.getExtras().getString("charge");

		String finalServiceId = null;
		String finalPrice = null;
		try {
			JSONArray chargeArray = new JSONArray(contentCharge);
			if (chargeArray.length() > 0) {
				for (int a = 0; a < chargeArray.length(); a++) {
					JSONObject jObj = new JSONObject(chargeArray.getString(a));
					String serviceId = jObj.getString("service_id");
					String servicePrice = jObj.getString("service_price");
					String telcoName = jObj.getString("telco_name");
					if (carrier.trim().equals(telcoName)) {
						finalServiceId = serviceId;
						finalPrice = servicePrice;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e(TAG, "error:" + e.getMessage());
		}

		if (finalPrice == null)
			finalPrice = amount;

		if (finalServiceId == null) {

			Toast.makeText(this,
					"Error on Service detection, please try again.",
					Toast.LENGTH_LONG).show();

		} else {

			/*
			 * clear cache on load and reload, next phase
			 */

			webView = (WebView) findViewById(R.id.chargeWebView);
			String tselLandingPage = "http://202.3.208.212/cp/db/index.jsp?TicketId="
					+ ticketId
					+ "&Amount="
					+ finalPrice
					+ /* "&SerialID="+serialId+ */"&ServiceId="
					+ finalServiceId;

			webView.getSettings().setJavaScriptEnabled(true);

			webView.clearHistory();
			webView.clearCache(true);
			webView.clearFormData();

			webView.loadUrl(tselLandingPage);

			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// TODO Auto-generated method stub
					view.loadUrl(url);
					return false;
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
				}
			});

			registerReceiver(mHandleMessageReceiver, new IntentFilter(
					DISPLAY_MESSAGE_ACTION));
		}

	}

	private void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(Charge.this, msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			if (newMessage != null) {
				try {
					JSONObject jsonResponse = new JSONObject(newMessage);
					String type = jsonResponse.getString("type");
					if (type.equals("trxnotif")) {
						String title = jsonResponse.getString("msg");
						if (title.contains("Success")) {

							Intent i = new Intent(Charge.this,
									StickerSelectActivity.class);
							i.putExtra("packageId", packageID);

							startActivity(StickerSelectActivity
									.createIntent(Charge.this));
							finish();

							// Waking up mobile if it is sleeping
							WakeLocker.acquire(getApplicationContext());
							// Releasing wake lock
							WakeLocker.release();

							showToast("Proccess Success");

						} else {
							showToast("Proccess Failed");
						}
						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	protected void onDestroy() {
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	};

}