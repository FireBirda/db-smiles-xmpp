
package com.digitalbuana.smiles.data.notification;

import android.content.Intent;

public interface NotificationItem {

	Intent getIntent();

	String getTitle();

	String getText();

}
