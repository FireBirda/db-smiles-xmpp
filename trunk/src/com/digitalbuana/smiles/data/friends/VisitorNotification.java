package com.digitalbuana.smiles.data.friends;

import android.content.Intent;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.VisitorActivity;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.notification.EntityNotificationItem;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.StringUtils;

public class VisitorNotification extends BaseEntity implements
		EntityNotificationItem {

	private FriendsModel visitor;

	public VisitorNotification(String account, String user, FriendsModel visitor) {
		super(account, user);
		this.visitor = visitor;
	}

	@Override
	public Intent getIntent() {
		return VisitorActivity.createClearTopIntent(Application.getInstance(),
				account, user);
	}

	@Override
	public String getTitle() {
		return RosterManager.getInstance().getBestContact(account, user)
				.getName()
				+ " "
				+ Application.getInstance().getString(R.string.visitor_view);
	}

	@Override
	public String getText() {
		return StringUtils.parseTimeVisitor(visitor.getTime());
	}

}
