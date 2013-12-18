/*
 * package com.digitalbuana.smiles.awan.activity;
 * 
 * import java.util.ArrayList; import java.util.Collection; import
 * java.util.HashMap; import java.util.List;
 * 
 * import com.digitalbuana.smiles.R; import
 * com.digitalbuana.smiles.activity.ChatViewActivity; import
 * com.digitalbuana.smiles.activity.ProfileDetailActivity; import
 * com.digitalbuana.smiles.awan.adapters.ExpandableListAdapter; import
 * com.digitalbuana.smiles.awan.model.FriendsActiveModel; import
 * com.digitalbuana.smiles.data.AppConstants; import
 * com.digitalbuana.smiles.data.Application; import
 * com.digitalbuana.smiles.data.account.OnAccountChangedListener; import
 * com.digitalbuana.smiles.data.entity.BaseEntity; import
 * com.digitalbuana.smiles.data.extension.vcard.OnVCardListener; import
 * com.digitalbuana.smiles.data.message.OnChatChangedListener; import
 * com.digitalbuana.smiles.data.roster.OnContactChangedListener; import
 * com.digitalbuana.smiles.xmpp.vcard.VCard;
 * 
 * import android.app.Activity; import android.content.SharedPreferences; import
 * android.graphics.Color; import android.graphics.drawable.GradientDrawable;
 * import android.graphics.drawable.GradientDrawable.Orientation; import
 * android.net.Uri; import android.os.Bundle; import
 * android.preference.PreferenceManager; import android.util.DisplayMetrics;
 * import android.util.Log; import android.view.View; import
 * android.widget.ExpandableListView; import
 * android.widget.ExpandableListView.OnChildClickListener;
 * 
 * public class MainActivity extends Activity implements
 * OnContactChangedListener, OnAccountChangedListener, OnChatChangedListener,
 * OnVCardListener {
 * 
 * private static final String SAVED_SEND_TEXT =
 * "com.digitalbuana.smiles.activity.HomeActivity.SAVED_SEND_TEXT";
 * 
 * public static int[] listViewDeviderColor = { Color.WHITE, Color.LTGRAY };
 * 
 * ExpandableListAdapter listAdapter; ExpandableListView expListView;
 * List<String> listDataHeader; HashMap<String, ArrayList<FriendsActiveModel>>
 * listDataChild; private String myAccount; private SharedPreferences mSettings;
 * private String TAG = getClass().getSimpleName();
 * 
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
 * 
 * mSettings = PreferenceManager.getDefaultSharedPreferences(this); myAccount =
 * mSettings.getString(AppConstants.USERNAME_KEY, null);
 * 
 * if(myAccount==null){ finish(); }
 * 
 * expListView = (ExpandableListView) findViewById(R.id.lvExp); listAdapter =
 * new ExpandableListAdapter(this); expListView.setAdapter(listAdapter);
 * listAdapter.getContactList(true); listAdapter.getRoomList();
 * //listAdapter.getGroupList(true); //listAdapter.getChatList();
 * 
 * expListView.setDivider(new GradientDrawable(Orientation.BOTTOM_TOP,
 * listViewDeviderColor)); expListView.setDividerHeight(1);
 * 
 * expListView.expandGroup(0); expListView.expandGroup(2);
 * expListView.expandGroup(3);
 * 
 * DisplayMetrics metrics = new DisplayMetrics();
 * getWindowManager().getDefaultDisplay().getMetrics(metrics); int width =
 * metrics.widthPixels; expListView.setIndicatorBounds(width -
 * GetPixelFromDips(18), width - GetPixelFromDips(10));
 * 
 * expListView.setOnChildClickListener(new OnChildClickListener() {
 * 
 * @Override public boolean onChildClick(ExpandableListView parent, View v, int
 * groupPosition, int childPosition, long id) { // TODO Auto-generated method
 * stub FriendsActiveModel item = (FriendsActiveModel)
 * listAdapter.getChild(groupPosition, childPosition); switch(groupPosition){
 * case 0: startActivity(ChatViewActivity.createSendIntent( MainActivity.this,
 * myAccount, item.getUserName(), SAVED_SEND_TEXT)); finish(); break; case 1:
 * break; case 2: break; case 3: startActivity(
 * ProfileDetailActivity.createIntent(MainActivity.this, myAccount,
 * Uri.encode(item.getUserName()))); break; } return false; } }); }
 * 
 * public int GetPixelFromDips(float pixels) { // Get the screen's density scale
 * final float scale = getResources().getDisplayMetrics().density; // Convert
 * the dps to pixels, based on density scale return (int) (pixels * scale +
 * 0.5f); }
 * 
 * private void contactChange(){ listAdapter.getContactList(false);
 * listAdapter.notifyDataSetChanged(); }
 * 
 * @Override public void onContactsChanged(Collection<BaseEntity> entities) { //
 * TODO Auto-generated method stub Log.e(TAG, "onContactsChanged");
 * //contactChange(); }
 * 
 * @Override public void onAccountsChanged(Collection<String> accounts) { //
 * TODO Auto-generated method stub //contactChange(); Log.e(TAG,
 * "onAccountsChanged"); }
 * 
 * @Override public void onChatChanged(String account, String user, boolean
 * incoming) { // TODO Auto-generated method stub listAdapter.getChatList();
 * listAdapter.notifyDataSetChanged(); Log.e(TAG, "onChatChanged"); }
 * 
 * @Override public void onVCardReceived(String account, String bareAddress,
 * VCard vCard) { // TODO Auto-generated method stub //contactChange();
 * Log.e(TAG, "onVCardReceived"); }
 * 
 * @Override public void onVCardFailed(String account, String bareAddress) { //
 * TODO Auto-generated method stub
 * 
 * }
 * 
 * @Override protected void onResume() { // TODO Auto-generated method stub
 * super.onResume();
 * Application.getInstance().addUIListener(OnAccountChangedListener.class,
 * this);
 * Application.getInstance().addUIListener(OnContactChangedListener.class,
 * this); Application.getInstance().addUIListener(OnChatChangedListener.class,
 * this); Application.getInstance().addUIListener(OnVCardListener.class, this);
 * listAdapter.getChatList(); listAdapter.notifyDataSetChanged(); }
 * 
 * @Override protected void onDestroy() { // TODO Auto-generated method stub
 * super.onDestroy();
 * Application.getInstance().removeUIListener(OnAccountChangedListener.class,
 * this);
 * Application.getInstance().removeUIListener(OnContactChangedListener.class,
 * this);
 * Application.getInstance().removeUIListener(OnChatChangedListener.class,
 * this); Application.getInstance().removeUIListener(OnVCardListener.class,
 * this); } }
 */