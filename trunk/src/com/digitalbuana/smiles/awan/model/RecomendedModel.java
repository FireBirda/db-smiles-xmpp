package com.digitalbuana.smiles.awan.model;

public class RecomendedModel {
	
	private String _userId=null;
	private String _userName=null;
	private String _fullName=null;
	private String _msisdn=null;
	private String _email="";
	
	public void setUserId(String userId){
		this._userId = userId;
	}
	public void setUserName(String userName){
		this._userName = userName;
	}
	public void setFullName(String fullName){
		this._fullName = fullName;
	}
	public void setMsisdn(String msisdn){
		this._msisdn = msisdn;
	}
	public void setEmail(String email){
		this._email = email;
	}
	
	public String getUserId(){
		return this._userId;		
	}
	public String getUserName(){
		return this._userName;
	}
	public String getFullName(){
		return this._fullName;
	}
	public String getMsisdn(){ return this._msisdn; }
	public String getMail(){ return this._email; }
	
}
