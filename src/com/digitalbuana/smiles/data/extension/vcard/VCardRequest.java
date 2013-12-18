package com.digitalbuana.smiles.data.extension.vcard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.digitalbuana.smiles.data.entity.BaseEntity;

class VCardRequest extends BaseEntity {

	private final String packetId;

	private final HashSet<String> hashes;

	public VCardRequest(String account, String bareAddress, String packetId) {
		super(account, bareAddress);
		this.packetId = packetId;
		this.hashes = new HashSet<String>();
	}

	public String getPacketId() {
		return packetId;
	}

	public Collection<String> getHashes() {
		return Collections.unmodifiableCollection(hashes);
	}

	public void addHash(String hash) {
		hashes.add(hash);
	}

}