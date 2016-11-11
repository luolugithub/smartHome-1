package com.begood.smarthome.tools;

import com.begood.smarthome.staticString.StringRes;

/**
 * Created by leishi on 16/1/27.
 */
public class WeatherInformationTools {
    //
    public static boolean isCitySupportGetWeather(String city){

        for (String tempCity: StringRes.weatherSupportCity) {
            if(tempCity.equals(city)){
                return true;
            }
        }
        return false;
    }
}
