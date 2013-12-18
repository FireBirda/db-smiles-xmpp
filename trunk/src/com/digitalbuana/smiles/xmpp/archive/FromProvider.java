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
package com.digitalbuana.smiles.xmpp.archive;

import org.xmlpull.v1.XmlPullParser;

public class FromProvider extends AbstractMessageProvider<From> {

	@Override
	protected From createInstance(XmlPullParser parser) {
		return new From();
	}

	private FromProvider() {
	}

	private static final FromProvider instance = new FromProvider();

	public static FromProvider getInstance() {
		return instance;
	}

}