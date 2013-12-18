package com.digitalbuana.smiles.awan.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import com.digitalbuana.smiles.awan.model.Contact;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.utils.StringUtils;

public class PhoneContactHelper {

	public static Set<String> tmpSetPhone;
	public static Set<String> tmpSetMail;

	public static String[][] tmpContactList = null;

	public static String[][] getContactList(Context cntx) {
		if (tmpContactList != null)
			return tmpContactList;
		tmpContactList = getContact(cntx);
		return tmpContactList;
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static String[][] getContact(Context cntx) {

		String[][] result = null;
		List<Contact> contacts = new ArrayList<Contact>();

		ContentResolver cr = cntx.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {

			int x = 0;
			int f = 0;

			while (cur.moveToNext()) {

				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					String phone = "";
					while (pCur.moveToNext()) {
						if (phone.equals("")) {
							phone = pCur
									.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						} else {
							phone = phone
									+ ","
									+ pCur.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						}

					}

					f++;
					pCur.close();
					Cursor emailCur = cr.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					String email = "";
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array
						email = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						String emailType = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
					}
					emailCur.close();

					phone = phone.replace(" ", "");
					phone = phone.replace("-", "");
					phone = phone.replace("+", "");

					Contact con = new Contact();
					con.setName(StringUtils.UppercaseFirstLetters(name));
					con.setPhone(phone);
					con.setMail(email);
					contacts.add(con);
					x++;
				}
			}
			Collections.sort(contacts);
			String[][] tmpArr = new String[3][f];
			int j = 0;

			Set<String> setPhone = new HashSet<String>();
			Set<String> setMail = new HashSet<String>();

			for (Contact cn : contacts) {
				if (cn.getName() != null && !cn.getName().equals("")) {

					tmpArr[0][j] = cn.getName();
					tmpArr[1][j] = cn.getPhone();
					tmpArr[2][j] = cn.getMail();

					setPhone.add(cn.getPhone());
					setMail.add(cn.getMail());

					j++;
				}
			}

			SharedPreferences mSettings = PreferenceManager
					.getDefaultSharedPreferences(cntx);
			SharedPreferences.Editor settingsEditor = mSettings.edit();
			try {
				settingsEditor.putStringSet(
						AppConstants.LOCAL_PHONE_CONTACT_CACHE_TAG, setPhone);
				settingsEditor.putStringSet(
						AppConstants.LOCAL_MAIL_CONTACT_CACHE_TAG, setMail);
			} catch (NoSuchMethodError e) {
				tmpSetPhone = setPhone;
				tmpSetMail = setMail;
			}

			settingsEditor.commit();

			result = tmpArr;
		}
		return result;
	}

	public static void getSetLocalContact(final Context c) {
		new Thread("Thread get Contact...") {
			public void run() {
				try {
					PhoneContactHelper.getContactList(c);
				} finally {
					this.interrupt();
				}
			}
		}.start();
	}

	public static ArrayList<Contact> getContactArr() {
		ArrayList<Contact> list = new ArrayList<Contact>();
		for (int a = 0; a < tmpContactList.length; a++) {
			Contact c = new Contact();
			c.setName(tmpContactList[0][a]);
			c.setPhone(tmpContactList[1][a]);
			c.setMail(tmpContactList[2][a]);
			list.add(c);
		}
		return list;
	}

}
