package com.demo.smarthome.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.demo.smarthome.R;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AddDeviceActivity extends Activity {

    EditText textDevId;
    EditText textDevPwd;
    Button   addSumbit;
    String deviceId;
    String devicePassword;
    String jsonResult;
    ServerReturnResult getResult = new ServerReturnResult();

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what)
            {
                case Cfg.REG_SUCCESS:
                    Toast.makeText(AddDeviceActivity.this, jsonResult, Toast.LENGTH_SHORT).show();
                    handler.postDelayed(r, 1000);
                    break;
                case Cfg.SERVER_CANT_CONNECT:
                    break;
            }
        }
    };

    Runnable r = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        TextView title = (TextView) findViewById(R.id.titleAddDevice);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        textDevId = (EditText) findViewById(R.id.addDeviceId);
        textDevPwd = (EditText) findViewById(R.id.addDevicePwd);

        addSumbit = (Button) findViewById(R.id.addDeviceSumbit);
        addSumbit.setOnClickListener(new addDev());

    }
    class addDev implements OnClickListener {
        @Override
        public void onClick(View v) {
            new addDeviceThread().start();
        }
    }
    class addDeviceThread extends Thread {
        @Override
        public void run() {
            Message message = new Message();
            message.what = Cfg.REG_ERROR;
            Gson gson = new Gson();

            deviceId = textDevId.getText().toString();
            devicePassword = textDevPwd.getText().toString();

            String[] paramsName = {"userName","deviceId","devicePassword"};
            String[] paramsValue = {Cfg.userName,deviceId,devicePassword};

            setServerURL addDevSet= new setServerURL();

            //需要判断服务器是否开启
            if((jsonResult = addDevSet.sendParamToServer("addDeviceForUser", paramsName, paramsValue)).isEmpty()){
                message.what = Cfg.SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                getResult = gson.fromJson(jsonResult
                        , com.demo.smarthome.server.ServerReturnResult.class);
            }
            catch (JsonSyntaxException e){
                e.printStackTrace();
            }

            if(Cfg.debug)
            {
                Log.d("add", getResult.getMsg());
                Log.d("add",String.valueOf(getResult.getCode()));
            }

            switch (Integer.parseInt(getResult.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    message.what = Cfg.REG_SUCCESS;
                    break;
                case Cfg.CODE_PWD_ERROR:
                    message.what = Cfg.REG_PWD_ERROR;
                    break;
                case Cfg.CODE_USER_EXISTED:
                    message.what = Cfg.REG_USER_EXISTED;
                    break;

                case Cfg.CODE_EXCEPTION:
                    message.what = Cfg.REG_EXCEPTION;
                    break;
                default:
                    message.what = Cfg.REG_ERROR;
                    break;
            }
            handler.sendMessage(message);
        }

    }

}
