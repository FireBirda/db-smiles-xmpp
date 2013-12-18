package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;

import com.digitalbuana.smiles.data.BaseUIListener;

public interface OnVisitorChangeListener extends BaseUIListener {
	public void onVisitorChanged(ArrayList<FriendsModel> listVisitor);
}
