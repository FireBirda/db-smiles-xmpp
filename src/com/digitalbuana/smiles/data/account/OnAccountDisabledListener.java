package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;

public interface OnAccountDisabledListener extends BaseManagerInterface {
	void onAccountDisabled(AccountItem accountItem);

}
