package com.digitalbuana.smiles.adapter;

import com.digitalbuana.smiles.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter {
	private String[][] list;
	LayoutInflater mInflater;
	public ContactAdapter(Context c, String[][] strArrList){
		list = strArrList;
		mInflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list[1].length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub 
		ViewHolder holder = new ViewHolder();
		convertView = mInflater.inflate(R.layout.item_contact_row, parent, false);
		
		holder.txtName = (TextView)convertView.findViewById(R.id.roomName);
		holder.txtNumber= (TextView)convertView.findViewById(R.id.roomDesc);
		holder.tmpThumb = (ImageView)convertView.findViewById(R.id.roomThumbnail);
		holder.tmpThumb.setVisibility(View.GONE);
		
		holder.txtName.setText(list[0][position]);
		holder.txtNumber.setText(list[1][position]);
		
		return convertView;
	}
	static class ViewHolder{
    	TextView txtName;        
    	TextView txtNumber;               
    	ImageView tmpThumb;
    }

}

