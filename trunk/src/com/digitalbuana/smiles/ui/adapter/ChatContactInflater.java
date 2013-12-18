package com.digitalbuana.smiles.ui.adapter;

import java.util.Date;

import android.app.Activity;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView.BufferType;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.DateHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.MessageTable;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.utils.Emoticons;

/**
 * Inflate view with contact's last message or status text as well as active
 * chat badge.
 * 
 * @author alexander.ivanov
 * 
 */
public class ChatContactInflater extends BaseContactInflater {

	private final int textColorPrimary;
	private final int textColorSecondary;
	private String TAG = getClass().getSimpleName();

	public ChatContactInflater(Activity activity) {
		super(activity);
		TypedArray typedArray;
		typedArray = activity.getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.textColorPrimary,
						android.R.attr.textColorSecondary, });
		textColorPrimary = typedArray.getColor(0, 0);
		textColorSecondary = typedArray.getColor(1, 0);
		typedArray.recycle();
	}

	@Override
	ViewHolder createViewHolder(int position, View view) {
		return new ViewHolder(view);
	}

	@Override
	String getStatusText(AbstractContact abstractContact) {
		if (MessageManager.getInstance().hasActiveChat(
				abstractContact.getAccount(), abstractContact.getUser()))
			return MessageManager.getInstance().getLastText(
					abstractContact.getAccount(), abstractContact.getUser());
		else
			return super.getStatusText(abstractContact);
	}

	@Override
	public void getView(View view, AbstractContact abstractContact) {
		super.getView(view, abstractContact);

		// final ViewHolder contactViewHolder = (ViewHolder) view.getTag();
		ViewHolder contactViewHolder = new ViewHolder(view);

		contactViewHolder.textUnreadCounter.setVisibility(View.GONE);
		contactViewHolder.textLastUnreadTime.setVisibility(View.GONE);
		contactViewHolder.chatStatusImageView.setVisibility(View.GONE);

		if (MessageManager.getInstance().hasActiveChat(
				abstractContact.getAccount(), abstractContact.getUser())) {

			String user = abstractContact.getUser();
			String txt = contactViewHolder.status.getText().toString();

			if (user.contains(AppConstants.XMPPGroupsServer)
					|| user.contains(AppConstants.XMPPRoomsServer)) {
			} else {
				txt = txt.replace("Send an ", "").replace("Send a ", "");
			}

			contactViewHolder.status.setText(Emoticons.getSmiledText(
					this.activity.getBaseContext(), txt), BufferType.SPANNABLE);

			contactViewHolder.name.setTextColor(textColorPrimary);
			contactViewHolder.status.setTextColor(textColorSecondary);

			Cursor cursors = MessageTable.getInstance().last(
					abstractContact.getAccount(), abstractContact.getUser());

			if (cursors.getCount() > 0) {

				cursors.moveToFirst();

				boolean isIncoming = MessageTable.isIncoming(cursors);
				boolean isDelivered = MessageTable.isDelivered(cursors);
				boolean isSent = MessageTable.isSent(cursors);
				boolean isErr = MessageTable.hasError(cursors);
				boolean isRead = MessageTable.isRead(cursors);

				Date timeStamp = MessageTable.getTimeStamp(cursors);
				String lastDate = DateHelper.setRecentChatTime(timeStamp);
				contactViewHolder.textLastUnreadTime.setText(lastDate);
				contactViewHolder.textLastUnreadTime
						.setVisibility(View.VISIBLE);

				String lastMessage = MessageTable.getText(cursors);

				if (lastMessage != null && !lastMessage.trim().equals("")) {

					if (!isIncoming) {

						contactViewHolder.chatStatusImageView
								.setVisibility(View.VISIBLE);

						int messageResource = R.drawable.ic_message_not_sent;

						try {

							boolean isReadByFrind = MessageTable
									.getReadByFriend(cursors);

							if (isReadByFrind) {
								messageResource = R.drawable.ic_message_read;
							} else {
								if (isDelivered) {
									messageResource = R.drawable.ic_message_delivered;
								} else {
									if (isSent) {
										messageResource = R.drawable.ic_message_not_sent;
									} else if (isErr) {
										messageResource = R.drawable.ic_message_has_error;
									} else {
										messageResource = R.drawable.ic_message_has_error;
									}
								}
							}
						} catch (NullPointerException e) {
							Log.e(TAG, "NullPointerException");
						}

						contactViewHolder.chatStatusImageView
								.setImageResource(messageResource);

					} else if (isIncoming && !lastMessage.trim().equals("")) {

						contactViewHolder.chatStatusImageView
								.setVisibility(View.GONE);

						if (!isRead) {

							Cursor readCounter = MessageTable.getInstance()
									.unreadCounter(
											abstractContact.getAccount(),
											abstractContact.getUser());

							contactViewHolder.textUnreadCounter.setText(String
									.valueOf(readCounter.getCount()));

							contactViewHolder.textUnreadCounter
									.setVisibility(View.VISIBLE);

						}
					}
				}
			}

			cursors.close();

		} else {
			contactViewHolder.chatStatusImageView.setVisibility(View.GONE);
			contactViewHolder.name.setTextColor(textColorPrimary);
			contactViewHolder.status.setTextColor(textColorSecondary);
		}
	}

	@Override
	View createView(int position, ViewGroup parent) {
		return layoutInflater
				.inflate(R.layout.base_contact_item, parent, false);
	}

}
