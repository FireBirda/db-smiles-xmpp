package com.digitalbuana.smiles.ui.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.utils.RecentUtils;

public class ListActivityConnection extends ListActivity {
	protected String urlKu = "";
	protected String loadingMessage = "";
	protected List<NameValuePair> postData;
	protected boolean isAsyncLoading = false;
	private SmilesProgressDialog dialog;
	private boolean isDialog;
	private String TAG = getClass().getSimpleName();

	protected APISmilesAsync asyncTask = null;

	protected void doPostAsync(Context context, String url,
			List<NameValuePair> postDatanya, String loadingTxt,
			boolean isDialogShow) {
		if (RecentUtils.checkNetwork()) {
			this.isDialog = isDialogShow;
			if (isDialog) {
				dialog = new SmilesProgressDialog(context, loadingTxt);
				dialog.show();
			}
			isAsyncLoading = true;
			postData = postDatanya;
			urlKu = url;
			if (url != null) {
				if (asyncTask != null) {
					asyncTask.cancel(true);
				}
				asyncTask = new APISmilesAsync();
				asyncTask.execute(urlKu);
			}
		}
	}

	protected void finishAsync(String result) {
		isAsyncLoading = false;
		if (dialog != null && isDialog == true) {
			if (dialog.isShowing()) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

			}
		}
		asyncTask = null;
	}

	public class APISmilesAsync extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			urlKu = params[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(urlKu);
			BufferedReader inStream = null;
			try {
				httppost.addHeader("device", "android");
				httppost.addHeader("User-Agent", "android");
				if (postData != null) {
					httppost.setEntity(new UrlEncodedFormEntity(postData));
				}
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				inStream = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer buffer = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = inStream.readLine()) != null) {
					buffer.append(line + NL);
				}
				inStream.close();
				String result = buffer.toString();
				return result;
			} catch (Exception e) {
				finishAsync("");
				Log.i(TAG, "PostError : " + e.getMessage());
				return "";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			finishAsync(result);
		}
	}
}
