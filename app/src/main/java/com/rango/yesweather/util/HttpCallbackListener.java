package com.rango.yesweather.util;

/**
 * Created by Rango on 2016/2/11.
 */
public interface HttpCallbackListener {

    /*
    请求完成时返回
     */
    void onFinish(String response);

    /*
    请求出错时返回
     */
    void onError(Exception e);
}
