package com.digitalbuana.smiles.data.roster;

import com.digitalbuana.smiles.data.account.StatusMode;

public class ResourceItem implements Comparable<ResourceItem> {

	private String verbose;
	private StatusMode statusMode;
	private String statusText;
	private int priority;

	public ResourceItem(String verbose, StatusMode statusMode,
			String statusText, int priority) {
		this.verbose = verbose;
		this.statusMode = statusMode;
		this.statusText = statusText;
		this.priority = priority;
	}

	public String getVerbose() {
		return verbose;
	}

	public void setVerbose(String verbose) {
		this.verbose = verbose;
	}

	public String getUser(String bareAddress) {
		return bareAddress + "/" + verbose;
	}

	public StatusMode getStatusMode() {
		return statusMode;
	}

	public void setStatusMode(StatusMode statusMode) {
		this.statusMode = statusMode;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(ResourceItem another) {
		int result = priority - another.priority;
		if (result != 0)
			return result;
		return statusMode.compareTo(another.statusMode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + priority;
		result = prime * result
				+ ((statusMode == null) ? 0 : statusMode.hashCode());
		result = prime * result
				+ ((statusText == null) ? 0 : statusText.hashCode());
		result = prime * result + ((verbose == null) ? 0 : verbose.hashCode());
		return result;
	}
}
