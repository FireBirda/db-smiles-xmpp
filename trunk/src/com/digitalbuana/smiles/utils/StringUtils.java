/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import com.digitalbuana.smiles.data.AppConstants;

/**
 * Helper class to get plural forms.
 * 
 * @author alexander.ivanov
 * 
 */
public class StringUtils {

	private static final DateFormat DATE_TIME;
	private static final DateFormat TIME;

	public static final String attention = "Send an Attention";
	public static final String sticker = "Send a Sticker";
	public static final String ikonia = "Send an Ikonia";
	public static final String image = "Send an Image";
	public static final String audio = "Send an Audio";
	public static final String video = "Send a Video";
	public static final String location = "Share a Location";
	public static final String contact = "Send a Contact";

	static {
		DATE_TIME = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT);
		TIME = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	}

	private StringUtils() {
	}

	public static String getQuantityString(Resources resources,
			int stringArrayResourceId, long quantity) {
		String[] strings = resources.getStringArray(stringArrayResourceId);
		String lang = resources.getConfiguration().locale.getLanguage();
		if ("ru".equals(lang) && strings.length == 3) {
			quantity = quantity % 100;
			if (quantity >= 20)
				quantity = quantity % 10;
			if (quantity == 1)
				return strings[0];
			if (quantity >= 2 && quantity < 5)
				return strings[1];
			return strings[2];
		} else if (("cs".equals(lang) || "pl".equals(lang))
				&& strings.length == 3) {
			if (quantity == 1) {
				return strings[0];
			} else if (quantity >= 2 && quantity <= 4) {
				return strings[1];
			} else {
				return strings[2];
			}
		} else {
			if (quantity == 1) {
				return strings[0];
			} else {
				return strings[1];
			}
		}
	}

	public static String escapeHtml(String input) {
		StringBuilder builder = new StringBuilder();
		int pos = 0;
		int len = input.length();
		while (pos < len) {
			int codePoint = Character.codePointAt(input, pos);
			if (codePoint == '"')
				builder.append("&quot;");
			else if (codePoint == '&')
				builder.append("&amp;");
			else if (codePoint == '<')
				builder.append("&lt;");
			else if (codePoint == '>')
				builder.append("&gt;");
			else if (codePoint == '\n')
				builder.append("<br />");
			else if (codePoint >= 0 && codePoint < 160)
				builder.append(Character.toChars(codePoint));
			else
				builder.append("&#").append(codePoint).append(';');
			pos += Character.charCount(codePoint);
		}
		return builder.toString();
	}

	/**
	 * @param timeStamp
	 * @return String with date and time to be display.
	 */
	public static String getDateTimeText(Date timeStamp) {
		synchronized (DATE_TIME) {
			return DATE_TIME.format(timeStamp);
		}
	}

	/**
	 * @param timeStamp
	 * @return String with time or with date and time depend on current time.
	 */
	public static String getSmartTimeText(Date timeStamp) {
		if (timeStamp == null)
			return "";
		Date date = new Date();
		long delta = date.getTime() - timeStamp.getTime();
		if (delta < 20 * 60 * 60 * 1000)
			synchronized (TIME) {
				return TIME.format(timeStamp);
			}
		else
			return getDateTimeText(timeStamp);
	}

	public static String replaceStringEquals(String text) {
		String texttemp = text;
		if (texttemp != null) {
			texttemp = texttemp.replace("%40", "@");
			texttemp = texttemp
					.replace("@" + AppConstants.XMPPGroupsServer, "");
			texttemp = texttemp.replace("@" + AppConstants.XMPPRoomsServer, "");
			texttemp = texttemp.replace("@" + AppConstants.XMPPServerHost, "");

			if (texttemp.contains(AppConstants.UniqueKeyBroadcast)) {
				texttemp = texttemp
						.replace(AppConstants.UniqueKeyBroadcast, "");
			}
			if (texttemp.contains(AppConstants.UniqueKeySticker)) {
				texttemp = sticker;
			}
			if (texttemp.contains(AppConstants.UniqueKeyIkonia)) {
				texttemp = ikonia;
			}
			if (texttemp.contains(AppConstants.UniqueKeyLocation)) {
				texttemp = location;
			}
			if (texttemp.contains(AppConstants.UniqueKeyFileImage)) {
				texttemp = image;
			}
			if (texttemp.contains(AppConstants.UniqueKeyFileVideo)) {
				texttemp = video;
			}
			if (texttemp.contains(AppConstants.UniqueKeyFileAudio)) {
				texttemp = audio;
			}
			if (texttemp.contains(AppConstants.UniqueKeyFileContact)) {
				texttemp = contact;
			}
			if (texttemp.contains(AppConstants.UniqueKeyAttention)) {
				texttemp = attention;
			}
			if (texttemp.contains(AppConstants.UniqueKeyGroup)) {
				int pos = texttemp.indexOf(AppConstants.UniqueKeyGroup);
				texttemp = texttemp.substring(0, pos);
			}
			return texttemp;
		} else {
			return null;
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static String parseTimeVisitor(String time) {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss").parse(time);
			String newstring = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss")
					.format(date);
			return newstring;
		} catch (ParseException e) {
			return "No Time";
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static String parseTimeVisitorTahun(String time) {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss").parse(time);
			String newstring = new SimpleDateFormat("dd MMMM yyyy")
					.format(date);
			return newstring;
		} catch (ParseException e) {
			return "No Time";
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static String parseTimeVisitorJam(String time) {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss").parse(time);
			String newstring = new SimpleDateFormat("HH:mm:ss").format(date);
			return newstring;
		} catch (ParseException e) {
			return "00:00:00";
		}
	}

	public static String UppercaseFirstLetters(String str) {
		boolean prevWasWhiteSp = true;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (Character.isLetter(chars[i])) {
				if (prevWasWhiteSp) {
					chars[i] = Character.toUpperCase(chars[i]);
				}
				prevWasWhiteSp = false;
			} else {
				prevWasWhiteSp = Character.isWhitespace(chars[i]);
			}
		}
		return new String(chars);
	}
}
