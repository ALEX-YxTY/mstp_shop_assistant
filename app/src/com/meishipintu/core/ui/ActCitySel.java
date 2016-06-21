package com.meishipintu.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.ui.MainActivity;
import com.meishipintu.assistant.ui.auth.ActLogin;
import com.milai.asynctask.PostGetTask;
import com.milai.dao.CityCodeDao;
import com.milai.model.HotCity;
import com.milai.processor.HotCityProcessor;
import com.meishipintu.core.utils.MyDialogUtil;
import com.milai.utils.ValidCity;

public class ActCitySel extends FragmentActivity implements
        AMapLocationListener, Runnable {
    protected CityAdapter mAdapter;

    private TextView mTvPositionCity;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    public AMapLocation aMapLocation=null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private Handler handler = new Handler();
    private String mPosCity = null;
    private boolean mFirstUse = false;

    private AsyncTask<Void, Void, Integer> mGetActTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);



        setContentView(R.layout.city_sel);
        TextView tv = (TextView) findViewById(R.id.tv_title);
        tv.setText(R.string.city_sel);
        Intent in = getIntent();
        if (in.hasExtra("first_use")) {
            mFirstUse = in.getBooleanExtra("first_use", false);
        }
        getHotCity();

        //通过Loader异步加载
        //param1为识别号，param2为附加信息，param3为回调接口
        getSupportLoaderManager().initLoader(0, null, cityLoadCB);

        this.mAdapter = new CityAdapter(this);
        ListView lv = (ListView) findViewById(R.id.lv_valid_city);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Cursor c = (Cursor) parent.getAdapter().getItem(position);
                Cookies.saveCityId((int) c.getLong(c.getColumnIndex(HotCity.COLUMN_NAME_CITY_ID)));
                Cookies.saveCity(c.getString(c.getColumnIndex(HotCity.COLUMN_NAME_CITY_NAME)));
                if (Cookies.getMyLat() == 0 || Cookies.getMyLon() == 0) {
                    double lat = c.getDouble(c.getColumnIndex(HotCity.COLUMN_NAME_LAT));
                    double lon = c.getDouble(c.getColumnIndex(HotCity.COLUMN_NAME_LON));
                    Cookies.saveLoc((float) lat, (float) lon);
                }
                if (mFirstUse) {
                    Intent intent = new Intent();
                    intent.setClass(ActCitySel.this, ActLogin.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
                finishAndAni();
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(ll);
        mTvPositionCity = (TextView) findViewById(R.id.tv_cur_city);
        mTvPositionCity.setOnClickListener(ll);

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if(tm!=null){
                mLocationClient.startLocation();
                handler.postDelayed(this, 12000);// 设置超过12秒还没有定位到就停止定位
                mTvPositionCity.setText("正在定位...");

        }

     }

    private OnClickListener ll = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_back:
                    finishAndAni();
                    break;
                case R.id.tv_cur_city:
                    if (mPosCity != null) {
                        int cityId = 0;
                        Log.d("zcz",mPosCity);
                        String[] ci = CityCodeDao.getInstance().queryCityInfo(
                                ActCitySel.this, mPosCity);
                        if (ci != null) {
                            cityId = Integer.parseInt(ci[0]);
                        }
                        if (ValidCity.isValidCity(cityId)) {
                            Cookies.saveCityId(cityId);
                            Cookies.saveCity(mPosCity);
                            if (mFirstUse) {
                                Intent intent = new Intent();
                                intent.setClass(ActCitySel.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.right_in,
                                        R.anim.left_out);
                            }
                            finishAndAni();
                        } else {
                            // 非开通城市， 提示用户
                            MyDialogUtil dialog = new MyDialogUtil(ActCitySel.this) {
                                @Override
                                public void onClickPositive() {
                                }

                                @Override
                                public void onClickNagative() {
                                }

                            };
                            dialog.showCustomMessageOK("",
                                    getString(R.string.prompts_invalid_city), "确定");
                        }
                    } else {
                        Toast.makeText(ActCitySel.this, "请等待定位结束",
                                Toast.LENGTH_LONG).show();
                    }

                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAndAni();
    }

    protected void finishAndAni() {
        finish();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocation();// 停止定位
    }

    public void stopLocation(){
        mLocationClient.stopLocation();
    }

    /**
     * 此方法已经废弃



    @Override
    public void onProviderDisabled(String provider) {

    }


    public void onProviderEnabled(String provider) {

    }


    public void onStatusChanged(String provider, int status, Bundle extras) {

    }*/

    private void getHotCity() {

        if (mGetActTask != null) {
            mGetActTask.cancel(true);
            mGetActTask = null;
        }
        mGetActTask = new PostGetTask<Integer>(this) {
            @Override
            protected Integer doBackgroudJob() throws Exception {
                HotCityProcessor.getInstance().putDefault(ActCitySel.this);
                return HotCityProcessor.getInstance().getAll(ActCitySel.this);
            }

            @Override
            protected void doPostJob(Exception e, Integer result) {

                if (e == null && result != null) {

                }
                return;
            }

        };
        mGetActTask.execute();
    }

    private LoaderCallbacks<Cursor> cityLoadCB = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader onCreateLoader(int arg0, Bundle arg1) {
            return new CursorLoader(ActCitySel.this, HotCity.CONTENT_URI,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cs) {
            //swapCursor vs changeCursor:
            //swapCursor don't close the cursor and it will be closed by the Loader
            mAdapter.swapCursor(cs);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            mAdapter.swapCursor(null);
        }

    };

    /**
     * 混合定位回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location!= null) {
            aMapLocation= location;

            if(aMapLocation.getErrorCode() == 0){
                mPosCity = location.getCity();
                if (mPosCity != null)
                mTvPositionCity.setText(mPosCity);
                Cookies.saveLoc((float) location.getLatitude(),
                        (float) location.getLongitude());
            }else {
                Log.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void run() {
        if (aMapLocation == null) {
            stopLocation();// 销毁掉定位
        }
    }

    private class CityAdapter extends SimpleCursorAdapter {
//        private Cursor c;
        private Context mCtx;
        private LayoutInflater inflater;

        public class ViewHolder {
            TextView tvCityName;
        }

        @SuppressWarnings("deprecation")
        public CityAdapter(Activity context) {
            super(context, -1, null, new String[]{}, new int[]{});
            inflater = LayoutInflater.from(context);
            this.mCtx = context;
        }

        @Override
        public View getView(int position, View cv, ViewGroup parent) {
            ViewHolder viewHolder;
            if (cv == null || cv.getTag() == null) {
                viewHolder = new ViewHolder();
                cv = inflater.inflate(R.layout.item_cityname, null);
                viewHolder.tvCityName = (TextView) cv;

                cv.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) cv.getTag();
            }
            Cursor c = (Cursor) getItem(position);
            String cn = c.getString(c.getColumnIndex(HotCity.COLUMN_NAME_CITY_NAME));
            viewHolder.tvCityName.setText(cn);
            return cv;
        }
//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃ 　
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//    ┃　　　┃  神兽保佑　　　　　　　　
//    ┃　　　┃  代码无BUG！
//    ┃　　　┗━━━┓
//    ┃　　　　　　　┣┓
//    ┃　　　　　　　┏┛
//    ┗┓┓┏━┳┓┏┛
//      ┃┫┫　┃┫┫
//      ┗┻┛　┗┻┛

    }
}
