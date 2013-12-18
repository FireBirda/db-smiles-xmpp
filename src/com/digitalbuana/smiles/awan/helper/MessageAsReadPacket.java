package com.digitalbuana.smiles.awan.helper;

import org.jivesoftware.smack.packet.Message;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionManager;

public class MessageAsReadPacket extends Message {

	public MessageAsReadPacket() {
		super();
	}

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return super.toXML();
	}

	public static void sendPacket(String packetId, String to) {
		final MessageAsReadPacket marp = new MessageAsReadPacket();
		String vurbose = AccountManager.getInstance().getAccountKu() + "@"
				+ AppConstants.XMPPServerHost + "/"
				+ AppConstants.XMPPConfResource;
		marp.setFrom(vurbose);
		marp.setTo(to + "/" + AppConstants.XMPPConfResource);
		marp.setSubject("readreport");
		marp.setThread(packetId);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					ConnectionManager.getInstance().sendPacket(
							AccountManager.getInstance().getAccountKu(), marp);
				} catch (NetworkException e) {
				}
			}
		});
	}

}
