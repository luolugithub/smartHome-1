package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.demo.smarthome.R;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.demo.smarthome.server.ServerReturnResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.Toast;
import com.demo.smarthome.view.HistoryDataLineView;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.demo.smarthome.view.MyDialogView;

public class historyDataActivity extends Activity {

    TextView title = null;
    Spinner dataSpinner;
    String dataType;

    List<String> dateList;
    ArrayAdapter<String> mAdapter;
    List<DeviceDataSet> allDataList;
    String jsonResult;
    Gson gson = new Gson();
    ServerReturnResult getDate = new ServerReturnResult();
    DeviceDataResult historyData = new DeviceDataResult();

    HistoryDataLineView historyDataViewItem;
    MyDialogView dialogView;

    String userSetDate;

    Button btnRefresh;
    //用于设置坐标系的Y轴最大值
    float YmaxValue;
    //用于设备每个Y轴标度的大小
    float YaverageValue;

    //如果是PM2.5或PM10
    boolean pmFlag = false;

    //time step length (second)
    static final int timeStepLength = 5*60;
    // step length in one day
    static final int stepCount = 24*60/5;

    boolean haveTemperature = false;
    static final int GET_DATE_SUCCEED      = 0;
    static final int GET_DATE_ERROR        = 1;
    static final int DEVICE_ID_ERROR       = 2;
    static final int GET_DATA_SUCCED       = 3;
    static final int GET_DATA_FAIL         = 4;
    static final int DELETE_ERROR          = 5;
    static final int SERVER_CANT_CONNECT   = 8;
    static final int SERVE_EXCEPTION       = 9;

    static final int DRAW_SUCCEED          = 0;
    static final int DRAW_FAIL             = 1;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case GET_DATE_SUCCEED:
                    dialogView.closeMyDialog();
                    mAdapter = new ArrayAdapter<String>(historyDataActivity.this,android.R.layout.simple_spinner_item, dateList);

                    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataSpinner.setAdapter(mAdapter);
                    dataSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            userSetDate = dateList.get(position);
                            if (userSetDate.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "日期数据错误", Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            new getDataByDate().start();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;
                case GET_DATE_ERROR:
                    dialogView.closeMyDialog();
                        break;
                case GET_DATA_SUCCED:

                    if(dataType.isEmpty()||allDataList == null) {
                        Toast.makeText(getApplicationContext(), "数据错误", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    drawLineByHistoryData(dataType,allDataList);
                        break;
                case GET_DATA_FAIL:
                        break;
                default:
                        break;

            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序

        setContentView(R.layout.activity_history_data);

        title = (TextView) findViewById(R.id.titleHistoryView);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }

        });
        dataSpinner = (Spinner)findViewById(R.id.dataSpinner);
        btnRefresh = (Button)findViewById(R.id.deviceDataRefresh);
        btnRefresh.setOnClickListener(new refreshOnClickListener());

        if(Cfg.currentDeviceID.isEmpty()) {
            Toast.makeText(getApplicationContext(), "设备ID错误", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        historyDataViewItem = (HistoryDataLineView)findViewById(R.id.historyLine);

        Bundle bundle = getIntent().getExtras();
        dataType = bundle.getString("dataName");

        //等待框
        dialogView = new MyDialogView(historyDataActivity.this);
        dialogView.showMyDialog("读取数据中", "正在从服务器中读取历史数据,请等待");

        new getDateList().start();

    }
    /**
     * 刷新 按钮监听事件
     *
     * @author Administrator
     *
     */
    class refreshOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            finish();
            Bundle bundleData = new Bundle();
            bundleData.putString("dataName", dataType);
            Intent intent = new Intent(historyDataActivity.this, historyDataActivity.class);
            intent.putExtras(bundleData);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class getDateList extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_DATE_ERROR;

            String[] paramsName = {"deviceID"};
            String[] paramsValue = {Cfg.currentDeviceID};

            setServerURL regiterUser= new setServerURL();

            if((jsonResult = regiterUser.sendParamToServer("getDateList", paramsName, paramsValue)).isEmpty()){
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                getDate = gson.fromJson(jsonResult, ServerReturnResult.class);
            }
            catch (JsonSyntaxException e){
                e.printStackTrace();
                handler.sendMessage(message);
            }

            switch (Integer.parseInt(getDate.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    dateList = getDate.getRows();
                    message.what = GET_DATE_SUCCEED;
                    break;
                default:
                    message.what = GET_DATE_ERROR;
                    break;
            }
            handler.sendMessage(message);
        }
    }
    class getDataByDate extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_DATA_FAIL;

            String[] paramsName = {"deviceID", "date"};
            String[] paramsValue = {Cfg.currentDeviceID, userSetDate};

            setServerURL regiterUser = new setServerURL();

            if ((jsonResult = regiterUser.sendParamToServer("getDataBydevIDAndDate", paramsName, paramsValue)).isEmpty()) {
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
                    allDataList = historyData.getRows();
                    message.what = GET_DATA_SUCCED;
                    break;
                default:
                    message.what = GET_DATA_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }
    }
    //根据类型画出数据曲线
    private int drawLineByHistoryData(String type,List<DeviceDataSet> allData){

        int result = DRAW_FAIL;

        if(type.equals("hcho")){
            //设置Y轴最大值
            hchoSetYMaxValue(allData);
            //接收服务器发送的数要除1000倍
            SetDateToline(allData,type,100);
            historyDataViewItem.setyUnit(this.getResources()
                    .getString(R.string.device_hcho_unit));

        }else if(type.equals("pm2_5")) {
            pm2_5SetYMaxValue(allData);
            SetDateToline(allData,type, 1);
            historyDataViewItem.setyUnit(this.getResources()
                    .getString(R.string.device_pm2_5_unit));
        }else if(type.equals("pm10")){
            pm10SetYMaxValue(allData);
            SetDateToline(allData,type, 1);
            historyDataViewItem.setyUnit(this.getResources()
                    .getString(R.string.device_pm10_unit));
        }else if(type.equals("tvoc")){
            tvocSetYMaxValue(allData);
            SetDateToline(allData,type, 100);
            historyDataViewItem.setyUnit(this.getResources()
                    .getString(R.string.device_tvoc_unit));
        }

        return result;
    }

    //根据hcho数据最大值确定Y轴最大值
    private void hchoSetYMaxValue(List<DeviceDataSet> data){

        int maxValue = 0;
        //找出最大值
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
    //最大值1000
    private void pm2_5SetYMaxValue(List<DeviceDataSet> data){

        int maxValue = 0;
        //找出最大值
        for(int i = 0;i < data.size();i++) {
            if(maxValue < Integer.parseInt(data.get(i).getPm2_5())){
                maxValue = Integer.parseInt(data.get(i).getPm2_5());
            }
        }

        if(maxValue > 800){
            YmaxValue = 1000;
            YaverageValue = 200;
        }else if(maxValue>500){
            YmaxValue = 800;
            YaverageValue = 100;
        }else if(maxValue>300){
            YmaxValue = 500;
            YaverageValue = 100;
        }else if(maxValue>200){
            YmaxValue = 300;
            YaverageValue = 50;
        }else if(maxValue>100){
            YmaxValue = 200;
            YaverageValue = 40;
        }else {
            YmaxValue = 100;
            YaverageValue = 20;
        }
        pmFlag = true;
    }
    //最大值1000
    private void pm10SetYMaxValue(List<DeviceDataSet> data){

        int maxValue = 0;
        //找出最大值
        for(int i = 0;i < data.size();i++) {
            if(maxValue < Integer.parseInt(data.get(i).getPm10())){
                maxValue = Integer.parseInt(data.get(i).getPm10());
            }
        }

        if(maxValue > 800){
            YmaxValue = 1000;
            YaverageValue = 200;
        }else if(maxValue>500){
            YmaxValue = 800;
            YaverageValue = 100;
        }else if(maxValue>300){
            YmaxValue = 500;
            YaverageValue = 100;
        }else if(maxValue>200){
            YmaxValue = 300;
            YaverageValue = 50;
        }else if(maxValue>100){
            YmaxValue = 200;
            YaverageValue = 40;
        }else {
            YmaxValue = 100;
            YaverageValue = 20;
        }
        pmFlag = true;
    }
    //最大值5.00
    private void tvocSetYMaxValue(List<DeviceDataSet> data){

        int maxValue = 0;
        //找出最大值
        for(int i = 0;i < data.size();i++) {
            if(maxValue < Integer.parseInt(data.get(i).getTvoc())){
                maxValue = Integer.parseInt(data.get(i).getTvoc());
            }
        }

        if(maxValue > 800){
            YmaxValue = 10.0f;
            YaverageValue = 2.0f;
        }else if(maxValue>500){
            YmaxValue = 8.0f;
            YaverageValue = 1.0f;
        }else if(maxValue > 300){
            YmaxValue = 5.0f;
            YaverageValue = 0.1f;
        }else if(maxValue>200){
            YmaxValue = 3.0f;
            YaverageValue = 0.50f;
        }else if(maxValue>1.0){
            YmaxValue = 2.0f;
            YaverageValue = 0.4f;
        }else {
            YmaxValue = 1.0f;
            YaverageValue = 0.20f;
        }
    }

    /*
    *   用于组成步长为timeStepLength的数据集合,画曲线时可获得所有
     */
    private void SetDateToline(List<DeviceDataSet> allData,String dataType,int unitChange){
        String tempTime = userSetDate + " 00:00:00";
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i,j,diff;
        Double value = 0.0;
        //for draw
        ArrayList<Double> yData = new ArrayList<Double>();
        try{
            Date Start = dfs.parse(tempTime);
            Date CreateTime = dfs.parse(allData.get(0).getCreateTime());
            for(i = 0,j=0; i < stepCount ;i++){

                //毫秒需要转换成秒
                diff = (int)((CreateTime.getTime() - Start.getTime())/1000);
                //接收历史数据中储存的时间每条之间间隔为5分钟
                if(diff < timeStepLength && j < allData.size()) {
                    //服务器中读取的数据需要除个单位
                    if(dataType.equals("hcho")) {
                        value = ((double) Integer.parseInt(allData.get(j).getHcho())) / unitChange;
                    }else if(dataType.equals("tvoc")) {
                        value = ((double) Integer.parseInt(allData.get(j).getTvoc())) / unitChange;
                    }else if(dataType.equals("pm2_5")) {
                        value = ((double) Integer.parseInt(allData.get(j).getPm2_5())) / unitChange;
                    }else if(dataType.equals("pm10")) {
                        value = ((double) Integer.parseInt(allData.get(j).getPm10())) / unitChange;
                    }
                    yData.add(value);
                    j++;
                    if(j < allData.size()) {
                        CreateTime = dfs.parse(allData.get(j).getCreateTime());
                    }
                }else{
                    yData.add(-1.0);
                }
                Start.setTime(Start.getTime() + timeStepLength * 1000);
            }
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        historyDataViewItem.setData(yData,YmaxValue,YaverageValue,pmFlag);
    }

}
