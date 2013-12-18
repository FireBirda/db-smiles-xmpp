package com.digitalbuana.smiles.data.message;

import com.digitalbuana.smiles.data.BaseUIListener;

public interface OnChatChangedListener extends BaseUIListener {
	public void onChatChanged(String account, String user, boolean incoming);
}
