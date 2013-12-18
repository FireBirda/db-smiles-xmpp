package com.digitalbuana.smiles.awan.helper;

import java.util.Collection;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bookmark.BookmarkManager;
import org.jivesoftware.smackx.bookmark.BookmarkedConference;

import com.digitalbuana.smiles.data.account.AccountManager;

public class BookmarkHelper {

	private String _bookmarkName = "";
	private String _bookmarkValue = "";
	private boolean _autoJoin = true;
	private String _password = "";

	private XMPPConnection xmppConnection;
	private BookmarkManager bm;
	private String activeAccount = null;

	public BookmarkHelper() throws XMPPException {
		xmppConnection = AccountManager.getInstance().getActiveAccount()
				.getConnectionThread().getXMPPConnection();
		bm = BookmarkManager.getBookmarkManager(xmppConnection);
		activeAccount = AccountManager.getInstance().getAccountKu();
	}

	public Collection<BookmarkedConference> getBookmark() throws XMPPException {
		return bm.getBookmarkedConferences();
	}

	public void setBookmarkName(String _name) {
		this._bookmarkName = _name;
	}

	public void setBookmarkValue(String _value) {
		this._bookmarkValue = _value;
	}

	public void setAutojoin(boolean _autojoin) {
		this._autoJoin = _autojoin;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}

	public void setRoomBookmark() throws XMPPException {
		bm.addBookmarkedConference(this._bookmarkName, _bookmarkValue,
				this._autoJoin, activeAccount, this._password);
	}

}
