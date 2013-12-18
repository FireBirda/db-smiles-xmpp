package com.digitalbuana.smiles.data.notification;
import java.util.Collection;

import android.net.Uri;

public interface NotificationProvider<T extends NotificationItem> {

	Collection<T> getNotifications();

	boolean canClearNotifications();

	void clearNotifications();

	Uri getSound();

	int getStreamType();

	int getIcon();

}
