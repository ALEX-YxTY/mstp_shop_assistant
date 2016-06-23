package com.meishipintu.assistant.dao;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.meishipintu.assistant.bean.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class NetDataHelper {
    private static NetDataHelper dataHelper;
    private static RequestQueue mQueue;
    private static Gson gson;
    private Context context;
    //TODO 更改url
    public static String domain = "http://b.milaipay.com/";

    private NetDataHelper(Context context) {
        mQueue = Volley.newRequestQueue(context);
        gson = new Gson();
        this.context = context;
    }

    public static synchronized NetDataHelper getInstance(Context context) {
        if (dataHelper == null)
            dataHelper = new NetDataHelper(context.getApplicationContext());
        return dataHelper;
    }

    /**
     * 获取版本信息，无返回值，通过回调拿到版本信息类
     * @param callBack
     */
    public void getVersion(final NetCallBack<VersionInfo> callBack) {
        String url = domain + "test/getSystem";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 1) {
                        VersionInfo versionInfo = gson.fromJson(response.getJSONObject("data").toString()
                                , VersionInfo.class);
                        callBack.onSuccess(versionInfo);
                    } else {
                        callBack.onError("网络连接错误");
                    }
                } catch (JSONException e) {
                    callBack.onError("网络连接错误");
                }

            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError("网络连接错误");
            }
        });

        mQueue.add(request);
    }


}
