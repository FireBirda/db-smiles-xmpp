//package com.digitalbuana.smiles.data.friends;
//
//
//public class StickerManager {
//
//	private final static StickerManager instance;
//	
//	private FriendsListManager listFriendsList;
//	private FriendsBlockedManager listFriendsBLocked;
//	private FriendsPendingConfirmManager listFriendsPendding;
//	private FriendsWaitingApproveManager listFriendsRequest;
//	static {
//		instance = new StickerManager();
//	}
//	public static StickerManager getInstance() {
//		return instance;
//	}
//
//	public StickerManager() {
//		listFriendsList = new FriendsListManager();
//		listFriendsBLocked = new FriendsBlockedManager();
//		listFriendsPendding = new FriendsPendingConfirmManager();
//		listFriendsRequest = new FriendsWaitingApproveManager();
//	}
//	
//	public FriendsListManager getFriendsListManager(){
//		listFriendsList.open();
//		return listFriendsList;
//	}
//	public FriendsBlockedManager getFriendsBlockedManager(){
//		listFriendsBLocked.open();
//		return listFriendsBLocked;
//	}
//	public FriendsPendingConfirmManager getFriendsPenddingManager(){
//		listFriendsPendding.open();
//		return listFriendsPendding;
//	}
//	public FriendsWaitingApproveManager getFriendsRequestManager(){
//		listFriendsRequest.open();
//		return listFriendsRequest;
//	}
//}
