package com.digitalbuana.smiles.data.account;

import com.digitalbuana.smiles.data.BaseManagerInterface;

public interface OnAccountArchiveModeChangedListener extends BaseManagerInterface {

	void onAccountArchiveModeChanged(AccountItem accountItem);

}
