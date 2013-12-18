package com.digitalbuana.smiles.awan.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.awan.model.MessageApiModel;
import com.digitalbuana.smiles.awan.model.MessageApiSentDateModel;
import com.digitalbuana.smiles.awan.stores.MessageAPIStore;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.message.AbstractChat;
import com.digitalbuana.smiles.data.message.ChatAction;
import com.digitalbuana.smiles.data.message.MessageItem;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.MessageTable;
import com.digitalbuana.smiles.utils.StringUtils;

public class MessageHelper {

	public static void sendMessageStatus(String userName, String targetName,
			String delivered, String read) {

		AQuery aq = new AQuery(Application.getInstance()
				.getApplicationContext());

		Map<String, String> params = new HashMap<String, String>();
		params.put("username", userName);
		params.put("targetname", targetName);
		params.put("delivered", delivered);
		params.put("read", read);

		aq.ajax(AppConstants.APIMessageStatus, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject object,
							AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, object, status);
						if (object != null) {
							Log.v("MessageStatusPushHelper.sendMessageStatus:",
									object.toString());
						} else {
							Log.v("MessageStatusPushHelper.sendMessageStatus:",
									"status:" + status.getError());
						}
					}
				});
	}

	public static void getArchieveMessage(final String actionWithAccount,
			final String actionWithUser) {

		Log.v("getArchieveMessage", " ::::: > " + actionWithAccount + " to "
				+ actionWithUser);

		final ArrayList<MessageItem> messages = new ArrayList<MessageItem>(
				MessageManager.getInstance().getMessages(actionWithAccount,
						actionWithUser));

		if (messages.size() > 0) {
			AQuery aq = new AQuery(Application.getInstance()
					.getApplicationContext());

			Map<String, String> params = new HashMap<String, String>();
			params.put("to", actionWithAccount);
			params.put("from", StringUtils.replaceStringEquals(actionWithUser));
			params.put("r", AppConstants.ofSecret);

			aq.ajax(AppConstants.APIMessageList, params, JSONArray.class,
					new AjaxCallback<JSONArray>() {
						@Override
						public void callback(String url, JSONArray object,
								AjaxStatus status) {
							super.callback(url, object, status);

							if (object != null) {

								Log.v("getArchieveMessage", object.toString());

								MessageAPIStore mas = new MessageAPIStore(
										object);
								ArrayList<MessageApiModel> messageLIst = mas
										.getList();

								if (messageLIst != null
										&& messageLIst.size() > 0)
									syncReceivedMessage(actionWithAccount,
											actionWithUser, messageLIst,
											messages);

							}
						}
					});
		} else
			Log.v("getArchieveMessage", " messages > 0 ");

	}

	public static void syncReceivedMessage(final String actionWithAccount,
			final String actionWithUser,
			ArrayList<MessageApiModel> messageLIst,
			ArrayList<MessageItem> messages) {

		Log.v("getArchieveMessage", "messages:" + messages.size()
				+ ", messageLIst:" + messageLIst.size());

		if (messageLIst.size() > 0 && messages.size() > 0) {

			for (final MessageApiModel mam : messageLIst) {

				final String body = mam.getBody();
				final String resource = mam.getFromJIDResource();
				final String text = mam.getBody();

				boolean isExists = false;
				for (MessageItem mi : messages) {
					if (mi.isIncoming()) {
						String localTmpBody = mi.getText();
						if (localTmpBody.equals(body))
							isExists = true;
					}
				}

				Log.v("syncReceivedMessage", text + ":" + isExists);

				final MessageApiSentDateModel masdm = mam.getSentDate();

				if (!isExists) {

					Log.v("syncReceivedMessage", "try to save....");

					MessageManager.getInstance().closeChat(actionWithAccount,
							actionWithUser);

					new Thread() {

						@SuppressLint("SimpleDateFormat")
						public void run() {

							try {

								long timestamp = mam.getTimestamp();

								Log.v("syncReceivedMessage", "timestamp:"
										+ timestamp);

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeZone(TimeZone
										.getTimeZone("GMT"));
								calendar.setTimeInMillis(timestamp);

								String convertToGMTDate = String
										.valueOf(calendar.get(Calendar.YEAR))
										+ "-"
										+ String.valueOf(calendar
												.get(Calendar.MONTH))
										+ "-"
										+ String.valueOf(calendar
												.get(Calendar.DATE))
										+ " "
										+ String.valueOf(calendar
												.get(Calendar.HOUR_OF_DAY))
										+ ":"
										+ String.valueOf(calendar
												.get(Calendar.MINUTE))
										+ ":"
										+ String.valueOf(calendar
												.get(Calendar.SECOND))
										+ "."
										+ String.valueOf(calendar
												.get(Calendar.MILLISECOND));

								Log.v("syncReceivedMessage", "to GMT:"
										+ convertToGMTDate);

								Date dt = new Date();

								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd hh:mm:ss");

								try {
									dt = dateFormat.parse(convertToGMTDate);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									// e.printStackTrace();
									Log.v("syncReceivedMessage",
											"ParseException:" + e.getMessage());
								}

								Log.v("syncReceivedMessage",
										"dt2:" + dt.getTime());

								MessageTable.getInstance().add(
										actionWithAccount, actionWithUser, "",
										resource, text, null, dt, null, true,
										false, true, false, "", false);

								MessageManager.getInstance()
										.requestToLoadLocalHistory(
												actionWithAccount,
												actionWithUser);

								final Date dtFinal = dt;

								final AbstractChat ac = new AbstractChat(
										actionWithAccount, actionWithUser) {

									@Override
									public MessageItem newMessage(String text) {
										// TODO Auto-generated method stub
										return new MessageItem(this, "",
												resource, body,
												ChatAction.chat, dtFinal,
												dtFinal, true, false, true,
												false, false, false, false, "",
												false);
									}

									@Override
									public Type getType() {
										// TODO Auto-generated method stub
										return Type.chat;
									}

									@Override
									public String getTo() {
										// TODO Auto-generated method stub
										return actionWithAccount;
									}
								};

								Application.getInstance().runOnUiThreadDelay(
										new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub
												try {
													MessageManager
															.getInstance()
															.addChat(ac);

													MessageManager
															.getInstance()
															.openChat(
																	actionWithAccount,
																	actionWithUser);
													MessageManager
															.getInstance()
															.onChatChanged(
																	actionWithAccount,
																	actionWithUser,
																	true);
												} catch (IllegalStateException e) {

												}

											}
										}, 1000);

							} finally {

								this.interrupt();

							}

						}

					}.start();

				}
			}

		}
	}

	public static void retrieveMessage(final String user, final int max) {
		try {
			if (AccountManager.getInstance().getActiveAccount() != null) {
				Packet packet = new Packet() {
					@Override
					public String toXML() {
						String xExtension = "<iq type=\"get\" id=\""
								+ Packet.nextID()
								+ "\">"
								+ "<retrieve xmlns=\"urn:xmpp:archive\""
								+ " with=\""
								+ user
								+ "\""
								+ ">"
								+ "<set xmlns=\"http://jabber.org/protocol/rsm\"/><max>"
								+ String.valueOf(max)
								+ "</max></retrieve></iq>";
						return xExtension;
					}
				};
				AccountManager.getInstance().getActiveAccount()
						.getConnectionThread().getXMPPConnection()
						.sendPacket(packet);
			}
		} catch (RuntimeException e) {
			Log.e("retrieveMessage", "...");
		}

	}

}
