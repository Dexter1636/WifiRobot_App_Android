package org.blackant.wifirobotappandroid.utilities;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Json请求工具类
 */
// TODO: 19-5-15 fix the class
public class JsonUtils {

    public static final String TAG = "JsonUtils";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    /**
     * 给服务端传递一个json串
     * @param json
     * @param url
     * @return
     */
    public static String postJson(String json, String url) {
        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = MyOkHttpUtils.getInstance().getOkHttpClient();
        //创建一个RequestBody(参数1：数据类型 参数2：传递的json串)
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        try {
            //创建一个请求对象
            Request request = new Request.Builder()
//                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(url)
                .post(requestBody)
                .build();
            //发送请求获取响应
            Response response = okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()) {
                String strResponse = response.body().string();
                //打印服务端返回结果
                Log.i(TAG, strResponse);
                return strResponse;
            }
            else {
                String strResponse = "Network Error";
                //打印错误消息
                Log.i(TAG, strResponse);
                return strResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Failed to connect to " + url);
        }
        return "Network Error";
    }


    public static String get(String url) {
        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = MyOkHttpUtils.getInstance().getOkHttpClient();
        try {
            //创建一个请求对象
            Request request = new Request.Builder()
                    .url(url)
                    .get()//默认就是GET请求，可以不写
                    .build();
            //发送请求获取响应
            Response response = okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()) {
                String strResponse = response.body().string();
                //打印服务端返回结果
                Log.i(TAG, strResponse);
                return strResponse;
            }
            else {
                String strResponse = "Network Error";
                //打印错误消息
                Log.i(TAG, strResponse);
                return strResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Failed to connect to " + url);
        }
        return "Network Error";
    }
}
