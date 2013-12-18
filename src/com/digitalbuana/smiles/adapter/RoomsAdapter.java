package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smackx.muc.HostedRoom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.utils.FontUtils;

public class RoomsAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater li;
	private ArrayList<ArrayList<String>> listHosted;

	
	public RoomsAdapter(Context c, Collection<HostedRoom> list) {
		this.context = c;
		listHosted = new ArrayList<ArrayList<String>>();
		for (HostedRoom room : list) {
			ArrayList<String> listToAdd = new ArrayList<String>();
			listToAdd.add(room.getName());
			listToAdd.add(room.getJid());
			listHosted.add(listToAdd);
		}
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	@Override
	public int getCount() {
		return listHosted.size();
	}

	@Override
	public Object getItem(int pos) {
		return null;
	}

	@Override
	public long getItemId(int groups) {
		return 0;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {	
		final ViewHolder holder;
		if (convertView == null) {
	    	convertView = li.inflate(R.layout.adapter_room, null);
	        holder = new ViewHolder();
	        holder.rootView = (FrameLayout)convertView.findViewById(R.id.adapterRoomRootView);
	        holder.thumbnail = (ImageView)convertView.findViewById(R.id.adapterRoomThumb);
	        holder.txtRooms = (TextView)convertView.findViewById(R.id.adapterRoomTitle);
	        FontUtils.setRobotoFont(context, holder.rootView);
	        convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		holder.txtRooms.setText(listHosted.get(pos).get(0));
		holder.jidRom = listHosted.get(pos).get(1);
		return convertView;
	}

	public static class ViewHolder {
		FrameLayout rootView;
		ImageView thumbnail;
		TextView txtRooms;
		String jidRom;
		public String getJIDRoom(){
			return jidRom;
		}
	}
}
