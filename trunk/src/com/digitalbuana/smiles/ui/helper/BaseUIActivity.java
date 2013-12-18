package com.digitalbuana.smiles.ui.helper;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.ActivityManager;
import com.digitalbuana.smiles.utils.ViewUtilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class BaseUIActivity extends ConnectionActivity
{
	protected FrameLayout rootView;
	protected Context context;
	protected int viewHeight;
	protected int viewWidth;
	
	
	protected Animation shake;
	protected Animation fadein;
	protected Animation fadeout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.getInstance().onCreate(this);
		context = this;
		ViewUtilities.GetInstance().setResolution(this);
		
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		resettingView();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	protected void resettingView()
	{
		ViewUtilities.GetInstance().setResolution(this);
		viewHeight = ViewUtilities.GetInstance().getHeight();
		viewWidth = ViewUtilities.GetInstance().getWidth();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ViewUtilities.GetInstance().setResolution(this);
		resettingView();
	}
	
}
