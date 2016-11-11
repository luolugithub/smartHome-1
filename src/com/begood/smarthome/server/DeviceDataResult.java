package com.begood.smarthome.server;

import java.util.List;

/**
 * Created by leishi on 15/10/28.
 */
public class DeviceDataResult {
    List<DeviceDataSet> Rows;
    String Msg;
    String Code;
    String Total;
    public List<DeviceDataSet> getRows() {
        return Rows;
    }

    public void setRows(List<DeviceDataSet> rows) {
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
