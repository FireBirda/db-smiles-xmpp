package com.digitalbuana.smiles.data.roster;

import android.content.Intent;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.notification.EntityNotificationItem;
import com.digitalbuana.smiles.ui.ContactAdd;
import com.digitalbuana.smiles.utils.StringUtils;

public class SubscriptionRequest extends BaseEntity implements EntityNotificationItem {

	public SubscriptionRequest(String account, String user) {
		super(account, user);
	}

	@Override
	public Intent getIntent() {
		return ContactAdd.createSubscriptionIntent(Application.getInstance(),account, user);
	}

	@Override
	public String getText() {
		return Application.getInstance().getString(R.string.subscription_request_message);
	}

	@Override
	public String getTitle() {
		return StringUtils.replaceStringEquals(user);
	}

	public String getConfirmation() {
		String accountName = AccountManager.getInstance().getVerboseName(account);
		String userName = RosterManager.getInstance().getName(account, user);
		return Application.getInstance().getString(R.string.contact_subscribe_confirm, userName, accountName);
	}

}
