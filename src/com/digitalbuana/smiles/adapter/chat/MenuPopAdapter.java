package com.digitalbuana.smiles.adapter.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.utils.FontUtils;

public class MenuPopAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater li;
	private boolean isMUC = false;
	private final static String[] menuPerson = { "View Profile",
			"Edit Contact",
			// "Show History",
			"Clear History",
	// "Export Chat",
	// "Chat Settings"
	};
	private final static String[] menuMUC = { "Invite Contact", "Users List",
			"Leave",
			// "Show History",
			"Clear History",
	// "Export Chat",
	// "Chat Settings"
	};

	public MenuPopAdapter(Context c) {
		this.context = c;
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setChangeMUC(boolean isMUC) {
		this.isMUC = isMUC;
		notifyDataSetChanged();
	}

	public boolean getISMUC() {
		return isMUC;
	}

	@Override
	public int getCount() {
		if (isMUC) {
			return menuMUC.length;
		} else {
			return menuPerson.length;
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
		ViewHolder holder;
		if (convertView == null) {
			convertView = li.inflate(R.layout.item_menu_pop, null);
			holder = new ViewHolder();
			holder.rootView = (FrameLayout) convertView
					.findViewById(R.id.itemMenuPopRoot);
			holder.text = (TextView) convertView
					.findViewById(R.id.itemMenuPopText);
			FontUtils.setRobotoFont(context, convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (isMUC) {
			holder.text.setText(menuMUC[pos]);
		} else {
			holder.text.setText(menuPerson[pos]);
		}
		return convertView;
	}

	public static class ViewHolder {
		FrameLayout rootView;
		TextView text;
	}

}
