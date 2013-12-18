package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.friends.AdminModel;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.ui.adapter.UpdatableAdapter;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class AdminUpdateAdapter extends BaseAdapter implements UpdatableAdapter {

	private ArrayList<AdminModel> listVisitor;
	private Context context;
	private LayoutInflater li;

	public AdminUpdateAdapter(Context c) {
		this.context = c;
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listVisitor = FriendsManager.getInstance().getAdminUpdateManager()
				.getAllNotification();
	}

	@Override
	public int getCount() {
		return listVisitor.size();
	}

	@Override
	public Object getItem(int pos) {
		return listVisitor.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = li.inflate(R.layout.item_friendsupdate, null);
			holder = new ViewHolder();
			holder.rootView = (LinearLayout) convertView
					.findViewById(R.id.visitorAdapterRoot);
			holder.avatar = (ImageView) convertView
					.findViewById(R.id.visitorAdapterAvatar);
			holder.txtName = (TextView) convertView
					.findViewById(R.id.visitorAdapterTxtName);
			holder.txtMessage = (TextView) convertView
					.findViewById(R.id.visitorAdapterTxtMessage);
			holder.txtTimeYear = (TextView) convertView
					.findViewById(R.id.visitorAdapterTxtYear);
			holder.txtTimeHour = (TextView) convertView
					.findViewById(R.id.visitorAdapterTxtHour);
			FontUtils.setRobotoFont(context, holder.rootView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.avatar.setImageResource(R.drawable.ic_launcher);
		holder.avatar.setVisibility(View.GONE);
		Log.i("", "" + listVisitor.get(pos).getID());
		Log.i("", listVisitor.get(pos).getMessage());
		Log.i("", listVisitor.get(pos).getTime());

		// AbstractContact abstractContact =
		// RosterManager.getInstance().getBestContact(AccountManager.getInstance().getAccountKu(),
		// listVisitor.get(pos).getJID());
		holder.txtName.setText(StringUtils.replaceStringEquals(listVisitor
				.get(pos).getMessage().replace("+", " ")));
		holder.txtMessage.setText(StringUtils.replaceStringEquals(listVisitor
				.get(pos).getTime().replace("+", " ")));
		// Time
		holder.txtTimeYear.setVisibility(View.GONE);
		holder.txtTimeHour.setVisibility(View.GONE);
		// holder.txtTimeYear.setText(StringUtils.parseTimeVisitorTahun(listVisitor.get(pos).getTime()));
		// holder.txtTimeHour.setText(StringUtils.parseTimeVisitorJam(listVisitor.get(pos).getTime()));
		if (pos % 2 == 0) {
			holder.rootView.setBackgroundResource(R.color.AbuStandard);
		} else {
			holder.rootView.setBackgroundResource(R.color.AbuStandardLow);
		}
		// FriendsUpdateManager.getInstance().removeAdminNotifications(AccountManager.getInstance().getAccountKu(),
		// listVisitor.get(pos).getName()+"@"+AppConstants.XMPPServerHost);
		// holder.avatar.setImageDrawable(abstractContact.getAvatar());
		// holder.friend = listVisitor.get(pos);

		return convertView;
	}

	public static class ViewHolder {
		LinearLayout rootView;
		ImageView avatar;
		TextView txtName;
		TextView txtMessage;
		TextView txtTimeYear;
		TextView txtTimeHour;
		AdminModel friend;

		public AdminModel getFriend() {
			return friend;
		}
	}

	@Override
	public void onChange() {
		listVisitor = FriendsManager.getInstance().getAdminUpdateManager()
				.getAllNotification();
		notifyDataSetChanged();
	}

}
