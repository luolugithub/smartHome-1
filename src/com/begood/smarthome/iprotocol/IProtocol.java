package com.begood.smarthome.iprotocol;

import java.util.List;

import com.begood.smarthome.protocol.Buff;
import com.begood.smarthome.protocol.Msg;

public interface IProtocol {
	List<Msg> checkMessage(Buff buff);

	boolean MessageEnCode(Msg msg);

	boolean MessagePackData(Msg msg, String[] listStr);
}
