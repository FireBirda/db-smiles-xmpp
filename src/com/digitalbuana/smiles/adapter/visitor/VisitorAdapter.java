package com.digitalbuana.smiles.adapter.visitor;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.friends.VisitorManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.ui.adapter.UpdatableAdapter;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class VisitorAdapter extends BaseAdapter
implements UpdatableAdapter
{

	private ArrayList<FriendsModel> listVisitor;
	private Context context;
	private LayoutInflater li;
	
	public VisitorAdapter(Context c, ArrayList<FriendsModel> object){
		this.context = c;
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listVisitor = object;//FriendsManager.getInstance().getVisitorManager().getAllVisitor();
	}
	
	@Override
	public int getCount() {
		return listVisitor.size();
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
	    	convertView = li.inflate(R.layout.item_visitor, null);
	        holder = new ViewHolder();
	        holder.rootView = (LinearLayout)convertView.findViewById(R.id.visitorAdapterRoot);
	        holder.avatar = (ImageView)convertView.findViewById(R.id.visitorAdapterAvatar);
	        holder.txtName = (TextView)convertView.findViewById(R.id.visitorAdapterTxtName);
	        holder.txtTimeYear = (TextView)convertView.findViewById(R.id.visitorAdapterTxtYear);
	        holder.txtTimeHour = (TextView)convertView.findViewById(R.id.visitorAdapterTxtHour);
	        
	        FontUtils.setRobotoFont(context, holder.rootView);
	        convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		AbstractContact abstractContact = RosterManager.getInstance().getBestContact(AccountManager.getInstance().getAccountKu(), listVisitor.get(pos).getName()+"@"+AppConstants.XMPPServerHost);
		holder.txtName.setText(StringUtils.replaceStringEquals(abstractContact.getName()));
		
		//Time
		holder.txtTimeYear.setText(StringUtils.parseTimeVisitorTahun(listVisitor.get(pos).getTime()));
		holder.txtTimeHour.setText(StringUtils.parseTimeVisitorJam(listVisitor.get(pos).getTime()));
		
		if(pos%2==0){
			holder.rootView.setBackgroundResource(R.color.AbuStandard);
		} else {
			holder.rootView.setBackgroundResource(R.color.AbuStandardLow);
		}
		VisitorManager.getInstance().removeVisitorNotifications(AccountManager.getInstance().getAccountKu(), listVisitor.get(pos).getName()+"@"+AppConstants.XMPPServerHost);
		holder.avatar.setImageDrawable(abstractContact.getAvatar());
		holder.friend = listVisitor.get(pos);
		
		return convertView;
	}

	public static class ViewHolder {
		LinearLayout rootView;
		ImageView avatar;
		TextView txtName;
		TextView txtTimeYear;
		TextView txtTimeHour;
		FriendsModel friend;
		
		public FriendsModel getFriend(){
			return friend;
		}
	}
	
	
	@Override
	public void onChange() {
		//listVisitor = FriendsManager.getInstance().getVisitorManager().getAllVisitor();
		notifyDataSetChanged();
	}

}
