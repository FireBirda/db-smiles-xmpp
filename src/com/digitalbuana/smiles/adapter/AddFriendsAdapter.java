package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class AddFriendsAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater li;
	private ArrayList<String> listSearch;
	private OnAddFriendsListener listener;
	private Collection<RosterContact> rosterContacts;
	private ArrayList<Boolean> isFriend;

	public AddFriendsAdapter(Context c, ArrayList<String> listSearch,
			OnAddFriendsListener listener) {
		this.listener = listener;
		this.listSearch = listSearch;
		this.context = c;
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rosterContacts = RosterManager.getInstance().getContacts();
		isFriend = new ArrayList<Boolean>();
		isFriendFilter();
	}

	private void isFriendFilter() {
		int b = 0;
		for (String userName : this.listSearch) {
			for (RosterContact contact : rosterContacts) {
				if (userName.equals(StringUtils.replaceStringEquals(contact
						.getUser())))
					isFriend.add(b, true);
			}
			if (isFriend.isEmpty() || isFriend.size() == b)
				isFriend.add(b, false);
			b++;
		}
	}

	public void setList(ArrayList<String> listSearch) {
		this.listSearch = listSearch;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return listSearch.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
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
		FontUtils.setRobotoFont(context, convertView);
		// } else {
		// holder = (ViewHolder) convertView.getTag();
		// }
		holder.name.setText(listSearch.get(pos));
		final String userJID = listSearch.get(pos).replace(
				"@" + AppConstants.XMPPServerHost, "");
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
				listener.addFriendsWithUsername(userJID);
			}
		});
		try {
			if (isFriend.get(pos))
				holder.btnAdd.setVisibility(View.GONE);
		} catch (IndexOutOfBoundsException e) {
		}

		return convertView;
	}

	public static class ViewHolder {
		LinearLayout rootView;
		ImageView avatar;
		TextView name;
		FrameLayout btnAdd;
	}

	public interface OnAddFriendsListener {
		void addFriendsWithUsername(String userWithoutJID);
	}
}
