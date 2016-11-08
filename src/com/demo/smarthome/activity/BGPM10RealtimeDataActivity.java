package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.ServerReturnResult;
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
import java.util.Date;
import android.widget.PopupMenu;
import android.view.MenuItem;


/**********************************************************
 *:2016-03-24
 **********************************************************/
public class BGPM10RealtimeDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    final String TAG = "BGPM10";
    Button deviceListBtn;
    Button hchoBtn;
    Button tvocBtn;
    Button pm2_5Btn;
    Button shareBtn;
    DrawerLayout drawerMenu;
    ListView drawerButtonList;
    ArrayList<String> menuLists;
    ArrayAdapter<String> drawerAdapter;

    CircleAndNumberView realDataView;
    int crrentValue;
    String jsonResult;
    Gson gson = new Gson();
    boolean is_device_online = true;
    boolean is_get_data_success = false;
    MyDialogView dialogView;
    String originalPassword;
    String changeNewPassword;
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
    static final int CHANGE_PASSWORD_ERROR           = 11;
    static final int INPUT_PASSWORD_ERROR            = 12;
    static final int CHANGE_PASSWORD_SUCCEED         = 15;
    //
    static int shareSucceed     = 0;

    private Timer timer;
    enum currentdataTitle
    {
        outline,
        hcho,
        tvoc,
        pm2_5
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
                    if(is_get_data_success&&is_device_online)
                    {
                        if(currentType == currentdataTitle.hcho) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho_light);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            crrentValue = Integer.parseInt(currentData.getHcho());
                            if(crrentValue<= 10)
                            {
                                presentation.setText("优良");
                            }else if(crrentValue<= 30){
                                presentation.setText("轻微超标");
                            }else{
                                presentation.setText("严重超标");
                            }
                            dataTitle.setText("甲醛");
                        }else if(currentType == currentdataTitle.tvoc) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc_light);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            crrentValue = Integer.parseInt(currentData.getTvoc());
                            if(crrentValue < 60)
                            {
                                presentation.setText("优良");
                            }else if(crrentValue<= 180){
                                presentation.setText("轻微超标");
                            }else{
                                presentation.setText("严重超标");
                            }
                            dataTitle.setText("TVOC");
                        }else if(currentType == currentdataTitle.pm2_5) {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
                            crrentValue = Integer.parseInt(currentData.getPm2_5());
                            if(crrentValue<= 35)
                            {
                                presentation.setText("优");
                            }else if(crrentValue<= 75){
                                presentation.setText("良");
                            }else if(crrentValue<= 115){
                                presentation.setText("轻度污染");
                            }else if(crrentValue<= 150){
                                presentation.setText("中度污染");
                            }else if(crrentValue<= 250){
                                presentation.setText("重度污染");
                            }else if(crrentValue > 250){
                                presentation.setText("严重污染");
                            }
                            dataTitle.setText("PM 2.5");
                        }else
                        {
                            hchoBtn.setBackgroundResource(R.drawable.hcho);
                            tvocBtn.setBackgroundResource(R.drawable.tvoc);
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            crrentValue = 0;
                            presentation.setText("");
                            realDataView.isWarningColor(false);
                            dataTitle.setText("未知");
                        }

                    }
                    else
                    {
                        crrentValue = 0;
                        dataTitle.setText("离线");
                        presentation.setText("");
                        dataTitle.setTextColor(ContextCompat.getColor
                                (BGPM10RealtimeDataActivity.this, R.color.sbc_snippet_text));
                        presentation.setText("设备不在线");
                        currentType = currentdataTitle.outline;
                    }

                    new Thread(updateDataThread).start();
                    break;
                case GET_CURRENT_FAIL:
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "设备错误", Toast.LENGTH_SHORT)
                        .show();

                    intent.setClass(BGPM10RealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case CHOSE_DEVICE_NULL:
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
                case CHANGE_PASSWORD_ERROR:
                    dialogView.closeMyDialog();
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "密码更新失败", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case INPUT_PASSWORD_ERROR:
                    dialogView.closeMyDialog();
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "原密码输入错误", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case CHANGE_PASSWORD_SUCCEED:
                    dialogView.closeMyDialog();
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "密码更新成功", Toast.LENGTH_SHORT)
                            .show();
                    Cfg.userPassword = changeNewPassword;
                    dbService = new ConfigDao(BGPM10RealtimeDataActivity.this.getBaseContext());
                    dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, changeNewPassword);
                    break;
                case SERVER_CANT_CONNECT:
                    AlertDialog.Builder failAlert = new AlertDialog.Builder(BGPM10RealtimeDataActivity.this);
                    failAlert.setTitle("错误").setMessage("服务器出现异常")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityControl.getInstance().finishAllActivity();
                                }
                            });
                    failAlert.create().show();
                    break;
                default:
                    Toast.makeText(BGPM10RealtimeDataActivity.this, "错误", Toast.LENGTH_SHORT)
                            .show();
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
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

        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new shareToWeixin());
        hchoBtn = (Button) findViewById(R.id.hchoBtn);
        hchoBtn.setOnTouchListener(hchoDataShow);
        tvocBtn = (Button) findViewById(R.id.tvocBtn);
        tvocBtn.setOnTouchListener(tvocDataShow);
        pm2_5Btn = (Button) findViewById(R.id.pm2_5Btn);
        pm2_5Btn.setOnTouchListener(pm2_5DataShow);
        temperatureTextView = (TextView)findViewById(R.id.temperature);
        dampnessTextView = (TextView)findViewById(R.id.dampness);
        presentation = (TextView)findViewById(R.id.presentation);

        //to touch view fresh data.
        realDataView = (CircleAndNumberView)findViewById(R.id.CircleData);
        dataTitle = (TextView)findViewById(R.id.dataType);

        //auto fresh
        timer = new Timer();
        timer.schedule(freshDataTimer, Cfg.autoFreshTime, Cfg.autoFreshTime);

        currentType = currentdataTitle.hcho;
        hchoBtn.setBackgroundResource(R.drawable.hcho_light);

        //左划菜单
        drawerMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerButtonList = (ListView) findViewById(R.id.left_menu);

        //添加5个列表项
        menuLists = new ArrayList<String>();
        menuLists.add("用户名:"+ Cfg.userName);
        menuLists.add("历史数据");
        menuLists.add("设备列表");
        menuLists.add("修改密码");
        menuLists.add("帮助");
        menuLists.add("注销登陆");
        //为列表设置适配器
        drawerAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, menuLists);
        drawerButtonList.setAdapter(drawerAdapter);

        //注册列表点击事件，注册为本类，所以本类要实现OnItemClickListener接口。
        drawerButtonList.setOnItemClickListener(this);

        deviceListBtn = (Button) findViewById(R.id.devicelist);
        deviceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                drawerMenu.openDrawer(drawerButtonList);
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
                //当数据为-1说明设备传感器初始化未完成
                if(crrentValue == -1)
                {
                    crrentValue = 0;
                }

                if(currentType == currentdataTitle.tvoc)
                {
                    realDataView.setText((float) crrentValue / 100 + "");
                    realDataView.setUnit(getResources().getString(R.string.device_tvoc_unit));
                    if(crrentValue < 60)
                    {
                        realDataView.isWarningColor(false);
                    }else
                    {
                        realDataView.isWarningColor(true);
                    }
                }else if(currentType == currentdataTitle.pm2_5)
                {
                    if(crrentValue < 250)
                    {
                        realDataView.isWarningColor(false);
                    }else
                    {
                        realDataView.isWarningColor(true);
                    }

                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit(getResources().getString(R.string.device_pm2_5_unit));

                }else if(currentType == currentdataTitle.hcho)
                {
                    if(crrentValue<= 5)
                    {
                        realDataView.isWarningColor(false);
                    }else if(crrentValue<= 10){
                        realDataView.isWarningColor(false);
                    }else{
                        realDataView.isWarningColor(true);
                    }

                    realDataView.setText((float) crrentValue / 100 + "");
                    realDataView.setUnit(getResources().getString(R.string.device_hcho_unit));

                    range = 300;
                }else{
                    realDataView.isWarningColor(false);
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

    //分享到微信
    private class shareToWeixin implements View.OnClickListener {
        PopupMenu popup;
        @Override
        public void onClick(View v) {

            popup = new PopupMenu(BGPM10RealtimeDataActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.share_list, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            Toast.makeText(BGPM10RealtimeDataActivity.this, "分享朋友圈中,请等待", Toast.LENGTH_SHORT).show();
                            switch (item.getItemId())
                            {
                                case R.id.friends:
                                    if(shareToWiexin.shareToFriend(BGPM10RealtimeDataActivity.this)
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
                                    break;
                                case R.id.timeline:
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
                                    break;
                                default:
                                    break;
                            }
                            popup.dismiss();
                            return true;
                        }
                    });
            popup.show();
        }
    }

    private View.OnTouchListener hchoDataShow = new View.OnTouchListener(){

        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            Cfg.historyType = DeviceInformation.HISTORY_TYPE_HCHO;
            if (iAction == MotionEvent.ACTION_DOWN) {
                hchoBtn.setBackgroundResource(R.drawable.hcho_light);
                tvocBtn.setBackgroundResource(R.drawable.tvoc);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if (is_get_data_success && is_device_online) {
                    crrentValue = Integer.parseInt(currentData.getHcho());
                    if(crrentValue<= 10)
                    {
                        presentation.setText("优良");
                    }else if(crrentValue<= 30){
                        presentation.setText("轻微超标");
                    }else{
                        presentation.setText("严重超标");
                    }
                    dataTitle.setText("甲醛");
                    currentType = currentdataTitle.hcho;
                } else {
                    crrentValue = 0;
                    presentation.setText("");
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
            Cfg.historyType = DeviceInformation.HISTORY_TYPE_TVOC;
            if (iAction == MotionEvent.ACTION_DOWN) {
                tvocBtn.setBackgroundResource(R.drawable.tvoc_light);
                hchoBtn.setBackgroundResource(R.drawable.hcho);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getTvoc());
                    if(crrentValue < 60)
                    {
                        presentation.setText("优良");
                    }else if(crrentValue<= 180){
                        presentation.setText("轻微超标");
                    }else{
                        presentation.setText("严重超标");
                    }
                    dataTitle.setText("TVOC");
                    currentType = currentdataTitle.tvoc;
                }
                else
                {
                    crrentValue = 0;
                    presentation.setText("");
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
            Cfg.historyType = DeviceInformation.HISTORY_TYPE_PM2_5;
            if (iAction == MotionEvent.ACTION_DOWN) {
                hchoBtn.setBackgroundResource(R.drawable.hcho);
                tvocBtn.setBackgroundResource(R.drawable.tvoc);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getPm2_5());
                    if(crrentValue<= 35)
                    {
                        presentation.setText("优");
                    }else if(crrentValue<= 75){
                        presentation.setText("良");
                    }else if(crrentValue<= 115){
                        presentation.setText("轻度污染");
                    }else if(crrentValue<= 150){
                        presentation.setText("中度污染");
                    }else if(crrentValue<= 250){
                        presentation.setText("重度污染");
                    }else if(crrentValue > 250){
                        presentation.setText("严重污染");
                    }
                    dataTitle.setText("PM 2.5");
                    currentType = currentdataTitle.pm2_5;
                }
                else
                {
                    crrentValue = 0;
                    presentation.setText("");
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

    private TimerTask freshDataTimer = new TimerTask(){
        @Override
        public void run() {
            new getCurrentDataThread().start();
        }
    };

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

                    if(currentTime.getTime() - deviceTime.getTime() > Cfg.outlineTime){
                        is_device_online = false;
                    }
                    else {
                        is_device_online = true;
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
                }
                return GET_CURRENT_SUCCED;
            default:
                is_get_data_success = false;
                return  CHOSE_DEVICE_NULL;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (position)
        {
            case 1:
                intent.setClass(BGPM10RealtimeDataActivity.this,DeviceHistoryDataActivitiy.class);
                startActivity(intent);
                break;
            case 2:
                intent.setClass(BGPM10RealtimeDataActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case 3:
                showChangePasswordDialog();
                break;
            case 4:
                intent.setClass(BGPM10RealtimeDataActivity.this,HelpActivity.class);
                startActivity(intent);
                break;
            case 5:
                resign();
                break;
            default:
                break;

        }

        //主视图显示碎片后把导航栏关了
        drawerMenu.closeDrawer(drawerButtonList);
    }

    private void showChangePasswordDialog(){
        LayoutInflater inflater = LayoutInflater.from(BGPM10RealtimeDataActivity.this);
        final View changePwdlayout = inflater.inflate(R.layout.change_password, null);

        AlertDialog.Builder myDialog = new AlertDialog.Builder(BGPM10RealtimeDataActivity.this)
                .setTitle("修改密码");
        myDialog.setView(changePwdlayout);

        myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText originalPasswordEt = (EditText) changePwdlayout.findViewById(R.id.originalPassword);
                originalPassword = originalPasswordEt.getText().toString();
                if (originalPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入原始密码", Toast.LENGTH_SHORT).show();
                    originalPasswordEt.setFocusable(true);
                    return;
                }
                EditText newPasswordEt = (EditText) changePwdlayout.findViewById(R.id.newPassword);
                changeNewPassword = newPasswordEt.getText().toString();
                if (changeNewPassword.isEmpty() || (changeNewPassword.length() < 6)) {
                    Toast.makeText(getApplicationContext(), "密码至少为六位", Toast.LENGTH_SHORT).show();
                    newPasswordEt.setFocusable(true);
                    return;
                }
                if(changeNewPassword.equals(originalPassword)){
                    Toast.makeText(getApplicationContext(), "更改的密码需要不一样", Toast.LENGTH_SHORT).show();
                    newPasswordEt.setFocusable(true);
                    return;
                }
                EditText confirmPasswordEt = (EditText) changePwdlayout.findViewById(R.id.confirmPassword);
                String confirmPassword = confirmPasswordEt.getText().toString();
                if(!changeNewPassword.equals(confirmPassword)){
                    Toast.makeText(getApplicationContext(), "两次填写的密码不一致", Toast.LENGTH_SHORT).show();
                    confirmPasswordEt.setFocusable(true);
                    return;
                }
                new changePasswordThread().start();
                dialog.dismiss();
                dialogView = new MyDialogView(BGPM10RealtimeDataActivity.this);
                dialogView.showMyDialog("更改密码中", "...请等待");
            }
        });
        myDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {
                dialog.dismiss();
            }
        });
        myDialog.create().show();
    }
    class changePasswordThread extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            int result;
            Gson gsonChange = new Gson();
            String jsonResultChange;
            ServerReturnResult returnDate;
            String[] paramsName = {"userName","userPassword","newPassword"};
            String[] paramsValue = {Cfg.userName,originalPassword,changeNewPassword};
            String methodName = "changePassword";

            setServerURL regiterUser= new setServerURL();

            if((jsonResultChange = regiterUser.sendParamToServer(methodName, paramsName, paramsValue)).isEmpty()){
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                returnDate = gsonChange.fromJson(jsonResultChange, ServerReturnResult.class);
            }
            catch (Exception e){
                e.printStackTrace();
                message.what = CHANGE_PASSWORD_ERROR;
                handler.sendMessage(message);
                return;
            }

            switch (Integer.parseInt(returnDate.getCode())) {
                case Cfg.CODE_SUCCESS:
                    result = CHANGE_PASSWORD_SUCCEED;
                    break;
                case Cfg.CODE_PWD_ERROR:
                    result = INPUT_PASSWORD_ERROR;
                    break;
                default:
                    result =   CHANGE_PASSWORD_ERROR;
                    break;

            }
            message.what = result;
            handler.sendMessage(message);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        freshDataTimer.cancel();
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
