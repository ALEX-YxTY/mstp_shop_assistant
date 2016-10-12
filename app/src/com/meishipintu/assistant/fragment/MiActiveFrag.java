package com.meishipintu.assistant.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterMiActive;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.bean.MiActive;
import com.meishipintu.assistant.interfaces.OnRefreshingCouponOrMi;
import com.meishipintu.assistant.interfaces.OnUseCouponOrMi;
import com.meishipintu.core.utils.MyDialogUtil;
import com.meishipintu.core.utils.ToastUtils;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milaifucai.asynctask.PostGetTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/7.
 */
public class MiActiveFrag extends Fragment implements OnRefreshingCouponOrMi{

    private ArrayList<MiActive> dataList;
    private AdapterMiActive adapter;
    private ListView lv;
    private int totalMi;
    private String mTel;
    private OnUseCouponOrMi onUseCouponOrMiListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onUseCouponOrMiListener = (OnUseCouponOrMi) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_list, container, false);
        lv = (ListView) view.findViewById(R.id.lv_list);
        totalMi = getArguments().getInt("totalMi");
        mTel = getArguments().getString("mobile");
        try {
            JSONArray activeArray = new JSONArray(getArguments().getString("activeArray"));
            dataList = new ArrayList<MiActive>();
            Gson gson = new Gson();
            for(int i=0;i<activeArray.length();i++) {
                MiActive active = gson.fromJson(activeArray.get(i).toString(), MiActive.class);
                dataList.add(active);
            }
            adapter = new AdapterMiActive(getActivity(), dataList, totalMi);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i("test", "click:" + position);
                    if (dataList.get(position).getRice() <= totalMi) {
                        MiActive active = dataList.get(position);
                        showDialogUse(active.getRice(), active.getId());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onRefreshingCoupon(String couponArrayString) {
    }

    @Override
    public void onRefreshingMi(int totalMi) {
        this.totalMi = totalMi;
        adapter.setTotalMi(totalMi);
        adapter.notifyDataSetChanged();
//        adapter = new AdapterMiActive(getActivity(), dataList, totalMi);
//        lv.setAdapter(adapter);
    }

    //显示使用卡券弹窗
    private void showDialogUse(final int totoalMi, final int activeId) {
        // 显示是否验证对话框
        MyDialogUtil qDialog = new MyDialogUtil(getContext()) {
            @Override
            public void onClickPositive() {
                //调用用米兑换接口
                useMiNet(activeId);
            }

            @Override
            public void onClickNagative() {
            }
        };
        qDialog.showCustomMessage(getContext().getString(R.string.notice), "是否确定用米兑换此产品？",
                "确定","取消");
    }

    //调用接口兑换商品
    private void useMiNet(final int activeId) {
        new PostGetTask<JSONObject>(getActivity(), R.string.communicating
                , R.string.fail_communicate, true, true, false) {

            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("shop_id", Cookies.getShopId()+"");
                map.put("activity_id", activeId+"");
                map.put("mobile", mTel);
                JSONObject jRet = HttpMgr.getInstance().postMap(ServerUrlConstants.useMiActivity(), map, true);
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject jsonObject) {
                if (exception == null && jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") != 1) {
                            //返回错误信息
                            ToastUtils.selfToastShow(getActivity(), jsonObject.getString("msg"));
                        } else {
                            //调用成功
                            MyDialogUtil dialog = new MyDialogUtil(getContext()) {
                                @Override
                                public void onClickPositive() {
                                    onUseCouponOrMiListener.onUseMi();
                                }

                                @Override
                                public void onClickNagative() {
                                }
                            };
                            dialog.showCustomMessageOK(getContext().getString(R.string.notice), "活动兑换成功", "确定");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.selfToastShow(getActivity(), "解析用户数据错误");
                    }
                }
            }
        }.execute();

    }
}
