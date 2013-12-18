package com.digitalbuana.smiles.data.account;

import java.util.Collection;

import com.digitalbuana.smiles.data.BaseUIListener;

public interface OnAccountChangedListener extends BaseUIListener {
	public void onAccountsChanged(Collection<String> accounts);
}
