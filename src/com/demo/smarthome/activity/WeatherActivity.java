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
*   ?????????��????????????API???????????shileifavorite@163.com ??????130890??
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

    //????AMapLocationClient?????
    protected AMapLocationClient mLocationClient;
    //??????��?????????
    protected AMapLocationListener mLocationListener;
    //????mLocationOption????
    protected AMapLocationClientOption mLocationOption = null;
    //??��???????
    private String ErrorInfo = " ?????????????";

    //?????????????
    WeatherInfo weatherInfoText;
//    Gson gson;
//    WeatherInfoDataResult weatherData;
//    String jsonResult;

    //��??
//    protected String mLongitude ="";
    //????
//    protected String mLatitude = "";
    //?
    protected String mProvince = "";
    //????
    protected String mCity = "";
    //????
    protected String mDistrict = "";

    //?????
    AlertDialog.Builder failAlert;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            dialogView.closeMyDialog();
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_WEATHER_SUCCEED:
                    StringBuffer cityTitle = new StringBuffer();
                    cityTitle.append(mCity + "??\n");
                    cityTitle.append(weatherInfoText.getWeatherInfo() + "(��????)");
                    ((TextView)findViewById(R.id.TempText)).setText(cityTitle.toString());
                    StringBuffer weatherText = new StringBuffer();
                    weatherText.append("???:" +weatherInfoText.getTemperature() +"(???)\n");
                    weatherText.append("???:" +weatherInfoText.getHumidity() +"(???)\n");
                    weatherText.append("????????:(��????)" +weatherInfoText.getAirQuality() +"\n");
                    weatherText.append("PM2.5:" +weatherInfoText.getPm2_5() +"\n");
                    weatherText.append("PM10:" +weatherInfoText.getPm10() +"\n");
                    ((TextView)findViewById(R.id.TempText2)).setText(weatherText.toString());
                    break;
                case GET_CITY_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("??????????????").setIcon(R.drawable.cloud_fail).setMessage(ErrorInfo)
                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case CITY_NOT_SUPPORT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("??????????????").setIcon(R.drawable.cloud_fail).setMessage(mCity + " ?��??��???????????")
                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_JSON_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("???????????").setIcon(R.drawable.cloud_fail).setMessage(mCity + " ???????????")
                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_CANT_CONNECT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("???????????").setIcon(R.drawable.cloud_fail).setMessage("?????????????????!")
                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
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

        //??????????????????
        if(!NetworkStatusTools.isNetworkAvailable(WeatherActivity.this)){
            failAlert = new AlertDialog.Builder(WeatherActivity.this);
            failAlert.setTitle("????????????").setIcon(R.drawable.cloud_fail).setMessage("??????????????????")
                    .setPositiveButton("???", new DialogInterface.OnClickListener() {
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
    //???????��
    protected void initLoaction(){
        mLocationClient = null;
        mLocationListener = new AMapLocationListener() {
            //??��?????????????��???????????
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                getLocationResult(aMapLocation);
            }
        };
        //???????��
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //?????��???????
        mLocationClient.setLocationListener(mLocationListener);

        //???????��????
        mLocationOption = new AMapLocationClientOption();
        //?????��????????????Battery_Saving??????????Device_Sensors????��??
        //??????��??????????????�N��??GPS??��?????????????????��?????
        //??????��???????????GPS???????????�N��??Wi-Fi??????��????
        //?????��??��????????????????��????GPS???��?��????????????????????????��??

        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        //????????????????????????????
        mLocationOption.setNeedAddress(true);
        //?????????��???
        mLocationOption.setOnceLocation(true);


        //??????????????GPS??��????????30????GPS??��????��???????????�N��
//        mLocationOption.setGpsFirst(true);

        //????��?????????????��????
        mLocationClient.setLocationOption(mLocationOption);
        //??????��
        mLocationClient.startLocation();

        //?????
        dialogView = new MyDialogView(WeatherActivity.this);
        dialogView.showMyDialog("?????��", "?????��");
    }

    //?????��???
    public void getLocationResult(AMapLocation amapLocation) {

        Message msg = new Message();

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //??��?????????????????????

                mProvince = amapLocation.getProvince();//????
                //??????? (?????"??"????? "??")
                if(amapLocation.getCity().contains("??")) {
                    mCity = (amapLocation.getCity().split("??"))[0];//??????? (??? "??")
                }else{
                    mCity = amapLocation.getCity();
                }
                mDistrict = amapLocation.getDistrict();//???????
//                mLatitude = String.valueOf(amapLocation.getLatitude());//???��??
//                mLongitude = String.valueOf(amapLocation.getLongitude());//???????
//                amapLocation.getLocationType();//????????��??????
//                amapLocation.getAccuracy();//??????????
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(amapLocation.getTime());
//                df.format(date);//??��???
//                amapLocation.getAddress();//????????option??????isNeedAddress?false??????��????????�N��????��??��???????GPS??��?????????????
//                amapLocation.getCountry();//???????

//                amapLocation.getStreet();//??????
//                amapLocation.getStreetNum();//???????????
//                amapLocation.getCityCode();//???��???
//                amapLocation.getAdCode();//????????
                //??��??????????
                String result = getLocationStr(amapLocation);
                Log.i(TAG, result);
                //?????��??????��???????????
                if(WeatherInformationTools.isCitySupportGetWeather(mCity) == false){
                    msg.what = CITY_NOT_SUPPORT;
                    handler.sendMessage(msg);
                    return;
                }
                //??��???��?????????????????
                new getWeatherFromServerThread().start();
            } else {
                StringBuffer sb = new StringBuffer();
                //??��???
                sb.append("??��???" + "\n");
                sb.append("??????:" + amapLocation.getErrorCode() + "\n");
                sb.append("???????:" + amapLocation.getErrorInfo() + "\n");
                sb.append("????????:" + amapLocation.getLocationDetail() + "\n");
                ErrorInfo = sb.toString();
                Log.e(TAG,ErrorInfo);
                msg.what = GET_CITY_ERROR;
                handler.sendMessage(msg);
            }
        }

    }
    //??????????
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

            //????��???????????
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
             * ???AMapLocationClient??????Activity????????
             * ??Activity??onDestroy?????????AMapLocationClient??onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationClient = null;
        }
    }

    /**
     * ?????��????????��??????????
     * @param
     * @return
     */
    private synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode????0????��??????????????��????????????????????��?????????
        if(location.getErrorCode() == 0){
            sb.append("??��???" + "\n");
            sb.append("??��????: " + location.getLocationType() + "\n");
            sb.append("??    ??    : " + location.getLongitude() + "\n");
            sb.append("��    ??    : " + location.getLatitude() + "\n");
            sb.append("??    ??    : " + location.getAccuracy() + "??" + "\n");
            sb.append("????    : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // ????????????????GPS??????
                sb.append("??    ??    : " + location.getSpeed() + "??/??" + "\n");
                sb.append("??    ??    : " + location.getBearing() + "\n");
                // ??????????��????????????
                sb.append("??    ??    : "
                        + location.getSatellites() + "\n");
            } else {
                // ??????GPS???????????????
                sb.append("??    ??    : " + location.getCountry() + "\n");
                sb.append("?            : " + location.getProvince() + "\n");
                sb.append("??            : " + location.getCity() + "\n");
                sb.append("???��??? : " + location.getCityCode() + "\n");
                sb.append("??            : " + location.getDistrict() + "\n");
                sb.append("???? ??   : " + location.getAdCode() + "\n");
                sb.append("??    ?    : " + location.getAddress() + "\n");
                sb.append("?????    : " + location.getPoiName() + "\n");
            }
        } else {
            //??��???
            sb.append("??��???" + "\n");
            sb.append("??????:" + location.getErrorCode() + "\n");
            sb.append("???????:" + location.getErrorInfo() + "\n");
            sb.append("????????:" + location.getLocationDetail() + "\n");
        }
        return sb.toString();
    }
}
