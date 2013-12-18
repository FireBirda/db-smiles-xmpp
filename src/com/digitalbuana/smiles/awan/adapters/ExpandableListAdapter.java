/*
 * package com.digitalbuana.smiles.awan.adapters;
 * 
 * import java.util.ArrayList; import java.util.Collection; import
 * java.util.HashMap; import java.util.List; import java.util.Map;
 * 
 * import org.jivesoftware.smack.XMPPException; import
 * org.jivesoftware.smack.packet.Message.Type; import
 * org.jivesoftware.smackx.muc.HostedRoom; import
 * org.jivesoftware.smackx.muc.MultiUserChat; import org.json.JSONException;
 * import org.json.JSONObject;
 * 
 * import com.androidquery.AQuery; import
 * com.androidquery.callback.AjaxCallback; import
 * com.androidquery.callback.ImageOptions; import com.digitalbuana.smiles.R;
 * import com.digitalbuana.smiles.awan.helper.DateHelper; import
 * com.digitalbuana.smiles.awan.model.FriendsActiveModel; import
 * com.digitalbuana.smiles.awan.stores.FriendsActiveStore; import
 * com.digitalbuana.smiles.data.AppConstants; import
 * com.digitalbuana.smiles.data.Application; import
 * com.digitalbuana.smiles.data.account.AccountManager; import
 * com.digitalbuana.smiles.data.message.AbstractChat; import
 * com.digitalbuana.smiles.data.message.MessageItem; import
 * com.digitalbuana.smiles.data.message.MessageManager; import
 * com.digitalbuana.smiles.data.message.MessageTable; import
 * com.digitalbuana.smiles.data.message.RegularChat; import
 * com.digitalbuana.smiles.utils.Emoticons;
 * 
 * import android.content.Context; import android.content.SharedPreferences;
 * import android.database.Cursor; import
 * android.database.CursorIndexOutOfBoundsException; import
 * android.database.CursorWindow; import android.net.Uri; import
 * android.preference.PreferenceManager; import android.util.Log; import
 * android.view.LayoutInflater; import android.view.View; import
 * android.view.ViewGroup; import android.widget.BaseExpandableListAdapter;
 * import android.widget.ImageView; import android.widget.TextView; import
 * android.widget.TextView.BufferType;
 * 
 * public class ExpandableListAdapter extends BaseExpandableListAdapter {
 * 
 * private Context _context; private List<String> _listDataHeader; // header
 * titles // child data in format of header title, child title private
 * HashMap<String, ArrayList<FriendsActiveModel>> _listDataChild; private AQuery
 * aq;
 * 
 * //private String CHAT_LIST_CACHE = "chat_cache"; //private String
 * GROUP_LIST_CACHE = "group_cache"; //private String ROOM_LIST_CACHE =
 * "room_cache"; private String USER_LIST_CACHE = "contact_cache";
 * 
 * private String myAccount;
 * 
 * private SharedPreferences mSettings; private SharedPreferences.Editor
 * settingsEditor; private ImageOptions options;
 * 
 * private ArrayList<FriendsActiveModel> friendList = null; private int
 * MAX_RECENT_CHAT_LENGTH = 35;
 * 
 * private Collection<HostedRoom> listRooms = null;
 * 
 * private String TAG = getClass().getSimpleName();
 * 
 * public ExpandableListAdapter(Context context) { this._context = context;
 * this._listDataHeader = new ArrayList<String>(); setHeaderGroup();
 * this._listDataChild = new HashMap<String, ArrayList<FriendsActiveModel>>();
 * this.aq = new AQuery(_context); mSettings =
 * PreferenceManager.getDefaultSharedPreferences(context); settingsEditor =
 * mSettings.edit(); myAccount = mSettings.getString(AppConstants.USERNAME_KEY,
 * null); setImageOption(); }
 * 
 * private void setImageOption(){ options = new ImageOptions();
 * //options.animation = AQuery.FADE_IN; options.round = 5; options.fileCache =
 * true; options.memCache = true; options.targetWidth = 53; }
 * 
 * private void setHeaderGroup(){ _listDataHeader.add("Recent Chats");
 * _listDataHeader.add("Groups"); _listDataHeader.add("Rooms");
 * _listDataHeader.add("Contacts"); }
 * 
 * private void setHeaderCounter(int index, String counter){ String currentTitle
 * = _listDataHeader.get(index); _listDataHeader.set(index,
 * currentTitle+" ("+counter+")"); }
 * 
 * public void getContactList(boolean loadFromCache){ String friendCache =
 * mSettings.getString(USER_LIST_CACHE, null); if(loadFromCache){ if(friendCache
 * != null){ FriendsActiveStore fas; try { fas = new FriendsActiveStore(new
 * JSONObject(friendCache)); if(friendList!=null) friendList.clear(); friendList
 * = fas.getResult(); if(friendList!=null){ setHeaderCounter(3,
 * String.valueOf(friendList.size()));
 * _listDataChild.put(_listDataHeader.get(3), friendList); getChatList(); } }
 * catch (JSONException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } } } Map<String, String> parms = new HashMap<String,
 * String>(); parms.put("username", myAccount); aq.ajax(
 * AppConstants.APIActiveFriends, parms, JSONObject.class, new
 * AjaxCallback<JSONObject>(){ public void callback(String url, JSONObject
 * object, com.androidquery.callback.AjaxStatus status) { if(object != null){
 * settingsEditor.putString(USER_LIST_CACHE, object.toString());
 * settingsEditor.commit(); FriendsActiveStore fas = new
 * FriendsActiveStore(object); if(friendList!=null) friendList.clear();
 * friendList = fas.getResult(); _listDataChild.put(_listDataHeader.get(3),
 * friendList); getChatList(); } }; } ); }
 * 
 * public void getRoomList(){ if(listRooms==null){
 * Application.getInstance().runInBackground(new Runnable() {
 * 
 * @Override public void run() { try { listRooms =
 * MultiUserChat.getHostedRooms(AccountManager
 * .getInstance().getActiveAccount().getConnectionThread().getXMPPConnection(),
 * AppConstants.XMPPRoomsServer); if(!listRooms.isEmpty()){
 * ArrayList<FriendsActiveModel> roomList = new ArrayList<FriendsActiveModel>();
 * for(HostedRoom room : listRooms){ FriendsActiveModel famc = new
 * FriendsActiveModel(); famc.setAvatar(""); famc.setFullName(room.getName());
 * famc.setUserName(room.getJid()); famc.setStatus(""); roomList.add(famc); }
 * if(!roomList.isEmpty()){ setHeaderCounter(2,
 * String.valueOf(roomList.size())); _listDataChild.put(_listDataHeader.get(2),
 * roomList); ExpandableListAdapter.this.notifyDataSetChanged(); } } } catch
 * (XMPPException e) { e.printStackTrace(); } } }); } }
 * 
 * public void getGroupList(boolean loadFromCache){
 * 
 * }
 * 
 * private MessageItem createMessageItem(Cursor cursor, String targetAccount) {
 * AbstractChat ac = new AbstractChat(myAccount, targetAccount) {
 * 
 * @Override protected MessageItem newMessage(String text) { // TODO
 * Auto-generated method stub return null; }
 * 
 * @Override public Type getType() { // TODO Auto-generated method stub return
 * null; }
 * 
 * @Override public String getTo() { // TODO Auto-generated method stub return
 * null; } }; MessageItem messageItem = new MessageItem(ac,
 * MessageTable.getTag(cursor), MessageTable.getResource(cursor),
 * MessageTable.getText(cursor), MessageTable.getAction(cursor),
 * MessageTable.getTimeStamp(cursor), MessageTable.getDelayTimeStamp(cursor),
 * MessageTable.isIncoming(cursor), MessageTable.isRead(cursor),
 * MessageTable.isSent(cursor), MessageTable.hasError(cursor), true, false,
 * false, MessageTable.getPacketId(cursor),
 * MessageTable.getReadByFriend(cursor));
 * messageItem.setId(MessageTable.getId(cursor)); return messageItem; }
 * 
 * public void getChatList(){ if(friendList != null && !friendList.isEmpty()){
 * Application.getInstance().runInBackground(new Runnable() {
 * 
 * @Override public void run() { // TODO Auto-generated method stub
 * Application.getInstance().runOnUiThread(new Runnable() {
 * 
 * @Override public void run() { // TODO Auto-generated method stub
 * ArrayList<FriendsActiveModel> chatList = new ArrayList<FriendsActiveModel>();
 * ArrayList<String> userList = new ArrayList<String>(); Cursor cursor =
 * MessageTable.getInstance().recentList(); if (cursor.moveToFirst()) {
 * 
 * do { userList.add(MessageTable.getUser(cursor)); Log.e(TAG,
 * MessageTable.getUser(cursor)); } while (cursor.moveToNext()); }
 * cursor.close(); if(!userList.isEmpty()){ for(int b = 0; b < userList.size();
 * b++){ Cursor cursors = MessageTable.getInstance().last(myAccount,
 * userList.get(b)); try{ if (cursor.moveToFirst()) { MessageItem messageItem =
 * createMessageItem(cursors, userList.get(b)); FriendsActiveModel famc = new
 * FriendsActiveModel(); famc.setAvatar(""); famc.setFullName(userList.get(b));
 * famc.setUserName(userList.get(b)); famc.setStatus(messageItem.getText());
 * famc.setMessage(messageItem); chatList.add(famc); } cursors.close();
 * }catch(RuntimeException e){} } } if(!chatList.isEmpty()){
 * _listDataHeader.set(0, "Recent Chats"); setHeaderCounter(0,
 * String.valueOf(chatList.size())); _listDataChild.put(_listDataHeader.get(0),
 * chatList); }
 * 
 * } }); } });
 * 
 * } }
 * 
 * @Override public Object getChild(int groupPosition, int childPosititon) {
 * return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(
 * childPosititon); }
 * 
 * @Override public long getChildId(int groupPosition, int childPosition) {
 * return childPosition; }
 * 
 * @Override public View getChildView(int groupPosition, final int
 * childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 * 
 * FriendsActiveModel childText = (FriendsActiveModel) getChild(groupPosition,
 * childPosition);
 * 
 * if( groupPosition == 3 || groupPosition == 2 || groupPosition == 1 ){
 * 
 * LayoutInflater infalInflater = (LayoutInflater)
 * this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); convertView
 * = infalInflater.inflate(R.layout.list_item, null); TextView txtListChild =
 * (TextView) convertView.findViewById(R.id.lblListItem); TextView txtListStatus
 * = (TextView)convertView.findViewById(R.id.statusTextView);
 * 
 * ImageView avatar = (ImageView)convertView.findViewById(R.id.imageView1);
 * 
 * String currentAvatarUrl = childText.getAvatar();
 * 
 * if(currentAvatarUrl != null && !currentAvatarUrl.equals(""))
 * aq.id(avatar).image(childText.getAvatar(), options);
 * 
 * txtListChild.setText(childText.getFullName()); String currentText =
 * childText.getStatus();
 * 
 * if(currentText != null && !currentText.equals(""))
 * txtListStatus.setText(Emoticons.getSmiledText(this._context,
 * childText.getStatus()), BufferType.SPANNABLE);
 * 
 * }else if(groupPosition == 0){
 * 
 * LayoutInflater infalInflater = (LayoutInflater)
 * this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); convertView
 * = infalInflater.inflate(R.layout.list_chat_item, null); TextView txtListChild
 * = (TextView) convertView.findViewById(R.id.lblListItem); TextView
 * txtListStatus = (TextView)convertView.findViewById(R.id.statusTextView);
 * TextView unredcounter =
 * (TextView)convertView.findViewById(R.id.unredCounterTextView); TextView
 * datesText = (TextView)convertView.findViewById(R.id.datestext);
 * 
 * ImageView avatar = (ImageView)convertView.findViewById(R.id.imageView1);
 * ImageView chatStatus =
 * (ImageView)convertView.findViewById(R.id.chatStatusImageView);
 * 
 * MessageItem mi = childText.getMessage();
 * 
 * datesText.setText(DateHelper.setRecentChatTime(mi.getTimestamp()));
 * 
 * if(mi.isIncoming()){ chatStatus.setVisibility(View.GONE); if(!mi.isRead()){
 * 
 * }else{ unredcounter.setVisibility(View.GONE); } }else{ int messageResource =
 * 0;
 * 
 * Long currentMessageId = mi.getId(); boolean isReadByFrind = false;
 * if(currentMessageId != null && currentMessageId != 0){ try{ isReadByFrind =
 * RegularChat.getIsReadByFriend(currentMessageId);
 * }catch(CursorIndexOutOfBoundsException e){} }else isReadByFrind =
 * mi.getIsReadByFriend();
 * 
 * if(isReadByFrind) messageResource = R.drawable.ic_message_read; else{
 * if(mi.isDelivered()) messageResource = R.drawable.ic_message_not_delivered;
 * else{ if(mi.isSent()) messageResource = R.drawable.ic_message_not_sent; else
 * messageResource = R.drawable.ic_message_has_error; } }
 * 
 * chatStatus.setImageResource(messageResource); }
 * aq.id(avatar).image(childText.getAvatar(), options);
 * txtListChild.setText(childText.getFullName());
 * txtListStatus.setText(Emoticons.getSmiledText(this._context,
 * trimText(mi.getText())), BufferType.SPANNABLE);
 * 
 * }
 * 
 * return convertView; }
 * 
 * @Override public int getChildrenCount(int groupPosition) { try{ return
 * this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
 * }catch(NullPointerException e){ return 0; }
 * 
 * }
 * 
 * @Override public Object getGroup(int groupPosition) { return
 * this._listDataHeader.get(groupPosition); }
 * 
 * @Override public int getGroupCount() { return this._listDataHeader.size(); }
 * 
 * @Override public long getGroupId(int groupPosition) { return groupPosition; }
 * 
 * @Override public View getGroupView(int groupPosition, boolean isExpanded,
 * View convertView, ViewGroup parent) { String headerTitle = (String)
 * getGroup(groupPosition); if (convertView == null) { LayoutInflater
 * infalInflater = (LayoutInflater) this._context
 * .getSystemService(Context.LAYOUT_INFLATER_SERVICE); convertView =
 * infalInflater.inflate(R.layout.list_group, null); }
 * 
 * TextView lblListHeader = (TextView) convertView
 * .findViewById(R.id.lblListHeader); lblListHeader.setText(headerTitle);
 * switch(groupPosition){ case 0:
 * lblListHeader.setBackgroundResource(R.drawable.home_chat_category); break;
 * case 1: lblListHeader.setBackgroundResource(R.drawable.home_group_category);
 * break; case 2:
 * lblListHeader.setBackgroundResource(R.drawable.home_room_category); break;
 * case 3: lblListHeader.setBackgroundResource(R.drawable.home_friend_category);
 * break; } return convertView; }
 * 
 * @Override public boolean hasStableIds() { return false; }
 * 
 * @Override public boolean isChildSelectable(int groupPosition, int
 * childPosition) { return true; }
 * 
 * private String trimText(String text) { if (text.length() >
 * MAX_RECENT_CHAT_LENGTH) return text.substring(0, MAX_RECENT_CHAT_LENGTH - 3)
 * + "..."; else return text;
 * 
 * } }
 */