package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.R;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.tools.BitmapTools;
import com.demo.smarthome.tools.NetworkStatusTools;
import com.demo.smarthome.tools.shareToWiexin;
import com.demo.smarthome.view.DeviceHistoryDataView;
import com.demo.smarthome.view.HistoryDataLineView;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;
import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class DeviceHistoryDataActivitiy extends Activity {

    int currentType;
    static final int type_outline              = 0;
    static final int type_day                  = 1;
    static final int type_week                 = 2;
    static final int type_month                = 3;
    MyDialogView dialogView;
    DeviceHistoryDataView historyDataViewItem;
    List<DeviceDataSet> dayDataList;

    static final int GET_DATA_SUCCESS              = 0;
    static final int GET_DATA_FAIL                 = 1;
    static final int SERVER_CANT_CONNECT           = 10;

    String jsonResult;
    Gson gson = new Gson();
    DeviceDataResult historyData = new DeviceDataResult();

    float YmaxValue;
    float YaverageValue;

    Button dayBtn;
    Button weekBtn;
    Button monthBtn;
    Button shareBtn;

    String currentDay;
    String tomorrow;

    static int shareSucceed     = 0;
    static int fileNotExist     = 1;
    static int screenShotFail   = 2;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    if(dayDataList != null)
                    {
                        if(dayDataList.size() != 0)
                        {
                            drawHistoryData(currentType, dayDataList);
                            dialogView.closeMyDialog();
                            break;
                        }
                    }
                    dialogView.closeMyDialog();
                    Toast.makeText(getApplicationContext(), "无历史数据", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                    break;
                case GET_DATA_FAIL:
                    dialogView.closeMyDialog();
                    Toast.makeText(getApplicationContext(), "获取数据失败", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                    break;
                case SERVER_CANT_CONNECT:
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
        setContentView(R.layout.activity_device_history_data);

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setClickable(true);
        backBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });


        if(Cfg.currentDeviceID.isEmpty()) {
            Toast.makeText(getApplicationContext(), "设备错误,请重新选择", Toast.LENGTH_SHORT)
                    .show();
            Intent intent = new Intent();
            intent.setClass(DeviceHistoryDataActivitiy.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        historyDataViewItem = (DeviceHistoryDataView)findViewById(R.id.historyDataView);
        //for various screen
//        if(Cfg.phoneWidth == 480){
//            historyDataViewItem.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    BitmapTools.dp2px(DeviceHistoryDataActivitiy.this, 280)));
//        }else if(Cfg.phoneWidth == 1440){
//            historyDataViewItem.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    BitmapTools.dp2px(DeviceHistoryDataActivitiy.this, 400)));
//        }
        shareBtn = (Button)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new shareToTimeline());

        dayBtn = (Button)findViewById(R.id.todayBtn);
        dayBtn.setOnTouchListener(showViewByDay);
        weekBtn = (Button)findViewById(R.id.weekBtn);
        weekBtn.setOnTouchListener(showViewByWeek);
        monthBtn = (Button)findViewById(R.id.monthBtn);
        monthBtn.setOnTouchListener(showViewByMonth);

        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");
        currentDay = dfs.format(new java.util.Date());

        Calendar cal = Calendar.getInstance();
        //30th day before today
        cal.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow = dfs.format(cal.getTime());

        //wait dialog
        dialogView = new MyDialogView(DeviceHistoryDataActivitiy.this);
        //network is available or not
        if(!NetworkStatusTools.isNetworkAvailable(DeviceHistoryDataActivitiy.this)){
            currentType = type_outline;
            Toast.makeText(getApplicationContext(), "无网络", Toast.LENGTH_SHORT)
                    .show();
            drawHistoryData(currentType, dayDataList);
        }else {
            dialogView.showMyDialog("读取数据", "正在读取数据");
            currentType = type_day;
            dayBtn.setBackgroundResource(R.drawable.day_check);
            weekBtn.setBackgroundResource(R.drawable.week);
            monthBtn.setBackgroundResource(R.drawable.month);
            new getDataByDay().start();
        }

    }

    class shareToTimeline implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(shareToWiexin.shareToWeiXinTimeline(DeviceHistoryDataActivitiy.this)
                    != shareSucceed){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DeviceHistoryDataActivitiy.this);
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

    private View.OnTouchListener showViewByDay = new View.OnTouchListener(){

        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                dayBtn.setBackgroundResource(R.drawable.day_check);
                weekBtn.setBackgroundResource(R.drawable.week);
                monthBtn.setBackgroundResource(R.drawable.month);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if (dialogView.isDialogRunning() || currentType == type_day) {
                    return false;
                }

                if (!NetworkStatusTools.isNetworkAvailable(DeviceHistoryDataActivitiy.this)) {
                    currentType = type_outline;
                    Toast.makeText(getApplicationContext(), "无网络", Toast.LENGTH_SHORT)
                            .show();
                    drawHistoryData(currentType, dayDataList);
                } else {
                    dialogView.showMyDialog("读取数据", "正在读取数据");
                    currentType = type_day;
                    new getDataByDay().start();
                }
            }
            return false;
        }
    };

    private View.OnTouchListener showViewByWeek = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                dayBtn.setBackgroundResource(R.drawable.day);
                weekBtn.setBackgroundResource(R.drawable.week_check);
                monthBtn.setBackgroundResource(R.drawable.month);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if (dialogView.isDialogRunning() || currentType == type_week) {
                    return false;
                }

                if(!NetworkStatusTools.isNetworkAvailable(DeviceHistoryDataActivitiy.this)){
                    currentType = type_outline;
                    Toast.makeText(getApplicationContext(), "无网络", Toast.LENGTH_SHORT)
                            .show();
                    drawHistoryData(currentType, dayDataList);
                }else {
                    dialogView.showMyDialog("读取数据", "正在读取数据");
                    currentType = type_week;
                    new getDataByWeek().start();
                }

            }
            return false;
        }
    };

    private View.OnTouchListener showViewByMonth = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent event) {
            int iAction = event.getAction();
            if (iAction == MotionEvent.ACTION_DOWN) {
                dayBtn.setBackgroundResource(R.drawable.day);
                weekBtn.setBackgroundResource(R.drawable.week);
                monthBtn.setBackgroundResource(R.drawable.month_check);
            } else if (iAction == MotionEvent.ACTION_UP) {
                if (dialogView.isDialogRunning() || currentType == type_month) {
                    return false;
                }
                if(!NetworkStatusTools.isNetworkAvailable(DeviceHistoryDataActivitiy.this)){
                    currentType = type_outline;
                    Toast.makeText(getApplicationContext(), "无网络", Toast.LENGTH_SHORT)
                            .show();
                    drawHistoryData(currentType, dayDataList);
                }else {
                    dialogView.showMyDialog("读取数据", "正在读取数据");
                    currentType = type_month;
                    new getDataByMonth().start();
                }
            }
            return false;
        }
    };
    class getDataByDay extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_DATA_FAIL;

            //get date today


            String[] paramsName = {"deviceID", "day"};
            String[] paramsValue = {Cfg.currentDeviceID, currentDay};


            setServerURL regiterUser = new setServerURL();

            if ((jsonResult = regiterUser.sendParamToServer("getDataByDay", paramsName, paramsValue)).isEmpty()) {
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                historyData = gson.fromJson(jsonResult, DeviceDataResult.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (Integer.parseInt(historyData.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    dayDataList = historyData.getRows();
                    message.what = GET_DATA_SUCCESS;
                    break;
                default:
                    message.what = GET_DATA_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }
    }
    class getDataByWeek extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_DATA_FAIL;

            //get date today
            SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");

            Calendar cal = Calendar.getInstance();
            //30th day before today
            cal.add(Calendar.DAY_OF_MONTH, -6);
            String beforeAWeek = dfs.format(cal.getTime());

            String[] paramsName = {"deviceID","beforeWeek"};
            String[] paramsValue = {Cfg.currentDeviceID,beforeAWeek};


            setServerURL regiterUser = new setServerURL();

            if ((jsonResult = regiterUser.sendParamToServer("getDataByWeek", paramsName, paramsValue)).isEmpty()) {
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                historyData = gson.fromJson(jsonResult, DeviceDataResult.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (Integer.parseInt(historyData.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    dayDataList = historyData.getRows();
                    message.what = GET_DATA_SUCCESS;
                    break;
                default:
                    message.what = GET_DATA_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }
    }
    class getDataByMonth extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_DATA_FAIL;

            //get date today
            SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");

            Calendar cal = Calendar.getInstance();
            //30th day before today
            cal.add(Calendar.DAY_OF_MONTH, -29);
            String beforeAMonth = dfs.format(cal.getTime());

            String[] paramsName = {"deviceID","beforeMonth"};
            String[] paramsValue = {Cfg.currentDeviceID,beforeAMonth};


            setServerURL regiterUser = new setServerURL();

            if ((jsonResult = regiterUser.sendParamToServer("getDataByMonth", paramsName, paramsValue)).isEmpty()) {
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                historyData = gson.fromJson(jsonResult, DeviceDataResult.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (Integer.parseInt(historyData.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    dayDataList = historyData.getRows();
                    message.what = GET_DATA_SUCCESS;
                    break;
                default:
                    message.what = GET_DATA_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }
    }
    private void drawHistoryData(int type,List<DeviceDataSet> allData){

        if(type != type_outline){
            SetYMaxValue(allData);
            SetViewParameter(allData);
        }else{
            SetBlankView();
        }

    }
    //set Y coordinate
    private void SetYMaxValue(List<DeviceDataSet> data){

        int maxValue = 0;
        //
        for(int i = 0;i < data.size();i++) {
            if(maxValue < Integer.parseInt(data.get(i).getHcho())) {
                maxValue = Integer.parseInt(data.get(i).getHcho());
            }
        }

        if(maxValue>200){
            YmaxValue = 3.0f;
            YaverageValue = 0.5f;
        }else if(maxValue > 100){
            YmaxValue = 2.0f;
            YaverageValue = 0.4f;
        }else{
            YmaxValue = 1.0f;
            YaverageValue = 0.2f;
        }
    }

    private void SetViewParameter(List<DeviceDataSet> allData){
        String tempTime;
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
        int i,j,diff;
        Double value;
        // step length in a day
        int stepCount;
        //time step length (second)
        int timeStepLength;
        //for draw
        if(currentType == type_week){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            tempTime = dateSDF.format(cal.getTime()) + " 00:00:00";

            stepCount = 7*24;
            timeStepLength = 60*60;
        }else if(currentType == type_month){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -29);
            tempTime = dateSDF.format(cal.getTime()) + " 00:00:00";
            //the count of x
            stepCount = 8*30;
            timeStepLength = 3*60*60;
        }else {
            tempTime = currentDay + " 00:00:00";
            // step length in a day
            stepCount = 24*60/5;
            //time step length (second)
            timeStepLength = 5*60;
        }

        ArrayList<Double> yData = new ArrayList();
        try{
            Date Start = dfs.parse(tempTime);
            Date CreateTime = dfs.parse(allData.get(0).getCreateTime());
            for(i = 0,j=1; i < stepCount ;i++){

                diff = (int)((CreateTime.getTime() - Start.getTime())/1000);

                if(diff >= 0 && diff < timeStepLength && j < allData.size()) {
                    //The data from server need divide 100
                    value = ((double) Integer.parseInt(allData.get(j).getHcho())) / 100;
                    yData.add(value);
                    CreateTime = dfs.parse(allData.get(j).getCreateTime());
                    j++;
                }else{
                    yData.add(-1.0);
                }
                Start.setTime(Start.getTime() + timeStepLength * 1000);
            }
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        historyDataViewItem.setData(yData,YmaxValue,YaverageValue,currentType);
    }
    private void SetBlankView(){

        int stepCount = 24*60/5;

        ArrayList<Double> yData = new ArrayList();
        for(int i = 0; i < stepCount ;i++){
           yData.add(-1.0);
        }

        historyDataViewItem.setData(yData,3.0f,0.5f,type_day);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_device_history_data_activitiy, menu);
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
