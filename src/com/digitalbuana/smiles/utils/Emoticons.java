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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;
import android.util.Log;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.SettingsManager;

/**
 * Emoticons.
 * 
 * @author alexander.ivanov
 * 
 */
public class Emoticons {

	public static final Map<Pattern, Integer> ANDROID_EMOTICONS = new HashMap<Pattern, Integer>();
	public static final Map<Pattern, Integer> NONE_EMOTICONS = new HashMap<Pattern, Integer>();

	private static final Factory spannableFactory = Spannable.Factory.getInstance();
	
	private static final String TAG = "Emoticons";
	
	private static ArrayList<Integer> emoList = null;
	public static ArrayList<Integer> getEmoList(){
		
		if(emoList != null)
			return emoList;
		
		emoList = new ArrayList<Integer>();
		
		emoList.add(R.drawable.img_emo_angel);
		emoList.add(R.drawable.img_emo_applause);
		emoList.add(R.drawable.img_emo_battingeyelashes);
		emoList.add(R.drawable.img_emo_biggrin);
		emoList.add(R.drawable.img_emo_blushing);
		emoList.add(R.drawable.img_emo_chicken);
		emoList.add(R.drawable.img_emo_clown);
		emoList.add(R.drawable.img_emo_coffee);
		emoList.add(R.drawable.img_emo_confused);
		emoList.add(R.drawable.img_emo_cool);		
		emoList.add(R.drawable.img_emo_cow);
		emoList.add(R.drawable.img_emo_crying);
		emoList.add(R.drawable.img_emo_devil);
		emoList.add(R.drawable.img_emo_yelling);
		emoList.add(R.drawable.img_emo_doze);
		emoList.add(R.drawable.img_emo_frustated);
		emoList.add(R.drawable.img_emo_hah);
		emoList.add(R.drawable.img_emo_happy);
		emoList.add(R.drawable.img_emo_hypnotize);
		emoList.add(R.drawable.img_emo_kiss);		
		emoList.add(R.drawable.img_emo_laughing);
		emoList.add(R.drawable.img_emo_love);
		emoList.add(R.drawable.img_emo_lovestruck);
		emoList.add(R.drawable.img_emo_monkey);
		emoList.add(R.drawable.img_emo_notalking);
		emoList.add(R.drawable.img_emo_peacesign);
		emoList.add(R.drawable.img_emo_pig);
		emoList.add(R.drawable.img_emo_praying);
		emoList.add(R.drawable.img_emo_rabbit);
		emoList.add(R.drawable.img_emo_sad);		
		emoList.add(R.drawable.img_emo_shameonyou);
		emoList.add(R.drawable.img_emo_sick);
		emoList.add(R.drawable.img_emo_skull);
		emoList.add(R.drawable.img_emo_sleepy);
		emoList.add(R.drawable.img_emo_straightface);
		emoList.add(R.drawable.img_emo_tongue);
		emoList.add(R.drawable.img_emo_winking);
		emoList.add(R.drawable.img_emo_wtf);
		emoList.add(R.drawable.img_emo_dollar);
		emoList.add(R.drawable.img_emo_whistling);		
		emoList.add(R.drawable.img_emo_worried);
		
		return emoList;
	}
	static {
		
		addPattern(ANDROID_EMOTICONS, "O:)", R.drawable.img_emo_angel);
		addPattern(ANDROID_EMOTICONS, "O:-)", R.drawable.img_emo_angel);
		addPattern(ANDROID_EMOTICONS, "(angel)", R.drawable.img_emo_angel);
		
		addPattern(ANDROID_EMOTICONS, ":clap", R.drawable.img_emo_applause);
		addPattern(ANDROID_EMOTICONS, ":applause", R.drawable.img_emo_applause);
		
		addPattern(ANDROID_EMOTICONS, ":battingeyelashes", R.drawable.img_emo_battingeyelashes);

		addPattern(ANDROID_EMOTICONS, "^_^", R.drawable.img_emo_biggrin);
		addPattern(ANDROID_EMOTICONS, ":biggrin", R.drawable.img_emo_biggrin);
		
		addPattern(ANDROID_EMOTICONS, "^3^", R.drawable.img_emo_blushing);
		addPattern(ANDROID_EMOTICONS, ":blushing", R.drawable.img_emo_blushing);
		
		addPattern(ANDROID_EMOTICONS, ":chicken", R.drawable.img_emo_chicken);
		addPattern(ANDROID_EMOTICONS, ":ayam", R.drawable.img_emo_chicken);
		
		addPattern(ANDROID_EMOTICONS, ":clown", R.drawable.img_emo_clown);
		addPattern(ANDROID_EMOTICONS, ":badut", R.drawable.img_emo_clown);
		
		addPattern(ANDROID_EMOTICONS, ":coffee", R.drawable.img_emo_coffee);
		addPattern(ANDROID_EMOTICONS, ":kopi", R.drawable.img_emo_coffee);
		
		addPattern(ANDROID_EMOTICONS, ":confused", R.drawable.img_emo_confused);
		addPattern(ANDROID_EMOTICONS, ":[", R.drawable.img_emo_confused);
		addPattern(ANDROID_EMOTICONS, ":\\", R.drawable.img_emo_confused);
		addPattern(ANDROID_EMOTICONS, ":-\\", R.drawable.img_emo_confused);
		addPattern(ANDROID_EMOTICONS, ";\\", R.drawable.img_emo_confused);
		
		addPattern(ANDROID_EMOTICONS, "B)", R.drawable.img_emo_cool);
		addPattern(ANDROID_EMOTICONS, "B-)", R.drawable.img_emo_cool);
		addPattern(ANDROID_EMOTICONS, "8)", R.drawable.img_emo_cool);
		addPattern(ANDROID_EMOTICONS, "8-)", R.drawable.img_emo_cool);
		
		addPattern(ANDROID_EMOTICONS, ":cow", R.drawable.img_emo_cow);
		addPattern(ANDROID_EMOTICONS, ":sapi", R.drawable.img_emo_cow);
		
		addPattern(ANDROID_EMOTICONS, ":crying", R.drawable.img_emo_crying);
		addPattern(ANDROID_EMOTICONS, "T_T", R.drawable.img_emo_crying);
		addPattern(ANDROID_EMOTICONS, ":'(", R.drawable.img_emo_crying);
		
		addPattern(ANDROID_EMOTICONS, ":devil", R.drawable.img_emo_devil);
		addPattern(ANDROID_EMOTICONS, "]:)", R.drawable.img_emo_devil);
		
		addPattern(ANDROID_EMOTICONS, ">:O", R.drawable.img_emo_yelling);
		addPattern(ANDROID_EMOTICONS, ">:0", R.drawable.img_emo_yelling);
		addPattern(ANDROID_EMOTICONS, ">:o", R.drawable.img_emo_yelling);
		
		addPattern(ANDROID_EMOTICONS, ":doze", R.drawable.img_emo_doze);

		addPattern(ANDROID_EMOTICONS, ":{", R.drawable.img_emo_frustated);
		addPattern(ANDROID_EMOTICONS, ">_<", R.drawable.img_emo_frustated);
		addPattern(ANDROID_EMOTICONS, ":frustated", R.drawable.img_emo_frustated);
		
		addPattern(ANDROID_EMOTICONS, "=-O", R.drawable.img_emo_hah);
		addPattern(ANDROID_EMOTICONS, ":hah", R.drawable.img_emo_hah);
		addPattern(ANDROID_EMOTICONS, "O_o", R.drawable.img_emo_hah);
		addPattern(ANDROID_EMOTICONS, "o_O", R.drawable.img_emo_hah);
		addPattern(ANDROID_EMOTICONS, ":0", R.drawable.img_emo_hah);
		
		addPattern(ANDROID_EMOTICONS, ":)", R.drawable.img_emo_happy);
		addPattern(ANDROID_EMOTICONS, "^^", R.drawable.img_emo_happy);
		addPattern(ANDROID_EMOTICONS, "^_^", R.drawable.img_emo_happy);
		addPattern(ANDROID_EMOTICONS, ":-)", R.drawable.img_emo_happy);
		addPattern(ANDROID_EMOTICONS, ":happy", R.drawable.img_emo_happy);
		addPattern(ANDROID_EMOTICONS, ":^0 ", R.drawable.img_emo_happy);
		

		addPattern(ANDROID_EMOTICONS, ":hypnotize", R.drawable.img_emo_hypnotize);
		addPattern(ANDROID_EMOTICONS, "x_x", R.drawable.img_emo_hypnotize);
		addPattern(ANDROID_EMOTICONS, "X_X", R.drawable.img_emo_hypnotize);
		addPattern(ANDROID_EMOTICONS, "XO", R.drawable.img_emo_hypnotize);
		addPattern(ANDROID_EMOTICONS, "X-O", R.drawable.img_emo_hypnotize);

		
		addPattern(ANDROID_EMOTICONS, ":kiss", R.drawable.img_emo_kiss);
		addPattern(ANDROID_EMOTICONS, ":cium", R.drawable.img_emo_kiss);
		addPattern(ANDROID_EMOTICONS, ":*", R.drawable.img_emo_kiss);
		addPattern(ANDROID_EMOTICONS, ":-*", R.drawable.img_emo_kiss);
		
		addPattern(ANDROID_EMOTICONS, ":D", R.drawable.img_emo_laughing);
		addPattern(ANDROID_EMOTICONS, ":-D", R.drawable.img_emo_laughing);
		
		addPattern(ANDROID_EMOTICONS, ":love", R.drawable.img_emo_love);
		addPattern(ANDROID_EMOTICONS, ":heart", R.drawable.img_emo_love);
		addPattern(ANDROID_EMOTICONS, "(heart)", R.drawable.img_emo_love);
		addPattern(ANDROID_EMOTICONS, "<3", R.drawable.img_emo_love);
		
		addPattern(ANDROID_EMOTICONS, ":lovestruck", R.drawable.img_emo_lovestruck);
		addPattern(ANDROID_EMOTICONS, "<3_<3", R.drawable.img_emo_lovestruck);
		
		addPattern(ANDROID_EMOTICONS, ":monyet", R.drawable.img_emo_monkey);
		addPattern(ANDROID_EMOTICONS, ":monkey", R.drawable.img_emo_monkey);
		
		addPattern(ANDROID_EMOTICONS, ":x", R.drawable.img_emo_notalking);
		addPattern(ANDROID_EMOTICONS, ":X", R.drawable.img_emo_notalking);
		
		addPattern(ANDROID_EMOTICONS, ":peace", R.drawable.img_emo_peacesign);
		addPattern(ANDROID_EMOTICONS, ":damai", R.drawable.img_emo_peacesign);
		
		addPattern(ANDROID_EMOTICONS, ":pig", R.drawable.img_emo_pig);
		addPattern(ANDROID_EMOTICONS, ":babi", R.drawable.img_emo_pig);
		
		addPattern(ANDROID_EMOTICONS, ":pray", R.drawable.img_emo_praying);
		addPattern(ANDROID_EMOTICONS, ":doa", R.drawable.img_emo_praying);
		
		addPattern(ANDROID_EMOTICONS, ":rabbit", R.drawable.img_emo_rabbit);
		addPattern(ANDROID_EMOTICONS, ":kelinci", R.drawable.img_emo_rabbit);
		
		addPattern(ANDROID_EMOTICONS, ":(", R.drawable.img_emo_sad);
		addPattern(ANDROID_EMOTICONS, ":-(", R.drawable.img_emo_sad);
		addPattern(ANDROID_EMOTICONS, ":-[", R.drawable.img_emo_sad);
		
		addPattern(ANDROID_EMOTICONS, ":ok", R.drawable.img_emo_shameonyou);
		addPattern(ANDROID_EMOTICONS, ":OK", R.drawable.img_emo_shameonyou);
		addPattern(ANDROID_EMOTICONS, ":setuju", R.drawable.img_emo_shameonyou);
		addPattern(ANDROID_EMOTICONS, ":same", R.drawable.img_emo_shameonyou);
		
		addPattern(ANDROID_EMOTICONS, ":sick", R.drawable.img_emo_sick);
		addPattern(ANDROID_EMOTICONS, ":[]", R.drawable.img_emo_sick);
		addPattern(ANDROID_EMOTICONS, ":S", R.drawable.img_emo_sick);
		addPattern(ANDROID_EMOTICONS, ":-S", R.drawable.img_emo_sick);
		
		
		addPattern(ANDROID_EMOTICONS, ":skull", R.drawable.img_emo_skull);
		addPattern(ANDROID_EMOTICONS, ":stengkorak", R.drawable.img_emo_skull);
		
		addPattern(ANDROID_EMOTICONS, ":sleepy", R.drawable.img_emo_sleepy);
		
		addPattern(ANDROID_EMOTICONS, ":|", R.drawable.img_emo_straightface);
		addPattern(ANDROID_EMOTICONS, "-_-", R.drawable.img_emo_straightface);
		addPattern(ANDROID_EMOTICONS, ":-!", R.drawable.img_emo_straightface);
		
		addPattern(ANDROID_EMOTICONS, ";p", R.drawable.img_emo_tongue);
		addPattern(ANDROID_EMOTICONS, ";-p", R.drawable.img_emo_tongue);
		addPattern(ANDROID_EMOTICONS, ":p", R.drawable.img_emo_tongue);
		addPattern(ANDROID_EMOTICONS, ":-p", R.drawable.img_emo_tongue);
		addPattern(ANDROID_EMOTICONS, ":P", R.drawable.img_emo_tongue);
		addPattern(ANDROID_EMOTICONS, ":-P", R.drawable.img_emo_tongue);
		
		addPattern(ANDROID_EMOTICONS, ";)", R.drawable.img_emo_winking);
		addPattern(ANDROID_EMOTICONS, ";-)", R.drawable.img_emo_winking);
		
		addPattern(ANDROID_EMOTICONS, ":O", R.drawable.img_emo_wtf);
		addPattern(ANDROID_EMOTICONS, ":-O", R.drawable.img_emo_wtf);

		
		addPattern(ANDROID_EMOTICONS, ":$", R.drawable.img_emo_dollar);
		addPattern(ANDROID_EMOTICONS, ":-$", R.drawable.img_emo_dollar);
		
		addPattern(ANDROID_EMOTICONS, ":whistling", R.drawable.img_emo_whistling);
		addPattern(ANDROID_EMOTICONS, ":siul", R.drawable.img_emo_whistling);
	
		addPattern(ANDROID_EMOTICONS, "'^_^", R.drawable.img_emo_worried);
		addPattern(ANDROID_EMOTICONS, ":worried", R.drawable.img_emo_worried);
	}
	
	public static String getEmoPatern(int res){
		String paternResult = null;
		for(Entry<Pattern, Integer> ae : ANDROID_EMOTICONS.entrySet()){
			if(ae.getValue()==res)
				return ae.getKey().toString();
				
			
		}
		return paternResult;
	}

	private static void addPattern(Map<Pattern, Integer> map, String smile, int resource) {
		map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	private Emoticons() {
	}

	public static Spannable newSpannable(CharSequence text) {
		return spannableFactory.newSpannable(text);
	}

	public static boolean getSmiledText(Context context, Spannable spannable) {
		boolean hasChanges = false;
		Map<Pattern, Integer> emoticons = SettingsManager.interfaceSmiles();
		for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(spannable);
			while (matcher.find()) {
				boolean set = true;
				for (ImageSpan span : spannable.getSpans(matcher.start(),
						matcher.end(), ImageSpan.class))
					if (spannable.getSpanStart(span) >= matcher.start()
							&& spannable.getSpanEnd(span) <= matcher.end())
						spannable.removeSpan(span);
					else {
						set = false;
						break;
					}
				if (set) {
					spannable.setSpan(new ImageSpan(context, entry.getValue()),
							matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					hasChanges = true;
				}
			}
		}
		return hasChanges;
	}

	public static Spannable getSmiledText(Context context, CharSequence text) {
		Spannable spannable = spannableFactory.newSpannable(text);
		getSmiledText(context, spannable);
		return spannable;
	}
}
