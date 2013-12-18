package com.digitalbuana.smiles.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.StickerGridAdapter;
import com.digitalbuana.smiles.awan.activity.Charge;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.StickerManager;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

public class StickerSelectActivity extends ManagedActivity implements
		View.OnClickListener, OnItemClickListener {

	private FrameLayout btnClose;

	private ArrayList<String> listStickerPackage;
	private ArrayList<String> listStickerDetail;
	private StickerGridAdapter adapterStickerPackage;
	private StickerGridAdapter adapterStickerDetail;

	private AQuery aq;
	private int indexSelect = 0;
	private FrameLayout btnSticker;
	private FrameLayout btnIkonia;

	private String packageID = "";

	private boolean isDetailVisible = false;

	// Loading
	private FrameLayout loadingView;
	// NoItem
	private FrameLayout noItemContainer;
	// Package
	private GridView gridStickerPackage;

	// Detail
	private ScrollView scrollDetail;
	private GridViewKu gridStickerDetail;
	private TextView txtStickerName;
	private TextView txtStickerDesc;
	private FrameLayout btnDownload;
	private TextView txtDownload;
	private TextView priceTextView;

	private JSONObject tmpJsonSelected = null;
	private JSONObject tmpJSONCharge = null;
	private String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sticker_select);

		rootView = (FrameLayout) findViewById(R.id.stickerSelectRootView);
		btnClose = (FrameLayout) findViewById(R.id.StickerSelectBtnClose);

		btnSticker = (FrameLayout) findViewById(R.id.StickerSelectBtnSticker);
		btnIkonia = (FrameLayout) findViewById(R.id.StickerSelectBtnIkonia);

		// Loading
		loadingView = (FrameLayout) findViewById(R.id.StickerSelectLoading);

		// NoItem
		noItemContainer = (FrameLayout) findViewById(R.id.StickerSelectNoItemConteiner);

		// PAckage
		gridStickerPackage = (GridView) findViewById(R.id.StickerSelectGridView);

		// Detail
		gridStickerDetail = (GridViewKu) findViewById(R.id.StickerSelectGridViewKu);
		txtStickerName = (TextView) findViewById(R.id.StickerSelectTxtStickerName);
		txtStickerDesc = (TextView) findViewById(R.id.StickerSelectTxtStickerDesc);
		btnDownload = (FrameLayout) findViewById(R.id.StickerSelectBtndownload);
		txtDownload = (TextView) findViewById(R.id.StickerSelectTxtDownload);
		scrollDetail = (ScrollView) findViewById(R.id.StickerSelectScrollStricker);

		priceTextView = (TextView) findViewById(R.id.priceTextView);

		btnClose.setOnClickListener(this);
		btnSticker.setOnClickListener(this);
		btnIkonia.setOnClickListener(this);
		btnDownload.setOnClickListener(this);
		gridStickerPackage.setOnItemClickListener(this);

		listStickerPackage = new ArrayList<String>();
		listStickerDetail = new ArrayList<String>();
		adapterStickerPackage = new StickerGridAdapter(context,
				listStickerPackage, ViewUtilities.GetInstance().convertDPtoPX(
						100));
		adapterStickerDetail = new StickerGridAdapter(context,
				listStickerDetail, ViewUtilities.GetInstance().convertDPtoPX(
						100));
		gridStickerPackage.setAdapter(adapterStickerPackage);
		gridStickerDetail.setAdapter(adapterStickerDetail);
		gridStickerDetail.setEnabled(false);
		gridStickerDetail.setClickable(false);

		aq = new AQuery(this, rootView);
		final ImageView kakatuaView = (ImageView) findViewById(R.id.kakatuaKuimgProgress);
		kakatuaView.setBackgroundResource(R.drawable.kakatua_junior_anim);
		kakatuaView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) kakatuaView
						.getBackground();
				anim.start();
			}
		});
		FontUtils.setRobotoFont(context, rootView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent parms = getIntent();
		if (parms.getStringExtra("packageId") != null) {
			packageID = parms.getStringExtra("packageId");
			List<NameValuePair> postDataNya = new ArrayList<NameValuePair>();
			String accountName = AccountManager.getInstance().getAccountKu();
			if (accountName != null) {
				postDataNya
						.add(new BasicNameValuePair("username", accountName));
				postDataNya
						.add(new BasicNameValuePair("package_id", packageID));
				doPostAsync(context, AppConstants.APIStickerPackageDetail,
						postDataNya, null, true);
			}
		} else {
			noItemContainer.setVisibility(View.VISIBLE);
			scrollDetail.setVisibility(View.GONE);
			gridStickerPackage.setVisibility(View.GONE);
			indexSelect = 0;
			isDetailVisible = false;
			loadSticker();
		}
	}

	private void toggleSelect() {
		if (isDetailVisible) {
			hideDetail();
			return;
		}
		btnSticker.setBackgroundResource(R.drawable.img_btn_tab_left_unselect);
		btnIkonia.setBackgroundResource(R.drawable.img_btn_tab_right_unselect);
		if (indexSelect == 0) {
			btnSticker
					.setBackgroundResource(R.drawable.img_btn_tab_left_select);
			loadSticker();
		} else if (indexSelect == 1) {
			btnIkonia
					.setBackgroundResource(R.drawable.img_btn_tab_right_select);
			loadIkonia();
		}
	}

	@Override
	protected void resettingView() {
		super.resettingView();
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, StickerSelectActivity.class);
	}

	@Override
	public void onClick(View v) {
		if (v == btnClose) {
			if (isDetailVisible) {
				hideDetail();
			} else {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "");
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		} else if (v == btnSticker) {
			indexSelect = 0;
			toggleSelect();
		} else if (v == btnIkonia) {
			indexSelect = 1;
			toggleSelect();
		} else if (v == btnDownload) {

			String btnDownloadText = txtDownload.getText().toString();
			if (btnDownloadText.equals("Remove")) {
				StickerManager.getInstance().removeSticker(packageID);
				finish();
			} else if (btnDownloadText.equals("Download"))
				downloadItem();
			else {
				if (tmpJsonSelected != null) {

					try {

						JSONArray jCharge = null;
						JSONObject tmpJson = null;
						JSONArray tmpJArr = tmpJsonSelected
								.getJSONArray("DATA");
						for (int a = 0; a < tmpJArr.length(); a++) {
							JSONObject jobj = tmpJArr.getJSONObject(a);
							if (jobj.getString("package_id").equals(packageID)) {
								jCharge = jobj.getJSONArray("charge");
								tmpJson = jobj;
							}
						}

						if (jCharge != null) {

							String carrier = AppConstants.getCarrier(this);

							JSONArray chargeArray = jCharge;
							String finalServiceId = null;
							String finalPrice = null;
							for (int a = 0; a < chargeArray.length(); a++) {
								JSONObject jObj = new JSONObject(
										chargeArray.getString(a));
								String serviceId = jObj.getString("service_id");
								String servicePrice = jObj
										.getString("service_price");
								String telcoName = jObj.getString("telco_name");

								if (carrier.trim().equals(telcoName)) {
									finalServiceId = serviceId;
									finalPrice = servicePrice;
								}
							}
							if (finalServiceId != null && finalPrice != null) {
								tmpJSONCharge = tmpJson;
								purchaseItem(finalPrice, finalServiceId);
							} else
								showToast(getString(R.string.error_charge)
										+ "[1]");
						} else
							showToast(getString(R.string.error_charge)
									+ "[1.1]");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						showToast(getString(R.string.error_charge) + ":"
								+ e.getMessage() + "[2]");
					}

				} else
					showToast(getString(R.string.error_charge) + "[3]");
			}
		}
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void purchaseItem(final String price, final String serialId) {

		String tmpApiItem = "[{\"type\":\"STICKER\",\"id\":\"" + packageID
				+ "\",\"amount\":\"" + price + "\"}]";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", AccountManager.getInstance().getAccountKu());
		params.put("amount", price);
		params.put("items", tmpApiItem);

		aq.progress(ScreenHelper.getDialogProgress(this)).ajax(
				AppConstants.APIGenerateTicket, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject json,
							AjaxStatus status) {
						// TODO Auto-generated method stub

						if (json != null) {
							Intent i = new Intent(StickerSelectActivity.this,
									Charge.class);
							try {
								i.putExtra("ticketId",
										json.getString("TICKET_ID"));
								i.putExtra("amount", price);
								i.putExtra("packageId", packageID);
								i.putExtra("charge",
										tmpJSONCharge.getString("charge"));
								startActivity(i);
								finish();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
								showToast(getString(R.string.error_charge)
										+ ":" + e.getMessage() + "[4]");
							}

						} else
							showToast(getString(R.string.error_charge) + "[5]");

					}
				});

	}

	private void loadSticker() {
		indexSelect = 0;
		loadingView.setVisibility(View.VISIBLE);
		if (!AppConfiguration.getInstance().getTimeSticker()
				.equals(StickerManager.getInstance().getStickerDate())) {
			List<NameValuePair> postDatas = new ArrayList<NameValuePair>(2);
			postDatas.add(new BasicNameValuePair("username", AccountManager
					.getInstance().getActiveAccount().getAccount()));
			postDatas.add(new BasicNameValuePair("type", "sticker"));
			doPostAsync(context, AppConstants.APIStickerPackage, postDatas,
					null, false);
		} else {
			StickerManager.getInstance().refreshStickerTemp();
			showSticker();
		}
	}

	private void showSticker() {

		String stikerTime = StickerManager.getInstance().getStickerDate();
		AppConfiguration.getInstance().setTimeSticker(stikerTime);
		indexSelect = 0;
		isDetailVisible = false;
		loadingView.setVisibility(View.GONE);
		scrollDetail.setVisibility(View.GONE);
		gridStickerPackage.setVisibility(View.GONE);
		noItemContainer.setVisibility(View.GONE);
		int stickerSize = StickerManager.getInstance().getStickerListAll()
				.size();
		if (stickerSize >= 1) {
			gridStickerPackage.setVisibility(View.VISIBLE);
			listStickerPackage.clear();
			for (int i = 0; i < StickerManager.getInstance()
					.getStickerListAll().size(); i++) {
				listStickerPackage.add(StickerManager.getInstance()
						.getStickerListAll().get(i).getThumbnail());
			}
			adapterStickerPackage.setList(listStickerPackage);
		} else {
			noItemContainer.setVisibility(View.VISIBLE);
		}
	}

	private void loadIkonia() {
		indexSelect = 1;
		loadingView.setVisibility(View.VISIBLE);
		if (!AppConfiguration.getInstance().getTimeIkonia()
				.equals(StickerManager.getInstance().getIkoniaDate())) {
			List<NameValuePair> postDataa = new ArrayList<NameValuePair>(2);
			postDataa.add(new BasicNameValuePair("username", AccountManager
					.getInstance().getAccountKu()));
			postDataa.add(new BasicNameValuePair("type", "ikonia"));
			doPostAsync(context, AppConstants.APIStickerPackage, postDataa,
					null, false);
		} else {
			StickerManager.getInstance().refreshStickerTemp();
			showIkonia();
		}
	}

	private void showIkonia() {
		String ikoniaTime = StickerManager.getInstance().getIkoniaDate();
		AppConfiguration.getInstance().setTimeIkonia(ikoniaTime);
		indexSelect = 1;
		isDetailVisible = false;
		loadingView.setVisibility(View.GONE);
		scrollDetail.setVisibility(View.GONE);
		gridStickerPackage.setVisibility(View.GONE);
		noItemContainer.setVisibility(View.GONE);
		int ikoniaSize = StickerManager.getInstance().getIkoniaListAll().size();
		if (ikoniaSize >= 1) {
			gridStickerPackage.setVisibility(View.VISIBLE);
			listStickerPackage.clear();
			for (int i = 0; i < StickerManager.getInstance().getIkoniaListAll()
					.size(); i++) {
				listStickerPackage.add(StickerManager.getInstance()
						.getIkoniaListAll().get(i).getThumbnail());
			}
			adapterStickerPackage.setList(listStickerPackage);
		} else {
			noItemContainer.setVisibility(View.VISIBLE);
		}
	}

	private void downloadItem() {
		List<NameValuePair> postDataNya = new ArrayList<NameValuePair>();
		String accountName = AccountManager.getInstance().getAccountKu();
		if (accountName != null) {
			postDataNya.add(new BasicNameValuePair("username", accountName));
			postDataNya.add(new BasicNameValuePair("package_id", packageID));
			doPostAsync(context, AppConstants.APIStickerPackageDownload,
					postDataNya, null, true);
		}
	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadingView.setVisibility(View.GONE);
			}
		});

		if (result.length() >= 10) {

			if (urlKu == AppConstants.APIStickerPackage) {

				try {
					tmpJsonSelected = new JSONObject(result);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
				if (result.length() >= 10) {
					switch (indexSelect) {
					case 0:
						StickerManager.getInstance().setStickerPackage(result);
						showSticker();
						break;
					case 1:
						StickerManager.getInstance().setIconiaPackage(result);
						showIkonia();
						break;
					}
				} else {
					noItemContainer.setVisibility(View.VISIBLE);
				}
			} else if (urlKu == AppConstants.APIStickerPackageDetail) {
				if (result.length() >= 10) {
					StickerManager.getInstance().setStickerDetail(result,
							packageID);
					parseStickerDetail(result);
					isDetailVisible = true;
				} else {
					noItemContainer.setVisibility(View.VISIBLE);
				}
			} else if (urlKu == AppConstants.APIStickerPackageDownload) {
				if (result.length() >= 10) {
					parseDownloadSticker(result);
				} else {
					Toast.makeText(context,
							"Error Parsing Data Please Contact Support",
							Toast.LENGTH_SHORT).show();
				}

			}
		}
	}

	private void parseDownloadSticker(String result) {
		try {
			JSONObject jsonKu = new JSONObject(result);
			String status = jsonKu.getString("STATUS");
			if (status.equals("SUCCESS")) {
				StickerManager.getInstance().saveSticker(packageID);
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", indexSelect);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void parseStickerDetail(String result) {
		try {
			scrollDetail.post(new Runnable() {
				@Override
				public void run() {
					scrollDetail.scrollTo(0, 0);
				}
			});
			JSONObject jsonKu = new JSONObject(result);
			// JSONArray jsonArrayKu = jsonKu.getJSONArray("STICKERS");
			String namaSticker = jsonKu.getString("PACKAGE_NAME");
			String descSticker = jsonKu.getString("PACKAGE_DESC");
			String allowUse = jsonKu.getString("ALLOW_USE");
			String packagePrice = jsonKu.getString("PACKAGE_PRICE");
			txtStickerName.setText(namaSticker);
			txtStickerDesc.setText(descSticker);
			priceTextView.setText("Rp. " + packagePrice);
			listStickerDetail.clear();
			listStickerDetail = StickerManager.getInstance()
					.parseStickerDetail(result, packageID);
			aq.id(R.id.StickerSelectImgDetail)
					.progress(R.id.StickerSelectProgressDetail)
					.image(listStickerDetail.get(0), true, true);
			adapterStickerDetail.setList(listStickerDetail);
			if (StickerManager.getInstance().getIsDownlaod(packageID)) {
				txtDownload.setText("Remove");
				// btnDownload.setEnabled(false);
				btnDownload.setOnClickListener(this);
				btnDownload.setClickable(true);
				btnDownload.setBackgroundResource(R.drawable.btn_defaulta);
			} else if (allowUse.trim().equals("true")) {
				txtDownload.setText("Download");
				btnDownload.setEnabled(true);
				btnDownload.setOnClickListener(this);
				btnDownload.setClickable(true);
				btnDownload.setBackgroundResource(R.drawable.btn_defaultb);
			} else {
				txtDownload.setText("Purchase");
				btnDownload.setEnabled(true);
				btnDownload.setOnClickListener(this);
				btnDownload.setClickable(true);
				btnDownload.setBackgroundResource(R.drawable.btn_defaultb);
			}
			gridStickerPackage.setVisibility(View.GONE);
			scrollDetail.setVisibility(View.VISIBLE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
		if (parent == gridStickerPackage) {
			if (!isDetailVisible) {
				packageID = "";
				if (indexSelect == 0) {
					packageID = StickerManager.getInstance()
							.getStickerListAll().get(pos).getPackageID();
				} else if (indexSelect == 1) {
					packageID = StickerManager.getInstance().getIkoniaListAll()
							.get(pos).getPackageID();
				}
				if (packageID.length() >= 1) {
					List<NameValuePair> postDataNya = new ArrayList<NameValuePair>();
					String accountName = AccountManager.getInstance()
							.getAccountKu();
					if (accountName != null) {
						postDataNya.add(new BasicNameValuePair("username",
								accountName));
						postDataNya.add(new BasicNameValuePair("package_id",
								packageID));
						doPostAsync(context,
								AppConstants.APIStickerPackageDetail,
								postDataNya, null, true);
					}
				} else {
					noItemContainer.setVisibility(View.VISIBLE);
				}
			}
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

	private void hideDetail() {
		isDetailVisible = false;
		scrollDetail.setVisibility(View.GONE);
		toggleSelect();
	}

	@Override
	public void onBackPressed() {
		if (isDetailVisible) {
			hideDetail();
		} else {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", "");
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
	}

}
