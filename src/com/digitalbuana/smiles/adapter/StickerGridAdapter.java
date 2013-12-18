package com.digitalbuana.smiles.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.utils.FontUtils;

public class StickerGridAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater li;
	private ArrayList<String> list;
	private int heightGrid;
//	private ImageLoader imageLoader;
//	private DisplayImageOptions options;
	
	public StickerGridAdapter(Context c, ArrayList<String> list, int height) {
		this.context = c;
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list =list;
		this.heightGrid=height;
//		options = new DisplayImageOptions.Builder()
//		.showStubImage(R.drawable.img_default_no_sticker)
//		.showImageForEmptyUri(R.drawable.img_default_no_sticker)
//		.showImageOnFail(R.drawable.kosong)
//		.cacheInMemory()
//		.cacheOnDisc()
//		.delayBeforeLoading(1)
//		.build();
//		imageLoader = ImageLoader.getInstance();
	}
	
	public void setList(ArrayList<String> newList) {
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
	        FontUtils.setRobotoFont(context, holder.rootView);
	        convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		holder.avatar.getLayoutParams().height = heightGrid;
		holder.title.setVisibility(View.GONE);		
		final AQuery aq = new AQuery(convertView);
		aq.id(R.id.adapterStickerGridImage).progress(R.id.adapterStickerGridProgress).
		image(
			list.get(pos), 
			true, 
			true, 
			0, 
			R.drawable.img_default_no_sticker, 
			new BitmapAjaxCallback(){
				@Override
				protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
					// TODO Auto-generated method stub
					super.callback(url, iv, bm, status);
					if(status.getCode() != AjaxStatus.NETWORK_ERROR && 
							status.getCode() != AjaxStatus.AUTH_ERROR && 
							status.getCode() != AjaxStatus.TRANSFORM_ERROR)
						iv.setImageBitmap(bm);
					else{
						// hapus file jika sudah tersimpan sebagai cache
						try{
							File file = aq.getCachedFile(list.get(pos));
							file.delete();
						}catch(NullPointerException e){}
					}
				}
			}
		);		
		return convertView;
	}

	public static class ViewHolder {
		FrameLayout rootView;
		ImageView avatar;
		TextView title;
		ProgressBar progress;
	}

}
