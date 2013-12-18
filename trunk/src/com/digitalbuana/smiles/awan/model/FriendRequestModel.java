package com.digitalbuana.smiles.awan.model;

public class FriendRequestModel {
	
	private String _relCode;
	private String _userId;
	private String _userName;
	private String _email;
	private String _fullname;
	private String _gender;
	private String _gender_preff;
	private String _status;
	private String _online;
	private String _isBlocked;
	private String _avatar;
	
	public void setRelCode(String relCode){ this._relCode = relCode; }
	public void setUserId(String userId){ this._userId = userId; }
	public void setUserName(String userName){ this._userName = userName; }
	public void setEmail(String email){ this._email = email; }
	public void setFullName(String fullName){ this._fullname = fullName; }
	public void setGender(String gender){ this._gender = gender; }
	public void setGenderPreff(String genderPreff){ this._gender_preff=genderPreff; }
	public void setStatus(String status){ this._status = status; }
	public void setOnline(String online){ this._online = online; }
	public void setIsBlocked(String isBlocked){ this._isBlocked = isBlocked; }
	public void setAvatar(String avatar){ this._avatar = avatar; }
	
	public String getRelcode(){ return this._relCode; }
	public String getUserId(){ return this._userId; }
	public String getUserName(){ return this._userName; }
	public String getEmail(){ return this._email; }
	public String getFullName(){ return this._fullname; }
	public String getGender(){ return this._gender; }
	public String getGenderPreff(){ return this._gender_preff; }
	public String getStatus(){ return this._status; }
	public String getOnline(){ return this._online; }
	public String getIsBlocked(){ return this._isBlocked; }
	public String getAvatar(){ return this._avatar; }
}
