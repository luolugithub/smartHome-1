package com.begood.smarthome.service;

import java.util.List;

import com.begood.smarthome.device.Dev;

public interface DevService {
	boolean saveDev(Dev dev);

	boolean removeDev(String id);

	Dev getDevById(String id);

	List<Dev> getDevList();

	int findDevMaxId();

	boolean findDevById(int id);
}
