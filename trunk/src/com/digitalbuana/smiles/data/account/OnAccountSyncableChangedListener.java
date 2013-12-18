package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;


public interface OnAccountSyncableChangedListener extends BaseManagerInterface {

	void onAccountSyncableChanged(AccountItem accountItem);

}
