package com.begood.smarthome.service;

/**
 * ���ݿⱣ���������Ϣ ����ӿ�
 * 
 * @author sl
 * 
 */
public interface ConfigService {
	public String getCfgByKey(String key);

	public boolean SaveSysCfgByKey(String key, String value);
}
