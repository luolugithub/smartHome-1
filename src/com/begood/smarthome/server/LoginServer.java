package com.begood.smarthome.server;

import com.begood.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by leishi on 15/10/26.
 *
 *
 *
 */

public class LoginServer {


    public LoginServer() {
    }

    public static ServerReturnResult LoginServerMethod() {

        Gson gson = new Gson();
        ServerReturnResult loginResult = new ServerReturnResult();
        String jsonResult;

        loginResult.setCode(String.valueOf(Cfg.CODE_PWD_ERROR));
        if(Cfg.userName == null|| Cfg.userPassword ==null){
            loginResult.setCode(String.valueOf(Cfg.USERNAME_EXCEPTION));
            return loginResult;
        }

        String[] paramsName = {"userName", "pwd"};
        String[] paramsValue = {Cfg.userName, Cfg.userPassword};
        String methodName = "login";

        setServerURL regiterUser = new setServerURL();

        if((jsonResult = regiterUser.sendParamToServer(methodName, paramsName, paramsValue)).isEmpty()){
            loginResult.setCode(String.valueOf(Cfg.SERVER_CANT_CONNECT));
            return loginResult;
        }
        try {
            loginResult = gson.fromJson(jsonResult
                    , com.begood.smarthome.server.ServerReturnResult.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            loginResult.setCode(String.valueOf(Cfg.SERVER_CANT_CONNECT));
            return loginResult;
        }
        if(loginResult.getRows().size() != 0) {
            Cfg.devInfo = loginResult.getRows().get(0).split(",");
            for(int i = 0;i<Cfg.devInfo.length;i++)
            {
                if(Cfg.devInfo[i].equals(Cfg.currentDeviceID))
                {
                    return loginResult;
                }
            }
            Cfg.currentDeviceID = "";
        }else{
            Cfg.devInfo = new String[]{};
            Cfg.currentDeviceID = "";
        }
        return loginResult;
    }

    //返回true表示成功,false表失败
    public static boolean getDeviceType(String deviceId) {

        Gson gson = new Gson();
        ServerReturnResult tempData;
        String jsonResult;
        String[] paramsName = {"deviceID"};
        String[] paramsValue = new String[1];

        if (deviceId.isEmpty()) {
            return false;
        }
        paramsValue[0] = deviceId;

        setServerURL regiterUser = new setServerURL();

        if ((jsonResult = regiterUser.sendParamToServer("getDeviceType", paramsName
                , paramsValue)).isEmpty()) {
            return false;
        }
        try {
            tempData = gson.fromJson(jsonResult, ServerReturnResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        switch (Integer.parseInt(tempData.getCode())) {
            case Cfg.CODE_SUCCESS:
                if (tempData.getRows().size() != 1) {
                    return false;
                }
                Cfg.deviceType = tempData.getRows().get(0);
                return true;
            default:
                return false;
        }
    }
    //返回true表示成功,false表失败
    public static String getType(String deviceId) {

        Gson gson = new Gson();
        ServerReturnResult tempData;
        String jsonResult;
        String[] paramsName = {"deviceID"};
        String[] paramsValue = new String[1];

        if (deviceId.isEmpty()) {
            return null;
        }
        paramsValue[0] = deviceId;

        setServerURL regiterUser = new setServerURL();

        if ((jsonResult = regiterUser.sendParamToServer("getDeviceType", paramsName
                , paramsValue)).isEmpty()) {
            return null;
        }
        try {
            tempData = gson.fromJson(jsonResult, ServerReturnResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        switch (Integer.parseInt(tempData.getCode())) {
            case Cfg.CODE_SUCCESS:
                if (tempData.getRows().size() != 1) {
                    return null;
                }
                return tempData.getRows().get(0);
            default:
                return null;
        }
    }
}
