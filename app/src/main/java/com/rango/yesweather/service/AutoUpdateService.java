package com.rango.yesweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rango.yesweather.receiver.AutoUpdateReceiver;
import com.rango.yesweather.util.HttpCallbackListener;
import com.rango.yesweather.util.HttpUtil;
import com.rango.yesweather.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        //定时器
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //八小时刷新一次
        int time = 8 * 60 * 60 * 1000;
        //int time = 30 * 1000; //测试时间30s
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this,AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    /*
    更新天气信息
    */
    private void updateWeather() {
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = preferences.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this,response);
                //Log.d("AutoUpdate", "UpdateTime: " + sdf.format(new Date(System.currentTimeMillis())));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
