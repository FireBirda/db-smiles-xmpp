package com.digitalbuana.smiles.awan.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

public class DateHelper {

	private static int _hour;
	private static int _minute;
	private static int _second;

	private static int _date;
	private static int _month;
	private static int _year;

	public static void getInstance() {
		Calendar c = Calendar.getInstance();
		_date = c.get(Calendar.DATE);
		_month = c.get(Calendar.MONTH);
		_year = c.get(Calendar.YEAR);

		_hour = c.get(Calendar.HOUR);
		_minute = c.get(Calendar.MINUTE);
		_second = c.get(Calendar.SECOND);
	}

	public static String getDateTime() {

		String result = _date + "-" + _month + "-" + _year + " " + _hour + ":"
				+ _minute + ":" + _second;
		return result;
	}

	public static String setRecentChatTime(Date date) {
		Date _date = new Date();
		if (_date.getDay() - date.getDay() == 0) {
			return getTimeOnDate(date);
		} else if (_date.getDay() - date.getDay() == 1)
			return "yesterday";
		else
			return (String) DateFormat.format("yyyy-MM-dd", date);
	}

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	public static String getTimeOnDate(Date date) {
		try {
			return (String) DateFormat.format("kk:mm", date);
		} catch (NullPointerException e) {
			return (String) DateFormat.format("kk:mm", new Date());
		}

	}

	public String getDateCurrentTimeZone(long timestamp) {
		try {
			Calendar calendar = Calendar.getInstance();
			TimeZone tz = TimeZone.getDefault();
			calendar.setTimeInMillis(timestamp * 1000);
			calendar.add(Calendar.MILLISECOND,
					tz.getOffset(calendar.getTimeInMillis()));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currenTimeZone = (Date) calendar.getTime();
			return sdf.format(currenTimeZone);
		} catch (Exception e) {
		}
		return "";
	}

	@SuppressLint("SimpleDateFormat")
	public static Date getDateFromString(String date)
			throws java.text.ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.parse(date);
	}

}
