package com.digitalbuana.smiles.awan.adapters;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.holders.InviteHolder;
import com.digitalbuana.smiles.awan.model.RecomendedModel;

public class InviteAdapter extends BaseAdapter {
	private Vector<String> buffNum = new Vector<String>();
	private String[][] list;
	private LayoutInflater mInflater;
	private String _type;
	private Context cntx;
	private ArrayList<RecomendedModel> _regList;
	private String TAG = getClass().getSimpleName();

	public InviteAdapter(Context c, String[][] strArrList,
			ArrayList<RecomendedModel> finalContact, String type) {
		list = strArrList;
		mInflater = LayoutInflater.from(c);
		_type = type;
		cntx = c;
		_regList = finalContact;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final InviteHolder holder = new InviteHolder();
		convertView = mInflater.inflate(R.layout.contact_list_row, parent,
				false);

		holder.txtName = (TextView) convertView.findViewById(R.id.roomName);
		holder.txtNumber = (TextView) convertView.findViewById(R.id.roomDesc);
		holder.checkedContact = (CheckBox) convertView
				.findViewById(R.id.checkBox1);

		holder.txtName.setText(list[0][position]);

		if (_type.equals(cntx.getString(R.string.addFriendsItem1)))
			holder.txtNumber.setText(list[1][position]);
		else if (_type.equals(cntx.getString(R.string.addFriendsItem2)))
			holder.txtNumber.setText(list[2][position]);

		boolean isRegistered = false;

		for (RecomendedModel rm : _regList) {
			if (_type.equals(cntx.getString(R.string.addFriendsItem1))) {
				String msisdnList = list[1][position];
				if (msisdnList.contains(",")) {
					String[] splitNum = msisdnList.split(",");
					if (splitNum.length > 0) {
						for (int a = 0; a < splitNum.length; a++) {
							if (rm.getMsisdn().equals(splitNum[a]))
								isRegistered = true;
						}
					}
				} else {
					if (rm.getMsisdn().equals(msisdnList))
						isRegistered = true;
				}

			} else if (_type.equals(cntx.getString(R.string.addFriendsItem2))) {
				String mailList = list[2][position];
				if (mailList.contains(",")) {
					String[] splitNum = mailList.split(",");
					if (splitNum.length > 0) {
						for (int a = 0; a < splitNum.length; a++) {
							if (rm.getMail().equals(splitNum[a]))
								isRegistered = true;
						}
					}
				} else {
					if (rm.getMail().equals(mailList))
						isRegistered = true;
				}
			}
		}

		if (isRegistered)
			holder.checkedContact.setVisibility(View.GONE);
		else {
			boolean alreadyChecked = false;
			for (int d = 0; d < buffNum.size(); d++) {
				if (_type.equals(cntx.getString(R.string.addFriendsItem1))) {
					if (buffNum.get(d).equals(list[1][position]))
						alreadyChecked = true;
				} else if (_type.equals(cntx
						.getString(R.string.addFriendsItem2))) {
					if (buffNum.get(d).equals(list[2][position]))
						alreadyChecked = true;
				}
			}
			holder.checkedContact.setChecked(alreadyChecked);
		}
		holder.checkedContact
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (holder.checkedContact.isChecked()) {
							if (_type.equals(cntx
									.getString(R.string.addFriendsItem1))) {
								buffNum.add(list[1][position]);
							} else if (_type.equals(cntx
									.getString(R.string.addFriendsItem2))) {
								buffNum.add(list[2][position]);
							}
						} else {
							if (buffNum.size() > 0) {
								if (_type.equals(cntx
										.getString(R.string.addFriendsItem1))) {
									buffNum.remove(list[1][position]);
								} else if (_type.equals(cntx
										.getString(R.string.addFriendsItem2))) {
									buffNum.remove(list[2][position]);
								}
							}
						}
					}
				});

		return convertView;
	}

	public Vector<String> getBuff() {
		return buffNum;
	}

}
