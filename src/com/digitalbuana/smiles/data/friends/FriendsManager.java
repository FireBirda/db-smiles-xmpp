package com.digitalbuana.smiles.data.friends;

public class FriendsManager {

	private final static FriendsManager instance;

	private FriendsListManager listFriendsList;
	private FriendsBlockedManager listFriendsBLocked;
	private FriendsPendingHeConfirmManager listFriendsPendding;
	private FriendsWaitingMeApproveManager listFriendsRequest;

	static {
		instance = new FriendsManager();
	}

	public static FriendsManager getInstance() {
		return instance;
	}

	public FriendsManager() {
		listFriendsList = new FriendsListManager();
		listFriendsBLocked = new FriendsBlockedManager();
		listFriendsPendding = new FriendsPendingHeConfirmManager();
		listFriendsRequest = new FriendsWaitingMeApproveManager();
	}

	public FriendsListManager getFriendsListManager() {
		listFriendsList.open();
		return listFriendsList;
	}

	public FriendsBlockedManager getFriendsBlockedManager() {
		listFriendsBLocked.open();
		return listFriendsBLocked;
	}

	public FriendsPendingHeConfirmManager getFriendsPenddingHeConfirmManager() {
		listFriendsPendding.open();
		return listFriendsPendding;
	}

	public FriendsWaitingMeApproveManager getFriendsWaitingMeApproveManager() {
		listFriendsRequest.open();
		return listFriendsRequest;
	}

	public VisitorManager getVisitorManager() {
		VisitorManager.getInstance().open();
		return VisitorManager.getInstance();
	}

	public FriendsUpdateManager getFriendsUpdateManager() {
		FriendsUpdateManager.getInstance().open();
		return FriendsUpdateManager.getInstance();
	}

	public AdminManager getAdminUpdateManager() {
		AdminManager.getInstance().open();
		return AdminManager.getInstance();
	}
}
