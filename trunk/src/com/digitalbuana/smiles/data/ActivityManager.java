package com.digitalbuana.smiles.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.BroadcastMesssageActivity;
import com.digitalbuana.smiles.activity.HomeActivity;
import com.digitalbuana.smiles.activity.SplashActivity;
import com.digitalbuana.smiles.activity.UploadFileActivity;
import com.digitalbuana.smiles.data.SettingsManager.InterfaceTheme;
import com.digitalbuana.smiles.ui.PreferenceEditor;

public class ActivityManager implements OnUnloadListener {

	private static final String EXTRA_TASK_INDEX = "com.digitalbuana.smiles.data.ActivityManager.EXTRA_TASK_INDEX";

	private final Application application;
	private final ArrayList<Activity> activities;
	private int nextTaskIndex;
	private final WeakHashMap<Activity, Integer> taskIndexes;
	private OnErrorListener onErrorListener;
	private final static ActivityManager instance;
	private String TAG = getClass().getSimpleName();
	static {
		instance = new ActivityManager();
		Application.getInstance().addManager(instance);
	}

	public static ActivityManager getInstance() {
		return instance;
	}

	private ActivityManager() {
		this.application = Application.getInstance();
		activities = new ArrayList<Activity>();
		nextTaskIndex = 0;
		taskIndexes = new WeakHashMap<Activity, Integer>();
	}

	private void rebuildStack() {
		Iterator<Activity> iterator = activities.iterator();
		while (iterator.hasNext())
			if (iterator.next().isFinishing())
				iterator.remove();
	}

	public void clearStack(boolean finishRoot) {
		HomeActivity root = null;
		rebuildStack();
		for (Activity activity : activities) {
			if (!finishRoot && root == null && activity instanceof HomeActivity)
				root = (HomeActivity) activity;
			else
				activity.finish();
		}
		rebuildStack();
	}

	public void finishAll() {
		rebuildStack();
		for (Activity activity : activities) {
			activity.finish();
		}
		rebuildStack();
	}

	public boolean hasContactList(Context context) {
		rebuildStack();
		for (Activity activity : activities)
			if (activity instanceof HomeActivity)
				return true;
		return false;
	}

	private void applyTheme(Activity activity) {
		if (activity instanceof PreferenceEditor)
			return;
		if (activity instanceof BroadcastMesssageActivity)
			return;
		if (activity instanceof UploadFileActivity)
			return;
		InterfaceTheme theme = SettingsManager.interfaceTheme();
		if (theme == SettingsManager.InterfaceTheme.light)
			activity.setTheme(R.style.Theme_Light);
		else if (theme == SettingsManager.InterfaceTheme.dark)
			activity.setTheme(R.style.Theme_Dark);
		else if (theme == SettingsManager.InterfaceTheme.colorfull)
			activity.setTheme(R.style.Theme_LightColorfull);
	}

	public void onCreate(Activity activity) {
		applyTheme(activity);

		if (application.isClosing() && !(activity instanceof SplashActivity)) {
			activity.startActivity(SplashActivity.createIntent(activity));
			activity.finish();
		}

		activities.add(activity);
		rebuildStack();
		fetchTaskIndex(activity, activity.getIntent());
	}

	public void onDestroy(Activity activity) {
		activities.remove(activity);
	}

	public void onPause(Activity activity) {
		if (onErrorListener != null)
			application
					.removeUIListener(OnErrorListener.class, onErrorListener);
		onErrorListener = null;
	}

	public void onResume(final Activity activity) {
		if (!application.isInitialized()
				&& !(activity instanceof SplashActivity)) {
			activity.startActivity(SplashActivity.createIntent(activity));
		}
		if (onErrorListener != null)
			application
					.removeUIListener(OnErrorListener.class, onErrorListener);
		onErrorListener = new OnErrorListener() {
			@Override
			public void onError(final int resourceId) {
				/*
				 * Toast.makeText(activity, activity.getString(resourceId),
				 * Toast.LENGTH_LONG).show();
				 */
				Log.i(TAG, " :: >> :: " + activity.getString(resourceId)
						+ " :: << :: ");
			}
		};
		application.addUIListener(OnErrorListener.class, onErrorListener);
	}

	/**
	 * New intent received.
	 * 
	 * Must be called from {@link Activity#onNewIntent(Intent)}.
	 * 
	 * @param activity
	 * @param intent
	 */
	public void onNewIntent(Activity activity, Intent intent) {

	}

	/**
	 * Result has been received.
	 * 
	 * Must be called from {@link Activity#onActivityResult(int, int, Intent)}.
	 * 
	 * @param activity
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(Activity activity, int requestCode,
			int resultCode, Intent data) {
	}

	/**
	 * Adds task index to the intent if specified for the source activity.
	 * 
	 * Must be used when source activity starts new own activity from
	 * {@link Activity#startActivity(Intent)} and
	 * {@link Activity#startActivityForResult(Intent, int)}.
	 * 
	 * @param source
	 * @param intent
	 */
	public void updateIntent(Activity source, Intent intent) {
		Integer index = taskIndexes.get(source);
		if (index == null)
			return;
		intent.putExtra(EXTRA_TASK_INDEX, index);
	}

	/**
	 * Mark activity to be in separate activity stack.
	 * 
	 * @param activity
	 */
	public void startNewTask(Activity activity) {
		taskIndexes.put(activity, nextTaskIndex);
		nextTaskIndex += 1;
	}

	/**
	 * Either move main task to back, either close all activities in subtask.
	 * 
	 * @param activity
	 */
	public void cancelTask(Activity activity) {
		Integer index = taskIndexes.get(activity);
		if (index == null) {
			activity.moveTaskToBack(true);
		} else {
			for (Entry<Activity, Integer> entry : taskIndexes.entrySet())
				if (entry.getValue() == index)
					entry.getKey().finish();
		}
	}

	/**
	 * Fetch task index from the intent and mark specified activity.
	 * 
	 * @param activity
	 * @param intent
	 */
	private void fetchTaskIndex(Activity activity, Intent intent) {
		int index = intent.getIntExtra(EXTRA_TASK_INDEX, -1);
		if (index == -1)
			return;
		taskIndexes.put(activity, index);
	}

	@Override
	public void onUnload() {
		clearStack(true);
	}

}
