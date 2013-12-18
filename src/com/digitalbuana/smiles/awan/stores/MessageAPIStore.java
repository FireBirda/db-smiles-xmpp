package com.digitalbuana.smiles.awan.stores;

import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.digitalbuana.smiles.awan.helper.DateHelper;
import com.digitalbuana.smiles.awan.model.MessageApiModel;
import com.digitalbuana.smiles.awan.model.MessageApiSentDateModel;

public class MessageAPIStore {

	private ArrayList<MessageApiModel> list = null;
	private String TAG = getClass().getSimpleName();

	public MessageAPIStore(JSONArray json) {
		if (json.length() > 0) {
			list = new ArrayList<MessageApiModel>();
			for (int a = 0; a < json.length(); a++) {
				try {
					JSONObject jObj = json.getJSONObject(a);
					MessageApiModel mam = new MessageApiModel();
					mam.setFromJID(jObj.getString("fromJID"));
					mam.setFromJIDSource(jObj.getString("fromJIDResource"));
					mam.setToJID(jObj.getString("toJID"));
					mam.setToJIDResource(jObj.getString("toJIDResource"));
					mam.setSentDate(this.getSentDate(jObj
							.getJSONObject("sentDate")));
					mam.setBody(jObj.getString("body"));

					mam.setTimestamp(Long.parseLong(jObj.getString("timestamp")));

					list.add(mam);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				} catch (ParseException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}

	private MessageApiSentDateModel getSentDate(JSONObject object)
			throws ParseException, JSONException {
		MessageApiSentDateModel masdm = new MessageApiSentDateModel();
		masdm.setTimeSent(DateHelper.getDateFromString(object.getString("date")));
		masdm.setTimezoneType(Integer.valueOf(object.getString("timezone_type")));
		masdm.setTimezone(object.getString("timezone"));
		return masdm;
	}

	public ArrayList<MessageApiModel> getList() {
		return this.list;
	}

}
