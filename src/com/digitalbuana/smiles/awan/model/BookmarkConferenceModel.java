package com.digitalbuana.smiles.awan.model;

public class BookmarkConferenceModel {
	
	private String _jid;
	private String _autojoin;
	private String _name;
	
	public void setJid(String jid){
		this._jid = jid;
	}
	public void setAutoJoin(String autojoin){
		this._autojoin = autojoin;
	}
	public void setName(String name){
		this._name = name;
	}
	
	public String getJid(){
		return this._jid;
	}
	public String getAutoJoin(){
		return this._autojoin;
	}
	public String getName(){
		return this._name;
	}
}
