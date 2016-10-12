package com.meishipintu.assistant.interfaces;

import com.meishipintu.assistant.bean.MiActive;
import com.milai.model.Coupons;

import java.util.List;

/**
 * Created by Administrator on 2016/9/7.
 *
 * 在Activity实现过使用卡券和米后调用此接口刷新数据，在fragment中实现
 */
public interface OnRefreshingCouponOrMi {

    void onRefreshingCoupon(String couponArrayString);

    void onRefreshingMi(int totalMi);
}
