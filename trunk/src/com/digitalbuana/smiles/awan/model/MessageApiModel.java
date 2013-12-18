package com.digitalbuana.smiles.awan.model;


public class MessageApiModel {

	private String fromJID;
	private String fromJIDResource;
	private String toJID;
	private String toJIDResource;
	private MessageApiSentDateModel sentDate;
	private String body;
	private long timestamp;

	public MessageApiModel() {
		this.sentDate = new MessageApiSentDateModel();
	}

	public String getFromJID() {
		return this.fromJID;
	}

	public String getFromJIDResource() {
		return this.fromJIDResource;
	}

	public String getToJID() {
		return this.toJID;
	}

	public String getToJIDResource() {
		return this.toJIDResource;
	}

	public MessageApiSentDateModel getSentDate() {
		return this.sentDate;
	}

	public String getBody() {
		return this.body;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setFromJID(String _fromJID) {
		this.fromJID = _fromJID;
	}

	public void setFromJIDSource(String _fromJIDSource) {
		this.fromJIDResource = _fromJIDSource;
	}

	public void setToJID(String _toJID) {
		this.toJID = _toJID;
	}

	public void setToJIDResource(String _toJIDResource) {
		this.toJIDResource = _toJIDResource;
	}

	public void setSentDate(MessageApiSentDateModel _sentDate) {
		this.sentDate = _sentDate;
	}

	public void setBody(String _body) {
		this.body = _body;
	}

	public void setTimestamp(long _timestamp) {
		this.timestamp = _timestamp;
	}

}
