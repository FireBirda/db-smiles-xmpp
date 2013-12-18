package com.digitalbuana.smiles.activity;

import java.lang.reflect.Field;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.SoundMenuAdapter;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class CustomeSoundActivity extends ManagedActivity implements
		OnClickListener, OnItemClickListener {

	private ListView _settingList;
	private FrameLayout _settingsBtnBack;
	private SoundMenuAdapter adapter;
	public int selectedIndex = 99;
	private String[] CONTENT;

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		rootView = (FrameLayout) findViewById(R.id.settingsRootView);
		TextView title = (TextView) findViewById(R.id.settingTitle);
		title.setText(getString(R.string.custome_sound));
		_settingList = (ListView) findViewById(R.id.settingList);
		adapter = new SoundMenuAdapter(this);
		_settingList.setAdapter(adapter);
		_settingsBtnBack = (FrameLayout) findViewById(R.id.settingsBtnBack);
		_settingsBtnBack.setOnClickListener(this);
		_settingList.setOnItemClickListener(this);
		FontUtils.setRobotoFont(this, rootView);
		mSettings = PreferenceManager.getDefaultSharedPreferences(context);
		settingsEditor = mSettings.edit();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == _settingsBtnBack) {
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos,
			long arg3) {
		// TODO Auto-generated method stub
		if (adapter == _settingList) {
			selectedIndex = pos;
			getRawFiles();
		}
	}

	public void getRawFiles() {
		Field[] fields = R.raw.class.getFields();
		String[] tmpCONTENT = new String[fields.length];
		int a = 0;
		tmpCONTENT[a] = "default";
		for (Field f : fields) {
			String fileName = f.getName();
			if (fileName.contains("mp3")) {
				a++;
				tmpCONTENT[a] = fileName.replace("mp3", "");
			}
		}
		CONTENT = new String[a + 1];
		for (int b = 0; b < tmpCONTENT.length; b++) {
			String s = tmpCONTENT[b];
			if (s != null && !s.equals("")) {
				CONTENT[b] = s;
			}
		}
		generateSoundList();
	}

	private void generateSoundList() {
		String title = "", defaultSaved = "default";
		switch (selectedIndex) {
		case 0:
			title = getString(R.string.setting_sound_attention);
			defaultSaved = mSettings.getString(
					AppConstants.SOUND_ATTENTION_TAG, "default");
			break;
		case 1:
			title = getString(R.string.setting_sound_chat_notif);
			defaultSaved = mSettings.getString(
					AppConstants.SOUND_CHAT_NOTIF_TAG, "default");
			break;
		case 2:
			title = getString(R.string.setting_sound_chat_send);
			defaultSaved = mSettings.getString(
					AppConstants.SOUND_CHAT_SEND_TAG, "default");
			break;
		}
		int selectedSound = 0;
		for (int a = 0; a < CONTENT.length; a++) {
			if (defaultSaved.replace("mp3", "").equals(CONTENT[a]))
				selectedSound = a;
		}
		Dialog dialog = new Dialog(context);
		dialog.setCancelable(true);
		dialog.setTitle(title + " " + getString(R.string.profile_sound_list));
		dialog.setContentView(R.layout.list_general);
		dialog.show();
		ListView list = (ListView) dialog.findViewById(R.id.gelist);
		list.setItemsCanFocus(false);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, CONTENT));
		list.setSelection(selectedSound);
		list.setItemChecked(selectedSound, true);
		FontUtils.setRobotoFont(this, list);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String fileName = "mp3" + CONTENT[arg2];
				if (arg2 != 0 && arg2 != -1) {
					int resID = context.getResources().getIdentifier(fileName,
							"raw", context.getPackageName());
					MediaPlayer mediaPlayer = MediaPlayer
							.create(context, resID);
					mediaPlayer.start();
					switch (selectedIndex) {
					case 0:
						settingsEditor.putString(
								AppConstants.SOUND_ATTENTION_TAG, fileName);
						break;
					case 1:
						settingsEditor.putString(
								AppConstants.SOUND_CHAT_NOTIF_TAG, fileName);
						break;
					case 2:
						settingsEditor.putString(
								AppConstants.SOUND_CHAT_SEND_TAG, fileName);
						break;
					}
				} else if (arg2 == 0) {
					switch (selectedIndex) {
					case 0:
						settingsEditor.putString(
								AppConstants.SOUND_ATTENTION_TAG, "default");
						break;
					case 1:
						settingsEditor.putString(
								AppConstants.SOUND_CHAT_NOTIF_TAG, "default");
						break;
					case 2:
						settingsEditor.putString(
								AppConstants.SOUND_CHAT_SEND_TAG, "default");
						break;
					}
				}
				settingsEditor.commit();
			}
		});
	}
}
