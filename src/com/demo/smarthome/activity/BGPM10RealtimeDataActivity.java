package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.tools.ScreenShotTools;
import com.demo.smarthome.tools.shareToWiexin;
import com.demo.smarthome.view.CircleAndNumberView;
import com.demo.smarthome.R;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;

/**********************************************************
 *:2016-03-24
 **********************************************************/
public class BGPM10RealtimeDataActivity extends Activity {

    Button deviceListBtn;
    Button hchoBtn;
    Button tvocBtn;
    Button pm2_5Btn;
    Button pm10Btn;
    Button historyDataBtn;
    Button shareBtn;
    Button resignBtn;

    CircleAndNumberView realDataView;
    ProgressDialog dialogView;
    int crrentValue;
    String jsonResult;
    Gson gson = new Gson();
    boolean is_device_online = true;
    boolean is_get_data_success = false;
    TextView title = null;
    TextView dataTitle;
    TextView temperatureTextView;
    TextView dampnessTextView;
    TextView presentation;
    DeviceDataResult deviceData = new DeviceDataResult();
    DeviceDataSet currentData = new DeviceDataSet();

    static final int GET_COUNT_SUCCEED     = 0;
    static final int GET_COUNT_ERROR       = 1;
    static final int CHOSE_DEVICE_NULL       = 2;
    static final int GET_CURRENT_SUCCED    = 3;
    static final int GET_CURRENT_FAIL      = 4;
    static final int DELETE_ERROR          = 5;
    static final int SERVER_CANT_CONNECT   = 8;
    static final int SERVE_EXCEPTION       = 9;
    static final int UPDATE_DATA           = 10;

    //
    static int shareSucceed     = 0;
    //
    static int fileNotExist     = 1;
    //
    static int screenShotFail   = 2;

    enum currentdataTitle
    {
        outline,
        hcho,
        tvoc,
        pm2_5,
        pm10
    }
    currentdataTitle currentType;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent();
            switch (msg.what) {

                case GET_COUNT_ERROR:
                    break;
                case GET_CURRENT_SUCCED:
                    dialogView.dismiss();
                    if(is_get_data_success&&is_device_online)
                    {
                        int pm2_5 = Integer.parseInt(currentData.getPm2_5());
                        if(pm2_5<= 35)
                        {
                            presentation.setText("空气质量:优");
                        }else if(pm2_5<= 75){
                            presentation.setText("空气质量:良");
                        }else if(pm2_5<= 115){
                            presentation.setText("空气质量:轻度污染");
                        }else if(pm2_5<= 150){
                            presentation.setText("空气质量:中度污染");
                        }else if(pm2_5<= 250){
                            presentation.setText("空气质量:重度污染");
                        }else if(pm2_5 > 250){
                            presentation.setText("空气质量:严重污染");
                        }
                        if(currentType == currentdataTitle.hcho) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho_light);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            pm10Btn.setBackgroundResource(R.drawable.pm10);
                            crrentValue = Integer.parseInt(currentData.getHcho());
                            dataTitle.setText("甲醛");
                        }else if(currentType == currentdataTitle.tvoc) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc_light);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            pm10Btn.setBackgroundResource(R.drawable.pm10);
                            crrentValue = Integer.parseInt(currentData.getTvoc());
                            dataTitle.setText("TVOC");
                        }else if(currentType == currentdataTitle.pm2_5) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
                            pm10Btn.setBackgroundResource(R.drawable.pm10);
                            crrentValue = pm2_5;
                            dataTitle.setText("PM 2.5");
                        }else if(currentType == currentdataTitle.pm10) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            pm10Btn.setBackgroundResource(R.drawable.pm10_light);
                            crrentValue = Integer.parseInt(currentData.getPm10());
                            dataTitle.setText("PM 10");
                        }
                    }
                    else
                    {
                        crrentValue = 0;
                        dataTitle.setText("离线");
                        dataTitle.setTextColor(ContextCompat.getColor
                                (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                        presentation.setText("设备不在线");
                        currentType = currentdataTitle.outline;
                    }
                    new Thread(updateDataThread).start();
                    break;
                case GET_CURRENT_FAIL:
                    dialogView.dismiss();
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "设备错误", Toast.LENGTH_SHORT)
                            .show();

                    intent.setClass(BGPM10RealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case CHOSE_DEVICE_NULL:
                    dialogView.dismiss();
                    ConfigService dbService = new ConfigDao(BGPM10RealtimeDataActivity.this.getBaseContext());
                    Cfg.currentDeviceID = "";
                    dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, Cfg.currentDeviceID);

                    Toast.makeText(BGPM10RealtimeDataActivity.this, "设备错误,请重新选择", Toast.LENGTH_SHORT)
                            .show();
                    intent.setClass(BGPM10RealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case UPDATE_DATA:
                    int tempInt;
                    float tempFloat;
                    String temperature = "0";
                    String dampness = "0";
                    if (currentData.getTemperature().length() > 3) {
                        tempInt = Integer.parseInt(currentData.getTemperature().substring(0
                                , currentData.getTemperature().length() - 3));
                        tempFloat = ((float) tempInt) / 10;
                        temperature = String.valueOf(tempFloat);
                        dampness = currentData.getTemperature().substring(currentData.getTemperature().length() - 3);

                        //to use space instead of the front zero
                        byte[] byteHygrometer = dampness.getBytes();
                        for (int i = 0; i < dampness.length(); i++) {
                            if (byteHygrometer[i] == '0') {
                                byteHygrometer[i] = ' ';
                            } else {
                                break;
                            }
                        }
                        dampness = new String(byteHygrometer);

                    } else if (currentData.getTemperature().length() <= 3) {
                        dampness = currentData.getTemperature();
                    }
                    if(!(is_get_data_success&&is_device_online)) {
                        temperature = "0";
                        dampness = "0";
                    }
                    temperatureTextView.setText("温度:" + temperature
                            + getResources().getString(R.string.device_temperature_unit));
                    dampnessTextView.setText("湿度:" +dampness
                            + getResources().getString(R.string.device_hygrometer_unit));
                    break;
                default:
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(Cfg.isNavigationBar){
            setContentView(R.layout.activity_bgpm10_realtime_data);
        }else {
            setContentView(R.layout.activity_bgpm10_realtime_data_no_navigation_bar);
        }

        ActivityControl.getInstance().addActivity(this);

        //initialize current data
        crrentValue = 0;
        //device list
        //历史数据参数
        Cfg.historyType = DeviceInformation.HISTORY_TYPE_HCHO;
        deviceListBtn = (Button) findViewById(R.id.devicelist);
        deviceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(BGPM10RealtimeDataActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        historyDataBtn = (Button)findViewById(R.id.historyDataBtn);
        historyDataBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent();
                intent.setClass(BGPM10RealtimeDataActivity.this,DeviceHistoryDataActivitiy.class);
                startActivity(intent);
            }
        });
        resignBtn = (Button)findViewById(R.id.resignBtn);
        resignBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0)
            {
                resign();
            }
        });
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new shareToTimeline());
        hchoBtn = (Button) findViewById(R.id.hchoBtn);
        hchoBtn.setOnTouchListener(hchoDataShow);
        tvocBtn = (Button) findViewById(R.id.tvocBtn);
        tvocBtn.setOnTouchListener(tvocDataShow);
        pm2_5Btn = (Button) findViewById(R.id.pm2_5Btn);
        pm2_5Btn.setOnTouchListener(pm2_5DataShow);
        pm10Btn = (Button) findViewById(R.id.pm10Btn);
        pm10Btn.setOnTouchListener(pm10DataShow);
        temperatureTextView = (TextView)findViewById(R.id.temperature);
        dampnessTextView = (TextView)findViewById(R.id.dampness);
        presentation = (TextView)findViewById(R.id.presentation);

        //to touch view fresh data.
        realDataView = (CircleAndNumberView)findViewById(R.id.CircleData);
        realDataView.setOnClickListener(new freshData());
        dataTitle = (TextView)findViewById(R.id.dataType);
        dataTitle.setOnClickListener(new freshData());


        currentType = currentdataTitle.hcho;
        hchoBtn.setBackgroundResource(R.drawable.hcho_light);

        dialogView = new ProgressDialog(BGPM10RealtimeDataActivity.this);
        dialogView.setTitle("读取数据");
        dialogView.setMessage("正在读取数据,请等待");

        dialogView.setCanceledOnTouchOutside(false);
        dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
                "请等待...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogView.show();
        dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(false);

        dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        new getCurrentDataThread().start();
    }

    //updata current data
    final Runnable updateDataThread = new Runnable() {
        public void run() {
            //Add lock for safe of the thread
            synchronized (this) {
//                int tempNum = 0;
//                realDataView.resetCount();
//                while (tempNum <= crrentValue) {
//                    realDataView.incrementProgress();
//                    tempNum++;
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                //The range of value
                Message message = new Message();
                message.what = UPDATE_DATA;
                int range = 1000;
                if(currentType == currentdataTitle.tvoc)
                {
                    realDataView.setText((float) crrentValue / 100 + "");
                    realDataView.setUnit(getResources().getString(R.string.device_tvoc_unit));
                }else if(currentType == currentdataTitle.pm2_5)
                {
                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit(getResources().getString(R.string.device_pm2_5_unit));
                }else if(currentType == currentdataTitle.pm10)
                {
                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit(getResources().getString(R.string.device_pm2_5_unit));
                }else if(currentType == currentdataTitle.hcho)
                {
                    realDataView.setText((float) crrentValue / 100 + "");
                    realDataView.setUnit(getResources().getString(R.string.device_hcho_unit));
                    range = 300;
                }else{
                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit("");
                }
                realDataView.setProgress(crrentValue,range);
                handler.sendMessage(message);
            }
        }
    };
    private void resign(){
        AlertDialog.Builder failAlert = new AlertDialog.Builder(BGPM10RealtimeDataActivity.this);
        failAlert.setTitle("注销").setMessage("是否注销登录")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(BGPM10RealtimeDataActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        failAlert.create().show();
    }
    //
    @Override
    public void onBackPressed(){
        AlertDialog.Builder failAlert = new AlertDialog.Builder(BGPM10RealtimeDataActivity.this);
        failAlert.setTitle("注销").setMessage("是否退出程序")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityControl.getInstance().finishAllActivity();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        failAlert.create().show();
    }

    //分享到朋友圈
    class shareToTimeline implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Toast.makeText(BGPM10RealtimeDataActivity.this, "分享朋友圈中,请等待", Toast.LENGTH_SHORT)
                    .show();
            if(shareToWiexin.shareToWeiXinTimeline(BGPM10RealtimeDataActivity.this)
                    != shareSucceed){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BGPM10RealtimeDataActivity.this);
                alertDialog.setTitle("错误").setIcon(R.drawable.error_01).setMessage("请确定微信可以启动")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                return;
                            }
                        });
                alertDialog.create().show();
            }
        }
    }

    private View.OnTouchListener hchoDataShow = new View.OnTouchListener(){

        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                hchoBtn.setBackgroundResource(R.drawable.hcho_light);
                tvocBtn.setBackgroundResource(R.drawable.tvoc);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                pm10Btn.setBackgroundResource(R.drawable.pm10);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if (is_get_data_success && is_device_online) {
                    crrentValue = Integer.parseInt(currentData.getHcho());
                    dataTitle.setText("甲醛");
                    currentType = currentdataTitle.hcho;
                } else {
                    crrentValue = 0;
                    dataTitle.setText("离线");
                    dataTitle.setTextColor(ContextCompat.getColor
                            (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                    currentType = currentdataTitle.outline;
                }
                new Thread(updateDataThread).start();
            }
            return false;
        }
    };
    private View.OnTouchListener tvocDataShow = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                tvocBtn.setBackgroundResource(R.drawable.tvoc_light);
                hchoBtn.setBackgroundResource(R.drawable.hcho);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                pm10Btn.setBackgroundResource(R.drawable.pm10);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getTvoc());
                    dataTitle.setText("TVOC");
                    currentType = currentdataTitle.tvoc;
                }
                else
                {
                    crrentValue = 0;
                    dataTitle.setText("离线");
                    dataTitle.setTextColor(ContextCompat.getColor
                            (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                    currentType = currentdataTitle.outline;
                }
                new Thread(updateDataThread).start();
            }
            return false;
        }
    };
    private View.OnTouchListener pm2_5DataShow = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                hchoBtn.setBackgroundResource(R.drawable.hcho);
                tvocBtn.setBackgroundResource(R.drawable.tvoc);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
                pm10Btn.setBackgroundResource(R.drawable.pm10);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getPm2_5());
                    dataTitle.setText("PM 2.5");
                    currentType = currentdataTitle.pm2_5;
                }
                else
                {
                    crrentValue = 0;
                    dataTitle.setText("离线");
                    dataTitle.setTextColor(ContextCompat.getColor
                            (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                    currentType = currentdataTitle.outline;
                }
                new Thread(updateDataThread).start();
            }
            return false;
        }
    };
    private View.OnTouchListener pm10DataShow = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                hchoBtn.setBackgroundResource(R.drawable.hcho);
                tvocBtn.setBackgroundResource(R.drawable.tvoc);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                pm10Btn.setBackgroundResource(R.drawable.pm10_light);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getPm10());
                    dataTitle.setText("PM 10");
                    currentType = currentdataTitle.pm10;
                }
                else
                {
                    crrentValue = 0;
                    dataTitle.setText("离线");
                    dataTitle.setTextColor(ContextCompat.getColor
                            (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                    currentType = currentdataTitle.outline;
                }
                new Thread(updateDataThread).start();
            }
            return false;
        }
    };

    class freshData implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            dialogView.setTitle("刷新");
            dialogView.setMessage("刷新中,请等待");
            dialogView.show();
            new getCurrentDataThread().start();
        }
    }

    class getCurrentDataThread extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = getCurrentData();
            handler.sendMessage(message);
        }
    }

    int getCurrentData(){

        if(Cfg.currentDeviceID.isEmpty()) {
            return CHOSE_DEVICE_NULL;
        }

        String[] paramsName = {"deviceID"};
        String[] paramsValue = {Cfg.currentDeviceID};

        setServerURL regiterUser= new setServerURL();

        if((jsonResult = regiterUser.sendParamToServer("getDeviceRealtimeData", paramsName
                , paramsValue)).isEmpty()){
            return SERVER_CANT_CONNECT;
        }
        try {
            deviceData = gson.fromJson(jsonResult, DeviceDataResult.class);
        }
        catch (Exception e){
            e.printStackTrace();
            return  CHOSE_DEVICE_NULL;
        }

        switch (Integer.parseInt(deviceData.getCode()))
        {
            case Cfg.CODE_SUCCESS:
                if(deviceData.getRows().size() != 1)
                {
                    return  GET_CURRENT_FAIL;
                }
                currentData = deviceData.getRows().get(0);

                //Device is outline if the last time of data is beynod ten minutes from now.
                SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date deviceTime = dfs.parse(currentData.getCreateTime());
                    Date currentTime = new Date();
                    if((currentTime.getTime() - deviceTime.getTime())/1000 > 60){
                        is_device_online = false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    is_device_online = false;
                    is_get_data_success = false;
                    return  GET_CURRENT_FAIL;
                }
                is_get_data_success = true;
                //if the before status is outline ,but the status is online by now.
                if(currentType == currentdataTitle.outline)
                {
                    currentType = currentdataTitle.hcho;
                    hchoBtn.setBackgroundResource(R.drawable.hcho_light);
                }
                return GET_CURRENT_SUCCED;
            default:
                is_get_data_success = false;
                return  CHOSE_DEVICE_NULL;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从栈中移除该Activity
        ActivityControl.getInstance().removeActivity(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_device_realtime_data, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}
