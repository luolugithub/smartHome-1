package com.begood.smarthome.device;
import com.begood.smarthome.R;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leishi on 15/10/31.
 */
public class DeviceType {


    DeviceDataString hcho;
    DeviceDataString tvoc;
    DeviceDataString pm2_5;
    DeviceDataString pm10;

    public DeviceType(Context context) {
        hcho = new DeviceDataString(context.getResources().getString(R.string.device_hcho_name), "hcho"
                , context.getResources().getString(R.string.device_hcho_unit));
        tvoc = new DeviceDataString(context.getResources().getString(R.string.device_tvoc_name), "tvoc"
                , context.getResources().getString(R.string.device_tvoc_unit));
        pm2_5 = new DeviceDataString(context.getResources().getString(R.string.device_pm2_5_name), "pm2_5"
                , context.getResources().getString(R.string.device_pm2_5_unit));
        pm10 = new DeviceDataString(context.getResources().getString(R.string.device_pm10_name), "pm10"
                , context.getResources().getString(R.string.device_pm10_unit));
    }


    public List<DeviceDataString> getHchoMonitor() {

        List<DeviceDataString> hchoMonitor = new ArrayList<DeviceDataString>();
        hchoMonitor.add(hcho);
        hchoMonitor.add(tvoc);
        hchoMonitor.add(pm2_5);
        hchoMonitor.add(pm10);
        return hchoMonitor;
    }
}
