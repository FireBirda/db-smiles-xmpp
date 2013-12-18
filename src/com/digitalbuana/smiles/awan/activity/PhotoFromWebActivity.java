package com.digitalbuana.smiles.awan.activity;

import com.androidquery.AQuery;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class PhotoFromWebActivity extends Activity{
	private WebView webView;
	private AQuery aq;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		aq = new AQuery(this);		
		setContentView(R.layout.charge_popup);
		webView = (WebView) findViewById(R.id.chargeWebView);		
		Intent i = getIntent();
		String photoUrl = i.getStringExtra("photourl");
		aq.id(webView).progress(ScreenHelper.getDialogProgress(this)).webImage(photoUrl);
	}
}
