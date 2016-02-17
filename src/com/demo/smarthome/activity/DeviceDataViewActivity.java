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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceDataString;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.service.ConfigService;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

public class DeviceDataViewActivity extends Activity implements OnRefreshListener{

    TextView title = null;
    TextView pullRefreshText;
    ListView listView;
//    pull refresh
    SwipeRefreshLayout refresh_layout;
    boolean refresh_flag = false;
    String jsonResult;

    DeviceDataResult deviceData = new DeviceDataResult();

    ProgressDialog dialogView;

    Gson gson = new Gson();

    DeviceDataSet currentData = new DeviceDataSet();
    boolean is_device_online = true;
    List<DeviceDataString> deviceListType;
    boolean haveTemperature = false;
    static final int GET_COUNT_SUCCEED     = 0;
    static final int GET_COUNT_ERROR       = 1;
    static final int CHOSE_DEVICE_NULL       = 2;
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
            Intent intent = new Intent();
            switch (msg.what) {

                case GET_COUNT_ERROR:
                    break;
                case GET_CURRENT_SUCCED:
                    if(refresh_flag){
                        refresh_layout.setRefreshing(false);
                        refresh_flag = false;
                        pullRefreshText.setText("����ˢ��");
                    }
                    else{
                        dialogView.dismiss();
                    }
                    showDataList();
                    break;
                case GET_CURRENT_FAIL:
                    dialogView.dismiss();
                    Toast.makeText(DeviceDataViewActivity.this, "���ӷ�����ʧ��", Toast.LENGTH_SHORT)
                            .show();

                    intent.setClass(DeviceDataViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case CHOSE_DEVICE_NULL:
                    dialogView.dismiss();
                    ConfigService dbService = new ConfigDao(DeviceDataViewActivity.this.getBaseContext());
                    Cfg.currentDeviceID = "";
                    dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, Cfg.currentDeviceID);

                    Toast.makeText(DeviceDataViewActivity.this, "��ѡ�豸û������", Toast.LENGTH_SHORT)
                            .show();
                    intent.setClass(DeviceDataViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
        setContentView(R.layout.activity_device_data);

        title = (TextView) findViewById(R.id.titleHCHOView);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(DeviceDataViewActivity.this, LoginActivity.class);

                startActivity(intent);
                finish();
            }
        });
        TextView titleText = (TextView) findViewById(R.id.titleHCHOViewText);
        titleText.setClickable(true);
        titleText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(DeviceDataViewActivity.this, LoginActivity.class);

                startActivity(intent);
                finish();
            }
        });

        //�����ת����������
        TextView getWeatherText = (TextView) findViewById(R.id.getWeather);
        getWeatherText.setClickable(true);
        getWeatherText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(DeviceDataViewActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        //�������
        TextView titleConfig = (TextView) findViewById(R.id.titleConfig);
        titleConfig.setClickable(true);
        titleConfig.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(DeviceDataViewActivity.this, MainActivity.class);

                startActivity(intent);
            }
        });

        listView = (ListView) this.findViewById(R.id.dataListView);

        //�ٷ�����ˢ�µĿؼ�
        refresh_layout = (SwipeRefreshLayout) this.findViewById(R.id.refresh_layout);
        refresh_layout.setColorSchemeResources(R.color.green, R.color.blue_50, R.color.viewfinder_laser);
        //����ˢ�¼�����
        refresh_layout.setOnRefreshListener(this);
        //����ˢ��
        pullRefreshText = (TextView) this.findViewById(R.id.pullRefreshText);
        //�ȴ���
        dialogView = new ProgressDialog(DeviceDataViewActivity.this);
        dialogView.setTitle("��ȡ������");
        dialogView.setMessage("���ڴӷ������ж�ȡ����,��ȴ�");
        //����ȴ�������ȴ�����ʧ
        dialogView.setCanceledOnTouchOutside(false);
        dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
                "��ȴ�...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogView.show();
        dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(false);

        dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //���η��ؼ�
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
//    //���˰���
//    @Override
//    public void onBackPressed(){
//        Intent intent = new Intent();
//        intent.setClass(DeviceDataViewActivity.this, LoginActivity.class);
//
//        startActivity(intent);
//        finish();
//    }

    //���������ĺ���
    @Override
    public void onRefresh() {

        refresh_flag = true;
        pullRefreshText.setText("����ˢ��");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = getCurrentData();
                handler.sendMessage(message);
            }
        }).start();
    }

    //�˵���
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = new Intent();
        intent.setClass(DeviceDataViewActivity.this, MainActivity.class);

        startActivity(intent);
        return true;
    }

    private void showDataList() {

        // ����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���
        MyBaseAdapter adapter = new MyBaseAdapter();

        // ʵ���б����ʾ
        listView.setAdapter(adapter);
        //ɾ���ָ���
        listView.setDivider(null);
        listView.setOnItemClickListener(new ItemClickListener());

    }
    //��ʾ��list�е�����
    static final int LIST_HCHO = 0;
    static final int LIST_TVOC = 1;
    static final int LIST_PM2_5 = 2;
    static final int LIST_PM10 = 3;
    static final int LIST_TEMPERATURE = 4;

    public int setListTypeByDeviceType(int position){
//        if(currentData.getType().equals(currentData.getType())){
        if(true){
            switch (position){
                case 0:
                    return LIST_HCHO;
                case 1:
                    return LIST_TVOC;
                case 2:
                    return LIST_PM2_5;
                case 3:
                    return LIST_PM10;
                case 4:
                    return LIST_TEMPERATURE;
                default:
                    return LIST_HCHO;
            }
        }
        return LIST_HCHO;
    }

    private class MyBaseAdapter extends BaseAdapter {
        public static final int DECVICE_NO_TEMPERATURE = 1;
        public static final int DECVICE_TEMPERATURE = 2;

        public static final int TYPE_NOT_TEMPERATURE = 0;
        public static final int TYPE_TEMPERATURE = 1;

        public static final int HCHO_DETECTOR = 5;

        int viewTypeCount;
        int count ;
        int TemperaturePosition ;
        MyBaseAdapter (){
            //����������ʾlistView������
            if(currentData.getType().equals(currentData.getType())){
                viewTypeCount = DECVICE_TEMPERATURE;
                count = HCHO_DETECTOR;
                TemperaturePosition = LIST_TEMPERATURE;
            }
        }

        //����position��ȡ��Ӧitemʹ�õ�View���͡�
        @Override
        public int getItemViewType(int position) {
            if(setListTypeByDeviceType(position) == TemperaturePosition){
                return TYPE_TEMPERATURE;
            }
            else {
                return TYPE_NOT_TEMPERATURE;
            }

        }
        //����itemʹ�õ�View���͵�������Ĭ��Ϊ1��
        @Override
        public int getViewTypeCount() {
            return viewTypeCount;
        }
        //��������Դ���������������
        @Override
        public int getCount() {
            return count;
        }
        //����position������Դ�л�ȡ������
        @Override
        public Object getItem(int position) {
            return null;
        }
        //����position������Դ�л�ȡ������ID
        @Override
        public long getItemId(int position) {
            return position;
        }
        //��ʪ��ֵ���ɵ��
        @Override
        public boolean isEnabled(int position) {
            if(setListTypeByDeviceType(position) == TemperaturePosition){
                return false;
            }
            return true;
        }
        //�ú����Ժ�Ҫ�ǵøĶ�,̫����
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                TextView typeName;
                TextView adapterValue;
                TextView adapterUnit;
                TextView tempteratureText;
                TextView hygrometerText;

                int tempInt;
                float tempFloat;

                //����ǳ�����ʪ�������adapter
                if(getItemViewType(position) == TYPE_NOT_TEMPERATURE) {

                        String tempValue = "";
                        if(Cfg.phoneWidth == 480){
                            convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter_low, parent, false);
                        }else if(Cfg.phoneWidth == 1440){
                            convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter_high, parent, false);
                        }else{
                            convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter, parent, false);
                        }

                        typeName = (TextView) convertView.findViewById(R.id.adapterTypeName);
                        adapterValue = (TextView) convertView.findViewById(R.id.adapterValue);
                        adapterUnit = (TextView) convertView.findViewById(R.id.adapterUnit);
                        //��ȩ��TVOC����ֵҪ��1000
                        if (setListTypeByDeviceType(position) == LIST_HCHO) {
                            tempInt = Integer.parseInt(currentData.getHcho());
                            //��ֹ����0.0�����
                            if (tempInt == 0) {
                                tempValue = "0";
                            } else {
                                tempFloat = ((float) tempInt) / 100;
                                tempValue = String.valueOf(tempFloat);
                            }
                            typeName.setText(convertView.getResources().getString(R.string.device_hcho_name));
                            adapterUnit.setText(convertView.getResources().getString(R.string.device_hcho_unit));
                        } else if (setListTypeByDeviceType(position) == LIST_TVOC) {
                            tempInt = Integer.parseInt(currentData.getTvoc());
                            if (tempInt == 0) {
                                tempValue = "0";
                            } else {
                                tempFloat = ((float) tempInt) / 100;
                                tempValue = String.valueOf(tempFloat);
                            }
                            typeName.setText(convertView.getResources().getString(R.string.device_tvoc_name));
                            adapterUnit.setText(convertView.getResources().getString(R.string.device_tvoc_unit));
                        } else if (setListTypeByDeviceType(position) == LIST_PM2_5) {
                            tempValue = currentData.getPm2_5();
                            typeName.setText(convertView.getResources().getString(R.string.device_pm2_5_name));
                            adapterUnit.setText(convertView.getResources().getString(R.string.device_pm2_5_unit));
                        } else if (setListTypeByDeviceType(position) == LIST_PM10) {
                            tempValue = currentData.getPm10();
                            typeName.setText(convertView.getResources().getString(R.string.device_pm10_name));
                            adapterUnit.setText(convertView.getResources().getString(R.string.device_pm10_unit));
                        }

                        if(is_device_online) {
                            adapterValue.setText(tempValue);
                        }
                        else{
                            adapterValue.setText("");
                        }

                } else if(getItemViewType(position) == TYPE_TEMPERATURE){
                    String temperature = "";
                    String hygrometer = "";
                    if(Cfg.phoneWidth == 480){
                        convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter_tem_low, parent, false);
                    }else if(Cfg.phoneWidth == 1440){
                        convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter_tem_high, parent, false);
                    }else {
                        convertView = getLayoutInflater().inflate(R.layout.activity_device_adapter_tem, parent, false);
                    }

                    tempteratureText = (TextView) convertView.findViewById(R.id.adapterValueTem);
                    hygrometerText = (TextView) convertView.findViewById(R.id.adapterValueDam);

                    if (currentData.getTemperature().length() > 3) {
                        tempInt = Integer.parseInt(currentData.getTemperature().substring(0
                                , currentData.getTemperature().length() - 3));
                        tempFloat = ((float) tempInt) / 10;
                        temperature = String.valueOf(tempFloat);
                        hygrometer = currentData.getTemperature().substring(currentData.getTemperature().length() - 3);

                        //��ǰ���0���ɿո�
                        byte[] byteHygrometer = hygrometer.getBytes();
                        for (int i = 0; i < hygrometer.length(); i++) {
                            if (byteHygrometer[i] == '0') {
                                byteHygrometer[i] = ' ';
                            } else {
                                break;
                            }
                        }
                        hygrometer = new String(byteHygrometer);

                    } else if (currentData.getTemperature().length() <= 3) {
                        temperature = "0";
                        hygrometer = currentData.getTemperature();
                    }
                    if(is_device_online) {
                        tempteratureText.setText(temperature);
                        hygrometerText.setText(hygrometer);
                    }
                    else{
                        tempteratureText.setText("");
                        hygrometerText.setText("");
                    }
                }
            }
            return convertView;
        }
    }
    // ��ȡ���listView�¼�
    private final class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            Bundle bundleData = new Bundle();


            if(setListTypeByDeviceType(position) == LIST_HCHO){
                bundleData.putString("dataName", "hcho");
            }
            else if(setListTypeByDeviceType(position) == LIST_PM2_5){
                bundleData.putString("dataName", "pm2_5");
            }else if(setListTypeByDeviceType(position) == LIST_PM10){
                bundleData.putString("dataName", "pm10");
            }else if(setListTypeByDeviceType(position) == LIST_TVOC){
                bundleData.putString("dataName", "tvoc");
            }
            else{
                Toast.makeText(getApplicationContext(), "���ʹ���", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            Intent tempIntent = new Intent();
            tempIntent.setClass(DeviceDataViewActivity.this, historyDataActivity.class);
            tempIntent.putExtras(bundleData);
            startActivity(tempIntent);

        }
    }

//        @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_hcho_monitor, menu);
//        return true;
//    }
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

    //��ȡ��ǰ����
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
        //��Ҫ�жϷ������Ƿ���
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
                //���ʱ���೬��10����,��Ϊ�豸������
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
                    return  GET_CURRENT_FAIL;
                }

                return GET_CURRENT_SUCCED;
            default:
                return  CHOSE_DEVICE_NULL;
        }

    }

}
