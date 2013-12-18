package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;


public interface OnAccountOfflineListener extends BaseManagerInterface {

	void onAccountOffline(AccountItem accountItem);

}
