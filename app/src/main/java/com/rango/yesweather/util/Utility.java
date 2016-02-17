package com.rango.yesweather.util;

import android.text.TextUtils;

import com.rango.yesweather.db.YesWeatherDB;
import com.rango.yesweather.model.City;
import com.rango.yesweather.model.County;
import com.rango.yesweather.model.Province;

/**
 * Created by Rango on 2016/2/11.
 */
public class Utility {

    /*
    处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(YesWeatherDB yesWeatherDB, String response) {

        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            Province province = null;
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    yesWeatherDB.saveProvince(province);
                }
            }
            return true;
        }

        return false;
    }

    /*
    处理服务器返回的市级数据
     */
    public synchronized static boolean handleCitiesResponse(YesWeatherDB yesWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            City city = null;
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到City表
                    yesWeatherDB.saveCity(city);
                }
            }
            return true;
        }

        return false;
    }

    /*
    处理服务器返回的县城数据
     */
    public synchronized static boolean handleCountiesResponse(YesWeatherDB yesWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            County county = null;
            if (allCounties != null && allCounties.length > 0){
                for (String c : allCounties){
                    String[] array = c.split("\\|");
                    county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的的数据存储到Country表
                    yesWeatherDB.saveCounty(county);
                }
            }
            return true;
        }
        return  false;
    }

}
