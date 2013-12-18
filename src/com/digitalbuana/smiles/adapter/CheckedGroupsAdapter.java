package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.roster.RosterGroup;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.FontUtils;

public class CheckedGroupsAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater li;
	public ArrayList<RosterGroup> allContact;
	public ArrayList<RosterGroup> checkedContact;
	private String TAG = getClass().getSimpleName();
	
	public CheckedGroupsAdapter(Context c) {
		this.context = c;
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		allContact = new ArrayList<RosterGroup>();
		checkedContact = new ArrayList<RosterGroup>();
		for (RosterGroup rosterContact : RosterManager.getInstance().getRosterGroups()) {
		    allContact.add(rosterContact);
		}
	}
	
	@Override
	public int getCount() {
		return allContact.size();
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
		if (convertView == null) {
	    	convertView = li.inflate(R.layout.adapter_checkedfriends, null);
	        holder = new ViewHolder();
	        holder.rootView = (LinearLayout)convertView.findViewById(R.id.adapterCheckedFriendsRoot);
	        holder.avatar = (ImageView)convertView.findViewById(R.id.adapterCheckedFriendsAvatar);
	        holder.name = (TextView)convertView.findViewById(R.id.adapterCheckedFriendsName);
	        holder.check = (CheckBox)convertView.findViewById(R.id.adapterCheckedFriendsCheck);
	        convertView.setTag(holder);
	        holder.avatar.getLayoutParams().width=0;
	        holder.avatar.setImageDrawable(null);
	        FontUtils.setRobotoFont(context, convertView);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		holder.name.setText(allContact.get(pos).getName());
		holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					checkedContact.add(allContact.get(pos));
				} else {
					checkedContact.remove(allContact.get(pos));
				}
				Log.i(TAG, "checkedContact"+checkedContact.size());
			}
			

		});
		return convertView;
	}

	public static class ViewHolder {
		LinearLayout rootView;
		ImageView avatar;
		TextView name;
		CheckBox check;
	}
}
