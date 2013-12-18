package com.digitalbuana.smiles.data.extension.archive;

import android.content.Intent;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.entity.AccountRelated;
import com.digitalbuana.smiles.data.notification.AccountNotificationItem;
import com.digitalbuana.smiles.ui.ArchiveRequest;

public class AvailableArchiveRequest extends AccountRelated implements
		AccountNotificationItem {

	public AvailableArchiveRequest(String account) {
		super(account);
	}

	@Override
	public Intent getIntent() {
		return ArchiveRequest.createIntent(Application.getInstance(), account);
	}

	@Override
	public String getTitle() {
		return Application.getInstance().getString(R.string.archive_available_request_title);
	}

	@Override
	public String getText() {
		return AccountManager.getInstance().getVerboseName(account);
	}

}
