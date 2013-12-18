package com.digitalbuana.smiles.awan.adapters;

import java.util.ArrayList;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.CreateGroupActivity;
import com.digitalbuana.smiles.awan.holders.FriendsActiveHolder;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.StringUtils;

@SuppressLint("DefaultLocale")
public class GroupInviteAdapter extends ArrayAdapter<RosterContact> {

	private LayoutInflater mInflater;
	private Vector<String> buffNum = new Vector<String>();

	private static ArrayList<RosterContact> originalContact;
	private static ArrayList<RosterContact> fContact;

	private static ArrayList<RosterContact> objects;

	private Filter filter;
	private Context c;

	public GroupInviteAdapter(Context context, int textViewResourceId) {

		super(context, textViewResourceId);

		objects = new ArrayList<RosterContact>();
		generateContactList();

		originalContact = new ArrayList<RosterContact>(objects);
		fContact = new ArrayList<RosterContact>(objects);

		mInflater = LayoutInflater.from(context);
		filter = new Filtering();
		c = context;
	}

	private void generateContactList() {
		for (RosterContact rosterContact : RosterManager.getInstance()
				.getContacts()) {
			objects.add(rosterContact);
		}
	}

	public Vector<String> getBuffName() {
		return buffNum;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final FriendsActiveHolder holder;

		convertView = mInflater.inflate(R.layout.item_friend_select, null);
		holder = new FriendsActiveHolder();
		holder.avatar = (ImageView) convertView.findViewById(R.id.friendAvatar);
		holder.username = (TextView) convertView.findViewById(R.id.userName);
		holder.status = (TextView) convertView.findViewById(R.id.userStatus);
		holder.checkedContact = (CheckBox) convertView
				.findViewById(R.id.userSelected);

		if (fContact.size() > 0) {

			final RosterContact fam = fContact.get(position);

			boolean alreadyChecked = false;
			for (int d = 0; d < buffNum.size(); d++) {
				if (buffNum.get(d).equals(fam.getUser()))
					alreadyChecked = true;
			}
			holder.checkedContact.setChecked(alreadyChecked);
			holder.checkedContact
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							if (holder.checkedContact.isChecked()) {
								buffNum.add(fam.getUser());
							} else {
								if (buffNum.size() > 0) {
									buffNum.remove(fam.getUser());
								}
							}
							try {
								CreateGroupActivity.memberCounterTextView.setText("("
										+ buffNum.size()
										+ "/"
										+ c.getString(R.string.muc_max_member)
										+ ")");
							} catch (NullPointerException e) {
							}
						}
					});
			holder.avatar.setImageDrawable(fContact.get(position).getAvatar());
			holder.username.setText(StringUtils.replaceStringEquals(fam
					.getName()));
			holder.status.setText(fam.getStatusText());
		}

		return convertView;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		if (filter == null) {
			filter = new Filtering();
		}
		return filter;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fContact.size();
	}

	private class Filtering extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults results = new FilterResults();
			String prefix = constraint.toString().toLowerCase();

			if (prefix == null || prefix.length() == 0) {

				ArrayList<RosterContact> list = new ArrayList<RosterContact>(
						originalContact);
				results.values = list;
				results.count = list.size();

			} else {

				final ArrayList<RosterContact> nlist = new ArrayList<RosterContact>();

				for (RosterContact list : originalContact) {

					final String username = list.getName().toLowerCase();
					final String fullName = list.getUser().toLowerCase();

					if (username.contains(prefix) || fullName.contains(prefix)) {

						nlist.add(list);

					}
					results.values = nlist;
					results.count = nlist.size();
				}
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// TODO Auto-generated method stub
			fContact = (ArrayList<RosterContact>) results.values;
			notifyDataSetChanged();
			clear();
			int count = fContact.size();
			for (int i = 0; i < count; i++) {
				add(fContact.get(i));
				notifyDataSetInvalidated();
			}
		}

	}

}
