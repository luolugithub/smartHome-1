package com.demo.smarthome.tools;

import com.demo.smarthome.staticString.StringRes;

/**
 * Created by leishi on 16/1/27.
 */
public class WeatherInformationTools {
    //�ó����Ƿ�֧��������ѯ
    public static boolean isCitySupportGetWeather(String city){

        for (String tempCity: StringRes.weatherSupportCity) {
            if(tempCity.equals(city)){
                return true;
            }
        }
        return false;
    }
}
