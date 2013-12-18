package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;

import com.digitalbuana.smiles.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class EmoticonAdapter extends BaseAdapter
{
    private ArrayList<Integer> emoList ;
    private Activity activity;
 
    public EmoticonAdapter(Activity activity, ArrayList<Integer> emoticonList) {
        super();
       /* this.listCountry = emoticonList;*/
        this.emoList = emoticonList;
        this.activity = activity;
    }
 
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return emoList.size();
    }
 
    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        return String.valueOf(emoList.get(position));
    }
 
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
 
    public static class ViewHolder
    {
        public ImageView imgViewFlag;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        LayoutInflater inflator = activity.getLayoutInflater();
 
        if(convertView==null)
        {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.emoticon_row, null);
 
            view.imgViewFlag = (ImageView) convertView.findViewById(R.id.stickerThumbImageView);
 
            convertView.setTag(view);
        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }
 
        view.imgViewFlag.setImageResource(emoList.get(position));
 
        return convertView;
    }
}