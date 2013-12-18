package com.digitalbuana.smiles.awan.helper;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class MapItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext=null;
	
	public MapItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		}
	
	/*public MapItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		}*/
	
	/*@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub
		if(mContext!=null){
			OverlayItem item = mOverlays.get(index);
			  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			  dialog.setTitle(item.getTitle());
			  dialog.setMessage(item.getSnippet());
			  dialog.setPositiveButton(mContext.getString(R.string.yes_message), new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			  dialog.setNegativeButton(mContext.getString(R.string.no_message), null);
			  dialog.show();
		}

		  return true;
	}*/

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	

}
