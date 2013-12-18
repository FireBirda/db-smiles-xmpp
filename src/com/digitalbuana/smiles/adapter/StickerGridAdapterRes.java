package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.IconAttachmentModel;
import com.digitalbuana.smiles.utils.FontUtils;

public class StickerGridAdapterRes extends BaseAdapter {

	private Context context;
	private LayoutInflater li;
	private ArrayList<IconAttachmentModel> list;
	private int heightGrid;

	public StickerGridAdapterRes(Context c, ArrayList<IconAttachmentModel> list, int height) {
		this.context = c;
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list =list;
		this.heightGrid=height;
	}
	
	public void setList(ArrayList<IconAttachmentModel> newList) {
		this.list = newList;
		this.notifyDataSetChanged();
	}

	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int pos) {
		return null;
	}

	@Override
	public long getItemId(int pos) {
		return 0;
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
	    	convertView = li.inflate(R.layout.adapter_sticker_grid, null);
	        holder = new ViewHolder();
	        holder.rootView = (FrameLayout)convertView.findViewById(R.id.adapterStickerGridRoot);
	        holder.avatar = (ImageView)convertView.findViewById(R.id.adapterStickerGridImage);
	        holder.title = (TextView)convertView.findViewById(R.id.adapterStickerTxt);
	        holder.progress = (ProgressBar)convertView.findViewById(R.id.adapterStickerGridProgress);
	        holder.progress.setVisibility(View.GONE);
	        FontUtils.setRobotoFont(context, holder.rootView);
	        convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		holder.avatar.getLayoutParams().height = heightGrid;
		holder.avatar.setPadding(2, 2, 2, 2);
		holder.title.setVisibility(View.VISIBLE);
		holder.title.setText(list.get(pos).getTitle());
		holder.avatar.setImageResource(list.get(pos).getResource());
		return convertView;
	}

	public static class ViewHolder {
		FrameLayout rootView;
		ImageView avatar;
		TextView title;
		ProgressBar progress;
	}

}
