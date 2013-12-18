package com.digitalbuana.smiles.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.utils.FontUtils;

public class SmilesDefaultDialog extends Dialog {

	private FrameLayout rootView;
	private FrameLayout btnOK;
	private TextView txtTitle;
	private TextView txtMessge;
	
	private String title;
	private String message;

	private Context context;
	
	public SmilesDefaultDialog(Context context, String title, String message) {
		super(context, R.style.SmilesTransparantDialogTheme);
		
		this.title = title;
		this.message = message;
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_default);
		
		rootView = (FrameLayout)findViewById(R.id.dialogDefaultRoot);
		btnOK = (FrameLayout)findViewById(R.id.dialogDefaultBtnOK);
		
		txtTitle = (TextView)findViewById(R.id.dialogDefaultTitle);
		txtMessge = (TextView)findViewById(R.id.dialogDefaultMessage);

		txtTitle.setText(title);
		txtMessge.setText(message);
		
		FontUtils.setRobotoFont(context, rootView);
		
		//Animating
		final ImageView imgAnim = (ImageView)findViewById(R.id.dialogDefaultAnim);
		imgAnim.setBackgroundResource(R.drawable.kakatua_kedip_anim);
		rootView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) imgAnim.getBackground();
		        anim.start();
			}
		});
		
		btnOK.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		this.setCanceledOnTouchOutside(true);
	}

}
