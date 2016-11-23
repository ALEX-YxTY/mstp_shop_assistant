package com.meishipintu.assistant.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterCoupons;
import com.meishipintu.assistant.interfaces.OnRefreshingCouponOrMi;
import com.milai.model.Coupons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/7.
 */
public class CouponFrag extends Fragment implements OnRefreshingCouponOrMi{

    private ArrayList<Coupons> couponArray;
    private AdapterCoupons adapter;
    private ListView lv;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_list, container, false);
        lv = (ListView) view.findViewById(R.id.lv_list);
        Bundle bundle = getArguments();
        if (bundle != null) {
            couponArray = getCouponList(bundle.getString("couponArray"));
            adapter = new AdapterCoupons(getActivity(),couponArray,0,bundle.getString("mTel"));//isFrom=0 从vip界面进入
            lv.setAdapter(adapter);
        }
        return view;
    }


    private ArrayList<Coupons> getCouponList(String couponArray) {
        ArrayList<Coupons> couponsArrayList = new ArrayList<Coupons>();
        try {
            JSONArray arrayJsonCoupon = new JSONArray(couponArray);
            for (int i = 0; i < arrayJsonCoupon.length(); i++) {
                JSONObject coupon = arrayJsonCoupon.getJSONObject(i);
                Coupons couponMode = null;
                try {
                    couponMode = new Coupons(coupon);
                } catch (Exception e) {
                    Log.i("test", "数据解析错误1");
                    e.printStackTrace();
                }
                if (couponMode.getMinPrice().equals("0")) {
                    couponsArrayList.add(i, couponMode);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("test", "数据解析错误2");
        }
        return couponsArrayList;
    }

    @Override
    public void onRefreshingCoupon(String couponArrayString) {
        //使用notifyDataSetChanged不能改变原数据指向的内存地址，如果改变则notify无效，因此只能通过
        //clear()和addAll()的方法更换数据
        couponArray.clear();
        couponArray.addAll(getCouponList(couponArrayString));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshingMi(int totalMi) {
    }
}
