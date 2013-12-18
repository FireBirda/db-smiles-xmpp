package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.utils.FontUtils;

public class SoundMenuAdapter extends BaseAdapter {

	public static int indexLeftMenuPos = 1;
	private ArrayList<LeftMenuItem> list=null;
	private Context context;
	private LayoutInflater li;

	public SoundMenuAdapter(Context c){
		
		this.context = c;
		
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		list = new ArrayList<SoundMenuAdapter.LeftMenuItem>();
		//Setting Item
		LeftMenuItem item1 = new LeftMenuItem(1, 0, 1, true, c.getString(R.string.setting_sound_attention), null);
		LeftMenuItem item2 = new LeftMenuItem(2, 0, 2, true, c.getString(R.string.setting_sound_chat_notif), null);
		LeftMenuItem item3 = new LeftMenuItem(3, 0, 3, true, c.getString(R.string.setting_sound_chat_send), null);
		
		list.add(item1);
		list.add(item2);
		list.add(item3);
		indexLeftMenuPos = 99;
	}
	
	@Override
	public int getCount() {
		if(list.size()>0||list!=null){
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
	    	convertView = li.inflate(R.layout.adapter_settings, null);
	        holder = new ViewHolder();
	        holder.rootView = (FrameLayout)convertView.findViewById(R.id.adapterLeftMenuRoot);
	        holder.backgroundView = (FrameLayout)convertView.findViewById(R.id.adapterLeftMenuBackground);
	        holder.selected = (FrameLayout)convertView.findViewById(R.id.adapterLeftMenuSelected);
	        holder.text = (TextView)convertView.findViewById(R.id.adapterLeftMenuText);
	        holder.isSelectable = list.get(pos).isSelectable;
	        convertView.setTag(holder);
	        FontUtils.setRobotoFont(context, convertView);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }		
		holder.text.setText(list.get(pos).namaMenu);		
		return convertView;
	}
	
	public static class ViewHolder {
		FrameLayout rootView;
		FrameLayout backgroundView;
		FrameLayout selected;
		TextView text;
		boolean isSelectable;
		int index;
		public boolean isSelectable(){
			return isSelectable;
		}
		public int getIndex(){
			return index;
		}
	}
	
	public class LeftMenuItem
	{
		int index;
		int indexMain;
		int indexSubMain;
		boolean isSelectable;
		public String namaMenu;
		String numberOfUnread;
		
		public LeftMenuItem(
				int index,
				int indexMain,
				int indexSubMain,
				boolean isSelectable,
				String namaMenu,
				String numberOfUnread
				){
			this.index = index;
			this.indexMain = indexMain;
			this.indexSubMain = indexSubMain;
			this.isSelectable = isSelectable;
			this.namaMenu = namaMenu;
			this.numberOfUnread=numberOfUnread;
		}
	}

}
