package com.digitalbuana.smiles.awan.adapters;

import java.util.ArrayList;

import com.androidquery.AQuery;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.awan.holders.RightMenuMultimediaHolder;
import com.digitalbuana.smiles.awan.model.RightMenuMultimediaModel;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class RightMenuMultimediaAdapter extends ArrayAdapter<RightMenuMultimediaModel>{
	
	private AQuery aq;
	private ArrayList<RightMenuMultimediaModel> list;
	private LayoutInflater mInflater;
	private Activity act;
	private int thumbBannerWidth;
	private int sceenWidth;
	public RightMenuMultimediaAdapter(Activity a, Context context, int textViewResourceId,
			ArrayList<RightMenuMultimediaModel> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		aq = new AQuery(a);
		list = objects;
		mInflater=LayoutInflater.from(context);
		act = a;
		sceenWidth = ScreenHelper.getScreenWidth(act); 
		thumbBannerWidth = (int) (sceenWidth/2);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		RightMenuMultimediaHolder holder;
		if(convertView == null){
    		convertView=mInflater.inflate(R.layout.item_rightmenu_multimedia, null);
    		holder = new RightMenuMultimediaHolder();
    		holder.thumb = (ImageView)convertView.findViewById(R.id.imageView1);
    		convertView.setTag(holder);
    	}else{
    		holder=(RightMenuMultimediaHolder)convertView.getTag();
    	}
		
		LayoutParams params = holder.thumb.getLayoutParams();
		
		params.height = sceenWidth/4;
		params.width = thumbBannerWidth;
		
		RightMenuMultimediaModel hm = list.get(position);  
		aq.id(holder.thumb).image(hm.getIcon(), true, true, thumbBannerWidth, 0, null, AQuery.FADE_IN, AQuery.RATIO_PRESERVE /*ScreenHelper.AQueryImageOption()*/);
    	holder.thumb.setTag(hm.getIcon());
    	
    	RightMenuMultimediaModel ct = new RightMenuMultimediaModel();
    	ct.setIcon(hm.getIcon());
    	
    	convertView.setTag(R.id.detailTag, ct);
    	
		return convertView;
	}

}
