package com.digitalbuana.smiles.ui.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.digitalbuana.smiles.utils.ViewUtilities;

public class BaseUIListActivity extends ManagedListActivity
{
	protected FrameLayout rootView;
	protected Context context;
	protected int viewHeight;
	protected int viewWidth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		ViewUtilities.GetInstance().setResolution(this);
	}

	protected void resettingView()
	{
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
