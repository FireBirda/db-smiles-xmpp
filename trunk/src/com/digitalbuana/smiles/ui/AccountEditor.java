package com.digitalbuana.smiles.ui;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.ArchiveMode;
import com.digitalbuana.smiles.data.intent.AccountIntentBuilder;
import com.digitalbuana.smiles.ui.helper.BaseSettingsActivity;

public class AccountEditor extends BaseSettingsActivity implements
		OnPreferenceClickListener {

	private static final int OAUTH_WML_REQUEST_CODE = 1;

	private static final String SAVED_TOKEN = "com.digitalbuana.smiles.ui.AccountEditor.TOKEN";

	private static final String INVALIDATED_TOKEN = "com.digitalbuana.smiles.ui.AccountEditor.INVALIDATED";

	private String account;
	private AccountItem accountItem;

	private String token;

	private Preference oauthPreference;

	@Override
	protected void onInflate(Bundle savedInstanceState) {
		account = AccountEditor.getAccount(getIntent());
		if (account == null) {
			finish();
			return;
		}
		accountItem = AccountManager.getInstance().getAccount(account);
		if (accountItem == null) {
			Application.getInstance().onError(R.string.NO_SUCH_ACCOUNT);
			finish();
			return;
		}
		addPreferencesFromResource(R.xml.account_editor_xmpp);
		if (!Application.getInstance().isContactsSupported())
			getPreferenceScreen().removePreference(
					findPreference(getString(R.string.account_syncable_key)));
		if (savedInstanceState == null)
			token = accountItem.getConnectionSettings().getPassword();
		else
			token = savedInstanceState.getString(SAVED_TOKEN);
		oauthPreference = findPreference(getString(R.string.account_oauth_key));
		if (oauthPreference != null)
			oauthPreference.setOnPreferenceClickListener(this);
		onOAuthChange();
		AccountManager.getInstance().removeAuthorizationError(account);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_TOKEN, token);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (getString(R.string.account_port_key).equals(preference.getKey()))
			try {
				Integer.parseInt((String) newValue);
			} catch (NumberFormatException e) {
				Toast.makeText(this, getString(R.string.account_invalid_port),
						Toast.LENGTH_LONG).show();
				return false;
			}
		if (getString(R.string.account_tls_mode_key)
				.equals(preference.getKey())
				|| getString(R.string.account_archive_mode_key).equals(
						preference.getKey()))
			preference.setSummary((String) newValue);
		else if (!getString(R.string.account_password_key).equals(
				preference.getKey())
				&& !getString(R.string.account_priority_key).equals(
						preference.getKey()))
			super.onPreferenceChange(preference, newValue);
		return true;
	}

	private void onOAuthChange() {
		if (oauthPreference == null)
			return;
		if (INVALIDATED_TOKEN.equals(token))
			oauthPreference.setSummary(R.string.account_oauth_invalidated);
		else
			oauthPreference.setSummary(R.string.account_oauth_summary);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// if
		// (getString(R.string.account_oauth_key).equals(preference.getKey())) {
		// startActivityForResult(OAuthActivity.createIntent(this,
		// AppConstants.XMPPConfProtocol),OAUTH_WML_REQUEST_CODE);
		// return true;
		// }
		return false;
	}

	@Override
	protected Map<String, Object> getValues() {
		Map<String, Object> source = new HashMap<String, Object>();
		putValue(source, R.string.account_custom_key, false);
		putValue(source, R.string.account_host_key, AppConstants.XMPPServerHost);
		putValue(source, R.string.account_port_key, AppConstants.XMPPConfPort);
		putValue(source, R.string.account_server_key,
				AppConstants.XMPPServerHost);
		putValue(source, R.string.account_username_key, accountItem
				.getConnectionSettings().getUserName());
		putValue(source, R.string.account_store_password_key, true);
		putValue(source, R.string.account_password_key, accountItem
				.getConnectionSettings().getPassword());
		putValue(source, R.string.account_resource_key,
				AppConstants.XMPPConfResource);
		putValue(source, R.string.account_priority_key,
				accountItem.getPriority());
		putValue(source, R.string.account_enabled_key, accountItem.isEnabled());
		putValue(source, R.string.account_sasl_key, accountItem
				.getConnectionSettings().isSaslEnabled());
		putValue(source, R.string.account_tls_mode_key, false);
		putValue(source, R.string.account_compression_key, accountItem
				.getConnectionSettings().useCompression());
		putValue(source, R.string.account_syncable_key, true);
		putValue(source, R.string.account_archive_mode_key,
				Integer.valueOf(accountItem.getArchiveMode().ordinal()));
		return source;
	}

	@Override
	protected Map<String, Object> getPreferences(Map<String, Object> source) {
		Map<String, Object> result = super.getPreferences(source);
		if (oauthPreference != null)
			putValue(result, R.string.account_password_key, token);
		return result;
	}

	@Override
	protected boolean setValues(Map<String, Object> source,
			Map<String, Object> result) {
		AccountManager.getInstance().updateAccount(
				account,
				getString(result, R.string.account_username_key),
				getString(result, R.string.account_password_key),
				getInt(result, R.string.account_priority_key),
				getBoolean(result, R.string.account_enabled_key),
				getBoolean(result, R.string.account_sasl_key),
				getBoolean(result, R.string.account_compression_key),
				ArchiveMode.values()[getInt(result,
						R.string.account_archive_mode_key)]);
		return true;
	}

	private static String getAccount(Intent intent) {
		return AccountIntentBuilder.getAccount(intent);
	}

	public static Intent createIntent(Context context, String account) {
		return new AccountIntentBuilder(context, AccountEditor.class)
				.setAccount(account).build();
	}

}
