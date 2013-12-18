package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

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
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;

public class StickerPagerAdapter extends PagerAdapter  {

	private ArrayList<String> list;
	private OnGridPagerClickListener onGridFeaturedClicklistener;
	private Context context;
	private int numOfGrid;
	private int numOfVer;
	private int numOfPage=0;
	private int heightGrid=0;
	private ArrayList<ArrayList<String>> listAll;
	private String TAG = getClass().getSimpleName();
	
	public StickerPagerAdapter(Context context, ArrayList<String> list, 
			OnGridPagerClickListener onGridFeaturedClicklistener, int numVer, int numHor, int height){
		this.listAll = new ArrayList<ArrayList<String>>();
		this.context=context;
		this.list=list;
		this.numOfGrid=numVer*numHor;
		this.numOfVer = numVer;
		this.heightGrid = height;
		this.onGridFeaturedClicklistener = onGridFeaturedClicklistener;
		float asal = ((float)list.size()/(float)numOfGrid)+0.45f;
		this.numOfPage = (int)Math.round(asal);
		
		if(list.size()==1 && numOfPage == 0)
			numOfPage = 1;
		
		for (int i = 0; i < numOfPage; i++) {
			ArrayList<String> listTemp = new ArrayList<String>();
			listAll.add(listTemp);
		}
		
		int containerX =-1;
		
		if(listAll.size() > 0 && this.list.size() > 0){
			for (int i = 0; i < this.list.size(); i++) {
				if(i%numOfGrid==0){
					containerX+=1;
				}
				if(listAll.get(containerX)!=null){
					if(list.get(i)!=null){
						listAll.get(containerX).add(list.get(i));
					}
				}
				
			}
		}		
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
		if(numOfPage==0){
			LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.adapter_sticker_pager_null, null);
			FrameLayout rootView = (FrameLayout) view.findViewById(R.id.adapterPagerRoot);
			FontUtils.setRobotoFont(context, rootView);
			((ViewPager) collection).addView(view, 0);
			return view;
		} else {
			LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.adapter_sticker_pager, null);
			FrameLayout rootView = (FrameLayout) view.findViewById(R.id.adapterPagerRoot);
			GridViewKu gridView = (GridViewKu) view.findViewById(R.id.adapterPagerGrid);
			StickerGridAdapter adapter = new StickerGridAdapter(context, listAll.get(position) , heightGrid);
			gridView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
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
	}
	
	@Override
	public boolean isViewFromObject(View container, Object object) {
		 return container == ((View) object);
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	public interface OnGridPagerClickListener
	{
		void gridPagerClicked(int pos);
	}
	
}