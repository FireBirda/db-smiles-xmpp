package com.digitalbuana.smiles.data.connection;
import org.jivesoftware.smack.packet.Packet;
import com.digitalbuana.smiles.data.BaseManagerInterface;

public interface OnPacketListener extends BaseManagerInterface {

	void onPacket(ConnectionItem connection, String bareAddress, Packet packet);

}
