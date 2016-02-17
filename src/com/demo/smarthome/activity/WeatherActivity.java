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
import android.view.View;
import android.widget.TextView;
import com.demo.smarthome.R;

import java.util.Date;
import java.text.SimpleDateFormat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.NetworkStatusTools;
import com.demo.smarthome.tools.WeatherInformationTools;
import com.demo.smarthome.view.MyDialogView;
import com.demo.smarthome.weather.WeatherInfo;
import com.demo.smarthome.weather.WeatherInfoDataResult;
import com.google.gson.Gson;

/**
*   ��ȡ���ڳ��еĹ���ʹ�õ��Ǹߵ�API���ߵµĵ�¼��Ϊshileifavorite@163.com ������130890��
*
 * */
public class WeatherActivity extends Activity {

    private final String TAG = "WeatherActivity";
    static final int GET_WEATHER_SUCCEED      = 0;
    static final int GET_CITY_ERROR       = 1;
    static final int CITY_NOT_SUPPORT     = 2;
    static final int SERVER_JSON_ERROR    = 8;
    static final int SERVER_CANT_CONNECT  = 9;
    MyDialogView dialogView;

    //����AMapLocationClient�����
    protected AMapLocationClient mLocationClient;
    //������λ�ص�������
    protected AMapLocationListener mLocationListener;
    //����mLocationOption����
    protected AMapLocationClientOption mLocationOption = null;
    //��λ������Ϣ
    private String ErrorInfo = " ��ȡ������Ϣʧ��";

    //��������ȡ����
    WeatherInfo weatherInfoText;
//    Gson gson;
//    WeatherInfoDataResult weatherData;
//    String jsonResult;

    //γ��
//    protected String mLongitude ="";
    //����
//    protected String mLatitude = "";
    //ʡ
    protected String mProvince = "";
    //����
    protected String mCity = "";
    //����
    protected String mDistrict = "";

    //�����
    AlertDialog.Builder failAlert;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            dialogView.closeMyDialog();
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_WEATHER_SUCCEED:
                    StringBuffer cityTitle = new StringBuffer();
                    cityTitle.append(mCity + "��\n");
                    cityTitle.append(weatherInfoText.getWeatherInfo() + "(δ����)");
                    ((TextView)findViewById(R.id.TempText)).setText(cityTitle.toString());
                    StringBuffer weatherText = new StringBuffer();
                    weatherText.append("�¶�:" +weatherInfoText.getTemperature() +"(�ٵ�)\n");
                    weatherText.append("ʪ��:" +weatherInfoText.getHumidity() +"(�ٵ�)\n");
                    weatherText.append("��������:(δ����)" +weatherInfoText.getAirQuality() +"\n");
                    weatherText.append("PM2.5:" +weatherInfoText.getPm2_5() +"\n");
                    weatherText.append("PM10:" +weatherInfoText.getPm10() +"\n");
                    ((TextView)findViewById(R.id.TempText2)).setText(weatherText.toString());
                    break;
                case GET_CITY_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("��ȡ��������ʧ��").setIcon(R.drawable.cloud_fail).setMessage(ErrorInfo)
                            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case CITY_NOT_SUPPORT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("��ȡ��������ʧ��").setIcon(R.drawable.cloud_fail).setMessage(mCity + " �ó��в�֧��������ѯ")
                            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_JSON_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("�Ʒ���������").setIcon(R.drawable.cloud_fail).setMessage(mCity + " ������ѯ����")
                            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_CANT_CONNECT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("�Ʒ���������").setIcon(R.drawable.cloud_fail).setMessage("����ϵ���ȿƼ��ۺ����!")
                            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        TextView titleText = (TextView) findViewById(R.id.titleWeatherView);
        titleText.setClickable(true);
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //�������������ֹ����
        if(!NetworkStatusTools.isNetworkAvailable(WeatherActivity.this)){
            failAlert = new AlertDialog.Builder(WeatherActivity.this);
            failAlert.setTitle("�޷����ӵ�����").setIcon(R.drawable.cloud_fail).setMessage("��ȷ���Ƿ�����������")
                    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            failAlert.create().show();
            return;
        }

        initLoaction();


    }
    //��ʼ����λ
    protected void initLoaction(){
        mLocationClient = null;
        mLocationListener = new AMapLocationListener() {
            //��λ�ص�����������λ��ɺ���ô˷���
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                getLocationResult(aMapLocation);
            }
        };
        //��ʼ����λ
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //���ö�λ�ص�����
        mLocationClient.setLocationListener(mLocationListener);

        //��ʼ����λ����
        mLocationOption = new AMapLocationClientOption();
        //���ö�λģʽΪ�߾���ģʽ��Battery_SavingΪ�͹���ģʽ��Device_Sensors�ǽ��豸ģʽ
        //�߾��ȶ�λģʽ����ͬʱʹ�����綨λ��GPS��λ�����ȷ�����߾��ȵĶ�λ�����
        //�͹��Ķ�λģʽ������ʹ��GPS��ֻ��ʹ�����綨λ��Wi-Fi�ͻ�վ��λ����
        //�����豸��λģʽ������Ҫ�������磬ֻʹ��GPS���ж�λ������ģʽ�²�֧�����ڻ����Ķ�λ��

        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        //�����Ƿ񷵻ص�ַ��Ϣ��Ĭ�Ϸ��ص�ַ��Ϣ��
        mLocationOption.setNeedAddress(true);
        //������ֻ��λһ��
        mLocationOption.setOnceLocation(true);


        //�����Ƿ����ȷ���GPS��λ��������30����GPSû�з��ض�λ�����������綨λ
//        mLocationOption.setGpsFirst(true);

        //����λ�ͻ��˶������ö�λ����
        mLocationClient.setLocationOption(mLocationOption);
        //������λ
        mLocationClient.startLocation();

        //�ȴ���
        dialogView = new MyDialogView(WeatherActivity.this);
        dialogView.showMyDialog("���ڶ�λ", "���ڶ�λ");
    }

    //��ȡ��λ���
    public void getLocationResult(AMapLocation amapLocation) {

        Message msg = new Message();

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //��λ�ɹ��ص���Ϣ�����������Ϣ

                mProvince = amapLocation.getProvince();//ʡ��Ϣ
                //������Ϣ (�����"��"��ȥ�� "��")
                if(amapLocation.getCity().contains("��")) {
                    mCity = (amapLocation.getCity().split("��"))[0];//������Ϣ (ȥ�� "��")
                }else{
                    mCity = amapLocation.getCity();
                }
                mDistrict = amapLocation.getDistrict();//������Ϣ
//                mLatitude = String.valueOf(amapLocation.getLatitude());//��ȡγ��
//                mLongitude = String.valueOf(amapLocation.getLongitude());//��ȡ����
//                amapLocation.getLocationType();//��ȡ��ǰ��λ�����Դ
//                amapLocation.getAccuracy();//��ȡ������Ϣ
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(amapLocation.getTime());
//                df.format(date);//��λʱ��
//                amapLocation.getAddress();//��ַ�����option������isNeedAddressΪfalse����û�д˽�������綨λ����л��е�ַ��Ϣ��GPS��λ�����ص�ַ��Ϣ��
//                amapLocation.getCountry();//������Ϣ

//                amapLocation.getStreet();//�ֵ���Ϣ
//                amapLocation.getStreetNum();//�ֵ����ƺ���Ϣ
//                amapLocation.getCityCode();//���б���
//                amapLocation.getAdCode();//��������
                //��λ��Ϣ������Ϣ
                String result = getLocationStr(amapLocation);
                Log.i(TAG, result);
                //�����λ���ĳ��в�֧��������ѯ
                if(WeatherInformationTools.isCitySupportGetWeather(mCity) == false){
                    msg.what = CITY_NOT_SUPPORT;
                    handler.sendMessage(msg);
                    return;
                }
                //��λ���гɹ���ӷ�������ȡ����
                new getWeatherFromServerThread().start();
            } else {
                StringBuffer sb = new StringBuffer();
                //��λʧ��
                sb.append("��λʧ��" + "\n");
                sb.append("������:" + amapLocation.getErrorCode() + "\n");
                sb.append("������Ϣ:" + amapLocation.getErrorInfo() + "\n");
                sb.append("��������:" + amapLocation.getLocationDetail() + "\n");
                ErrorInfo = sb.toString();
                Log.e(TAG,ErrorInfo);
                msg.what = GET_CITY_ERROR;
                handler.sendMessage(msg);
            }
        }

    }
    //��ȡ��ǰ����
    class getWeatherFromServerThread extends Thread {

        @Override
        public void run() {
            Message message = new Message();
            message.what = GET_WEATHER_SUCCEED;
            WeatherInfoDataResult weatherData;
            String jsonResult;
            Gson gson = new Gson();

            String[] paramsName = {"cityName"};
            String[] paramsValue = {mCity};

            setServerURL regiterUser = new setServerURL();

            //��Ҫ�жϷ������Ƿ���
            if((jsonResult = regiterUser.sendParamToServer("getWeatherInfoByCityName", paramsName
                    , paramsValue)).isEmpty()){
                message.what = SERVER_CANT_CONNECT;
                handler.sendMessage(message);
                return ;
            }
            try {
                weatherData = gson.fromJson(jsonResult, WeatherInfoDataResult.class);
            }
            catch (Exception e){
                e.printStackTrace();
                message.what = SERVER_JSON_ERROR;
                handler.sendMessage(message);
                return;
            }

            switch (Integer.parseInt(weatherData.getCode())) {
                case Cfg.CODE_SUCCESS:
                    weatherInfoText =  weatherData.getRows();
                    message.what = GET_WEATHER_SUCCEED;
                    handler.sendMessage(message);
                    break;
                case Cfg.CODE_NULL_CODE:
                    message.what = CITY_NOT_SUPPORT;
                    handler.sendMessage(message);
                    break;
                default:
                    message.what = CITY_NOT_SUPPORT;
                    handler.sendMessage(message);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mLocationClient) {
            /**
             * ���AMapLocationClient���ڵ�ǰActivityʵ�����ģ�
             * ��Activity��onDestroy��һ��Ҫִ��AMapLocationClient��onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationClient = null;
        }
    }

    /**
     * ���ݶ�λ������ض�λ��Ϣ���ַ���
     * @param
     * @return
     */
    private synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode����0����λ�ɹ���������Ϊ��λʧ�ܣ�����Ŀ��Բ��չ�����λ������˵��
        if(location.getErrorCode() == 0){
            sb.append("��λ�ɹ�" + "\n");
            sb.append("��λ����: " + location.getLocationType() + "\n");
            sb.append("��    ��    : " + location.getLongitude() + "\n");
            sb.append("γ    ��    : " + location.getLatitude() + "\n");
            sb.append("��    ��    : " + location.getAccuracy() + "��" + "\n");
            sb.append("�ṩ��    : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // ������Ϣֻ���ṩ����GPSʱ�Ż���
                sb.append("��    ��    : " + location.getSpeed() + "��/��" + "\n");
                sb.append("��    ��    : " + location.getBearing() + "\n");
                // ��ȡ��ǰ�ṩ��λ��������Ǹ���
                sb.append("��    ��    : "
                        + location.getSatellites() + "\n");
            } else {
                // �ṩ����GPSʱ��û��������Ϣ��
                sb.append("��    ��    : " + location.getCountry() + "\n");
                sb.append("ʡ            : " + location.getProvince() + "\n");
                sb.append("��            : " + location.getCity() + "\n");
                sb.append("���б��� : " + location.getCityCode() + "\n");
                sb.append("��            : " + location.getDistrict() + "\n");
                sb.append("���� ��   : " + location.getAdCode() + "\n");
                sb.append("��    ַ    : " + location.getAddress() + "\n");
                sb.append("��Ȥ��    : " + location.getPoiName() + "\n");
            }
        } else {
            //��λʧ��
            sb.append("��λʧ��" + "\n");
            sb.append("������:" + location.getErrorCode() + "\n");
            sb.append("������Ϣ:" + location.getErrorInfo() + "\n");
            sb.append("��������:" + location.getLocationDetail() + "\n");
        }
        return sb.toString();
    }
}
