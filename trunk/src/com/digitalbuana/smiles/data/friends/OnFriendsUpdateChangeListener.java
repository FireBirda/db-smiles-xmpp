package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;

import com.digitalbuana.smiles.data.BaseUIListener;

public interface OnFriendsUpdateChangeListener extends BaseUIListener {
	public void onFriendsUpdateChanged(ArrayList<FriendsModel> listVisitor);
}
