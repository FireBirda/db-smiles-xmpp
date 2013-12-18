package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.utils.FontUtils;

public class LeftMenuAdapter extends BaseAdapter {

	public static int indexLeftMenuPos = 1;
	private ArrayList<LeftMenuItem> list = null;
	private Context context;
	private LayoutInflater li;

	public LeftMenuAdapter(Context c) {
		this.context = c;
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		list = new ArrayList<LeftMenuAdapter.LeftMenuItem>();
		// Setting Item
		LeftMenuItem item1 = new LeftMenuItem(1, 0, 2, true, "Add Friends",
				null);
		LeftMenuItem item2 = new LeftMenuItem(2, 0, 4, true, "Friend Request",
				null);
		LeftMenuItem item3 = new LeftMenuItem(3, 0, 3, true,
				"Create Group Chat", null);
		LeftMenuItem item4 = new LeftMenuItem(4, 0, 6, true, "Blocked Friends",
				null);

		LeftMenuItem item5 = new LeftMenuItem(0, 0, 0, false, "Connections",
				null);

		LeftMenuItem item6 = new LeftMenuItem(5, 1, 0, true, "Friends Update",
				null);
		LeftMenuItem item7 = new LeftMenuItem(6, 1, 1, true, "Visitor", null);
		LeftMenuItem item8 = new LeftMenuItem(7, 1, 2, true, "Notification",
				null);
		LeftMenuItem item9 = new LeftMenuItem(8, 1, 3, true, "Join Rooms", null);
		LeftMenuItem item10 = new LeftMenuItem(9, 0, 1, true,
				"Broadcast Message", null);

		LeftMenuItem item11 = new LeftMenuItem(10, 2, 0, true, "Settings", null);
		LeftMenuItem item13 = new LeftMenuItem(11, 2, 0, true, "Search", null);
		LeftMenuItem item12 = new LeftMenuItem(12, 2, 0, true, "Log Out", null);

		list.add(item1);
		list.add(item2);
		list.add(item3);
		list.add(item4);
		list.add(item5);
		list.add(item6);
		list.add(item7);
		list.add(item8);
		list.add(item9);
		list.add(item10);
		list.add(item11);
		list.add(item13);
		list.add(item12);
		indexLeftMenuPos = 99;
	}

	@Override
	public int getCount() {
		if (list.size() > 0 || list != null) {
			return list.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int pos) {
		return pos;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = li.inflate(R.layout.adapter_leftmenu, null);
			holder = new ViewHolder();
			holder.adapterLeftMenuCounter = (TextView) convertView
					.findViewById(R.id.adapterLeftMenuCounter);
			holder.rootView = (FrameLayout) convertView
					.findViewById(R.id.adapterLeftMenuRoot);
			holder.backgroundView = (LinearLayout) convertView
					.findViewById(R.id.adapterLeftMenuBackground);
			holder.selected = (FrameLayout) convertView
					.findViewById(R.id.adapterLeftMenuSelected);
			holder.text = (TextView) convertView
					.findViewById(R.id.adapterLeftMenuText);
			holder.isSelectable = list.get(pos).isSelectable;
			convertView.setTag(holder);
			FontUtils.setRobotoFont(context, convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.isSelectable = list.get(pos).isSelectable;
		holder.text.setText(list.get(pos).namaMenu);

		holder.adapterLeftMenuCounter.setVisibility(View.GONE);

		int totalCounter = 0;

		if (pos == 1) { // -> friend request
			totalCounter = FriendsManager.getInstance()
					.getFriendsWaitingMeApproveManager().getAllFriends().size();
		} else if (pos == 3) { // -> friend blocked
			totalCounter = FriendsManager.getInstance()
					.getFriendsBlockedManager().getAllFriends().size();
		} else if (pos == 5) { // -> friend updates
			totalCounter = FriendsManager.getInstance()
					.getFriendsUpdateManager().getFriendsUpdate().size();
		} else if (pos == 6) { // -> visitors
			totalCounter = FriendsManager.getInstance().getVisitorManager()
					.getAllVisitor().size();
		} else if (pos == 7) { // -> notification
			totalCounter = FriendsManager.getInstance().getAdminUpdateManager()
					.getUnread();
		}

		if (totalCounter > 0) {
			holder.adapterLeftMenuCounter.setText(String.valueOf(totalCounter));
			holder.adapterLeftMenuCounter.setVisibility(View.VISIBLE);
		}

		if (pos == list.size() - 1) {
			holder.text.setTextColor(Color.parseColor("#a00000"));
		} else {
			holder.text.setTextColor(Color.parseColor("#ffffff"));
		}
		holder.index = list.get(pos).index;
		// Handler Selected
		holder.selected.setVisibility(View.GONE);

		if (list.get(pos).isSelectable) {
			holder.backgroundView
					.setBackgroundResource(R.drawable.btn_transparant_endog);
			holder.rootView
					.setBackgroundResource(R.drawable.expander_account_dark);
		} else if (!list.get(pos).isSelectable) {
			holder.backgroundView.setBackgroundResource(R.color.Hitam);
			holder.rootView.setBackgroundResource(R.color.Hitam);
		}
		return convertView;
	}

	public static class ViewHolder {
		FrameLayout rootView;
		LinearLayout backgroundView;
		FrameLayout selected;
		TextView text, adapterLeftMenuCounter;
		boolean isSelectable;
		int index;

		public boolean isSelectable() {
			return isSelectable;
		}

		public int getIndex() {
			return index;
		}
	}

	public class LeftMenuItem {
		int index;
		int indexMain;
		int indexSubMain;
		boolean isSelectable;
		public String namaMenu;

		// String numberOfUnread;

		public LeftMenuItem(int index, int indexMain, int indexSubMain,
				boolean isSelectable, String namaMenu, String numberOfUnread) {
			this.index = index;
			this.indexMain = indexMain;
			this.indexSubMain = indexSubMain;
			this.isSelectable = isSelectable;
			this.namaMenu = namaMenu;
			// this.numberOfUnread=numberOfUnread;
		}
	}

}
