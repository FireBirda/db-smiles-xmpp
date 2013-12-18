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
package com.digitalbuana.smiles.xmpp.address;

import java.util.Locale;

import org.jivesoftware.smack.util.StringUtils;

public class Jid {

	private Jid() {
	}

	public static String getResource(String user) {
		return user == null ? null : StringUtils.parseResource(user.toLowerCase(Locale.US));
	}

	public static String getServer(String user) {
		return user == null ? null : StringUtils.parseServer(user.toLowerCase(Locale.US));
	}

	public static String getName(String user) {
		return user == null ? null : StringUtils.parseName(user.toLowerCase(Locale.US));
	}

	public static String getBareAddress(String user) {
		return user == null ? null : StringUtils.parseBareAddress(user.toLowerCase(Locale.US));
	}

	public static String getStringPrep(String user) {
		return user == null ? null : user.toLowerCase(Locale.US);
	}

}
