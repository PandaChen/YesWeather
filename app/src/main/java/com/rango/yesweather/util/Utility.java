package com.rango.yesweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.rango.yesweather.db.YesWeatherDB;
import com.rango.yesweather.model.City;
import com.rango.yesweather.model.County;
import com.rango.yesweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    /*
    解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     */
    public static void handleWeatherResponse(Context context,String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temperature1 = weatherInfo.getString("temp1");
            String temperature2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temperature1,temperature2,weatherDesp,publishTime);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /*
    将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temperature1, String temperature2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temperature1", temperature1);
        editor.putString("temperature2", temperature2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

}
