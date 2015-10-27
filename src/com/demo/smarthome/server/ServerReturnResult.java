package com.demo.smarthome.server;

import java.util.List;

/**
 * Created by leishi on 15/10/23.
 */
public class ServerReturnResult {

    List<String> Rows;
    String Msg;
    String Code;
    String Total;

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public List<String> getRows() {
        return Rows;
    }

    public void setRows(List<String> rows) {
        Rows = rows;
    }

}
