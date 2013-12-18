package com.digitalbuana.smiles.ui.helper;

import android.widget.ImageView;

import com.digitalbuana.smiles.data.roster.AbstractContact;

public abstract class AbstractAvatarInflaterHelper {

	public abstract void updateAvatar(ImageView avatar, AbstractContact abstractContact);

	public static AbstractAvatarInflaterHelper createAbstractContactInflaterHelper() {
			return new DummyAvatarInflaterHelper();
	}

}
