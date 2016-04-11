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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.dao.ConfigDao;
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
 * @文件作者：sl
 * @文件描述：显示实时数据
 * @创建日期:2016-03-24
 **********************************************************/
public class DeviceRealtimeDataActivity extends Activity {

    Button deviceListBtn;
    Button hchoBtn;
    Button tvocBtn;
    Button pm2_5Btn;
    Button pm10Btn;
    Button historyDataBtn;
    Button shareBtn;
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

    //朋友圈分享成功
    static int shareSucceed     = 0;
    //截图不存在
    static int fileNotExist     = 1;
    //截图失败
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
                        if(currentType == currentdataTitle.hcho) {
                            crrentValue = Integer.parseInt(currentData.getHcho());
                            dataTitle.setText("甲醛");
                        }else if(currentType == currentdataTitle.tvoc) {
                            crrentValue = Integer.parseInt(currentData.getTvoc());
                            dataTitle.setText("TVOC");
                        }else if(currentType == currentdataTitle.pm2_5) {
                            crrentValue = Integer.parseInt(currentData.getTvoc());
                            dataTitle.setText("PM 2.5");
                        }else if(currentType == currentdataTitle.pm10) {
                            crrentValue = Integer.parseInt(currentData.getTvoc());
                            dataTitle.setText("PM 10");
                        }
                    }
                    else
                    {
                        crrentValue = 0;
                        dataTitle.setText("离线");
                        dataTitle.setTextColor(ContextCompat.getColor
                                (DeviceRealtimeDataActivity.this, R.color.sbc_snippet_text));
                        currentType = currentdataTitle.outline;
                    }
                    new Thread(updateDataThread).start();
                    break;
                case GET_CURRENT_FAIL:
                    dialogView.dismiss();
                    Toast.makeText(DeviceRealtimeDataActivity.this, "连接服务器失败", Toast.LENGTH_SHORT)
                            .show();

                    intent.setClass(DeviceRealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case CHOSE_DEVICE_NULL:
                    dialogView.dismiss();
                    ConfigService dbService = new ConfigDao(DeviceRealtimeDataActivity.this.getBaseContext());
                    Cfg.currentDeviceID = "";
                    dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, Cfg.currentDeviceID);

                    Toast.makeText(DeviceRealtimeDataActivity.this, "所选设备没有数据", Toast.LENGTH_SHORT)
                            .show();
                    intent.setClass(DeviceRealtimeDataActivity.this, MainActivity.class);
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
        setContentView(R.layout.activity_device_realtime_data);
        //initialize current data
        crrentValue = 0;
        deviceListBtn = (Button) findViewById(R.id.devicelist);
        deviceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(DeviceRealtimeDataActivity.this,MainActivity.class);
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
                intent.setClass(DeviceRealtimeDataActivity.this,DeviceHistoryDataActivitiy.class);
                startActivity(intent);
            }
        });
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new shareToTimeline());
        hchoBtn = (Button) findViewById(R.id.hchoBtn);
        hchoBtn.setOnClickListener(new hchoDataShow());
        tvocBtn = (Button) findViewById(R.id.tvocBtn);
        tvocBtn.setOnClickListener(new tvocDataShow());
        pm2_5Btn = (Button) findViewById(R.id.pm2_5Btn);
        pm2_5Btn.setOnClickListener(new pm2_5DataShow());
        pm10Btn = (Button) findViewById(R.id.pm10Btn);
        pm10Btn.setOnClickListener(new pm10DataShow());
        temperatureTextView = (TextView)findViewById(R.id.temperature);
        dampnessTextView = (TextView)findViewById(R.id.dampness);

        //to touch view fresh data.
        realDataView = (CircleAndNumberView)findViewById(R.id.CircleData);
        realDataView.setOnClickListener(new freshData());
        dataTitle = (TextView)findViewById(R.id.dataType);
        dataTitle.setOnClickListener(new freshData());

        //initial type
        currentType = currentdataTitle.hcho;

        //等待框
        dialogView = new ProgressDialog(DeviceRealtimeDataActivity.this);
        dialogView.setTitle("读取数据中");
        dialogView.setMessage("正在从服务器中读取数据,请等待");
        //点击等待框以外等待框不消失
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
            //屏蔽返回键
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

    //手机的back.
    @Override
    public void onBackPressed(){
        AlertDialog.Builder failAlert = new AlertDialog.Builder(DeviceRealtimeDataActivity.this);
        failAlert.setTitle("注销登陆").setMessage("   是否注销登陆")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(DeviceRealtimeDataActivity.this, LoginActivity.class);
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

    //分享到朋友圈
    class shareToTimeline implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(shareToWiexin.shareToWeiXinTimeline(DeviceRealtimeDataActivity.this)
                    != shareSucceed){
                Toast.makeText(DeviceRealtimeDataActivity.this, "分享到朋友圈失败", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    class hchoDataShow implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(is_get_data_success&&is_device_online)
            {
                crrentValue = Integer.parseInt(currentData.getHcho());
                dataTitle.setText("甲醛");
                currentType = currentdataTitle.hcho;
            }
            else
            {
                crrentValue = 0;
                dataTitle.setText("离线");
                dataTitle.setTextColor(ContextCompat.getColor
                        (DeviceRealtimeDataActivity.this, R.color.sbc_snippet_text));
                currentType = currentdataTitle.outline;
            }
            new Thread(updateDataThread).start();
        }
    }
    class tvocDataShow implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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
                        (DeviceRealtimeDataActivity.this, R.color.sbc_snippet_text));
                currentType = currentdataTitle.outline;
            }
            new Thread(updateDataThread).start();
        }
    }
    class pm2_5DataShow implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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
                        (DeviceRealtimeDataActivity.this, R.color.sbc_snippet_text));
                currentType = currentdataTitle.outline;
            }
            new Thread(updateDataThread).start();
        }
    }
    class pm10DataShow implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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
                        (DeviceRealtimeDataActivity.this, R.color.sbc_snippet_text));
                currentType = currentdataTitle.outline;
            }
            new Thread(updateDataThread).start();
        }
    }

    class freshData implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //等待框
            dialogView.setTitle("刷新");
            dialogView.setMessage("刷新数据,请等待");
            dialogView.show();
            new getCurrentDataThread().start();
        }
    }

    //获取当前数据
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
        //需要判断服务器是否开启
        if((jsonResult = regiterUser.sendParamToServer("getCurrentDeviceData", paramsName
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
                //如果时间差距超过10分钟,认为设备已离线
                //Device is outline if the last time of data is beynod ten minutes from now.
                SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date deviceTime = dfs.parse(currentData.getCreateTime());
                    Date currentTime = new Date();
                    if((currentTime.getTime() - deviceTime.getTime())/1000 > 10*60){
                        is_device_online = false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    is_device_online = false;
                    is_get_data_success = false;
                    return  GET_CURRENT_FAIL;
                }
                is_get_data_success = true;
                return GET_CURRENT_SUCCED;
            default:
                is_get_data_success = false;
                return  CHOSE_DEVICE_NULL;
        }
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
