package com.demo.smarthome.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.R;
import com.demo.smarthome.device.DeviceDataString;
import com.demo.smarthome.device.DeviceType;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.smarthome.server.DeviceDataSet;

public class DeviceDataViewActivity extends Activity {

    TextView title = null;
    ListView listView;
    ListView listViewTem;
    Button buttonRefresh = null;
    int dataCount;
    String deviceID;
    String pageNo;
    String pageSize;

    String jsonResult;
    DeviceDataResult deviceData = new DeviceDataResult();
    Gson gson = new Gson();
    //如果从服务器获取数据成功此标志位置true
    boolean getDataSuccess = false;
    DeviceDataSet currentData = new DeviceDataSet();
    List<DeviceDataString> deviceListType;
    boolean haveTemperature = false;
    static final int GET_COUNT_SUCCEED     = 0;
    static final int GET_COUNT_ERROR       = 1;
    static final int DEVICE_ID_ERROR       = 2;
    static final int GET_CURRENT_SUCCED    = 3;
    static final int GET_CURRENT_FAIL      = 4;
    static final int DELETE_ERROR          = 5;
    static final int SERVER_CANT_CONNECT   = 8;
    static final int SERVE_EXCEPTION       = 9;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case GET_COUNT_SUCCEED:
                    getDataSuccess = true;
                    showDataList();
                    break;
                case GET_COUNT_ERROR:
                    getDataSuccess = false;
                    break;
                case GET_CURRENT_SUCCED:
                    getDataSuccess = true;
                    showDataList();
                    break;
                case GET_CURRENT_FAIL:
                    getDataSuccess = false;
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
        setContentView(R.layout.activity_device_data);

        title = (TextView) findViewById(R.id.titleHCHOView);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }

        });

        deviceID = Cfg.deviceID;

        listView = (ListView) this.findViewById(R.id.dataListView);
        listViewTem = (ListView) this.findViewById(R.id.horizontalListView);

        buttonRefresh = (Button) findViewById(R.id.deviceDataRefresh);
        buttonRefresh.setOnClickListener(new refreshOnClickListener());

        new getCurrentData().start();

    }
    //该方法会根据ID区分需要显示那些adpter,温湿度需要单独的adpter类型
    private void showDataList() {
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> dataTem = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> itemTem = new HashMap<String, Object>();
        if(deviceID.equals(deviceID))
        {
            //为能够显示特殊字符
            deviceListType = new DeviceType(this).getHchoMonitor();

            //显示带有温度和湿度的listView
            itemTem.put("temperature", currentData.getTemperature());
            itemTem.put("hygrometer", "000");
            dataTem.add(itemTem);
            haveTemperature = true;
        }

        if(deviceListType == null) {
            return;
        }
        String tempValue;
        for (DeviceDataString dataTypeTemp : deviceListType) {
            HashMap<String, Object> item = new HashMap<String, Object>();

            if((dataTypeTemp.getType()).equals("hcho")){
                tempValue = currentData.getHcho();
            }else if((dataTypeTemp.getType()).equals("tvoc")){
                tempValue = currentData.getTvoc();
            }else if((dataTypeTemp.getType()).equals("pm2_5")){
                tempValue = currentData.getPm2_5();
            }else if((dataTypeTemp.getType()).equals("pm10")) {
                tempValue = currentData.getPm10();
            }
            else{
                getDataSuccess = false;
                return;
            }

            item.put("name", dataTypeTemp.getName());
            item.put("value", tempValue);
            item.put("unit", dataTypeTemp.getUnit());
            data.add(item);
        }
        // 创建SimpleAdapter适配器将数据绑定到item显示控件上
        SimpleAdapter adapter = new MySimpleAdapter(this, data,
                R.layout.activity_device_adapter, new String[] { "name", "value","unit"},
                new int[] { R.id.adapterTypeName, R.id.adapterValue,R.id.adapterUnit});

        // 实现列表的显示
        listView.setAdapter(adapter);
        //删除分割线
        listView.setDivider(null);
        listView.setOnItemClickListener(new ItemClickListener());
        //显示带有温度和湿度的listView
        if(haveTemperature){
            SimpleAdapter adapterTem = new MySimpleAdapter(this, dataTem,
                    R.layout.activity_device_adapter_tem, new String[] { "temperature", "hygrometer"},
                    new int[] { R.id.adapterValueTem, R.id.adapterValueDam});
            listViewTem.setAdapter(adapterTem);
            listViewTem.setDivider(null);
            listViewTem.setOnItemClickListener(new ItemTemClickListener());
        }
    }

    class MySimpleAdapter extends SimpleAdapter {

        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);

        }
    }
    // 获取点击listView事件
    private final class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView TempListView = (ListView) parent;
            HashMap<String, Object> data = (HashMap<String, Object>) TempListView
                    .getItemAtPosition(position);

            String dataName = (String) data.get("name");
            Bundle bundleData = new Bundle();

            if (dataName == null) {
                Toast.makeText(getApplicationContext(), "获取历史数据失败", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if(dataName.equals(getString(R.string.device_hcho_name))){
                bundleData.putString("dataName", "hcho");
            }
            else if(dataName.equals(getString(R.string.device_pm2_5_name))){
                bundleData.putString("dataName", "pm2_5");
            }else if(dataName.equals(getString(R.string.device_pm10_name))){
                bundleData.putString("dataName", "pm10");
            }else if(dataName.equals(getString(R.string.device_tvoc_name))){
                bundleData.putString("dataName", "tvoc");
            }
            else{
                Toast.makeText(getApplicationContext(), "类型错误", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            Intent tempIntent = new Intent();
            tempIntent.setClass(DeviceDataViewActivity.this, historyDataActivity.class);
            tempIntent.putExtras(bundleData);
            startActivity(tempIntent);

        }
    }

    // 点击温湿度事件
    private final class ItemTemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            //现在温湿度不支持历史数据查询
            if(true){
                return;
            }
            ListView TempListView = (ListView) parent;
            HashMap<String, Object> data = (HashMap<String, Object>) TempListView
                    .getItemAtPosition(position);

            Bundle dataDevId = new Bundle();

            // 跳转到设置界面
            dataDevId.putString("dataName", "temperature");

            Intent tempIntent = new Intent();
            tempIntent.setClass(DeviceDataViewActivity.this, historyDataActivity.class);
            tempIntent.putExtras(dataDevId);
            startActivity(tempIntent);

        }
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hcho_monitor, menu);
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

    class getCurrentData extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_CURRENT_SUCCED;

            if(deviceID.isEmpty()) {
                message.what = DEVICE_ID_ERROR;
                handler.sendMessage(message);
                return;
            }

            String[] paramsName = {"deviceID"};
            String[] paramsValue = {deviceID};

            setServerURL regiterUser= new setServerURL();
            //需要判断服务器是否开启
            if((jsonResult = regiterUser.sendParamToServer("getCurrentDeviceData", paramsName
                    , paramsValue)).isEmpty()){
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return;
            }
            try {
                deviceData = gson.fromJson(jsonResult, DeviceDataResult.class);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            switch (Integer.parseInt(deviceData.getCode()))
            {
                case Cfg.CODE_SUCCESS:
                    if(deviceData.getRows().size() != 1)
                    {
                        message.what = GET_CURRENT_FAIL;
                        break;
                    }
                    currentData = deviceData.getRows().get(0);
                    message.what = GET_CURRENT_SUCCED;
                    break;
                default:
                    message.what = GET_CURRENT_FAIL;
                    break;
            }
            handler.sendMessage(message);
        }

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
            Intent intent = new Intent(DeviceDataViewActivity.this, DeviceDataViewActivity.class);
            startActivity(intent);
        }
    }

}
