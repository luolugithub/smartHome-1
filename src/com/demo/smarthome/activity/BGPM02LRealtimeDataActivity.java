package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import com.demo.smarthome.R;
import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.tools.shareToWiexin;
import com.demo.smarthome.view.CircleAndNumberView;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**********************************************************

 **********************************************************/
public class BGPM02LRealtimeDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    final String TAG = "BGPM02L";
    Button deviceListBtn;
    Button pm2_5Btn;
    Button pm10Btn;
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
    TextView dataTitle;
    TextView presentation;
    MyDialogView dialogView;
    String originalPassword;
    String changeNewPassword;
    DeviceDataResult deviceData = new DeviceDataResult();
    DeviceDataSet currentData = new DeviceDataSet();
    private Timer timer;
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
                    if(is_get_data_success&&is_device_online)
                    {
                        if(currentType == currentdataTitle.pm2_5) {
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
                            pm10Btn.setBackgroundResource(R.drawable.pm10);
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
                            }else{
                                presentation.setText("严重污染");
                            }
                            dataTitle.setText("PM 2.5");
                        }else {
                            pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
                            pm10Btn.setBackgroundResource(R.drawable.pm10_light);
                            crrentValue = Integer.parseInt(currentData.getPm10());
                            if(crrentValue<= 50)
                            {
                                presentation.setText("优");
                            }else if(crrentValue<= 150){
                                presentation.setText("良");
                            }else if(crrentValue<= 250){
                                presentation.setText("轻度污染");
                            }else if(crrentValue<= 350){
                                presentation.setText("中度污染");
                            }else if(crrentValue<= 420){
                                presentation.setText("重度污染");
                            }else{
                                presentation.setText("严重污染");
                            }
                            dataTitle.setText("PM 10");
                        }
                    }
                    else
                    {
                        crrentValue = 0;
                        dataTitle.setText("离线");
                        dataTitle.setTextColor(ContextCompat.getColor
                                (BGPM02LRealtimeDataActivity.this, R.color.sbc_snippet_text));
                        presentation.setText("设备不在线");
                        currentType = currentdataTitle.outline;
                    }

                    new Thread(updateDataThread).start();
                    break;
                case GET_CURRENT_FAIL:
                    Toast.makeText(BGPM02LRealtimeDataActivity.this, "设备错误", Toast.LENGTH_SHORT)
                            .show();

                    intent.setClass(BGPM02LRealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case CHOSE_DEVICE_NULL:
                    ConfigService dbService = new ConfigDao(BGPM02LRealtimeDataActivity.this.getBaseContext());
                    Cfg.currentDeviceID = "";
                    dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, Cfg.currentDeviceID);

                    Toast.makeText(BGPM02LRealtimeDataActivity.this, "设备错误,请重新选择", Toast.LENGTH_SHORT)
                            .show();
                    intent.setClass(BGPM02LRealtimeDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                //没有温湿度
                case UPDATE_DATA:

                    break;
                case SERVER_CANT_CONNECT:
                    AlertDialog.Builder failAlert = new AlertDialog.Builder(BGPM02LRealtimeDataActivity.this);
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
                    Toast.makeText(BGPM02LRealtimeDataActivity.this, "错误", Toast.LENGTH_SHORT)
                            .show();
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove title
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        //根据是否有虚拟按键载入不同的布局
        if(Cfg.isNavigationBar){
            setContentView(R.layout.activity_bgpm02l_realtime_data);
        }else {
            setContentView(R.layout.activity_bgpm02l_realtime_data_no_navigation_bar);
        }

        ActivityControl.getInstance().addActivity(this);
        //initialize current data
        crrentValue = 0;
        //历史数据参数
        Cfg.historyType = DeviceInformation.HISTORY_TYPE_PM2_5;

        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new shareToWeixin());
        pm2_5Btn = (Button) findViewById(R.id.pm2_5Btn);
        pm2_5Btn.setOnTouchListener(pm2_5DataShow);
        pm10Btn = (Button)findViewById(R.id.pm10Btn);
        pm10Btn.setOnTouchListener(pm10DataShow);
        presentation = (TextView)findViewById(R.id.presentation);

        realDataView = (CircleAndNumberView)findViewById(R.id.CircleData);
        dataTitle = (TextView)findViewById(R.id.dataType);
        //auto fresh
        timer = new Timer();
        timer.schedule(freshDataTimer, Cfg.autoFreshTime, Cfg.autoFreshTime);

        currentType = currentdataTitle.pm2_5;
        pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);

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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        freshDataTimer.cancel();
        // 结束Activity&从栈中移除该Activity
        ActivityControl.getInstance().removeActivity(this);
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

               if(currentType == currentdataTitle.pm2_5)
                {
                    if(crrentValue < 75)
                    {
                        realDataView.isWarningColor(false);
                    }else
                    {
                        realDataView.isWarningColor(true);
                    }
                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit(getResources().getString(R.string.device_pm2_5_unit));
                }else if(currentType == currentdataTitle.pm10)
                {
                    if(crrentValue < 150)
                    {
                        realDataView.isWarningColor(false);
                    }else
                    {
                        realDataView.isWarningColor(true);
                    }
                    realDataView.setText( crrentValue  + "");
                    realDataView.setUnit(getResources().getString(R.string.device_pm2_5_unit));
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
        AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
        failAlert.setTitle("注销").setMessage("是否注销登录")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(BGPM02LRealtimeDataActivity.this, LoginActivity.class);
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
        AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
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

            popup = new PopupMenu(BGPM02LRealtimeDataActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.share_list, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    Toast.makeText(BGPM02LRealtimeDataActivity.this, "分享朋友圈中,请等待", Toast.LENGTH_SHORT).show();
                    switch (item.getItemId())
                    {
                        case R.id.friends:
                            if(shareToWiexin.shareToFriend(BGPM02LRealtimeDataActivity.this)
                                    != shareSucceed){
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BGPM02LRealtimeDataActivity.this);
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
                            if(shareToWiexin.shareToWeiXinTimeline(BGPM02LRealtimeDataActivity.this)
                                    != shareSucceed){
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BGPM02LRealtimeDataActivity.this);
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

    private View.OnTouchListener pm2_5DataShow = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            Cfg.historyType = DeviceInformation.HISTORY_TYPE_PM2_5;
            if (iAction == MotionEvent.ACTION_DOWN) {
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5_light);
                pm10Btn.setBackgroundResource(R.drawable.pm10);
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
                    }else{
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
                            (BGPM02LRealtimeDataActivity.this, R.color.sbc_snippet_text));
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
            Cfg.historyType = DeviceInformation.HISTORY_TYPE_PM10;
            if (iAction == MotionEvent.ACTION_DOWN) {
                pm10Btn.setBackgroundResource(R.drawable.pm10_light);
                pm2_5Btn.setBackgroundResource(R.drawable.pm2_5);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if(is_get_data_success&&is_device_online)
                {
                    crrentValue = Integer.parseInt(currentData.getPm10());
                    if(crrentValue<= 50)
                    {
                        presentation.setText("优");
                    }else if(crrentValue<= 150){
                        presentation.setText("良");
                    }else if(crrentValue<= 250){
                        presentation.setText("轻度污染");
                    }else if(crrentValue<= 350){
                        presentation.setText("中度污染");
                    }else if(crrentValue<= 420){
                        presentation.setText("重度污染");
                    }else {
                        presentation.setText("严重污染");
                    }
                    dataTitle.setText("PM 10");
                    currentType = currentdataTitle.pm10;
                }
                else
                {
                    crrentValue = 0;
                    dataTitle.setText("离线");
                    dataTitle.setTextColor(ContextCompat.getColor
                            (BGPM02LRealtimeDataActivity.this, R.color.sbc_snippet_text));
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
                intent.setClass(BGPM02LRealtimeDataActivity.this,DeviceHistoryDataActivitiy.class);
                startActivity(intent);
                break;
            case 2:
                intent.setClass(BGPM02LRealtimeDataActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case 3:
                showChangePasswordDialog();
                break;
            case 4:
                intent.setClass(BGPM02LRealtimeDataActivity.this,HelpActivity.class);
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
        LayoutInflater inflater = LayoutInflater.from(BGPM02LRealtimeDataActivity.this);
        final View changePwdlayout = inflater.inflate(R.layout.change_password, null);

        AlertDialog.Builder myDialog = new AlertDialog.Builder(BGPM02LRealtimeDataActivity.this)
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
                dialogView = new MyDialogView(BGPM02LRealtimeDataActivity.this);
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
