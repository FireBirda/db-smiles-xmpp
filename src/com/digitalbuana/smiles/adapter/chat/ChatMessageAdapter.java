package com.digitalbuana.smiles.adapter.chat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.DateHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.message.ChatAction;
import com.digitalbuana.smiles.data.message.MessageItem;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.RegularChat;
import com.digitalbuana.smiles.ui.adapter.UpdatableAdapter;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

public class ChatMessageAdapter extends BaseAdapter implements UpdatableAdapter {

	public static final int TYPE_MESSAGE = 0;
	public static final int TYPE_EMPTY = 2;
	public static final int TYPE_LOCATION = 13;
	public static final int TYPE_BROADCAST = 14;
	public static final int TYPE_ATTENTION = 15;
	public static final int TYPE_STICKER = 16;
	public static final int TYPE_IMAGE = 17;
	public static final int TYPE_VIDEO = 18;
	public static final int TYPE_AUDIO = 19;
	public static final int TYPE_CONTACT = 20;
	public static final int TYPE_DATE_GROUP = 21;

	private final Activity activity;
	private static SharedPreferences appPreference;
	private String account;
	private String user;
	private boolean isMUC;
	private List<MessageItem> messages;
	private final static String TAG = "ChatMessageAdapter";

	// private boolean isOnMessageStatus = false;
	// private boolean isFirstTime = false;
	// private boolean isRequestFromDatabase = false;

	private DateFormat df1;// = new DateFormat();

	private CharSequence tdf;

	public ChatMessageAdapter(Activity activity) {
		appPreference = PreferenceManager
				.getDefaultSharedPreferences(Application.getInstance()
						.getApplicationContext());
		this.activity = activity;
		messages = Collections.emptyList();
		account = null;
		user = null;
		df1 = new DateFormat();
		tdf = df1.format("yyyy-MM-dd", new Date());
	}

	public List<MessageItem> getList() {
		return messages;
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

	/*
	 * @Override public int getViewTypeCount() { return 22; }
	 */

	@Override
	public int getItemViewType(int position) {
		if (position < messages.size()) {
			MessageItem messageItem = (MessageItem) getItem(position);
			return getItemViewTypeMessage(messageItem);
		} else {
			return TYPE_EMPTY;
		}
	}

	public int getItemViewTypeMessage(MessageItem messageItem) {

		String temp = messageItem.getText();

		if (temp.contains(AppConstants.UniqueKeyBroadcast)) {
			return TYPE_BROADCAST;
		} else if (temp.contains(AppConstants.UniqueKeyIkonia)) {
			return TYPE_STICKER;
		} else if (temp.contains(AppConstants.UniqueKeySticker)) {
			return TYPE_STICKER;
		} else if (temp.contains(AppConstants.UniqueKeyLocation)) {
			return TYPE_LOCATION;
		} else if (temp.contains(AppConstants.UniqueKeyFileImage)) {
			return TYPE_IMAGE;
		} else if (temp.contains(AppConstants.UniqueKeyFileVideo)) {
			return TYPE_VIDEO;
		} else if (temp.contains(AppConstants.UniqueKeyFileAudio)) {
			return TYPE_AUDIO;
		} else if (temp.contains(AppConstants.UniqueKeyFileContact)) {
			return TYPE_CONTACT;
		} else if (temp.contains(AppConstants.UniqueKeyAttention)) {
			return TYPE_ATTENTION;
		} else if (temp.contains(AppConstants.uniqueKeyDateSeparator)) {
			return TYPE_DATE_GROUP;
		} else {
			ChatAction action = messageItem.getAction();
			if (action == ChatAction.attention_called
					|| action == ChatAction.attention_requested) {
				return TYPE_ATTENTION;
			}
			return TYPE_MESSAGE;
		}
	}

	private void append(SpannableStringBuilder builder, CharSequence text,
			CharacterStyle span) {
		int start = builder.length();
		builder.append(text);
		builder.setSpan(span, start, start + text.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int typeEmp = getItemViewType(position);

		final ViewHolder holder;
		// if (convertView == null) {
		holder = new ViewHolder();

		if (typeEmp == TYPE_EMPTY) {

			LayoutInflater li = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.chat_viewer_empty, parent, false);

		} else if (typeEmp == TYPE_DATE_GROUP) {

			LayoutInflater li = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.item_chat_date_group_separator,
					parent, false);

			TextView chatDateGroupSeparator = (TextView) convertView
					.findViewById(R.id.chatDateGroupSeparator);

			MessageItem messageItem = (MessageItem) getItem(position);

			DateFormat df = new DateFormat();
			CharSequence rdf2 = df.format("dd MMMM yyyy",
					messageItem.getTimestamp());

			chatDateGroupSeparator.setText(rdf2);

			return convertView;

		} else {
			final MessageItem messageItem = (MessageItem) getItem(position);
			final boolean incoming = messageItem.isIncoming();

			switch (typeEmp) {
			case TYPE_MESSAGE:
				/*
				 * hold the presence blank view from MUC
				 */
				if (messageItem.getText().equals("")) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.chat_viewer_empty,
							parent, false);
					return convertView;
				}

				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_incoming,
							parent, false);
					holder.txtMessage = (TextView) convertView
							.findViewById(R.id.itemMessageText);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.itemMessageTime);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_from,
							parent, false);
					holder.txtMessage = (TextView) convertView
							.findViewById(R.id.itemMessageText);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.itemMessageTime);
				}
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_BROADCAST:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_from,
							parent, false);
				}
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.ballon = (FrameLayout) convertView
						.findViewById(R.id.itemMessageBallon);
				holder.txtMessage.setTextColor(Color.parseColor("#b20202"));
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_STICKER:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_image_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_image_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_IMAGE:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.btnOpen = (FrameLayout) convertView
						.findViewById(R.id.itemMessageBtnOpen);
				holder.progres = (ProgressBar) convertView
						.findViewById(R.id.itemMessageProgress);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_VIDEO:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.iconVideo = (ImageView) convertView
						.findViewById(R.id.itemMessageVideoIcon);
				holder.btnOpen = (FrameLayout) convertView
						.findViewById(R.id.itemMessageBtnOpen);
				holder.progres = (ProgressBar) convertView
						.findViewById(R.id.itemMessageProgress);
				holder.iconVideo.setVisibility(View.VISIBLE);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_AUDIO:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.btnOpen = (FrameLayout) convertView
						.findViewById(R.id.itemMessageBtnOpen);
				holder.iconVideo = (ImageView) convertView
						.findViewById(R.id.itemMessageVideoIcon);
				holder.progres = (ProgressBar) convertView
						.findViewById(R.id.itemMessageProgress);
				holder.iconVideo.setVisibility(View.VISIBLE);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_LOCATION:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_file_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.btnOpen = (FrameLayout) convertView
						.findViewById(R.id.itemMessageBtnOpen);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			case TYPE_ATTENTION:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_image_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_image_from, parent,
							false);
				}
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.imgSticker = (ImageView) convertView
						.findViewById(R.id.itemMessageImage);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			default:
				if (incoming) {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_incoming,
							parent, false);
				} else {
					LayoutInflater li = (LayoutInflater) activity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = li.inflate(R.layout.item_message_from,
							parent, false);
				}
				holder.txtMessage = (TextView) convertView
						.findViewById(R.id.itemMessageText);
				holder.txtTime = (TextView) convertView
						.findViewById(R.id.itemMessageTime);
				holder.msgTimeContainer = (FrameLayout) convertView
						.findViewById(R.id.msgTimeContainer);
				break;
			}
		}
		convertView.setTag(holder);
		// } else {
		// // Log.e(AppConstants.TAG, "convert view not null "+position);
		// holder = (ViewHolder)convertView.getTag();
		// }

		if (typeEmp == TYPE_EMPTY) {
			return convertView;
		}

		final MessageItem messageItem = (MessageItem) getItem(position);

		Spannable text;

		try {
			text = messageItem.getSpannable(messageItem.getText());
		} catch (NullPointerException e) {
			text = null;
		}

		final boolean incoming = messageItem.isIncoming();

		String tmpTime = DateHelper.getTimeOnDate(messageItem.getTimestamp());

		if (incoming) {
			Date delay = messageItem.getDelayTimestamp();
			if (delay != null)
				tmpTime = DateHelper.getTimeOnDate(delay);
		}

		final String time = tmpTime;// DateHelper.getTimeOnDate(messageItem.getTimestamp());//
									// StringUtils.getSmartTimeText(messageItem.getTimestamp());
		final SpannableStringBuilder builderTime = new SpannableStringBuilder();
		final ChatAction action = messageItem.getAction();

		if (action == null) {

			if (!incoming) {

				int messageResource = R.drawable.ic_message_not_sent;

				Long currentMessageId = messageItem.getId();
				boolean isReadByFrind = false;
				if (currentMessageId != null && currentMessageId != 0) {
					try {
						isReadByFrind = RegularChat
								.getIsReadByFriend(currentMessageId);
					} catch (CursorIndexOutOfBoundsException e) {
					}
				} else
					isReadByFrind = messageItem.getIsReadByFriend();

				if (isReadByFrind)
					messageResource = R.drawable.ic_message_read;
				else {
					if (messageItem.isDelivered())
						messageResource = R.drawable.ic_message_delivered;
					else {
						if (messageItem.isSent())
							messageResource = R.drawable.ic_message_not_sent;
						else if (messageItem.isError())
							messageResource = R.drawable.ic_message_has_error;
						else
							messageResource = R.drawable.ic_message_has_error;
					}
				}

				append(builderTime, " ", new ImageSpan(activity,
						messageResource));
			}

			Emoticons.getSmiledText(activity.getApplication(), text);

			if (messageItem.getTag() == null) {
				builderTime.append(time);
			}

		}

		if (holder.txtTime != null) {
			if (builderTime.length() >= 4) {

				holder.txtTime.setText(builderTime, BufferType.SPANNABLE);

			} else {

				holder.txtTime.setText("Server History");
				holder.txtTime.setVisibility(View.GONE);

				if (holder.msgTimeContainer != null)
					holder.msgTimeContainer.setVisibility(View.GONE);

				if (incoming && builderTime.length() == 0 && !time.equals("")) {
					holder.txtTime.setText(time, BufferType.SPANNABLE);
					holder.msgTimeContainer.setVisibility(View.VISIBLE);
					holder.txtTime.setVisibility(View.VISIBLE);
				}
			}
		}

		String resource = StringUtils.replaceStringEquals(messageItem
				.getResource());

		if (typeEmp == TYPE_STICKER || typeEmp == TYPE_IMAGE
				|| typeEmp == TYPE_VIDEO || typeEmp == TYPE_AUDIO
				|| typeEmp == TYPE_LOCATION || typeEmp == TYPE_ATTENTION) {

			if (messageItem.getChat().getTo()
					.contains(AppConstants.UniqueKeyGroup)
					|| messageItem.getChat().getTo()
							.contains(AppConstants.XMPPRoomsServer)) {

				if (incoming) {
					holder.txtTime.setText(resource + "\n\r"
							+ holder.txtTime.getText());
				}
			}
		}

		switch (typeEmp) {
		case TYPE_MESSAGE:
			getMessageView(convertView, holder, messageItem);
			break;
		case TYPE_BROADCAST:
			getBroadcastView(convertView, holder, messageItem);
			break;
		case TYPE_STICKER:
			getStickerView(convertView, holder, messageItem);
			break;
		case TYPE_IMAGE:
			getImageView(convertView, holder, messageItem);
			break;
		case TYPE_VIDEO:
			getVideoView(convertView, holder, messageItem);
			break;
		case TYPE_AUDIO:
			getAudioView(convertView, holder, messageItem);
			break;
		case TYPE_LOCATION:
			getLocationView(convertView, holder, messageItem);
			break;
		case TYPE_ATTENTION:
			getAttentionView(convertView, holder, messageItem);
			break;
		default:
			getMessageView(convertView, holder, messageItem);
			break;
		}

		return convertView;
	}

	/** * Must be called on changes in chat (message sent, received, etc.). */
	public void onChatChange(View view, boolean incomingMessage) {
		/*
		 * ViewHolder holder = (ViewHolder) view.getTag(); if (incomingMessage)
		 * holder.nameHolder.startAnimation(shake);
		 * holder.chatMessageAdapter.onChange();
		 */
	}

	private View getMessageView(final View convertView, ViewHolder holder,
			MessageItem messageItem) {
		Spannable currentTextMessage;
		if (isMUC) {
			final String resource = StringUtils.replaceStringEquals(messageItem
					.getResource());
			currentTextMessage = Emoticons.getSmiledText(
					this.activity.getApplicationContext(),
					(messageItem.isIncoming() ? resource + " : " : "")
							+ com.digitalbuana.smiles.utils.StringUtils
									.replaceStringEquals(messageItem.getText()
											.toString()));
			holder.txtMessage.setText(currentTextMessage, BufferType.SPANNABLE);
		} else {
			Spannable currentTextMessages = Emoticons.getSmiledText(
					this.activity.getApplicationContext(),
					messageItem.getText());
			holder.txtMessage
					.setText(currentTextMessages, BufferType.SPANNABLE);
		}
		return convertView;
	}

	private View getBroadcastView(final View convertView, ViewHolder holder,
			MessageItem messageItem) {
		final boolean incoming = messageItem.isIncoming();
		if (incoming) {
			holder.ballon
					.setBackgroundResource(R.drawable.img_bg_chat_me_broadcast);
		} else {
			holder.ballon
					.setBackgroundResource(R.drawable.img_bg_chat_other_broadcast);
		}
		holder.txtMessage.setText(StringUtils.replaceStringEquals(messageItem
				.getText()));
		return convertView;
	}

	private View getAttentionView(final View convertView, ViewHolder holder,
			MessageItem messageItem) {
		int tempStickerHeight = ViewUtilities.GetInstance().getHeight() / 5;
		if (tempStickerHeight <= ViewUtilities.GetInstance().convertDPtoPX(100)) {
			tempStickerHeight = ViewUtilities.GetInstance().convertDPtoPX(100);
		}
		holder.imgSticker.getLayoutParams().height = tempStickerHeight;
		holder.imgSticker.getLayoutParams().width = tempStickerHeight;
		holder.imgSticker.setImageResource(R.drawable.img_kakatua_kaget);
		return convertView;
	}

	private View getStickerView(final View convertView, ViewHolder holder,
			MessageItem messageItem) {
		final AQuery aq = new AQuery(convertView);
		String urlImage = messageItem.getText();
		if (urlImage.contains(AppConstants.UniqueKeySticker)) {
			urlImage = urlImage.replace(AppConstants.UniqueKeySticker, "");
		}
		if (urlImage.contains(AppConstants.UniqueKeyIkonia)) {
			urlImage = urlImage.replace(AppConstants.UniqueKeyIkonia, "");
		}
		int tempStickerHeight = ViewUtilities.GetInstance().getHeight() / 4;
		holder.imgSticker.getLayoutParams().height = tempStickerHeight;
		holder.imgSticker.getLayoutParams().width = tempStickerHeight;

		// aq.id(R.id.itemMessageImage).image(urlImage, true, true, 0,
		// AQuery.GONE);
		final String finalImageUrl = urlImage;
		aq.id(R.id.itemMessageImage).image(finalImageUrl, true, true, 0,
				R.drawable.img_default_no_sticker, new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView iv,
							Bitmap bm, AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, iv, bm, status);
						if (status.getCode() != AjaxStatus.NETWORK_ERROR
								&& status.getCode() != AjaxStatus.AUTH_ERROR
								&& status.getCode() != AjaxStatus.TRANSFORM_ERROR)
							iv.setImageBitmap(bm);
						else {
							// hapus file jika sudah tersimpan sebagai cache
							try {
								File file = aq.getCachedFile(finalImageUrl);
								file.delete();
							} catch (NullPointerException e) {
							}
						}
					}
				});
		return convertView;
	}

	private View getImageView(final View convertView, final ViewHolder holder,
			final MessageItem messageItem) {
		final AQuery aq = new AQuery(convertView);
		int tempStickerHeight = ViewUtilities.GetInstance().getHeight() / 4;
		holder.imgSticker.getLayoutParams().height = tempStickerHeight;
		holder.imgSticker.getLayoutParams().width = tempStickerHeight;
		String desc = messageItem.getText().replace(
				AppConstants.UniqueKeyFileImage, "");
		int indexURL = desc.indexOf(AppConstants.UniqueKeyURL);
		int indexTHUMB = desc.indexOf(AppConstants.UniqueKeyTHUMB);
		String url = desc.substring(indexURL, indexTHUMB);
		String urlThumb = desc.substring(indexTHUMB, desc.length());
		urlThumb = urlThumb.replace(AppConstants.UniqueKeyTHUMB, "");
		final String urlFinal = url.replace(AppConstants.UniqueKeyURL, "");
		desc = desc.substring(0, indexURL);
		if (desc.length() < 1 || desc.equals("null") || desc == null) {
			holder.txtMessage.setVisibility(View.GONE);
		} else {
			holder.txtMessage.setText(desc);
			holder.txtMessage.setVisibility(View.VISIBLE);
		}
		holder.btnOpen.setOnClickListener(null);
		if (holder.btnOpen != null) {
			holder.btnOpen.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					openFile(holder.progres, urlFinal, messageItem, 0);
				}
			});
		}
		// aq.id(R.id.itemMessageImage).image(urlThumb, true, true, 0,
		// AQuery.GONE);
		aq.id(R.id.itemMessageImage).image(urlFinal, true, true, 0,
				R.drawable.img_default_no_sticker, new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView iv,
							Bitmap bm, AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, iv, bm, status);
						if (status.getCode() != AjaxStatus.NETWORK_ERROR
								&& status.getCode() != AjaxStatus.AUTH_ERROR
								&& status.getCode() != AjaxStatus.TRANSFORM_ERROR)
							iv.setImageBitmap(bm);
						else {
							// hapus file jika sudah tersimpan sebagai cache
							try {
								File file = aq.getCachedFile(urlFinal);
								file.delete();
							} catch (NullPointerException e) {
							}
						}
					}
				});
		return convertView;
	}

	private View getVideoView(final View convertView, final ViewHolder holder,
			final MessageItem messageItem) {
		final AQuery aq = new AQuery(convertView);
		int tempStickerHeight = ViewUtilities.GetInstance().getHeight() / 4;
		holder.imgSticker.getLayoutParams().height = tempStickerHeight;
		holder.imgSticker.getLayoutParams().width = tempStickerHeight;
		holder.iconVideo.getLayoutParams().height = tempStickerHeight / 2;
		holder.iconVideo.getLayoutParams().width = tempStickerHeight / 2;
		String desc = messageItem.getText().replace(
				AppConstants.UniqueKeyFileVideo, "");
		int indexURL = desc.indexOf(AppConstants.UniqueKeyURL);
		int indexTHUMB = desc.indexOf(AppConstants.UniqueKeyTHUMB);
		String url = desc.substring(indexURL, indexTHUMB);
		String urlThumb = desc.substring(indexTHUMB, desc.length());
		urlThumb = urlThumb.replace(AppConstants.UniqueKeyTHUMB, "");
		final String urlFinal = url.replace(AppConstants.UniqueKeyURL, "");
		desc = desc.substring(0, indexURL);
		if (desc.length() < 1 || desc.equals("null") || desc == null) {
			holder.txtMessage.setVisibility(View.GONE);
		} else {
			holder.txtMessage.setText(desc);
			holder.txtMessage.setVisibility(View.VISIBLE);
		}
		holder.iconVideo.setVisibility(View.VISIBLE);
		holder.btnOpen.setOnClickListener(null);
		if (holder.btnOpen != null) {
			holder.btnOpen.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					openFile(holder.progres, urlFinal, messageItem, 1);
				}
			});
		}
		// aq.id(R.id.itemMessageImage).image(urlThumb, true, true, 0,
		// AQuery.GONE);
		aq.id(R.id.itemMessageImage).image(urlFinal, true, true, 0,
				R.drawable.img_default_no_sticker, new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView iv,
							Bitmap bm, AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, iv, bm, status);
						if (status.getCode() != AjaxStatus.NETWORK_ERROR
								&& status.getCode() != AjaxStatus.AUTH_ERROR
								&& status.getCode() != AjaxStatus.TRANSFORM_ERROR)
							iv.setImageBitmap(bm);
						else {
							// hapus file jika sudah tersimpan sebagai cache
							try {
								File file = aq.getCachedFile(urlFinal);
								file.delete();
							} catch (NullPointerException e) {
							}
						}
					}
				});
		return convertView;
	}

	private View getAudioView(final View convertView, final ViewHolder holder,
			final MessageItem messageItem) {
		final AQuery aq = new AQuery(convertView);
		int tempStickerHeight = ViewUtilities.GetInstance().convertDPtoPX(90);
		holder.imgSticker.getLayoutParams().height = tempStickerHeight;
		holder.imgSticker.getLayoutParams().width = tempStickerHeight;
		holder.iconVideo.getLayoutParams().height = tempStickerHeight / 2;
		holder.iconVideo.getLayoutParams().width = tempStickerHeight / 2;
		String desc = messageItem.getText().replace(
				AppConstants.UniqueKeyFileAudio, "");
		int indexURL = desc.indexOf(AppConstants.UniqueKeyURL);
		int indexTHUMB = desc.indexOf(AppConstants.UniqueKeyTHUMB);
		String url = desc.substring(indexURL, indexTHUMB);
		String urlThumb = desc.substring(indexTHUMB, desc.length());
		urlThumb = urlThumb.replace(AppConstants.UniqueKeyTHUMB, "");
		final String urlFinal = url.replace(AppConstants.UniqueKeyURL, "");
		desc = desc.substring(0, indexURL);
		if (desc.length() < 1 || desc.equals("null") || desc == null) {
			holder.txtMessage.setVisibility(View.GONE);
		} else {
			holder.txtMessage.setText(desc);
			holder.txtMessage.setVisibility(View.VISIBLE);
		}
		holder.iconVideo.setVisibility(View.VISIBLE);
		holder.btnOpen.setOnClickListener(null);
		if (holder.btnOpen != null) {
			holder.btnOpen.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					openFile(holder.progres, urlFinal, messageItem, 2);
				}
			});
		}
		// aq.id(R.id.itemMessageImage).image(urlThumb, true, true, 0,
		// AQuery.GONE);
		aq.id(R.id.itemMessageImage).image(urlFinal, true, true, 0,
				R.drawable.img_default_no_sticker, new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView iv,
							Bitmap bm, AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, iv, bm, status);
						if (status.getCode() != AjaxStatus.NETWORK_ERROR
								&& status.getCode() != AjaxStatus.AUTH_ERROR
								&& status.getCode() != AjaxStatus.TRANSFORM_ERROR)
							iv.setImageBitmap(bm);
						else {
							// hapus file jika sudah tersimpan sebagai cache
							try {
								File file = aq.getCachedFile(urlFinal);
								file.delete();
							} catch (NullPointerException e) {
							}
						}
					}
				});
		return convertView;
	}

	@SuppressLint("SimpleDateFormat")
	private void openFile(final ProgressBar progress, final String path,
			final MessageItem messageItem, final int type) {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progress != null) {
					progress.setVisibility(View.VISIBLE);
				}
				File ext = Environment.getExternalStorageDirectory();
				SimpleDateFormat df = new SimpleDateFormat(
						"MM-dd-yyyy-HH-mm-ss");
				String reportDate = df.format(messageItem.getTimestamp());
				int posExtension = path.lastIndexOf(".");
				String extenstion = path.substring(posExtension, path.length());
				final File target;
				if (type == 0) {
					target = new File(ext, "SMILES/image/IMG_" + reportDate
							+ extenstion);
				} else if (type == 1) {
					target = new File(ext, "SMILES/video/VID_" + reportDate
							+ extenstion);
				} else if (type == 2) {
					target = new File(ext, "SMILES/audio/AUD_" + reportDate
							+ extenstion);
				} else {
					target = new File(ext, "SMILES/other/ETC_" + reportDate
							+ extenstion);
				}
				if (getFileSaved(path)) {
					progress.setVisibility(View.GONE);
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					if (type == 0) {
						intent.setDataAndType(Uri.fromFile(target), "image/*");
					} else if (type == 1) {
						intent.setDataAndType(Uri.fromFile(target), "video/*");
					} else if (type == 2) {
						intent.setDataAndType(Uri.fromFile(target), "audio/*");
					} else {
						intent.setDataAndType(Uri.fromFile(target), "*/*");
					}
					activity.startActivity(intent);
				} else {
					AQuery aq = new AQuery(Application.getInstance()
							.getApplicationContext());
					aq.download(path, target, new AjaxCallback<File>() {
						public void callback(String url, File file,
								AjaxStatus status) {
							if (progress != null) {
								progress.setVisibility(View.GONE);
							}
							if (file != null) {
								setFileSaved(path);
								Intent intent = new Intent();
								intent.setAction(android.content.Intent.ACTION_VIEW);
								if (type == 0) {
									intent.setDataAndType(Uri.fromFile(file),
											"image/*");
								} else if (type == 1) {
									intent.setDataAndType(Uri.fromFile(file),
											"video/*");
								} else if (type == 2) {
									intent.setDataAndType(Uri.fromFile(file),
											"audio/*");
								} else {
									intent.setDataAndType(Uri.fromFile(file),
											"*/*");
								}
								activity.startActivity(intent);
							} else {
								Toast.makeText(activity,
										"Error Downloading File",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
				}

			}
		});

	}

	private void setFileSaved(String name) {
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("ISDOWNLOAD_" + name, true);
		editor.commit();
	}

	private boolean getFileSaved(String name) {
		return appPreference.getBoolean("ISDOWNLOAD_" + name, false);
	}

	private View getLocationView(final View convertView,
			final ViewHolder holder, final MessageItem messageItem) {
		holder.imgSticker.getLayoutParams().height = ViewUtilities
				.GetInstance().convertDPtoPX(40);
		holder.imgSticker.getLayoutParams().width = ViewUtilities.GetInstance()
				.convertDPtoPX(40);
		holder.imgSticker.setImageResource(R.drawable.img_default_map);
		String stringKu = messageItem.getText().replace(
				AppConstants.UniqueKeyLocation, "");
		int indexLat = stringKu.indexOf("LAT:");
		int indexLong = stringKu.indexOf("/LONG:");
		final String latitude = stringKu.substring(indexLat, indexLong)
				.replace("LAT:", "");
		final String longitude = stringKu.substring(indexLong,
				stringKu.length()).replace("/LONG:", "");
		;
		// holder.txtMessage.setText("Location\nLatitude : "+latitude+"\nLongitude : "+longitude);
		holder.txtMessage.setText("  Location Share  ");
		if (holder.txtMessage != null) {
			holder.txtMessage.setOnClickListener(null);
			holder.txtMessage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					openLocation(latitude, longitude);
				}
			});
		}
		if (holder.btnOpen != null) {
			holder.btnOpen.setOnClickListener(null);
			holder.btnOpen.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					openLocation(latitude, longitude);
				}
			});
		}
		return convertView;
	}

	private void openLocation(String latitude, String longitude) {
		try {
			String uri = "geo:" + latitude + "," + longitude
					+ "?z=19&vpsrc=1&t=m&ie=UTF8&msa=0&q=" + latitude + ","
					+ longitude;
			activity.startActivity(new Intent(
					android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		} catch (ActivityNotFoundException e) {
			showToast("Your device is not compatible with this action");
		}
	}

	private void showToast(String msg) {
		Toast.makeText(this.activity.getBaseContext(), msg, Toast.LENGTH_SHORT)
				.show();
	}

	public static class ViewHolder {
		FrameLayout rootView;
		FrameLayout ballon, msgTimeContainer = null;
		TextView txtTime;
		TextView txtMessage;
		ImageView imgSticker;
		ImageView iconVideo;
		FrameLayout btnOpen;
		ProgressBar progres;
	}

	public void setChat(String account, String user) {
		this.account = account;
		this.user = user;
		this.isMUC = MUCManager.getInstance().hasRoom(account, user);
		onChange();
	}

	private long currentTimestamp = 0;

	private void setMsgDateGroup() {

		ArrayList<MessageItem> tmpMsg = new ArrayList<MessageItem>();

		for (MessageItem mi : messages) {

			Date currentDateMsg = mi.getTimestamp();

			if (currentTimestamp != 0) {

				CharSequence rdf1 = df1.format("yyyy-MM-dd", currentTimestamp);
				CharSequence rdf2 = df1.format("yyyy-MM-dd", currentDateMsg);

				if (!rdf1.equals(rdf2) /* && !rdf2.equals(tdf) */) {

					Date groupDate = new Date(currentTimestamp);

					MessageItem myItem = new MessageItem(null, null, null,
							AppConstants.uniqueKeyDateSeparator, null,
							groupDate, null, false, false, false, false, false,
							false, false, null, false);

					tmpMsg.add(myItem);
				}

			}
			tmpMsg.add(mi);
			currentTimestamp = currentDateMsg.getTime();
		}
		currentTimestamp = 0;
		messages.clear();
		messages = tmpMsg;
		notifyDataSetChanged();
	}

	@Override
	public void onChange() {

		MessageManager.getInstance().requestToLoadLocalHistory(account, user);

		messages = new ArrayList<MessageItem>(MessageManager.getInstance()
				.getMessages(account, user));

		setMsgDateGroup();

	}
}
