/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.ui.helper.AbstractAvatarInflaterHelper;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;


public abstract class BaseContactInflater {

	final Activity activity;

	final LayoutInflater layoutInflater;

	final AbstractAvatarInflaterHelper avatarInflaterHelper;

	BaseAdapter adapter;
	
	private int MAX_RECENT_CHAT_LENGTH = 35;

	public BaseContactInflater(Activity activity) {
		this.activity = activity;
		layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		avatarInflaterHelper = AbstractAvatarInflaterHelper.createAbstractContactInflaterHelper();
	}


	void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
	}

	abstract View createView(int position, ViewGroup parent);

	abstract ViewHolder createViewHolder(int position, View view);

	String getStatusText(AbstractContact abstractContact) {
		return abstractContact.getStatusText();
	}

	public void getView(View view, AbstractContact abstractContact) {
		
		ViewHolder viewHolder = new ViewHolder(view);//(ViewHolder) view.getTag();

		viewHolder.avatar.setVisibility(View.VISIBLE);
		viewHolder.avatar.setImageDrawable(abstractContact.getAvatarForContactList());
		avatarInflaterHelper.updateAvatar(viewHolder.avatar,abstractContact);

		viewHolder.name.setText(StringUtils.replaceStringEquals(abstractContact.getName()).trim());
		final String statusText = trimText(getStatusText(abstractContact));
		if ("".equals(statusText)) {
			viewHolder.name.setGravity(Gravity.CENTER_VERTICAL);
			viewHolder.status.setVisibility(View.GONE);
		} else {			
			viewHolder.name.setGravity(Gravity.BOTTOM);
			viewHolder.status.setText(
					Emoticons.getSmiledText(
						this.activity.getBaseContext(),
						StringUtils.replaceStringEquals(statusText).trim()
					)
					,BufferType.SPANNABLE
			);
			viewHolder.status.setVisibility(View.VISIBLE);
		}
		FontUtils.setRobotoFont(activity.getApplicationContext(), viewHolder.name);
		FontUtils.setRobotoFont(activity.getApplicationContext(), viewHolder.status);
		
		//viewHolder.name.setText(StringUtils.replaceStringEquals(abstractContact.getName()).trim());
	}

	class ViewHolder {

		ImageView color;
		ImageView avatar;
		LinearLayout panel;
		TextView name;
		TextView status;
		ImageView chatStatusImageView;
		TextView textUnreadCounter;
		TextView textLastUnreadTime;
		LinearLayout messageCounterHolderParent;

		public ViewHolder(View view) {
			color = (ImageView) view.findViewById(R.id.color);
			avatar = (ImageView) view.findViewById(R.id.avatar);
			panel = (LinearLayout) view.findViewById(R.id.panel);
			name = (TextView) view.findViewById(R.id.name);
			status = (TextView) view.findViewById(R.id.status);
			chatStatusImageView = (ImageView)view.findViewById(R.id.chatStatusImageView);
			textUnreadCounter = (TextView)view.findViewById(R.id.textUnreadCounter);
			textLastUnreadTime = (TextView)view.findViewById(R.id.textLastUnreadTime);
			messageCounterHolderParent = (LinearLayout)view.findViewById(R.id.messageCounterHolderParent);
		}
	}
	
	private String trimText(String text) {
		if (text.length() > MAX_RECENT_CHAT_LENGTH)
			return text.substring(0, MAX_RECENT_CHAT_LENGTH - 3) + "...";
		else
			return text;

	}

}
