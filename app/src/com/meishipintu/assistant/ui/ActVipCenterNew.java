package com.meishipintu.assistant.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterCoupons;
import com.meishipintu.assistant.adapter.AdapterViewPager;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.fragment.CouponFrag;
import com.meishipintu.assistant.fragment.MiActiveFrag;
import com.meishipintu.assistant.interfaces.OnRefreshingCouponOrMi;
import com.meishipintu.assistant.interfaces.OnUseCouponOrMi;
import com.meishipintu.core.utils.MyDialogUtil;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Coupons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActVipCenterNew extends FragmentActivity implements OnUseCouponOrMi{

    private final int REFRESHING_COUPON = 1;
    private final int REFRESHING_MI = 2;

    private TextView mTvTitle = null;
    private String mTel = "";
    private int isFromCode = 0;//0会员界面手机号进入 1。从收银进入  2.会员界面扫码进入

    private ArrayList<Coupons> couponsArray = null;

    private RadioGroup mRadioGroup;
    private ViewPager mViewpager;
    private ArrayList<Fragment> fragmentList;
    private AdapterViewPager viewPagerAdapter;
    private AdapterCoupons couponAdapter;
    private JSONArray couponArray,activeArray;

    private OnRefreshingCouponOrMi refreshingCoupon,refreshingMi;
    private boolean isRefreshing = false;
    private int refreshingType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_vip_center_new);
        fragmentList = new ArrayList<Fragment>();
        Intent inFrom = getIntent();
        isFromCode = inFrom.getIntExtra("From", 0);
        mTel = inFrom.getStringExtra("tel");

        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvTitle.setText("会员详情");
        Button btBack = (Button) findViewById(R.id.btn_back);
        btBack.setOnClickListener(mClickListener);
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_tab);
        mViewpager = (ViewPager) findViewById(R.id.listview_coupon);

        if (isFromCode == 2) {
            //扫码进入
            mRadioGroup.check(R.id.mi_active);
            mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        mRadioGroup.check(R.id.mi_active);
                    } else {
                        mRadioGroup.check(R.id.coupon);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            initFragment();
        } else if (isFromCode == 0) {
            //从会员界面输入电话号码进入
            mRadioGroup.setVisibility(View.GONE);
            mViewpager.setVisibility(View.GONE);
            getDataFromTel(inFrom);
        }
    }


    private void initFragment() {
        new PostGetTask<JSONObject>(this, R.string.loading, R.string.fail_load_more, true, true, false) {
            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("shopId", Cookies.getShopId());
                jsonParams.put("mobile", mTel);
                jsonParams.put("uid", Cookies.getUserId());
                jsonParams.put("token", Cookies.getToken());
                JSONObject jRet = null;
                jRet = HttpMgr.getInstance().postJson(
                        ServerUrlConstants.getScanVipInfo(), jsonParams, true);
//                Log.i("test", "jRet" + jRet.toString());
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject jsonObject) {
                if (exception == null && jsonObject != null) {
                    try {
                        if (jsonObject.getInt("result") == 1) {
                            JSONObject data1 = jsonObject.getJSONObject("data1");
                            JSONObject data2 = jsonObject.getJSONObject("data2");
                            if (data1.getInt("result") == 1) {
                                int items = data1.getInt("items");
                                String name = data1.getString("name");
                                int sex = data1.getInt("sex");
                                String amount = data1.getString("amount");
                                int miTotal = 0;
                                if (data1.getJSONObject("signinfo").getInt("result") == 1) {
                                    miTotal = data1.getJSONObject("signinfo").getInt("credit");
                                }
                                couponArray = data1.getJSONArray("list");
                                if (data2.getInt("result") == 1) {
                                    activeArray = data2.getJSONArray("list");
                                }
                                //刷新显示
                                refreshView(mTel, name, sex, items, amount, miTotal);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ActVipCenterNew.this, "解析用户数据错误", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ActVipCenterNew.this, "网络连接失败，请检查网络配置", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }

    //获取intent传来的信息，
    private void getDataFromTel(Intent data)
    {
        if (data != null) {
            int items = data.getIntExtra("items", 0);
            String amount = data.getStringExtra("amount");
            String name = data.getStringExtra("name");
            int totalMi = data.getIntExtra("totalMi", 0);
            int sex = data.getIntExtra("sex", 0);
            couponsArray = (ArrayList<Coupons>) data
                    .getSerializableExtra("dataArray");
            if (couponsArray != null && couponsArray.size() > 0) {
                couponAdapter = new AdapterCoupons(ActVipCenterNew.this,
                        couponsArray,isFromCode);
            }
            refreshView(mTel, name, sex, items, amount, totalMi);
        }
    }


    private void refreshView(final String tel, String name, int sex, int items,
                             String amount, int totalMi) {
        mTel = tel;
        TextView tvTel = (TextView) findViewById(R.id.tv_vip_tel);
        TextView tvAmount = (TextView) findViewById(R.id.tv_amount);
        TextView tvItems = (TextView) findViewById(R.id.tv_items);
        TextView tvName = (TextView) findViewById(R.id.tv_user_name);
        TextView tvIconSex=(TextView)findViewById(R.id.icon_user_sex);
        TextView tvSex = (TextView) findViewById(R.id.tv_user_sex);
        TextView tvTotalMi = (TextView) findViewById(R.id.mi_amount);
        findViewById(R.id.mi_active).setOnClickListener(mClickListener);
        findViewById(R.id.ll_tel).setOnClickListener(mClickListener);
        findViewById(R.id.coupon).setOnClickListener(mClickListener);

        if (name != null && name.length() > 0) {
            tvName.setText(name);
        } else {
            tvName.setText("暂无");
        }
        if (sex == 0) {
            tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_male);
            tvSex.setText("男");
        } else {
            tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_female);
            tvSex.setText("女");
        }
        tvTel.setText(formateTelString(tel));
        tvItems.setText(Integer.toString(items));

        tvAmount.setText(amount);
        tvTotalMi.setText(totalMi+"");
        if (isFromCode == 0) {
            ListView dataListView = (ListView) findViewById(R.id.lv_coupon);
            if (couponAdapter != null){
                dataListView.setAdapter(couponAdapter);
            }
        } else if (isFromCode == 2) {
            //扫码进入
            if (!isRefreshing) {
                //非刷新
                CouponFrag couponFrag = new CouponFrag();
                Bundle bundleCoupon = new Bundle();
                bundleCoupon.putString("couponArray", couponArray.toString());
                couponFrag.setArguments(bundleCoupon);
                MiActiveFrag activeFrag = new MiActiveFrag();
                Bundle bundleActive = new Bundle();
                bundleActive.putString("activeArray", activeArray.toString());
                bundleActive.putInt("totalMi", totalMi);
                bundleActive.putString("mobile", mTel);
                activeFrag.setArguments(bundleActive);
                fragmentList.add(activeFrag);
                fragmentList.add(couponFrag);
                //关联接口
                refreshingCoupon = couponFrag;
                refreshingMi = activeFrag;

                viewPagerAdapter = new AdapterViewPager(getSupportFragmentManager(), fragmentList);
                mViewpager.setAdapter(viewPagerAdapter);
                mRadioGroup.check(R.id.mi_active);
            } else {
                //区分刷新类型
                if (refreshingType == REFRESHING_MI) {
                    refreshingMi.onRefreshingMi(totalMi);
                } else if (refreshingType == REFRESHING_COUPON) {
                    Log.i("test","couponArray:"+couponArray);
                    refreshingCoupon.onRefreshingCoupon(couponArray.toString());
                }
                isRefreshing = false;
            }

        }

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_back: {
                    finish();
                    break;
                }
                case R.id.ll_tel: {
                    dailDialog();
                    break;
                }
                case R.id.mi_active:
                    mViewpager.setCurrentItem(0);
                    break;
                case R.id.coupon:
                    mViewpager.setCurrentItem(1);
                    break;
                default:
                    break;
            }
        }
    };


    private void dailDialog() {
        if (mTel != null && mTel.length() > 8) {
            MyDialogUtil qDialog = new MyDialogUtil(this) {

                @Override
                public void onClickPositive() {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + mTel.replace(" ", "")));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "调用设备拨打电话失败",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onClickNagative() {
                }
            };
            qDialog.showCustomMessage("提示!", "即将拨打：" + mTel + "\n是否继续？", "确定",
                    "取消");
        } else {
            Toast.makeText(getBaseContext(), "号码为空，请先验证号码", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private String formateTelString(String tel) {
        tel = tel.replace(" ", "");
        String temp = "";
        if (tel.length() == 11) {
            temp = tel.substring(0, 3) + " ";
            temp += tel.substring(3, 7) + " ";
            temp += tel.substring(7, 11);
        }
        return temp;
    }

    //重新获取卡券信息
    private void verifyTelFromNet() {
        com.milai.asynctask.PostGetTask<JSONObject> mGetVerifyTask = new PostGetTask<JSONObject>(this
                , R.string.submiting, R.string.submit_failed, true, true, false) {

            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("shopId", Cookies.getShopId());
                jsonParams.put("mobile", mTel);
                jsonParams.put("uid", Cookies.getUserId());
                jsonParams.put("token", Cookies.getToken());
                JSONObject jRet = null;
                jRet = HttpMgr.getInstance().postJson(
                        ServerUrlConstants.getVerifyTelUrl(), jsonParams, true);
                Log.i("test", "jRet" + jRet.toString());
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject result) {
                if (exception == null && result != null) {
                    try {
                        if (result.getInt("result") == 1) {

                            JSONArray data = null;
                            try {
                                data = result.getJSONArray("list");
                                List<Coupons> coupons = new ArrayList<Coupons>();
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject coupon = data.getJSONObject(i);
                                    try {
                                        Coupons couponMode = new Coupons(coupon);
                                        if (couponMode.getMinPrice().equals("0")) {
                                            coupons.add(i, couponMode);
                                        }
                                    } catch (Exception e) {
                                        Log.i("ActVerifyVipTel", "数据解析异常");
                                        e.printStackTrace();
                                    }
                                }
                                couponsArray.clear();
                                couponsArray.addAll(coupons);
                                couponAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                Toast.makeText(ActVipCenterNew.this,"解析红包数据错误",Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ActVipCenterNew.this,"网络连接失败，请检查网络配置",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        mGetVerifyTask.execute();
    }

    @Override
    public void onUseCoupon() {
        if (isFromCode == 0) {
            //号码进入
            if (couponAdapter != null) {
                verifyTelFromNet();
            }
        }else {
            //扫描进入的刷新
            isRefreshing = true;
            refreshingType = REFRESHING_COUPON;
            initFragment();
//            Toast.makeText(this,"使用商户券，刷新界面",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUseMi() {
        //刷新
        isRefreshing = true;
        refreshingType = REFRESHING_MI;
        initFragment();
//        Toast.makeText(this,"使用米活动，刷新界面",Toast.LENGTH_SHORT).show();
    }
}
