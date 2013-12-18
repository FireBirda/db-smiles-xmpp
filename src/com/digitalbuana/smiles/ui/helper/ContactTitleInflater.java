package com.digitalbuana.smiles.ui.helper;

import org.jivesoftware.smackx.ChatState;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.extension.cs.ChatStateManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.StringUtils;

public class ContactTitleInflater {

	public static void updateTitle(View titleView, final Activity activity, AbstractContact abstractContact) {
		final TextView nameView = (TextView) titleView.findViewById(R.id.name);
		final ImageView avatarView = (ImageView) titleView.findViewById(R.id.avatar);
		final TextView statusTextView = (TextView) titleView.findViewById(R.id.status_text);
		final FrameLayout backButton = (FrameLayout) titleView.findViewById(R.id.chatViewBackBtn);
		nameView.setText(StringUtils.replaceStringEquals(abstractContact.getName()));
		avatarView.setImageDrawable(abstractContact.getAvatar());
		ChatState chatState = ChatStateManager.getInstance().getChatState(abstractContact.getAccount(), abstractContact.getUser());
		final CharSequence statusText;
		if (chatState == ChatState.composing)
			statusText = activity.getString(R.string.chat_state_composing);
		else if (chatState == ChatState.paused)
			statusText = activity.getString(R.string.chat_state_paused);
		else
			statusText = Emoticons.getSmiledText(activity, abstractContact.getStatusText());
		statusTextView.setText(statusText);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});
	}

}
