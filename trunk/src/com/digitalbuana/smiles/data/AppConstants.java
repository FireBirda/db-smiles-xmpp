package com.digitalbuana.smiles.data;

import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.digitalbuana.smiles.R;

public class AppConstants {

	// public final static String TAG = "dheinaku";
	// Google project id
	public final static String GCMRegID = "359527134498";

	public final static String AdMobIDHome = "a1517d33206b9fd";

	public final static String urlTermsPrivacy = "http://www.smilesatme.com";

	public final static String XMPPServerHost = "lb1.smilesatme.com";
	public final static String XMPPConfProtocol = "xmpp";
	public final static String XMPPConfName = "smiles";
	public final static int XMPPConfPort = 5222;
	public final static String XMPPConfResource = "SmilesAndroid";
	public final static String XMPPGroupsServer = "group.lb1.smilesatme.com";
	public final static String XMPPRoomsServer = "room.lb1.smilesatme.com";
	public final static String XMPPSearchAPI = "search." + XMPPServerHost;

	public final static String APIHost = "http://api.smilesatme.com/";

	public static String countryCodeUrl = APIHost + "countrycodes.xml";

	public final static String APIConfiguration = APIHost + "configuration";
	public final static String APIGetVrifySMS = APIHost + "user/verify_phone";
	public final static String APICompareVrifySMS = APIHost
			+ "user/verify_phone";
	public final static String APIRegister = APIHost + "user/register";
	public final static String APISearch = APIHost + "user/search";
	public final static String APILogin = APIHost + "user/login";
	public final static String APILogOut = APIHost + "user/logout";
	public final static String APIPushFrindsUpdate = APIHost
			+ "user/broadcast_myupdate";
	public final static String APIGetProfile = APIHost + "user/get_profile";
	public final static String APIUpdateProfile = APIHost
			+ "user/update_profile";
	public final static String APIMessageStatus = APIHost
			+ "user/message_status";

	public final static String APIBlocked = APIHost + "friend/block";
	public final static String APIBlockedList = APIHost + "friend/list/block";
	public final static String APIAddFriends = APIHost + "friend/add";
	public final static String APIDeleteFriends = APIHost + "friend/delete";
	public final static String APIActiveFriends = APIHost
			+ "friend/list/active";
	public final static String APIRequestFriends = APIHost
			+ "friend/list/request";
	public final static String APIMyRequestFriends = APIHost
			+ "friend/list/myrequest";
	public final static String APIFriendUpdates = APIHost + "friend/updates";

	public final static String APIFriendVisits = APIHost + "user/visitor";

	public final static String APIPushService = APIHost
			+ "user/broadcast_myupdate";
	public final static String APIUserForgotPassword = APIHost
			+ "user/reset_password";
	public final static String APIUserChangePassword = APIHost
			+ "user/change_password";

	public final static String APIUploadContact = APIHost
			+ "user/upload_contacts";
	public final static String APIPushVisit = "";
	public final static String APIPushWakeup = APIHost + "user/wakeup";
	public final static String APIUserUploadPhoto = APIHost
			+ "user/photo/upload";
	public final static String APIUserGetText = APIHost + "user/get_text";
	public final static String APIUserGetRightMenu = APIHost
			+ "user/right_menu";

	public final static String APIStickerPackage = APIHost + "sticker/packages";
	public final static String APIStickerPackageDetail = APIHost
			+ "sticker/package_detail";
	public final static String APIStickerPackageDownload = APIHost
			+ "sticker/download";
	public final static String APIStickerPackageUsage = APIHost + "sticker/use";

	public final static String APIUploadFile = APIHost + "files";

	public final static String APIRightMenu = APIHost + "sticker/use";

	// transaction
	public static final String APITransaction = APIHost + "transaction/";
	public static final String APIGenerateTicket = APITransaction
			+ "generate_ticket";

	public final static String UniqueKeySticker = "5T1CK3RK03@";
	public final static String UniqueKeyIkonia = "1K0N14K03@";
	public final static String UniqueKeyBroadcast = "BR04DC4STK03@";
	public final static String UniqueKeyLocation = "L0C4T10NK03@";
	public final static String UniqueKeyAttention = "4T3NT10NK03@";
	public final static String UniqueKeyURL = "/URL:";
	public final static String UniqueKeyTHUMB = "/THUMB:";
	public final static String UniqueKeyGroup = "un1qu3";
	public final static String UniqueKeyFileImage = "F1L3K03@TYPE:IMAGE/DESC:";
	public final static String UniqueKeyFileVideo = "F1L3K03@TYPE:VIDEO/DESC:";
	public final static String UniqueKeyFileAudio = "F1L3K03@TYPE:AUDIO/DESC:";
	public final static String UniqueKeyFileContact = "F1L3K03@TYPE:CONTACT/DESC:";
	public final static String uniqueKeyDateSeparator = "@D4T3S3P4R4T0R";

	public static String USERNAME_KEY = "username";
	public static String PASSWORD_KEY = "password";

	public final static String FileSavedURL = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/SmilesKu/";

	public final static String[] listAttachmentTitle = { "Attention", "Image",
			"Video", "Camera", "Voice", "Contact", "Location" };
	public final static int[] listAttachmentRes = {
			R.drawable.img_send_attention, R.drawable.img_send_photo,
			R.drawable.img_send_video, R.drawable.img_send_camera,
			R.drawable.img_send_audio, R.drawable.img_send_contact,
			R.drawable.img_send_location };
	public static ArrayList<IconAttachmentModel> listAttachment = getListAttachment();

	private static ArrayList<IconAttachmentModel> getListAttachment() {
		ArrayList<IconAttachmentModel> list = new ArrayList<IconAttachmentModel>();
		for (int i = 0; i < listAttachmentTitle.length; i++) {
			String title = listAttachmentTitle[i];
			int resource = listAttachmentRes[i];
			IconAttachmentModel attac = new IconAttachmentModel(title, resource);
			list.add(attac);
		}
		return list;
	}

	public static String getCarrier(Context c) {
		TelephonyManager manager = (TelephonyManager) c
				.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getNetworkOperatorName();
	}

	// public static String bookmarkedContent = null;
	public static String ofSecret = "wXIcJcFe";

	public static String APIAddBookmark = APIHost
			+ "xmpp.helper/bookmark.add.php";
	public static String APIDelBookmark = APIHost
			+ "xmpp.helper/bookmark.delete.php";
	public static String APIBookmarkList = APIHost
			+ "xmpp.helper/bookmark.list.php";
	public static String APIMessageList = APIHost
			+ "xmpp.helper/message.list.php";

	public static String APIMessageStatusHelper = APIHost
			+ "xmpp.helper/message.status.php";

	public static String SOUND_ATTENTION_TAG = "chat_attention_tag";
	public static String SOUND_CHAT_NOTIF_TAG = "chat_notif_tag";
	public static String SOUND_CHAT_SEND_TAG = "chat_send_tag";

	public static String BOOKMARK_LIST_TAG = "bookmarkedlist";

	public static String LEFTMENU_NOTIF_TAG = "leftmenunotification";
	public static String FIRST_NOTIF_TAG = "firstnotification";

	public static String LOCAL_PHONE_CONTACT_CACHE_TAG = "localphonecontact";
	public static String LOCAL_MAIL_CONTACT_CACHE_TAG = "localmailcontact";

}
