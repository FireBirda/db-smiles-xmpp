package com.digitalbuana.smiles.data.extension.avatar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.OnLowMemoryListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.OAuthManager;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.OnPacketListener;
import com.digitalbuana.smiles.data.extension.vcard.VCardManager;
import com.digitalbuana.smiles.xmpp.address.Jid;
import com.digitalbuana.smiles.xmpp.avatar.VCardUpdate;

public class AvatarManager implements OnLoadListener, OnLowMemoryListener,
		OnPacketListener {

	private static final int MAX_SIZE = 256;
	private static final String EMPTY_HASH = "";
	private static final Bitmap EMPTY_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
	private final Application application;
	private final Map<String, String> hashes;
	private final Map<String, Bitmap> bitmaps;
	private final Map<String, Drawable> contactListDrawables;
	private final static AvatarManager instance;

	static {
		instance = new AvatarManager();
		Application.getInstance().addManager(instance);
	}

	public static AvatarManager getInstance() {
		return instance;
	}

	private AvatarManager() {
		this.application = Application.getInstance();
		hashes = new HashMap<String, String>();
		bitmaps = new HashMap<String, Bitmap>();
		contactListDrawables = new HashMap<String, Drawable>();
	}

	@Override
	public void onLoad() {
			final Map<String, String> hashes = new HashMap<String, String>();
			final Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
			Cursor cursor = AvatarTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						String hash = AvatarTable.getHash(cursor);
						hashes.put(AvatarTable.getUser(cursor), hash == null ? EMPTY_HASH : hash);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
			for (String hash : new HashSet<String>(hashes.values()))
				if (hash != EMPTY_HASH) {
					Bitmap bitmap = makeBitemap(AvatarStorage.getInstance().read(hash));
					bitmaps.put(hash, bitmap == null ? EMPTY_BITMAP : bitmap);
				}
			Application.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLoaded(hashes, bitmaps);
				};
			});
	}

	private void onLoaded(Map<String, String> hashes, Map<String, Bitmap> bitmaps) {
		this.hashes.putAll(hashes);
		this.bitmaps.putAll(bitmaps);
	}

	private void setHash(final String bareAddress, final String hash) {
		hashes.put(bareAddress, hash == null ? EMPTY_HASH : hash);
		contactListDrawables.remove(bareAddress);
		application.runInBackground(new Runnable() {
			@Override
			public void run() {
				AvatarTable.getInstance().write(bareAddress, hash);
			}
		});
	}
	
	
	private static Bitmap makeBitemap(byte[] value) {
		if (value == null)
			return null;

		// Load only size values
		BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
		sizeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(value, 0, value.length, sizeOptions);

		// Calculate factor to down scale image
		int scale = 1;
		int width_tmp = sizeOptions.outWidth;
		int height_tmp = sizeOptions.outHeight;
		while (width_tmp / 2 >= MAX_SIZE && height_tmp / 2 >= MAX_SIZE) {
			scale *= 2;
			width_tmp /= 2;
			height_tmp /= 2;
		}

		// Load image
		BitmapFactory.Options resultOptions = new BitmapFactory.Options();
		resultOptions.inSampleSize = scale;
		return BitmapFactory.decodeByteArray(value, 0, value.length, resultOptions);
	}

	private Bitmap getBitmap(String bareAddress) {
		String hash = hashes.get(bareAddress);
		//Log.i("dheinaku", "Baer Address : "+bareAddress+" hash : "+hash);
		if (hash == null || hash == EMPTY_HASH)
			return null;
		Bitmap bitmap = bitmaps.get(hash);
		if (bitmap == EMPTY_BITMAP)
			return null;
		else
			return bitmap;
	}

	private void setValue(final String hash, final byte[] value) {
		if (hash == null)
			return;
		Bitmap bitmap = makeBitemap(value);
		bitmaps.put(hash, bitmap == null ? EMPTY_BITMAP : bitmap);
		application.runInBackground(new Runnable() {
			@Override
			public void run() {
				AvatarStorage.getInstance().write(hash, value);
			}
		});
	}

	@Override
	public void onLowMemory() {
		contactListDrawables.clear();
	}

	public Drawable getAccountAvatar(String account) {
		String jid = OAuthManager.getInstance().getAssignedJid(account);
		if (jid == null)
			jid = account;
		Bitmap value = getBitmap(Jid.getBareAddress(jid));
		if (value != null)
			return new BitmapDrawable(value);
		else
			return application.getResources().getDrawable(R.drawable.img_default_avatar);
	}
	public Drawable getAccountAvatarNoRerun(String account) {
		String jid = OAuthManager.getInstance().getAssignedJid(account);
		if (jid == null)
			jid = account;
		Bitmap value = getBitmap(Jid.getBareAddress(jid));
		if (value != null)
			return new BitmapDrawable(value);
		else
			return null;
	}

	@SuppressWarnings("deprecation")
	public Drawable getUserAvatar(String user) {
		Bitmap value = getBitmap(user);
		if (value != null){
			return new BitmapDrawable(value);
		} else{
			return application.getResources().getDrawable(R.drawable.img_default_avatar);
		}	
	}

	public Bitmap getUserBitmap(String user) {
		Bitmap value = getBitmap(user);
		if (value != null)
			return value;
		else
			return ((BitmapDrawable) application.getResources().getDrawable(R.drawable.img_default_avatar)).getBitmap();
	}

	public Drawable getUserAvatarForContactList(String user) {
		Drawable drawable = contactListDrawables.get(user);
		if (drawable == null) {
			drawable = getUserAvatar(user);
			contactListDrawables.put(user, drawable);
		}
		return drawable;
	}

	public Drawable getRoomAvatar(String user) {
		return application.getResources().getDrawable(R.drawable.img_default_avatar_muc);
	}

	public Bitmap getRoomBitmap(String user) {
		return ((BitmapDrawable) getRoomAvatar(user)).getBitmap();
	}

	public Drawable getRoomAvatarForContactList(String user) {
		Drawable drawable = contactListDrawables.get(user);
		if (drawable == null) {
			drawable = getRoomAvatar(user);
			contactListDrawables.put(user, drawable);
		}
		return drawable;
	}

	public Drawable getOccupantAvatar(String user) {
		return application.getResources().getDrawable(R.drawable.img_default_avatar);
	}

	public void onAvatarReceived(String bareAddress, String hash, byte[] value) {
		setValue(hash, value);
		//Log.i(AppConstants.TAG, "bareAddress : "+bareAddress+hash);
		setHash(bareAddress, hash);
	}

	@Override
	public void onPacket(ConnectionItem connection, String bareAddress, Packet packet) {
		if (!(packet instanceof Presence) || bareAddress == null)
			return;
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		Presence presence = (Presence) packet;
		if (presence.getType() == Presence.Type.error)
			return;
		for (PacketExtension packetExtension : presence.getExtensions())
			if (packetExtension instanceof VCardUpdate) {
				VCardUpdate vCardUpdate = (VCardUpdate) packetExtension;
				if (vCardUpdate.isValid() && vCardUpdate.isPhotoReady()){
					onPhotoReady(account, bareAddress, vCardUpdate);
				}

			}
	}

	private void onPhotoReady(final String account, final String bareAddress, VCardUpdate vCardUpdate) {
		if (vCardUpdate.isEmpty()) {
			setHash(bareAddress, null);
			return;
		}
		final String hash = vCardUpdate.getPhotoHash();
		if (bitmaps.containsKey(hash)) {
			setHash(bareAddress, hash);
			return;
		}
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				loadBitmap(account, bareAddress, hash);
			}
		});
	}

	private void loadBitmap(final String account, final String bareAddress, final String hash) {
//		Log.i("dheinaku", "loadBitmap  : "+hash);
		final byte[] value = AvatarStorage.getInstance().read(hash);
		final Bitmap bitmap = makeBitemap(value);
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onBitmapLoaded(account, bareAddress, hash, value, bitmap);
			}
		});
	}

	private void onBitmapLoaded(String account, String bareAddress, String hash, byte[] value, Bitmap bitmap) {
		if (value == null) {
			VCardManager.getInstance().request(account, bareAddress, hash);
		} else {
			bitmaps.put(hash, bitmap == null ? EMPTY_BITMAP : bitmap);
			setHash(bareAddress, hash);
		}
	}

	public Bitmap createShortcutBitmap(Bitmap bitmap) {
		int size = getLauncherLargeIconSize();
		int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
		if (max == size)
			return bitmap;
		double scale = ((double) size) / max;
		int width = (int) (bitmap.getWidth() * scale);
		int height = (int) (bitmap.getHeight() * scale);
		return Bitmap.createScaledBitmap(bitmap, width, height, true);
	}

	private int getLauncherLargeIconSize() {
		if (Application.SDK_INT < 9)
			return BaseShortcutHelper.getLauncherLargeIconSize();
		else if (Application.SDK_INT < 11)
			return GingerbreadShortcutHelper.getLauncherLargeIconSize();
		else
			return HoneycombShortcutHelper.getLauncherLargeIconSize();
	}

}
