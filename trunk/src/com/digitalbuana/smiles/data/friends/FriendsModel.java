package com.digitalbuana.smiles.data.friends;

public class FriendsModel{
	private long id;
	private String name;
	private String jid;
	private String time;
	private String message;
	
	public FriendsModel(long id, String name, String jid, String time, String message){
		this.id=id;
		this.name=name;
		this.jid=jid;
		this.time = time;
		this.message = message;
	}
	public long getID(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getJID(){
		return jid;
	}
	public String getTime(){
		return time;
	}
	public String getMessage(){
		return message;
	}
}
