package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;

public interface OnAccountRemovedListener extends BaseManagerInterface {
	void onAccountRemoved(AccountItem accountItem);

}
