package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;

import com.digitalbuana.smiles.data.BaseUIListener;

public interface OnAdminUpdateChangeListener extends BaseUIListener {
	public void onAdminUpdateChanged(ArrayList<AdminModel> listNotification);
}
