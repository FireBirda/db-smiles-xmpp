package com.digitalbuana.smiles.data.notification;

import java.util.Date;

import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.utils.StringUtils;

public class MessageNotification extends BaseEntity {

	private String text;
	private Date timestamp;
	private int count;

	public MessageNotification(String account, String user, String text, Date timestamp, int count) {		
		super(account, user);		
		this.text = StringUtils.replaceStringEquals(text);
		this.timestamp = timestamp;
		this.count = count;
	}

	public String getText() {
		return text;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getCount() {
		return count;
	}

	public void addMessage(String text) {
		this.text = StringUtils.replaceStringEquals(text);
		this.timestamp = new Date();
		this.count += 1;
	}

}
