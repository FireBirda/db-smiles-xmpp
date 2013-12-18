package com.digitalbuana.smiles.awan.model;

public class AdsApiModel {
	
	private String _type;
	private String _active;
	private String _clientId;
	
	public void setType(String type){
		this._type = type;
	}
	public void setActive(String active){
		this._active = active;
	}
	public void setClientId(String clientId){
		this._clientId = clientId;
	}
	
	public String getType(){
		return this._type;
	}
	public String getActive(){
		return this._active;
	}
	public String getClientId(){
		return this._clientId;
	}
}
