package com.meishipintu.core.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA
 * User: Sirius
 * Date: 2015/6/19
 * Time: 15:20
 */
public class CashierSign {

	//业务demo，收银测试环境
    public static final String InvokeCashier_BPID="576243d491b9b216876a36b3";
    public static final String InvokeCashier_KEY="VoHPWeaDcjbpbZjdSmmLjvlSPqhGMu65";

    private static final String Tag="CashierSign";

    private static final String SignType="MD5";
    private static final String inputCharset="UTF-8";

    public static byte[] sign(String bpId , String invokeCashierKey, String channel, String payType,
                              String outTradeNo, String body, String attach, String feeType, String totalFee, Context context)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Map<String,String> dataMap=new HashMap<String, String>();
        dataMap.put("bp_id",bpId);
        dataMap.put("channel",channel);
        dataMap.put("payType",payType);
        dataMap.put("out_trade_no",outTradeNo);
        dataMap.put("body",body);
        dataMap.put("attach",attach);
        dataMap.put("fee_type",feeType);
        dataMap.put("total_fee",totalFee);
        dataMap.put("input_charset",inputCharset);
        dataMap.put("notify_url","http://apps.weipass.cn/pay/notify");
        dataMap.put("package",context.getPackageName());
        dataMap.put("classpath","com.meishipintu.assistant.ui.pay.ActNewPayment");


        String sign=getSign(invokeCashierKey,dataMap);
        dataMap.put("sign",sign);

        JSONObject json=new JSONObject(dataMap);
        return json.toString().getBytes(inputCharset);
    }

    public static byte[] sign(String bpId , String invokeCashierKey,
                              String outTradeNo, String cashierTradeNo, Context context)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Map<String,String> dataMap=new HashMap<String, String>();
        dataMap.put("bp_id",bpId);
        dataMap.put("out_trade_no",outTradeNo);
        dataMap.put("cashier_trade_no", cashierTradeNo);
        dataMap.put("input_charset",inputCharset);
        dataMap.put("action", "refund");
        dataMap.put("package",context.getPackageName());
        dataMap.put("classpath","com.meishipintu.assistant.ui.pay.ActPaymentDetail");


        String sign=getSign(invokeCashierKey,dataMap);
        dataMap.put("sign",sign);

        JSONObject json=new JSONObject(dataMap);
        return json.toString().getBytes(inputCharset);
    }

    private static String getSign(String invokeCashierKey, Map<String,String> dataMap) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        List<String> keyList=new ArrayList<String>(dataMap.keySet());
        Collections.sort(keyList);
        StringBuilder builder=new StringBuilder();
        for (String mapKey:keyList){
            builder.append(mapKey).append("=").append(dataMap.get(mapKey)).append("&");
        }
        builder.append("key=").append(invokeCashierKey);
        Log.i(Tag,"MD5加密前-->"+builder);
        MessageDigest md5= MessageDigest.getInstance(SignType);
        md5.update(builder.toString().getBytes(inputCharset));
        byte[] md5Bytes=md5.digest();
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        Log.i(Tag,"MD5加密后-->"+hexValue);
        return hexValue.toString();
    }
}
