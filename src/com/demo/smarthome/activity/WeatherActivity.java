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
*   获取所在城市的功能使用的是高德API，高德的登录名为shileifavorite@163.com 密码是130890。
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

    //声明AMapLocationClient类对象
    protected AMapLocationClient mLocationClient;
    //声明定位回调监听器
    protected AMapLocationListener mLocationListener;
    //声明mLocationOption对象
    protected AMapLocationClientOption mLocationOption = null;
    //定位错误信息
    private String ErrorInfo = " 获取天气信息失败";

    //服务器获取数据
    WeatherInfo weatherInfoText;
//    Gson gson;
//    WeatherInfoDataResult weatherData;
//    String jsonResult;

    //纬度
//    protected String mLongitude ="";
    //经度
//    protected String mLatitude = "";
    //省
    protected String mProvince = "";
    //城市
    protected String mCity = "";
    //区县
    protected String mDistrict = "";

    //警告框
    AlertDialog.Builder failAlert;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            dialogView.closeMyDialog();
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_WEATHER_SUCCEED:
                    StringBuffer cityTitle = new StringBuffer();
                    cityTitle.append(mCity + "市\n");
                    cityTitle.append(weatherInfoText.getWeatherInfo() + "(未付费)");
                    ((TextView)findViewById(R.id.TempText)).setText(cityTitle.toString());
                    StringBuffer weatherText = new StringBuffer();
                    weatherText.append("温度:" +weatherInfoText.getTemperature() +"(假的)\n");
                    weatherText.append("湿度:" +weatherInfoText.getHumidity() +"(假的)\n");
                    weatherText.append("空气质量:(未付费)" +weatherInfoText.getAirQuality() +"\n");
                    weatherText.append("PM2.5:" +weatherInfoText.getPm2_5() +"\n");
                    weatherText.append("PM10:" +weatherInfoText.getPm10() +"\n");
                    ((TextView)findViewById(R.id.TempText2)).setText(weatherText.toString());
                    break;
                case GET_CITY_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("获取城市天气失败").setIcon(R.drawable.cloud_fail).setMessage(ErrorInfo)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case CITY_NOT_SUPPORT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("获取城市天气失败").setIcon(R.drawable.cloud_fail).setMessage(mCity + " 该城市不支持天气查询")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_JSON_ERROR:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("云服务器故障").setIcon(R.drawable.cloud_fail).setMessage(mCity + " 天气查询错误")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    failAlert.create().show();
                break;
                case SERVER_CANT_CONNECT:
                    failAlert = new AlertDialog.Builder(WeatherActivity.this);
                    failAlert.setTitle("云服务器故障").setIcon(R.drawable.cloud_fail).setMessage("请联系贝谷科技售后服务部!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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

        //不开启网络就终止程序
        if(!NetworkStatusTools.isNetworkAvailable(WeatherActivity.this)){
            failAlert = new AlertDialog.Builder(WeatherActivity.this);
            failAlert.setTitle("无法连接到网络").setIcon(R.drawable.cloud_fail).setMessage("请确定是否连接了网络")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
    //初始化定位
    protected void initLoaction(){
        mLocationClient = null;
        mLocationListener = new AMapLocationListener() {
            //定位回调监听，当定位完成后调用此方法
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                getLocationResult(aMapLocation);
            }
        };
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        //高精度定位模式：会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
        //低功耗定位模式：不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
        //仅用设备定位模式：不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。

        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是只定位一次
        mLocationOption.setOnceLocation(true);


        //设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
//        mLocationOption.setGpsFirst(true);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        //等待框
        dialogView = new MyDialogView(WeatherActivity.this);
        dialogView.showMyDialog("正在定位", "正在定位");
    }

    //获取定位结果
    public void getLocationResult(AMapLocation amapLocation) {

        Message msg = new Message();

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息

                mProvince = amapLocation.getProvince();//省信息
                //城市信息 (如果带"市"字去掉 "市")
                if(amapLocation.getCity().contains("市")) {
                    mCity = (amapLocation.getCity().split("市"))[0];//城市信息 (去掉 "市")
                }else{
                    mCity = amapLocation.getCity();
                }
                mDistrict = amapLocation.getDistrict();//城区信息
//                mLatitude = String.valueOf(amapLocation.getLatitude());//获取纬度
//                mLongitude = String.valueOf(amapLocation.getLongitude());//获取经度
//                amapLocation.getLocationType();//获取当前定位结果来源
//                amapLocation.getAccuracy();//获取精度信息
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(amapLocation.getTime());
//                df.format(date);//定位时间
//                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                amapLocation.getCountry();//国家信息

//                amapLocation.getStreet();//街道信息
//                amapLocation.getStreetNum();//街道门牌号信息
//                amapLocation.getCityCode();//城市编码
//                amapLocation.getAdCode();//地区编码
                //定位信息调试信息
                String result = getLocationStr(amapLocation);
                Log.i(TAG, result);
                //如果定位到的城市不支持天气查询
                if(WeatherInformationTools.isCitySupportGetWeather(mCity) == false){
                    msg.what = CITY_NOT_SUPPORT;
                    handler.sendMessage(msg);
                    return;
                }
                //定位城市成功后从服务器获取数据
                new getWeatherFromServerThread().start();
            } else {
                StringBuffer sb = new StringBuffer();
                //定位失败
                sb.append("定位失败" + "\n");
                sb.append("错误码:" + amapLocation.getErrorCode() + "\n");
                sb.append("错误信息:" + amapLocation.getErrorInfo() + "\n");
                sb.append("错误描述:" + amapLocation.getLocationDetail() + "\n");
                ErrorInfo = sb.toString();
                Log.e(TAG,ErrorInfo);
                msg.what = GET_CITY_ERROR;
                handler.sendMessage(msg);
            }
        }

    }
    //获取当前数据
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

            //需要判断服务器是否开启
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
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationClient = null;
        }
    }

    /**
     * 根据定位结果返回定位信息的字符串
     * @param
     * @return
     */
    private synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if(location.getErrorCode() == 0){
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // 以下信息只有提供者是GPS时才会有
                sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                sb.append("角    度    : " + location.getBearing() + "\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : "
                        + location.getSatellites() + "\n");
            } else {
                // 提供者是GPS时是没有以下信息的
                sb.append("国    家    : " + location.getCountry() + "\n");
                sb.append("省            : " + location.getProvince() + "\n");
                sb.append("市            : " + location.getCity() + "\n");
                sb.append("城市编码 : " + location.getCityCode() + "\n");
                sb.append("区            : " + location.getDistrict() + "\n");
                sb.append("区域 码   : " + location.getAdCode() + "\n");
                sb.append("地    址    : " + location.getAddress() + "\n");
                sb.append("兴趣点    : " + location.getPoiName() + "\n");
            }
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        return sb.toString();
    }
}
