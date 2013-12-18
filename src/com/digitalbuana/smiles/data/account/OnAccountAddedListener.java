package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;

public interface OnAccountAddedListener extends BaseManagerInterface {
	void onAccountAdded(AccountItem accountItem);

}
