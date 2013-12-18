package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.StickerPagerAdapter.OnGridPagerClickListener;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;

public class EmoticonPagerAdapterRes extends PagerAdapter  {
	
	private ArrayList<Integer> emoList;
	private Activity _activity;
	private OnGridPagerClickListener onGridFeaturedClicklistener;
	private Context context;
	private int numOfGrid;
	private int numOfVer=8;
	private int numOfPage=0;
	
	public EmoticonPagerAdapterRes(Activity activity,
			OnGridPagerClickListener onGridFeaturedClicklistener){
		this._activity = activity;
		this.context=activity.getApplicationContext();
		this.emoList=Emoticons.getEmoList();
		this.onGridFeaturedClicklistener = onGridFeaturedClicklistener;
	}
	
	
	@Override
	public int getCount() {
		if(numOfPage==0){
			return 1;
		} else {
		      return numOfPage;
		}
    }

	@Override
	public Object instantiateItem(View collection, final int position) {
			LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.adapter_sticker_pager, null);
			FrameLayout rootView = (FrameLayout) view.findViewById(R.id.adapterPagerRoot);
			GridViewKu gridView = (GridViewKu) view.findViewById(R.id.adapterPagerGrid);
			EmoticonAdapter adapter = new EmoticonAdapter(_activity, emoList);
			gridView.setAdapter(adapter);
			//adapter.notifyDataSetChanged();
			gridView.setNumColumns(numOfVer);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					int pagePos = (position*numOfGrid)+arg2;
					//Log.e(AppConstants.TAG, "Grid cliced at pos :"+pagePos);
					onGridFeaturedClicklistener.gridPagerClicked(pagePos);
				}
			});
			FontUtils.setRobotoFont(context, rootView);
			((ViewPager) collection).addView(view, 0);
			return view;
	}
	
	@Override
	public boolean isViewFromObject(View container, Object object) {
		 return container == ((View) object);
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

}
