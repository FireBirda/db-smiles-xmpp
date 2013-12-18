package com.digitalbuana.smiles.awan.model;

import java.util.Date;

public class MessageApiSentDateModel {

	private Date timeSent;
	private int timezoneType;
	private String timezone;

	public MessageApiSentDateModel() {
	}

	public void setTimeSent(Date _timeSent) {
		this.timeSent = _timeSent;
	}

	public void setTimezoneType(int _timezoneType) {
		this.timezoneType = _timezoneType;
	}

	public void setTimezone(String _timezone) {
		this.timezone = _timezone;
	}

	public Date getTimeSent() {
		return this.timeSent;
	}

	public int getTimezoneType() {
		return this.timezoneType;
	}

	public String getTimezone() {
		return this.timezone;
	}

}
