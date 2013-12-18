package com.digitalbuana.smiles.data.message;

import java.util.Date;

import org.jivesoftware.smack.packet.Message;

import android.text.Spannable;
import android.text.util.Linkify;

import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.xmpp.uri.XMPPUri;

public class MessageItem implements Comparable<MessageItem> {

	private final AbstractChat chat;
	private String tag;
	private final String resource;
	private final String text;
	private Spannable spannable;
	private final ChatAction action;
	private Date timestamp;
	private Date delayTimestamp = null;
	private final boolean incoming;
	private final boolean unencypted;
	private Long id;
	private boolean error;
	private boolean delivered = false;
	private boolean sent;
	private boolean read;
	private final boolean offline;
	private String packetID;
	private Message.Type type;
	private String _packetId;
	private boolean _isReadByFriend;

	private String TAG = getClass().getSimpleName();

	public MessageItem(AbstractChat chat, String tag, String resource,
			String text, ChatAction action, Date timestamp,
			Date delayTimestamp, boolean incoming, boolean read, boolean sent,
			boolean error, boolean delivered, boolean unencypted,
			boolean offline, String packetId, boolean isReadByFriend) {
		this.chat = chat;
		this.tag = tag;
		this.resource = resource;
		this.text = text;
		this.action = action;
		this.timestamp = timestamp;
		this.delayTimestamp = delayTimestamp;
		this.incoming = incoming;
		this.read = read;
		this.sent = sent;
		this.error = error;
		this.delivered = delivered;
		this.unencypted = unencypted;
		this.offline = offline;
		this.id = null;
		this.packetID = null;
		this.type = Message.Type.chat;
		this._packetId = packetId;
		this._isReadByFriend = isReadByFriend;
	}

	public void setIsReadByFriend(boolean isReadByFriend) {
		this._isReadByFriend = isReadByFriend;
	}

	public boolean getIsReadByFriend() {
		return this._isReadByFriend;
	}

	public void setPacketId(String packageId) {
		this._packetId = packageId;
	}

	public String getPacketId() {
		return this._packetId;
	}

	public void setType(Message.Type type) {
		this.type = type;
	}

	public Message.Type getType() {
		return type;
	}

	public AbstractChat getChat() {
		return chat;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getResource() {
		return resource;
	}

	public String getText() {
		return text;
	}

	public Spannable getSpannable(String textnya) {
		if (spannable == null) {
			if (textnya == null) {
				spannable = Emoticons.newSpannable(text);
			} else {
				spannable = Emoticons.newSpannable(textnya);
			}
			Linkify.addLinks(this.spannable, Linkify.ALL);
			XMPPUri.addLinks(this.spannable);
		}
		return spannable;
	}

	public ChatAction getAction() {
		return action;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Date getDelayTimestamp() {
		return delayTimestamp;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isError() {
		return error;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public boolean isUnencypted() {
		return unencypted;
	}

	public boolean isOffline() {
		return offline;
	}

	public boolean isSent() {
		return sent;
	}

	public boolean isRead() {
		return read;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPacketID() {
		return packetID;
	}

	public void setPacketID(String packetID) {
		this.packetID = packetID;
	}

	void markAsError() {
		error = true;
	}

	void markAsSent() {
		sent = true;
	}

	void setSentTimeStamp(Date timestamp) {
		this.delayTimestamp = this.timestamp;
		this.timestamp = timestamp;
	}

	void markAsRead() {
		read = true;
	}

	void markAsDelivered() {
		delivered = true;
	}

	@Override
	public int compareTo(MessageItem another) {
		try {
			return timestamp.compareTo(another.timestamp);
		} catch (NullPointerException e) {
			return 0;
		}

	}

}
