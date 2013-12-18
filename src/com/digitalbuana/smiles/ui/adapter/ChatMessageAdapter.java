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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.SettingsManager.ChatsDivide;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.extension.muc.RoomContact;
import com.digitalbuana.smiles.data.message.ChatAction;
import com.digitalbuana.smiles.data.message.MessageItem;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

public class ChatMessageAdapter extends BaseAdapter implements UpdatableAdapter {

	public static final int TYPE_MESSAGE = 0;
	public static final int TYPE_EMPTY = 2;
	public static final int TYPE_STICKER = 11;
	public static final int TYPE_IKONIA = 12;
	public static final int TYPE_LOCATION = 13;
	public static final int TYPE_BROADCAST = 14;
	public static final int TYPE_ATTENTION = 15;
	public static final int TYPE_IMAGE = 16;
//	private static final int TYPE_VIDEO = 17;
//	private static final int TYPE_SOUND = 18;
//	private static final int TYPE_CONTACT = 19;
	private String TAG = getClass().getSimpleName();	
	
	private final Activity activity;
	private String account;
	private String user;
	private boolean isMUC;
	private List<MessageItem> messages;

	private final int appearanceStyle;

	private final String divider;

	private String hint;
	public ChatMessageAdapter(Activity activity) {
		this.activity = activity;
		messages = Collections.emptyList();
		account = null;
		user = null;
		appearanceStyle = SettingsManager.chatsAppearanceStyle();
		ChatsDivide chatsDivide = SettingsManager.chatsDivide();
		if (chatsDivide == ChatsDivide.always || (chatsDivide == ChatsDivide.portial && !activity.getResources().getBoolean(R.bool.landscape)))
			divider = "\n";
		else
			divider = " ";
		
	}

	@Override
	public int getCount() {
		return messages.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position < messages.size())
			return messages.get(position);
		else
			return null;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		if (position < messages.size()){
			return TYPE_MESSAGE;
		} else {
			return TYPE_EMPTY;
		}

	}

	private void append(SpannableStringBuilder builder, CharSequence text,CharacterStyle span) {
		int start = builder.length();
		builder.append(text);
		builder.setSpan(span, start, start + text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int type = getItemViewType(position);
		final View view;
		if (convertView == null) {
			final int resource;
			if(type==TYPE_MESSAGE){
				resource = R.layout.chat_viewer_message;
			} else if (type == TYPE_EMPTY){
				resource = R.layout.chat_viewer_empty;
			} else {
				throw new IllegalStateException();
			}
			view = activity.getLayoutInflater().inflate(resource, parent, false);
			FontUtils.setRobotoFont(activity.getApplicationContext(), view);
		} else {
			view = convertView;
		}
		
		if(type==TYPE_EMPTY){
			return view;
		}
		
		final MessageItem messageItem = (MessageItem) getItem(position);
		final String resource = messageItem.getResource();
		final boolean incoming = messageItem.isIncoming();
		final String txtTemp = messageItem.getText();
		int typeMessage = getMessageType(txtTemp);
		View rightContainer = (View) view.findViewById(R.id.messageRightTemp);
		View leftContainer = (View) view.findViewById(R.id.messageLeftTemp);
		View rootMessageContainer = (View) view.findViewById(R.id.rootMessageContainer);
		Spannable text = messageItem.getSpannable(txtTemp);
		String time = StringUtils.getSmartTimeText(messageItem.getTimestamp());
		SpannableStringBuilder builderTime = new SpannableStringBuilder();
		ChatAction action = messageItem.getAction();
		TextView timeRight = (TextView) view.findViewById(R.id.timeRight);
		TextView timeLeft = (TextView) view.findViewById(R.id.timeLeft);
		if (action == null) {
			int messageResource = R.drawable.ic_message_delivered;
			if (!incoming) {
				if (messageItem.isError())
					messageResource = R.drawable.ic_message_has_error;
				else if (!messageItem.isSent())
					messageResource = R.drawable.ic_message_not_sent;
				else if (!messageItem.isDelivered())
					messageResource = R.drawable.ic_message_not_delivered;
			}
			append(builderTime, " ", new ImageSpan(activity, messageResource));
			Emoticons.getSmiledText(activity.getApplication(), text);
			if (messageItem.getTag() == null){
				builderTime.append(time);
			}
		}
		if(action==ChatAction.attention_called||action==ChatAction.attention_requested){
			typeMessage=TYPE_ATTENTION;
		}
		timeLeft.setText(builderTime);
		timeRight.setText(builderTime);
		
		if(incoming){
			leftContainer.setVisibility(View.GONE);
			rightContainer.setVisibility(View.VISIBLE);
			((RelativeLayout.LayoutParams) rootMessageContainer.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else {
			rightContainer.setVisibility(View.GONE);
			leftContainer.setVisibility(View.VISIBLE);
			((RelativeLayout.LayoutParams) rootMessageContainer.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}

		hideAll(view);
		if(typeMessage == TYPE_STICKER || typeMessage == TYPE_IKONIA){
			return showSticker(position, typeMessage, txtTemp,incoming, view);
		} else if(typeMessage==TYPE_LOCATION){
			return view;
		}  else if(typeMessage==TYPE_ATTENTION){
			return showAttention(view,leftContainer,rightContainer);
		} else if(typeMessage==TYPE_IMAGE){
			return showImage(view, txtTemp,incoming);
		} else {
			showMessage(typeMessage,resource, incoming, txtTemp, messageItem, view);
		}
		
		return view;
	}
	
	private void hideAll(View rootView){
		rootView.setClickable(false);
		View messageContainer = (View) rootView.findViewById(R.id.messageContainer);
		ImageView stickerContainer = (ImageView) rootView.findViewById(R.id.stickerContainer);
		if(messageContainer!=null){
			messageContainer.setVisibility(View.GONE);
		}
		if(stickerContainer!=null){
			stickerContainer.setVisibility(View.GONE);	
		}
	}
	private View showImage(View rootView, final String txtTemp,boolean isComming){
		rootView.setClickable(true);
		final ImageView stickerImage = (ImageView) rootView.findViewById(R.id.stickerContainer);
		if(stickerImage!=null){
			stickerImage.setVisibility(View.VISIBLE);
			int tempStickerHeight = ViewUtilities.GetInstance().getHeight()/5;
			if(tempStickerHeight<=ViewUtilities.GetInstance().convertDPtoPX(100)){
				tempStickerHeight = ViewUtilities.GetInstance().convertDPtoPX(100);
			}
			stickerImage.getLayoutParams().height= tempStickerHeight;
			stickerImage.getLayoutParams().width= tempStickerHeight;
			
			if(isComming){
				stickerImage.setScaleType(ImageView.ScaleType.FIT_START);
			} else {
				stickerImage.setScaleType(ImageView.ScaleType.FIT_END);
			}
			
			String desc = txtTemp.replace(AppConstants.UniqueKeyFileImage, "");
			int indexURL = desc.indexOf(AppConstants.UniqueKeyURL);
			String url = AppConstants.FileSavedURL+desc.substring(indexURL, desc.length());
			final String urlFinal = url.replace(AppConstants.UniqueKeyURL, "");
			desc = desc.substring(0, indexURL);
			stickerImage.setImageBitmap(null);
			if(urlFinal != null){
				AQuery aq = new AQuery(rootView);
				aq.id(R.id.stickerContainer).image(urlFinal, true, true);
				stickerImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(txtTemp.contains(AppConstants.UniqueKeyFileImage)){
							openMedia(urlFinal, TYPE_IMAGE);
						}
					}
				});
			}

		}
		return rootView;
	}
	
	private void openMedia(String path, int type){
		Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
		Log.i(TAG, "Opening : "+path);
		if(type==TYPE_IMAGE){
			File file = new File(path);  
			newIntent.setDataAndType(Uri.fromFile(file),"image/*");
			activity.startActivity(newIntent);
		}
	}
	private View showAttention(View rootView, View leftContainer, View rightConteiner){
		rootView.setClickable(true);
		leftContainer.setVisibility(View.GONE);
		rightConteiner.setVisibility(View.GONE);
		final ImageView stickerImage = (ImageView) rootView.findViewById(R.id.stickerContainer);
		if(stickerImage!=null){
			stickerImage.setVisibility(View.VISIBLE);
			int tempStickerHeight = ViewUtilities.GetInstance().getHeight()/3;
			if(tempStickerHeight<=ViewUtilities.GetInstance().convertDPtoPX(100)){
				tempStickerHeight = ViewUtilities.GetInstance().convertDPtoPX(100);
			}
			stickerImage.getLayoutParams().height= tempStickerHeight;
			stickerImage.getLayoutParams().width= ViewUtilities.GetInstance().getWidth();
			stickerImage.setImageResource(R.drawable.img_kakatua_kaget);
			stickerImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		return rootView;
	}
	
	private View showSticker(int position,int typeMessage, String txtTemp,boolean isComming,  View rootView){
		rootView.setClickable(true);
		if(typeMessage==TYPE_STICKER){
			txtTemp = txtTemp.replace(AppConstants.UniqueKeySticker, "");
		} else {
			txtTemp = txtTemp.replace(AppConstants.UniqueKeyIkonia, "");
		}
		txtTemp = txtTemp.replace(" ", "");
		final ImageView stickerImage = (ImageView) rootView.findViewById(R.id.stickerContainer);
		if(stickerImage!=null){
			stickerImage.setVisibility(View.VISIBLE);
			int tempStickerHeight = ViewUtilities.GetInstance().getHeight()/3;
			if(tempStickerHeight<=ViewUtilities.GetInstance().convertDPtoPX(100)){
				tempStickerHeight = ViewUtilities.GetInstance().convertDPtoPX(100);
			}
			stickerImage.getLayoutParams().height= tempStickerHeight;
			stickerImage.getLayoutParams().width= tempStickerHeight;
//			imageLoader.displayImage(txtTemp, stickerImage, options);	
			if(isComming){
				stickerImage.setScaleType(ImageView.ScaleType.FIT_START);
			} else {
				stickerImage.setScaleType(ImageView.ScaleType.FIT_END);
			}
		}
		return rootView;
	}
	
	private View showMessage(
			int typeMessage, String resource,
			boolean incoming, String txtTemp, 
			MessageItem messageItem,
			View rootView){
		View messageContainer = (View) rootView.findViewById(R.id.messageContainer);
		if(messageContainer!=null){
			messageContainer.setVisibility(View.VISIBLE);
		}
		String name;
		if (isMUC) {
			name = resource.replace("@"+AppConstants.XMPPServerHost, "");
			txtTemp = name +" : "+txtTemp;
		} 

		if (incoming) {
			messageContainer.setBackgroundResource(R.drawable.img_bg_chat_other);
		} else {
			messageContainer.setBackgroundResource(R.drawable.img_bg_chat_me);
		}
		if(typeMessage==TYPE_BROADCAST){
			if (incoming) {
				messageContainer.setBackgroundResource(R.drawable.img_bg_chat_me_broadcast);
			} else {
				messageContainer.setBackgroundResource(R.drawable.img_bg_chat_other_broadcast);
			}
			txtTemp = txtTemp.replace(AppConstants.UniqueKeyBroadcast, "");
		}
		Spannable textKu = messageItem.getSpannable(txtTemp);
		TextView textMessage = (TextView) rootView.findViewById(R.id.text);
		ChatAction action = messageItem.getAction();
//		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (action == null) {
			Emoticons.getSmiledText(activity.getApplication(), textKu);
//			if (messageItem.getTag() == null){
//				builder.append(textKu);
//			}
		}
		textMessage.setText(txtTemp);
//		Log.e(AppConstants.TAG,"txtTemp "+txtTemp);
//		Log.e(AppConstants.TAG,"builder "+textKu.toString());
		textMessage.setMovementMethod(LinkMovementMethod.getInstance());
		return rootView;
	}
	
	private int getMessageType(String text){
		if(text.contains(AppConstants.UniqueKeySticker)){
			return TYPE_STICKER;
		} else if(text.contains(AppConstants.UniqueKeyIkonia)){
			return TYPE_IKONIA;
		} else if(text.contains(AppConstants.UniqueKeyBroadcast)){
			return TYPE_BROADCAST;
		} else if(text.contains(AppConstants.UniqueKeyLocation)){
			return TYPE_LOCATION;
		} else if(text.contains(AppConstants.UniqueKeyFileImage)){
			return TYPE_IMAGE;
		} else {
			return TYPE_MESSAGE;
		}
	}
	

	public String getAccount() {
		return account;
	}

	public String getUser() {
		return user;
	}

	public void setChat(String account, String user) {
		this.account = account;
		this.user = user;
		this.isMUC = MUCManager.getInstance().hasRoom(account, user);
		onChange();
	}

	@Override
	public void onChange() {
		messages = new ArrayList<MessageItem>(MessageManager.getInstance().getMessages(account, user));
		notifyDataSetChanged();
	}
	
	/**
	 * Contact information has been changed. Renews hint and updates data if
	 * necessary.
	 */
	public void updateInfo() {
		String info = getHint();
		if (this.hint == info || (this.hint != null && this.hint.equals(info)))
			return;
		this.hint = info;
		notifyDataSetChanged();
	}
	/**
	 * @return New hint.
	 */
	private String getHint() {
		AccountItem accountItem = AccountManager.getInstance().getAccount(
				account);
		boolean online;
		if (accountItem == null)
			online = false;
		else
			online = accountItem.getState().isConnected();
		final AbstractContact abstractContact = RosterManager.getInstance()
				.getBestContact(account, user);
		if (!online) {
			if (abstractContact instanceof RoomContact)
				return activity.getString(R.string.muc_is_unavailable);
			else
				return activity.getString(R.string.account_is_offline);
		} else if (!abstractContact.getStatusMode().isOnline()) {
			if (abstractContact instanceof RoomContact)
				return activity.getString(R.string.muc_is_unavailable);
			else
				return activity.getString(R.string.contact_is_offline,
						abstractContact.getName());
		}
		return null;
	}


}
