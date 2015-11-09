package com.demo.smarthome.service;

import android.content.Context;
import android.text.TextUtils;

import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;

/**
 * Created by leishi on 15/10/27.
 */
public class ConnectDevice {

    public ConnectDevice() {
    }
    public static int SmartLink(String apPassword,boolean isSsidHidden, Context context){
        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(context);
        //ssid是网络的ID,bssid是接入ap的mac
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        String apBssid = mWifiAdmin.getWifiConnectedBssid();
        if(TextUtils.isEmpty(apSsid)){
            return Cfg.SMART_GET_SSID_ERROR;
        }

        return Cfg.SMART_SUCCESS;
    }
}
