package com.digitalbuana.smiles.data.friends;

public class AdminModel {
	private long id;
	private String time;
	private String message;

	AdminModel(long id, String message, String time) {
		this.id = id;
		this.time = time;
		this.message = message;
	}

	public long getID() {
		return id;
	}

	public String getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}
}
