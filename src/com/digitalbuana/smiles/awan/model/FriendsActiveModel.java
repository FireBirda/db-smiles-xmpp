package com.digitalbuana.smiles.awan.model;

import com.digitalbuana.smiles.data.message.MessageItem;

public class FriendsActiveModel {
	
	private String relCode;
	private String userId;
	private String userName;
	private String email;
	private String fullName;
	private String gender;
	private String genderPreff;
	private String status;
	private String online;
	private String isBlocked;
	private String avatar;
	private MessageItem messageItem;
	
	public void setRelCode(String _relcode){ this.relCode = _relcode; }
	public void setUserId(String _userid){ this.userId = _userid; }
	public void setUserName(String _username){ this.userName = _username; }
	public void setEmail(String _email){ this.email = _email; }
	public void setFullName(String _fullname){ this.fullName = _fullname; }
	public void setGender(String _gender){ this.gender = _gender; }
	public void setGenderPreff(String _genderpreff){ this.genderPreff = _genderpreff; }
	public void setStatus(String _status){ this.status = _status; }
	public void setOnline(String _online){ this.online = _online; }
	public void setIsBlocked(String _isblocked){ this.isBlocked = _isblocked; }
	public void setAvatar(String _avatar){ this.avatar = _avatar; }
	public void setMessage(MessageItem messageItem){ this.messageItem = messageItem; }
	
	public String getRelCode(){ return this.relCode; }
	public String getUserId(){ return this.userId; }
	public String getUserName(){ return this.userName; }
	public String getEmail(){ return this.email; }
	public String getFullName(){ return this.fullName; }
	public String getGender(){ return this.gender; }
	public String getGenderPreff(){ return this.genderPreff; }
	public String getStatus(){ return this.status; }
	public String getOnline(){ return this.online; }
	public String getIsBlocked(){ return this.isBlocked; }
	public String getAvatar(){ return this.avatar; }
	public MessageItem getMessage(){ return this.messageItem; }
}
