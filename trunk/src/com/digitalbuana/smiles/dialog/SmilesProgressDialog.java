package com.digitalbuana.smiles.dialog;

import com.digitalbuana.smiles.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.TextView;

public class SmilesProgressDialog extends Dialog {
	
	public SmilesProgressDialog(Context context, String txtLoading) {
		super(context, R.style.SmilesTransparantDialogTheme);
		setContentView(R.layout.dialog_progress);
		
		TextView loadingTxt = (TextView)findViewById(R.id.dialogProgresTxt);
		if(txtLoading!=null){
			loadingTxt.setText(txtLoading);
		} else {
			loadingTxt.setText("LOADING");
		}
		final ImageView kakatuaView = (ImageView)findViewById(R.id.dialogProgresImg);
		if(kakatuaView!=null){
			kakatuaView.setBackgroundResource(R.drawable.kakatua_junior_anim);
			kakatuaView.post(new Runnable() {
				@Override
				public void run() {
					AnimationDrawable anim = (AnimationDrawable) kakatuaView.getBackground();
			        anim.start();
				}
			});
		}
		
	}
}
