package com.digitalbuana.smiles.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.utils.FontUtils;

public class SmilesConfirmDialog extends Dialog {

	private boolean isConfirm =false;
	private OnSmilesDialogClose listener=null;
	
	private FrameLayout rootView;
	private FrameLayout btnClose;
	private TextView txtTitle;
	private TextView txtMessage;
	
	private FrameLayout btnOK;
	private FrameLayout btnCancel;
	
	public SmilesConfirmDialog(Context context, final OnSmilesDialogClose listenernya, String title, String message) {
		super(context, R.style.SmilesTransparantDialogTheme);
		setContentView(R.layout.dialog_confirm);
		
		rootView = (FrameLayout)findViewById(R.id.dialogConfirmRoot);
		btnClose = (FrameLayout)findViewById(R.id.dialogConfirmBtnClose);
		txtTitle = (TextView)findViewById(R.id.dialogConfirmTitle);
		txtMessage = (TextView)findViewById(R.id.dialogConfirmMessage);
		
		btnOK = (FrameLayout)findViewById(R.id.dialogConfirmBtnOK);
		btnCancel= (FrameLayout)findViewById(R.id.dialogConfirmBtnCancel);
		
		
		txtTitle.setText(title);
		txtMessage.setText(message);
		
		btnClose.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		this.listener = listenernya;
		isConfirm =false;
		
		btnOK.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listenernya.onSmilesDialogClose(true);
				dismiss();
			}
		});
		btnCancel.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listenernya.onSmilesDialogClose(false);
				dismiss();
			}
		});
		
		FontUtils.setRobotoFont(context, rootView);
		
		setCanceledOnTouchOutside(true);
		//Animating
		final ImageView imgAnim = (ImageView)findViewById(R.id.dialogConfirmAnim);
		imgAnim.setBackgroundResource(R.drawable.kakatua_kedip_anim);
		rootView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) imgAnim.getBackground();
		        anim.start();
			}
		});
	}

	public interface OnSmilesDialogClose{
		void onSmilesDialogClose(boolean isConfirm);
	}
}
