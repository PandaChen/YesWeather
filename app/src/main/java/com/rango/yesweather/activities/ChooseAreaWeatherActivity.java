package com.rango.yesweather.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rango.yesweather.R;
import com.rango.yesweather.service.AutoUpdateService;
import com.rango.yesweather.util.HttpCallbackListener;
import com.rango.yesweather.util.HttpUtil;
import com.rango.yesweather.util.Utility;

public class ChooseAreaWeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfo_ly;
    //显示城市名
    private TextView cityName_tv;
    //显示发布时间
    private TextView publishTime_tv;
    //显示天气描述
    private TextView weatherDesp_tv;
    //显示最高气温
    private TextView temperature1_tv;
    //显示最低气温
    private TextView temperature2_tv;
    //显示当前日期
    private TextView currentDate_tv;
    //切换城市
    private Button changeCity_btn;
    //更新天气
    private Button refreshWeather_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area_weather_ly);

        weatherInfo_ly = (LinearLayout) findViewById(R.id.weather_info_ly);
        cityName_tv = (TextView) findViewById(R.id.choose_area_tv);
        publishTime_tv = (TextView) findViewById(R.id.publish_time_tv);
        weatherDesp_tv = (TextView) findViewById(R.id.weather_desp_tv);
        temperature1_tv = (TextView) findViewById(R.id.temperature1_tv);
        temperature2_tv = (TextView) findViewById(R.id.temperature2_tv);
        currentDate_tv = (TextView) findViewById(R.id.current_date_tv);
        changeCity_btn = (Button) findViewById(R.id.change_city_btn);
        refreshWeather_btn = (Button) findViewById(R.id.refresh_weather_btn);
        String countyCode = getIntent().getStringExtra("county_code");

        refreshWeather_btn.setOnClickListener(this);
        changeCity_btn.setOnClickListener(this);

        if (!TextUtils.isEmpty(countyCode)) {
            // 有县级代号时就去查询天气
            publishTime_tv.setText("同步中...");
            weatherInfo_ly.setVisibility(View.INVISIBLE);
            cityName_tv.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
        //switchCity.setOnClickListener(this);
        //refreshWeather.setOnClickListener(this);
    }

    /*
    查询县级代号所对应的天气代号
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气。
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /*
    根据传入的地址和类型去向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理返回的天气信息
                    //Log.d("weatherCode", "" + address);
                    Utility.handleWeatherResponse(ChooseAreaWeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishTime_tv.setText("同步失败");
                    }
                });
            }
        });
    }

    /*
    从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityName_tv.setText(preferences.getString("city_name", ""));
        temperature1_tv.setText(preferences.getString("temperature1", ""));
        temperature2_tv.setText(preferences.getString("temperature2", ""));
        weatherDesp_tv.setText(preferences.getString("weather_desp", ""));
        publishTime_tv.setText("今天" + preferences.getString("publish_time", "") + "发布");
        currentDate_tv.setText(preferences.getString("current_date", ""));
        weatherInfo_ly.setVisibility(View.VISIBLE);
        cityName_tv.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_city_btn:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather_btn:
                publishTime_tv.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

}
