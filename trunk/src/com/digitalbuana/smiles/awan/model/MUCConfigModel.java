package com.digitalbuana.smiles.awan.model;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

/***
 * This class is made for more eazier to set MultiUserChat for xmpp configuration
 * @author awan
 * */
public class MUCConfigModel {
	
	private MultiUserChat _multiUserChat = null;
	private Form _form = null;
	private Form _submitForm = null;
	
	/***
	 * Initiate the config model<br/>
     * @param multiUserChat MultiUserChat
     * @throws XMPPException
     * @author awan
	 * */
	public MUCConfigModel(MultiUserChat multiUserChat) throws XMPPException{
		this._multiUserChat = multiUserChat;
		this._form = multiUserChat.getConfigurationForm();
        this._submitForm = this._form.createAnswerForm();
	}
	
	/***
	 * Full List of Room Owners<br/>
     * <b>muc#roomconfig_roomowners</b>
     * @param ownerList ArrayList<String>
     * @author awan   
	 * */
	public void setRoomOwner(List<String> ownerList){
		this._submitForm.setAnswer("muc#roomconfig_roomowners", ownerList);
	}
	
	/***
	 * Make Room Publicly Searchable?<br/>
     * <b>muc#roomconfig_publicroom</b>
     * @param isPublic boolean
     * @author awan  
	 * */
	public void setIsPublicRoom(boolean isPublic){
		this._submitForm.setAnswer("muc#roomconfig_publicroom", isPublic);
	}
	
	/***
	 * Make Room Publicly Searchable?<br/>
     * <b>muc#roomconfig_publicroom</b>
     * @param isPublic boolean
     * @author awan     
	 * */
	public void setIsPersistentRoom(boolean isPersistent){
		this._submitForm.setAnswer("muc#roomconfig_persistentroom", isPersistent);
	}
	
	/***
	 * Make Room Moderated?<br/>
     * <b>muc#roomconfig_moderatedroom</b>
     * @param isModerated boolean
     * @author awan     
	 * */
	public void setIsModeratedRoom(boolean isModerated){
		this._submitForm.setAnswer("muc#roomconfig_moderatedroom", isModerated);
	}
	
	/***
	 * Roles for which Presence is Broadcasted<br/>
     * <b>muc#roomconfig_presencebroadcast</b>
     * @param broadcastPresenceList ArrayList<String>
     * @author awan     
	 * */
	public void setPresenceBroadcast(List<String> broadcastPresenceList){
		this._submitForm.setAnswer("muc#roomconfig_presencebroadcast", broadcastPresenceList);
	}
	
	/***
	 * Make Room Members Only?<br/>
     * <b>muc#roomconfig_membersonly</b>
     * @param isMemberOnly boolean
     * @author awan     
	 * */
	public void setIsMemberOnly(boolean isMemberOnly){
		this._submitForm.setAnswer("muc#roomconfig_membersonly", isMemberOnly);
	}
	
	/***
	 * Allow Occupants to Invite Others?<br/>
     * <b>muc#roomconfig_allowinvites</b>
     * @param userInvite boolean
     * @author awan     
	 * */
	public void setAllowUserToInvite(boolean userInvite){
		this._submitForm.setAnswer("muc#roomconfig_allowinvites", userInvite);
	}
	
	/***
	 * Allow Occupants to Change Subject?<br/>
     * <b>muc#roomconfig_changesubject</b>
     * @param changeSubject boolean
     * @author awan     
	 * */
	public void setUserCanChangeSubject(boolean changeSubject){
		this._submitForm.setAnswer("muc#roomconfig_changesubject", changeSubject);
	}
	
	/***
	 * Who May Discover Real JIDs?<br/>
     * <b>muc#roomconfig_whois</b>
     * @param broadcastList ArrayList<String>
     * @author awan     
	 * */
	public void setVisibleJIDTo(List<String> broadcastList){
		this._submitForm.setAnswer("muc#roomconfig_whois", broadcastList);
	}
	
	/***
	 * Room Admins<br/>
     * <b>muc#roomconfig_roomadmins</b>
     * @param adminList List<String>
     * @author awan     
	 * */
	public void setRoomAdmin(List<String> adminList){
		this._submitForm.setAnswer("muc#roomconfig_roomadmins", adminList);
	}
	
	/***
	 * Short Description of Room<br/>
     * <b>muc#roomconfig_roomdesc</b>
     * @param description String
     * @author awan     
	 * */
	public void setRoomDescription(String description){
		this._submitForm.setAnswer("muc#roomconfig_roomdesc", description);
	}
	
	/***
	 * Enable Public Logging?<br/>
     * <b>muc#roomconfig_enablelogging</b>
     * @param logging boolean
     * @author awan     
	 * */
	public void enabledLogging(boolean logging){
		this._submitForm.setAnswer("muc#roomconfig_enablelogging", logging);
	}
	
	/***
	 * The Room Password<br/>
     * <b>muc#roomconfig_roomsecret</b>
     * @param secret String
     * @author awan     
	 * */
	public void setRoomSecret(String secret){
		this._submitForm.setAnswer("muc#roomconfig_roomsecret", secret);
	}
	
	/***
	 * label Roles that May Send Private Messages<br/>
     * <b>muc#roomconfig_allowpm</b>
     * @param canSendPM       
	 * */
	public void setUserCanPM(boolean canSendPM){
		this._submitForm.setAnswer("muc#roomconfig_allowpm", canSendPM);
	}
	
	/***
	 * send generated config to server
	 * @throws XMPPException
	 * */
	public boolean submitResult(){
		try {
			this._multiUserChat.sendConfigurationForm(this._submitForm);
			return true;
		} catch (XMPPException e) { return false; }
	}

}
