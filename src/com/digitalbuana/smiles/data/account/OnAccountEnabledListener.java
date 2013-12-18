package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;


public interface OnAccountEnabledListener extends BaseManagerInterface {
	void onAccountEnabled(AccountItem accountItem);
}
