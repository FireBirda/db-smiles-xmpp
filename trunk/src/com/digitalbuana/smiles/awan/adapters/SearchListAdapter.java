package com.digitalbuana.smiles.awan.adapters;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.AddFriendsAdapter.OnAddFriendsListener;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class SearchListAdapter extends ArrayAdapter<String> {

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private AQuery aq;
	private int _resource;
	private LayoutInflater li;
	private Context _context;
	private OnAddFriendsListener _listener;
	private ArrayList<String> _list;
	private Collection<RosterContact> rosterContacts;
	private ArrayList<Boolean> isFriend;
	private String TAG = getClass().getSimpleName();

	public SearchListAdapter(Context context, int resource,
			ArrayList<String> objects, OnAddFriendsListener listener) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		mSettings = PreferenceManager.getDefaultSharedPreferences(context);
		settingsEditor = mSettings.edit();
		aq = new AQuery(context);
		_listener = listener;
		_context = context;
		_resource = resource;
		_list = objects;
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rosterContacts = RosterManager.getInstance().getContacts();
		isFriend = new ArrayList<Boolean>();
		isFriendFilter();
	}

	private void isFriendFilter() {
		int b = 0;
		for (String userName : _list) {
			for (RosterContact contact : rosterContacts) {
				String currentUser = StringUtils.replaceStringEquals(userName);
				if (currentUser.equals(StringUtils.replaceStringEquals(contact
						.getUser())))
					isFriend.add(b, true);
			}
			if (isFriend.isEmpty() || isFriend.size() == b)
				isFriend.add(b, false);
			b++;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		// if (convertView == null) {
		convertView = li.inflate(R.layout.adapter_addfriends, null);
		holder = new ViewHolder();
		holder.rootView = (LinearLayout) convertView
				.findViewById(R.id.adapterAddFriendsRoot);
		holder.avatar = (ImageView) convertView
				.findViewById(R.id.adapterAddFriendsAvatar);
		holder.name = (TextView) convertView
				.findViewById(R.id.adapterAddFriendsName);
		holder.btnAdd = (FrameLayout) convertView
				.findViewById(R.id.adapterAddFriendsbtn);
		convertView.setTag(holder);
		FontUtils.setRobotoFont(_context, convertView);
		// } else {
		// holder = (ViewHolder) convertView.getTag();
		// }
		holder.name.setText(_list.get(position));
		final String userJID = StringUtils.replaceStringEquals(_list
				.get(position));
		holder.avatar.setImageDrawable(RosterManager
				.getInstance()
				.getBestContact(
						AccountManager.getInstance().getActiveAccount()
								.getAccount(),
						userJID + "@" + AppConstants.XMPPServerHost)
				.getAvatar());
		holder.btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_listener.addFriendsWithUsername(userJID);
			}
		});
		if (isFriend.get(position))
			holder.btnAdd.setVisibility(View.GONE);
		return convertView;
	}

	public static class ViewHolder {
		LinearLayout rootView;
		ImageView avatar;
		TextView name;
		FrameLayout btnAdd;
	}

}
