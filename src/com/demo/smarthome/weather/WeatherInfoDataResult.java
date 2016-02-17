package com.demo.smarthome.weather;

import com.demo.smarthome.server.DeviceDataSet;

import java.util.List;

/**
 * Created by leishi on 16/1/29.
 */
public class WeatherInfoDataResult {
    WeatherInfo Rows;
    String Msg;
    String Code;
    String Total;
    public WeatherInfo getRows() {
        return Rows;
    }

    public void setRows(WeatherInfo rows) {
        Rows = rows;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }
}
