package com.meishipintu.assistant.ui.pay;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epos.bertlv.MisComm;
import com.epos.bertlv.MisConstants;
import com.epos.bertlv.MisDataResult;
import com.epos.bertlv.MisTLVValue;
import com.epos.bertlv.MisTLVValueConvert;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.exception.ReloginException;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.exception.ServiceOccupiedException;
import com.landicorp.android.eptapi.exception.UnsupportMultiProcess;
import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;
import com.lkl.cloudpos.util.Debug;
import com.epos.bertlv.MisDataCenter;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.posd.BaseListener;
import com.meishipintu.assistant.posd.PosdConnector;
import com.meishipintu.assistant.posd.PosdTerminalTrans;
import com.meishipintu.assistant.posd.PrintAction;
import com.meishipintu.assistant.posd.PrintTaskListener;
import com.meishipintu.core.utils.CommonUtils;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Coupons;
import com.milai.model.PaidFailed;
import com.meishipintu.assistant.orderdish.ActCaptureTicket;
import com.milai.processor.PaymentProcessor;
import com.meishipintu.assistant.ui.ActVipCenter;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.CustomProgressDialog;
import com.meishipintu.core.utils.MyDialogUtil;
import com.meishipintu.core.utils.PrintUtils;
import com.newland.MPosSignatureVerify;
import com.newland.jsums.paylib.NLPay;
import com.newland.jsums.paylib.model.ConsumeRequest;
import com.newland.jsums.paylib.model.NLResult;
import com.newland.jsums.paylib.model.OrderInfo;
import com.newland.jsums.paylib.model.ResultData;
import com.spos.sdk.debug.Alog;
import com.spos.sdk.imp.SposManager;
import com.spos.sdk.interfac.Printer;
import com.spos.sdk.interfac.PrinterBase.OnEventListener;
import com.spos.sdk.interfac.Spos.OnInitListener;
import com.umeng.analytics.MobclickAgent;
import com.wangpos.wopensdk.tools.WPosOpenRequest;

public class ActNewPayment extends FragmentActivity {
    private String Amoney=null;
    private String orderno=null;
    private String mRcvShop = null;
    private int mPayMoney = 0;
    private long mTicketId = -1;
    private long mShopId = 0;
    private String mTransId = null;
    private String mVoucher = null;
    private String mDynamicid = null;// 主动扫码
    private int timeTemp = 0;
    private String mOutTradeNo = null;

    private String printTradeNo = null;
    private String printPayType = null;

    private String mCouponId = "";
    private String mCouponName = "";
    private float mCouponValue = 0;
    private float mCouponMinPrice = 0;
    private boolean mCouponUse = false;     //flag 标注是否已经验证卡券
    private String mCouponSn = "";
    private String mUserTel = "";
    private boolean mIsCanChangeMoney = true;

    private String mFormatTemp = "";
    private String mFormatMoney = "";
    private String mFormatTelCoupon = "";
    final int LANDI_POS_REQUEST_CODE = 75;//
    final int LAKALA_POS_REQUEST_CODE = 76;//
    final int MILAI_PAY_REQUEST_CODE = 77;//
    private String mICCardId = null;

    protected CustomProgressDialog mLoadingDialog;
    public static ActNewPayment mActNewPayment = null;

    private EditText mEtMoney = null;
    //标注当前输入的EditText
    private EditText mFocusEditText = null;
    private TextView mPreferential = null;
    private EditText mPreEditText = null;
    private EditText mEtTelCoupon = null;
    private Button mVerifyTelCoupon = null;

    private View mPayPathView = null;
    private Timer mCardTimer;
    private Timer mAlipayScanerTimer;
    private Thread mCardThread;
    private static final String ZERO = "0.00";
    private static final int TIME_LIMIT = 120000;
    private static final int TIME_OUT = 2;
    private static final int CHECK_TIME_OK = 401;
    private static final int CHECK_TIME_OUT = 400;
    private ServiceConnection conn =null;
    private AidlPrinter lklPrinter = null;
    private SposManager sposManager = null;
    private Printer sposPrinter = null;
    public static final String LKL_SERVICE_ACTION = "lkl_cloudpos_mid_service";
    private boolean isDeviceServiceLogined = false;
    private com.landicorp.android.eptapi.device.Printer.Progress progress=null;
    private PosdConnector mPosdConnector = PosdConnector.getInstance();
    private PosdTerminalTrans mPosdTrans = PosdTerminalTrans.build();
    private PrintAction[] printActions;


    private final PrintTaskListener mPrintListener = new PrintTaskListener() {

        @Override
        public void onSuccess(Byte response) {



        }

        @Override
        public void onFailure(int errcode, String errmsg) {

        }
    };

    // 绑定lakala服务
    public void bindService() {
        if (Cookies.getShopType().contains("lkl")) {
            Intent intent = new Intent();
            intent.setAction(LKL_SERVICE_ACTION);
            boolean flag = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);

            if(flag){
                Log.i("zcz", "服务绑定成功");
            }else{
                Log.i("zcz", "服务绑定失败");
            }
        }
    }

    public void onDeviceConnected(AidlDeviceService serviceManager) {
        try {
            lklPrinter = AidlPrinter.Stub.asInterface(serviceManager.getPrinter());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /*
    * 联迪绑定
    * */
    public void bindDeviceService() {
        if(Cookies.getShopType().contains("ldi")){
            try {
                isDeviceServiceLogined = false;
                //当loginThrowException时则登录失败
                DeviceService.login(this);
                isDeviceServiceLogined = true;
                Log.i("zcz","联迪设备服务绑定成功");
            } catch (RequestException e) {

                e.printStackTrace();
            } catch (ServiceOccupiedException e) {
                e.printStackTrace();
            } catch (ReloginException e) {
                e.printStackTrace();
            } catch (UnsupportMultiProcess e) {
                e.printStackTrace();
            }
        }
        if(isDeviceServiceLogined){
            Log.i("zcz", "landi服务绑定成功");
        }else{
            Log.i("zcz", "landi服务绑定失败");
        }
    }

    public void unbindDeviceService() {
        DeviceService.logout();
        isDeviceServiceLogined = false;
    }

    private void initPrinter() {
        Log.i("zcz lkl test", "is conn == null?" + (conn == null));
        if (conn == null) {
            Log.i("zcz", "连接拉卡拉设备");
            conn = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
                    Log.i("zcz", "连接拉卡拉设备成功");
                    if (serviceBinder != null) { // 绑定成功
                        AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
                        onDeviceConnected(serviceManager);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i("zcz", "拉卡拉设备断开");
                }
            };
        }

        Log.i("zcz", "连接SPOS设备");
        if (SposManager.IsSposDevice()) {
            sposManager = SposManager.getInstance();
            if (!sposManager.isInit()) {
                sposManager.init(ActNewPayment.this, new OnInitListener() {

                    @Override
                    public void onInitOk() {
                        Log.i("zcz", "SPOS设备连接成功");
                        Log.i("zcz", "连接SPOS打印机");
                        if (sposPrinter == null) {
                            sposPrinter = sposManager.openPrinter();
                            sposPrinter.setOnEventListener(new OnEventListener() {

                                @Override
                                public void onEvent(int code, String info) {
                                    Log.i("zcz", " SPOS打印  CODE:" + code + " info:" + info);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String paramString) {
                        Log.i("zcz", "SPOS设备连接失败" + paramString);
                    }
                });
            } else {
                Log.i("zcz", "SPOS设备已连接");
                if (sposPrinter == null) {
                    sposPrinter = sposManager.openPrinter();

                    sposPrinter.setOnEventListener(new OnEventListener() {

                        @Override
                        public void onEvent(int code, String info) {
                            Log.i("zcz", " SPOS打印  CODE:" + code + " info:" + info);
                        }
                    });
                }
            }
        } else {
            Log.i("zcz", "非SPOS设备");
        }


        if(isDeviceServiceLogined){
            Log.i("zcz", "连接Landi成功");
            progress = new com.landicorp.android.eptapi.device.Printer.Progress() {
                @Override
                public void doPrint(com.landicorp.android.eptapi.device.Printer printer) throws Exception {
                    printer.printText(Cookies.getShopName() + "签购单\n");
                    printer.printText("商户名称:" + Cookies.getShopName() + "\n");
                    printer.printText("支付渠道:" + printPayType + "\n");
                    if (!TextUtils.isEmpty(printTradeNo)) {
                        printer.printText("订单号:" + printTradeNo + "\n");
                    }
                    String realAmount = String.valueOf(((float) mPayMoney) / 100.0f);
                    printer.printText("金额: RMB " + realAmount + "元\n");
                    printer.printText("--------------------------------");
                    printer.printText("\n");
                    printer.printText("\n");

                }

                @Override
                public void onFinish(int i) {

                }

                @Override
                public void onCrash() {

                }
            };

        }


    }

    public void printOrder() {
        Log.i("zcz", "尝试打印订单");

        if (lklPrinter != null) {
            Log.i("zcz", "拉卡拉-打印订单");
            List<PrintItemObj> list = new ArrayList<PrintItemObj>();
            list.add(new PrintItemObj(Cookies.getShopName() + "签购单"));
            list.add(new PrintItemObj("商户名称:" + Cookies.getShopName()));
            list.add(new PrintItemObj("支付渠道:" + printPayType));
            if (!TextUtils.isEmpty(printTradeNo)) {
                list.add(new PrintItemObj("订单号:" + printTradeNo));
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/DD hh:mm:ss", Locale.CHINA);
           // 获取当前时间
            String time = CommonUtils.DateUtilOne(System.currentTimeMillis());
            list.add(new PrintItemObj("日期时间:" + time));
            String realAmount = String.valueOf(((float) mPayMoney) / 100.0f);
            list.add(new PrintItemObj("金额: RMB " + realAmount + "元"));
            list.add(new PrintItemObj("--------------------------------"));
            list.add(new PrintItemObj(" "));
            list.add(new PrintItemObj(" "));
            list.add(new PrintItemObj(" "));
            PrintUtils.printText(lklPrinter, list, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    Log.i("zcz","打印出错，错误码为：" + arg0);
                }
            });
        } else if (sposPrinter != null) {
            Log.i("zcz", "SPOS-打印订单");
            sposPrinter.printText(Cookies.getShopName() + "签购单", Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            sposPrinter.printText("商户名称:" + Cookies.getShopName(), Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            sposPrinter.printText("支付渠道:" + printPayType, Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            if (!TextUtils.isEmpty(printTradeNo)) {
                sposPrinter.printText("订单号:" + printTradeNo, Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                        Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            }


            String time = CommonUtils.DateUtilOne(System.currentTimeMillis());
            sposPrinter.printText("日期时间:" + time, Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            String realAmount = String.valueOf(((float) mPayMoney) / 100.0f);
            sposPrinter.printText("金额: RMB " + realAmount + "元", Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            sposPrinter.printText("--------------------------------", Printer.FontFamily.SONG, Printer.FontSize.MEDIUM,
                    Printer.FontStyle.NORMAL, Printer.Gravity.LEFT);
            sposPrinter.startNewLine();
            sposPrinter.startNewLine();
            sposPrinter.startNewLine();
            sposPrinter.startNewLine();
            sposPrinter.startNewLine();
        }else if(progress!=null){
            Log.i("zcz", "landi-打印订单");
            try {
                progress.start();
            } catch (RequestException e) {
                e.printStackTrace();
            }
        }else{
            mPosdConnector.connect(getApplicationContext(), new BaseListener<String>() {
                @Override
                public void onSuccess(String response) {
                    Log.i("zcz", "PosdConnect");

                    printActions = new PrintAction[1];
                    int offest = 0;
                     /* TEXT START */


                    String text = Cookies.getShopName() + "签购单\n"+"商户名称:" +
                            Cookies.getShopName() + "\n"+"支付渠道:" + printPayType + "\n"+"金额: RMB "
                            + Amoney + "元\n"+"--------------------------------\n\n\n\n"+ Cookies.getShopName() + "签购单\n"+"商户名称:" +
                            Cookies.getShopName() + "\n"+"支付渠道:" + printPayType + "\n"+"金额: RMB "
                            + Amoney + "元\n"+"--------------------------------\n\n\n\n";
                    printActions[offest] = new PrintAction(PrintAction.PRINT_TYPE.TEXT);
                    printActions[offest].buildString(text);
                    offest += 1;
                    printActions = Arrays.copyOf(printActions,offest);
                    mPosdTrans.startPrint(printActions, mPrintListener);

                }

                @Override
                public void onFailure(int errcode, String errmsg) {
                    Log.i("zcz", "PosdConnect failed");

                    Toast.makeText(getBaseContext(), "失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_payment);
        mActNewPayment = this;
        //绑定联迪
        bindDeviceService();
        //获取传输数据
        Intent in = getIntent();
        mTicketId = in.getLongExtra(ConstUtil.INTENT_EXTRA_NAME.TICKET_ID, -1);
        mPayMoney = in.getIntExtra(ConstUtil.INTENT_EXTRA_NAME.MONEY_AMOUNT, 0);
        mUserTel = in.getStringExtra("USER_TEL");

        mRcvShop = Cookies.getShopName();
        mShopId = Cookies.getShopId();

        Typeface typefaceDigit = Typeface.createFromAsset(this.getAssets(), "fonts/digifaw.ttf");
        mEtMoney = (EditText) findViewById(R.id.et_receivable);
        mEtMoney.setInputType(InputType.TYPE_NULL);
        // mEtMoney.setTypeface(typefaceDigit);
        mEtTelCoupon = (EditText) findViewById(R.id.et_user_tel_coupon);
        //屏蔽软键盘并显示光标
        mEtTelCoupon.setInputType(InputType.TYPE_NULL);

        mFocusEditText = mEtMoney;
        mPreEditText = mEtMoney;

        LinearLayout ll_price = (LinearLayout) findViewById(R.id.ll_price);
        ll_price.setOnTouchListener(mOnTouchFocusChangeListener);
        mEtMoney.setOnTouchListener(mOnTouchFocusChangeListener);
        mEtTelCoupon.setOnTouchListener(mOnTouchFocusChangeListener);
        mEtTelCoupon.addTextChangedListener(mTextWatcher);

        findViewById(R.id.vw_focusprice).setVisibility(View.VISIBLE);
        findViewById(R.id.vw_focussn).setVisibility(View.INVISIBLE);

        editTextGetFocus();
        mPreferential = (TextView) findViewById(R.id.tv_preferential);
        mPreferential.setText("优惠价格：" + ZERO + "元");

        findViewById(R.id.rl_7).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_8).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_9).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_6).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_5).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_4).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_3).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_2).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_1).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_0).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_00).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_point).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.ll_clear).setOnClickListener(mOnKeyClkListener);
        findViewById(R.id.rl_back).setOnClickListener(mOnKeyClkListener);

        findViewById(R.id.qr_scan).setOnClickListener(mClkListener);
        findViewById(R.id.rl_pay_path).setOnClickListener(mClkListener);

        TextView tv_right = (TextView) findViewById(R.id.tv_right);
        tv_right.setBackgroundResource(R.drawable.pay_list_detail);
        tv_right.setOnClickListener(mClkListener);

        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(mRcvShop);
        Button bt_back = (Button) findViewById(R.id.btn_back);
        bt_back.setOnClickListener(mClkListener);

        mVerifyTelCoupon = (Button) findViewById(R.id.bt_verify_tel_coupon);
        mVerifyTelCoupon.setOnClickListener(mClkListener);
        Button btClear = (Button) findViewById(R.id.bt_clear_tel_coupon);
        btClear.setOnClickListener(mClkListener);

        if (mPayMoney > 0) {
            mEtMoney.setText(String.format("%.2f", ((float) mPayMoney / 100.0f)));
            mFormatTemp = mFormatMoney = String.format("%.2f", ((float) mPayMoney / 100.0f));
        } else {
            mEtMoney.setText(ZERO);
        }
        initPrinter();
        bindService();


        if (mUserTel != null && mUserTel.length() == 11) {
            mEtTelCoupon.setText(mUserTel);
        } else {
            mEtTelCoupon.setText("");
            // 设置不同hint大小
            SpannableString sString = new SpannableString("请输入SN码或手机号");
            AbsoluteSizeSpan aSs = new AbsoluteSizeSpan(20, true);
            sString.setSpan(aSs, 0, sString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mEtTelCoupon.setHint(new SpannedString(sString));
        }
        int isShowMessageDialog = in.getIntExtra("SHOW_MESSAGE_DIALOG", 0);
        //Log.i("onNewPayment_onActRESULT", Integer.toString(isShowMessageDialog));
        if (isShowMessageDialog == 1) {
            String message = "红包SN已发送至用户手机\n请在卡劵框中输入SN验证码";
            showCouponNoticeMessage(message);
        }

        // WindowManager.LayoutParams lp=getWindow().getAttributes();
        // lp.systemUiVisibility=View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        // getWindow().setAttributes(lp);
    }

    //flag 标注输入值是否更改
    private boolean isInputEtChange = false;

    /**
     * 1.如卡券已经验证，则不能修改金额
     * 2.金额输入合法时才可以验证卡券
     * 3.输入只是光标在点击后显示
     */
    private OnTouchListener mOnTouchFocusChangeListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
            switch (id) {
                case R.id.et_receivable:
                case R.id.ll_price: {
                    if (mCouponUse) {
                        selfToastShow("红包已验证不可改价");
                    } else {
                        isInputEtChange = true;
                        mFocusEditText = mEtMoney;
                        mFormatTemp = mFormatMoney;
                        findViewById(R.id.vw_focusprice).setVisibility(View.VISIBLE);
                        findViewById(R.id.vw_focussn).setVisibility(View.INVISIBLE);
                    }

                    break;
                }
                case R.id.et_user_tel_coupon: {
                    if (getPayMoney()) {
                        mFocusEditText = mEtTelCoupon;
                        mFormatTemp = mFormatTelCoupon;
                        findViewById(R.id.vw_focusprice).setVisibility(View.INVISIBLE);
                        findViewById(R.id.vw_focussn).setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
            Log.i("onTouch-mFormatTemp", mFormatTemp);
            editTextGetFocus();

            return true;
        }
    };

    public void setCoupon(boolean use, String couponId, String couponName, float couponValue, String couponSn,
                          float miniPrice, String tel) {

        mCouponUse = use;
        mCouponId = couponId;
        mCouponName = couponName;
        mCouponValue = couponValue;
        mCouponSn = couponSn;
        mCouponMinPrice = miniPrice;
        mUserTel = tel;
        if (use) {
            mVerifyTelCoupon.setEnabled(false);
            float money = ((float) mSaveMoney / 100.0f) - mCouponValue;
            if (money <= 0) {
                money = 0.0f;
                // selfToastShow("最少支付0.01元");
            }
            mEtMoney.setText(String.format("%.2f", money));// 使用红包
            mPreferential.setText("优惠价格" + String.format("%.2f", mCouponValue) + "元");
            findViewById(R.id.ll_coupon_info).setVisibility(View.VISIBLE);
            TextView tvValue = (TextView) findViewById(R.id.tv_coupon_value);
            tvValue.setText(String.format("%.2f", mCouponValue) + "元");
            TextView tvMinPrice = (TextView) findViewById(R.id.tv_coupon_able);
            tvMinPrice.setText("红包可用,满" + String.format("%.2f", mCouponMinPrice) + "元使用"
                    + String.format("%.2f", mCouponValue) + "元");

        } else {
            mVerifyTelCoupon.setEnabled(true);
            mEtMoney.setText(String.format("%.2f", ((float) mSaveMoney / 100.0f)));// 使用红包
            mPreferential.setText("红包不可用，优惠价格:" + ZERO + "元");
            findViewById(R.id.ll_coupon_info).setVisibility(View.INVISIBLE);
        }
    }

    private void editTextGetFocus() {
        Log.i("onclick-mFormatTemp", mFormatTemp);
        if (mFocusEditText != mEtMoney) {
            mEtMoney.setTextColor(getResources().getColor(R.color.white));
            // mEtMoney.getPaint().setUnderlineText(false);
        }
        if (mFocusEditText != mEtTelCoupon) {
            mEtTelCoupon.setTextColor(getResources().getColor(R.color.white));
            // mEtTelCoupon.getPaint().setUnderlineText(false);
        }
        mFocusEditText.setTextColor(this.getResources().getColor(R.color.white));

        // mFocusEditText.getPaint().setUnderlineText(true);
    }

    public void clearMoney() {
        mPayMoney = 0;
        mTicketId = -1;
        mOutTradeNo = "";
        mTransId = "";
        mVoucher = "";
        mDynamicid = "";
        timeTemp = 0;
        clearCoupon();
        mEtMoney.setText(ZERO);
        mPreferential.setText("" + "价格:" + ZERO + "元");
        mICCardId = null;
        mFormatMoney = "";
        mFormatTemp = "";
        mUserTel = "";
        mFocusEditText = mEtMoney;
        mSaveMoney = 0;
        editTextGetFocus();
    }

    private void clearCoupon() {
        mFormatTelCoupon = "";
        mEtTelCoupon.setText("");
        if (mEtTelCoupon == mFocusEditText) {
            mFormatTemp = mFormatTelCoupon;
        }
        if (mCouponUse) {
            setCoupon(false, "", "", 0, "", 0, "");
        }
        mCouponUse = false;
        mCouponName = "";
        mCouponValue = 0;
        mCouponSn = "";
        mCouponMinPrice = 0;
    }

    //使用卡券
    private void useCoupon(final String couponId, final String couponSn) {
        new PostGetTask<JSONObject>(this) {
            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                JSONObject jPara = new JSONObject();
                jPara.put("uid", Cookies.getUserId());
                jPara.put("couponId", couponId);// totalfee
                jPara.put("shopId", Cookies.getShopId());// shopid
                jPara.put("token", Cookies.getToken());
                jPara.put("couponSn", couponSn);
                jPara.put("totalFee", 9999);// totalfee
                JSONObject jReturn = HttpMgr.getInstance().postJson(ServerUrlConstants.getUseCouponResult(), jPara,
                        true);
                return jReturn;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject result) {
                if (exception == null && result != null) {
                    try {
                        if (result.getInt("result") == 1) {
                            clearMoney(); // 验证卡券
                            // selfToastShow("红包验证使用成功");
                            MyDialogUtil qDialog = new MyDialogUtil(ActNewPayment.this) {
                                @Override
                                public void onClickPositive() {
                                }

                                @Override
                                public void onClickNagative() {
                                }
                            };
                            qDialog.showCustomMessageOK(ActNewPayment.this.getString(R.string.notice), "红包验证使用成功",
                                    "知道了");

                        } else {
                            String msg = "红包使用失败";
                            if (result.has("msg")) {
                                msg = result.getString("msg");
                            }
                            selfToastShow(msg);
                        }
                    } catch (Exception e) {
                        selfToastShow("数据解析错误");
                    }
                } else {
                    selfToastShow("请检查网络连接");
                }
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
        }.execute();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        initPrinter();
        if (Cookies.getShopType().contains("lkl")) {
            bindService();
        }


        MobclickAgent.onResume(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        unbindDeviceService();
        MobclickAgent.onPause(this);
    }

    // 扫码中获取ticketId和Totalfee
    public void setTicketDetail(long ticketId, int totalPrice) {
        clearMoney();
        if (ticketId != -1) {
            mTicketId = ticketId;
            mEtMoney.setText(String.format("%.2f", ((float) totalPrice / 100.0f)));
            mFormatMoney = mEtMoney.getText().toString();
        }
    }

    private OnClickListener mClkListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            oldMessage = "";
            switch (v.getId()) {

                //扫描
                case R.id.qr_scan: {
                    Intent intent = new Intent();
                    intent.setClass(ActNewPayment.this, ActCaptureTicket.class);
                    intent.putExtra("CHECK_CODE", 2);
                    // startActivity(intent);
                    startActivityForResult(intent, ConstUtil.REQUEST_CODE.PAY_TO_SCAN_REQUEST);
                    break;
                }

                //账单详情
                case R.id.tv_right: {
                    Intent intent = new Intent();
                    intent.setClass(ActNewPayment.this, ActPayment.class);
                    startActivity(intent);
                    break;
                }

                //返回
                case R.id.btn_back: {
                    finishAndAni();
                    break;
                }

                //验证
                case R.id.bt_verify_tel_coupon: {
                    if (getPayMoney()) {
                        String temp = mEtTelCoupon.getText().toString();
                        temp = temp.replace(" ", "");
                        if (temp.length() == 11) {
                            //验证手机会员
                            verifyTelNative(temp);
                        } else if (temp.length() == 12) {
                            //验证卡券
                            getConpon(temp);
                        }
                    }
                    break;
                }

                //收款
                case R.id.rl_pay_path: {
                    if (getPayMoney()) {
                        int paytype = Cookies.getPayType(); // 得到支付方式

                        // 金额为0的支付方式，如果有卡券，就直接使用
                        String strmey = ((EditText) findViewById(R.id.et_receivable)).getText().toString();
                        float money = Float.parseFloat(strmey);
                        Amoney=String.valueOf((float)money);

                        if (money < 0.01f && mCouponUse && mCouponSn.length() > 0 && mCouponId.length() > 0) {
                            MyDialogUtil qDialog = new MyDialogUtil(ActNewPayment.this) {

                                @Override
                                public void onClickPositive() {
                                }

                                @Override
                                public void onClickNagative() {
                                    useCoupon(mCouponId, mCouponSn);
                                }
                            };
                            qDialog.showCustomMessage(getString(R.string.notice), "支付金额为0， 是否直接使用该卡券？", "否", "是");
                            return;
                        }

                        // 其它情况-
                        if (money < 0.01f) {
                            selfToastShow("最少支付0.01元");
                            return;
                        }

                        if (paytype == 0) { // 如果是米来支付方式
                            printPayType = "-消费 米来支付";
                            if (getPayMoney()) {
                                Intent out = new Intent();
                                out.putExtra(ConstUtil.INTENT_EXTRA_NAME.TICKET_ID, mTicketId);
                                out.putExtra(ConstUtil.INTENT_EXTRA_NAME.MONEY_AMOUNT, mPayMoney);
                                out.setClass(mActNewPayment, ActPayQRCode.class);
                                startActivityForResult(out, MILAI_PAY_REQUEST_CODE);
                            }
                        } else if (paytype == 7 || paytype == 12) { // 如果是支付宝条码或微信刷卡
                            if (getPayMoney()) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                intent.putExtra("CHECK_CODE", 1);
                                if (paytype == 7)
                                    intent.putExtra("CHECK_CODE_FROM", 0);
                                else if (paytype == 12)
                                    intent.putExtra("CHECK_CODE_FROM", 1);
                                intent.setClass(ActNewPayment.this, ActCaptureTicket.class);
                                startActivityForResult(intent, ConstUtil.REQUEST_CODE.CAPTURE_TICKET_NEWPAYMENT);
                            }
                        } else if (paytype == 10) { // 如果是ic卡支付
                            showICCardDialog();
                        } else {
                            signType(paytype, false);
                        }
                    }
                    break;
                }
                case R.id.bt_clear_tel_coupon: {
                    clearCoupon();
                    break;
                }

            }
        }
    };

    public void setCouponString(String couponSn) {
        mFormatTelCoupon = couponSn;
        if (mFocusEditText == mEtTelCoupon) {
            mFormatTemp = mFormatTelCoupon;
        }
        mEtTelCoupon.setText(couponSn);
    }

    private String formateNumber(float num) {
        DecimalFormat dfNum = (DecimalFormat) NumberFormat.getInstance();
        dfNum.applyPattern("0.00");
        return dfNum.format(num);
    }

    //键盘操作回调
    private OnClickListener mOnKeyClkListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            oldMessage = "";
            switch (id) {
                case R.id.rl_0: {
                    dishplayNum("0");
                    break;
                }
                case R.id.rl_00: {
                    dishplayNum("00");
                    break;
                }
                case R.id.rl_1: {
                    dishplayNum("1");
                    break;
                }
                case R.id.rl_2: {
                    dishplayNum("2");
                    break;
                }
                case R.id.rl_3: {
                    dishplayNum("3");
                    break;
                }
                case R.id.rl_4: {
                    dishplayNum("4");
                    break;
                }
                case R.id.rl_5: {
                    dishplayNum("5");
                    break;
                }
                case R.id.rl_6: {
                    dishplayNum("6");
                    break;
                }
                case R.id.rl_7: {
                    dishplayNum("7");
                    break;
                }
                case R.id.rl_8: {
                    dishplayNum("8");
                    break;
                }
                case R.id.rl_9: {
                    dishplayNum("9");
                    break;
                }
                case R.id.rl_point: {
                    dishplayNum(".");
                    break;
                }
                case R.id.rl_back: {
                    dishplayNum("back");
                    break;
                }
                case R.id.ll_clear: {
                    dishplayNum("clear");
                    break;
                }
            }
        }
    };

    /*

     */
    private void dishplayNum(String key) {

        String num_1 = "0";
        String num_2 = "00";
        if ("00123456789.".indexOf(key) >= 0) {
            // if (mEtPainIn != mFocusEditText) {
            // clearCoupon();
            // }
            if (isInputEtChange) {
                mFormatTemp = "";
            }
            if (mFormatTemp.indexOf(".") < 0 && key.equals(".") && mFocusEditText != mEtTelCoupon) {
                if (mFormatTemp.isEmpty()) {
                    display("0" + key);
                } else {
                    display(key);
                }

            } else if (!key.equals(".")) {
                display(key);
            }
            mPreEditText = mFocusEditText;
            isInputEtChange = false;
        } else if (key.equals("back")) {
            DecimalFormat dfNum = (DecimalFormat) NumberFormat.getInstance();
            dfNum.applyPattern("0.00");
            if (mFormatTemp.contains(".")) {
                while (mFormatTemp.endsWith("0")) {
                    mFormatTemp = mFormatTemp.substring(0, mFormatTemp.length() - 1);
                }
                if (mFormatTemp.endsWith(".")) {
                    mFormatTemp = mFormatTemp.substring(0, mFormatTemp.length() - 1);
                }
            }
            if (mFormatTemp.length() > 0) {
                mFormatTemp = mFormatTemp.substring(0, mFormatTemp.length() - 1);
                if (mFormatTemp.isEmpty() || mFormatTemp.length() <= 0) {
                    if (mFocusEditText == mEtMoney) {
                        mFormatTemp = "0";
                    }
                }

                if (mFocusEditText == mEtMoney) {
                    mFormatMoney = mFormatTemp;
                    String temp = dfNum.format(Float.parseFloat(mFormatTemp));
                    mFocusEditText.setText(temp);
                }
                if (mFocusEditText == mEtTelCoupon) {
                    mFormatTelCoupon = mFormatTemp;
                    mFocusEditText.setText(mFormatTemp);
                }
                // caculateDiscount();
            } else {
                mFormatTemp = "";
                if (mFocusEditText == mEtTelCoupon) {
                    mFocusEditText.setText("");
                } else {
                    mFocusEditText.setText(ZERO);
                }

            }
            // mPreEditText = mFocusEditText;
            isInputEtChange = false;
        } else if (key.equals("clear")) { // 按了清空按钮
            clearMoney();
            isInputEtChange = true;
        }
    }

    private void display(String key) {
        String value = null;
        if (mFormatTemp.contains(".")) {
            if (mFormatTemp.indexOf(".") > mFormatTemp.length() - 3) {
                mFormatTemp += key;
            }
        } else {
            mFormatTemp += key;
        }
        value = mFormatTemp;
        DecimalFormat dfNum = (DecimalFormat) NumberFormat.getInstance();
        dfNum.applyPattern("0.00");
        if (!key.equals(".")) {
            value = dfNum.format(Float.parseFloat(mFormatTemp));
        }

        if (mFocusEditText == mEtMoney) {
            mFormatMoney = mFormatTemp;
            mFocusEditText.setText(value);
        } else if (mFocusEditText == mEtTelCoupon) {
            if (mFormatTemp.length() > 12) {
                mFormatTemp = mFormatTemp.substring(0, 12);
            }
            mFormatTelCoupon = mFormatTemp;
            mFocusEditText.setText(mFormatTelCoupon);
        }

        // caculateDiscount();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mCouponUse) {
                setCoupon(false, "", "", 0, "", 0, "");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            formateNumberAsTelOrSn(s);
        }
    };

    // 格式化手机号码及couponsn
    private void formateNumberAsTelOrSn(Editable s) {
        if (s.length() < 3) {
            return;
        }
        if (s.charAt(0) == '1') {
            if (s.length() > 3 && s.length() <= 13) {
                if (s.charAt(3) != ' ') {
                    s.insert(3, " ");
                }
                if (s.length() > 8) {
                    if (s.charAt(8) != ' ') {
                        s.insert(8, " ");
                    }
                }
            }
            if (s.length() > 13) {
                s.delete(12, s.length() - 1);
                // selfToastShow("手机号码长度为11位");
                return;
            }
        } else {
            if (s.length() > 4 && s.length() <= 14) {
                if (s.charAt(4) != ' ') {
                    s.insert(4, " ");
                }
                if (s.length() > 9) {
                    if (s.charAt(9) != ' ') {
                        s.insert(9, " ");
                    }
                }
            }
            if (s.length() > 14) {
                s.delete(13, s.length() - 1);
                return;
            }

        }
    }

    public void OnAlipayOK() {
        setResult(RESULT_OK);
        // finishAndAni();
    }

    //判断输入金额是否合法
    private boolean getPayMoney() {
        float money = 0.0f;
        String strmey = ((EditText) findViewById(R.id.et_receivable)).getText().toString();
        try {
            money = Float.parseFloat(strmey);
            if (money < 0.01f) {
                // selfToastShow("支付金额不能为0");
                // return false;
                money = 0;
            } else if (money > 100000f) {
                selfToastShow("最多支付100000元");
                return false;
            }
        } catch (NumberFormatException ex) {
            selfToastShow("请输入正确的支付金额");
            return false;
        }
        mPayMoney = (int) (money * 100.0f);
        return true;
    }

    //支付金额
    private int mSaveMoney = 0;

    private int getSavePreMoney() {
        String strmey = ((EditText) findViewById(R.id.et_receivable)).getText().toString();
        int moneyInt = 0;
        if (strmey.length() > 0) {
            float money = Float.parseFloat(strmey);
            moneyInt = (int) (money * 100.0f);
        }
        mSaveMoney = moneyInt;
        return moneyInt;
    }

    void showDialog(int nConfirmDialogID, String strTitle, String strMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(strTitle);
        builder.setMessage(strMessage);
        switch (nConfirmDialogID) {
            case -1:
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        return;
                    }
                });
                break;
        }
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private ProgressDialog m_progressDialog;

    @SuppressLint({"HandlerLeak", "HandlerLeak"})
    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String strMsg = bundle.getString("INFO");
            switch (msg.what) {
                case 0: {
                    mVoucher = bundle.getString("VOUCHER");
                    signType(5, true);
                    showDialog(-1, "提示", strMsg);
                }
                break;
                case 1: {
                    showDialog(-1, "提示", strMsg);
                }
                break;
                case 2: { // 超时
                    mCardThread.interrupt();
                    showDialog(-1, "提示", "支付超时");
                }
                break;
                case 401: {
                    mAlipayScanerTimer.cancel();
                    showDialog(-1, "提示", "支付成功");
                    break;
                }
                case 402: {
                    mAlipayScanerTimer.cancel();
                    showDialog(-1, "提示", "支付超时");
                }
            }
            mCardTimer.cancel();
            m_progressDialog.dismiss();
        }
    };

    private Handler m_handler_check = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 401: {
                    showDialog(-1, "提示", "支付成功");
                    break;
                }
                case 402: {
                    showDialog(-1, "提示", "支付超时");
                    break;
                }
            }
            mAlipayScanerTimer.cancel();
            m_progressDialog.dismiss();
        }
    };

    private void cardPay(final String sub, final String body, final String price, final long shopId, final long tid,
                         String transId) {
        // MainActivity.mActivity.showActMPosSetting();
        m_progressDialog = new ProgressDialog(this);
        m_progressDialog.setMessage("等待刷卡，请稍候...");
        m_progressDialog.setCancelable(false);
        m_progressDialog.setCanceledOnTouchOutside(false);
        m_progressDialog.show();
        mCardThread = new Thread() {
            public void run() {
                MisTLVValue misRefTLVValue = null;
                MisTLVValue misVoucherTLVValue = null;
                MisTLVValue misMoneyTLVValue = null;
                MisTLVValue misSerialTLVValue = null;
                MisTLVValue misBatchTLVValue = null;
                MisTLVValue misMerNameTLVValue = null;
                MisTLVValue misCardNoTLVValue = null;
                MisTLVValue misDatetimeTLVValue = null;
                MisTLVValue misOperIdTLVValue = null;
                MisTLVValue misTermIdTLVValue = null;
                MisTLVValue misAuthIdTLVValue = null;
                MisTLVValue misEnNameTLVValue = null;
                MisTLVValue misResponseTLVValue = null;
                // 消费
                String fomprice = price;
                while (fomprice.length() < 6)
                    fomprice = "0" + fomprice;
                String orderid;
                if (mTransId.isEmpty()) {
                    return;
                } else {
                    orderid = mTransId;
                }
                // transId"20150112001234567890"
                while (orderid.length() < 20)
                    orderid = orderid + " ";
                MisDataResult dataResult = MisDataCenter.dataSwitch(ActNewPayment.this,
                        MisComm.getConsumeArray(fomprice, orderid).getEncoder());
                if (dataResult.tlvList != null) {
                    for (int i = 0; i < dataResult.tlvList.size(); i++) {
                        Log.e(MisDataCenter.getByteInfo(dataResult.tlvList.get(i).getmTag()),
                                MisDataCenter.getByteInfo(dataResult.tlvList.get(i).getmData()));
                        // logByteToString
                        Log.i("hcs_mTag", logByteToString(dataResult.tlvList.get(i).getmTag()));
                        Log.i("hcs_mLength", logByteToString(dataResult.tlvList.get(i).getmLength()));
                        Log.i("hcs_mData", logByteToString(dataResult.tlvList.get(i).getmData()));
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.REPONSE_CODE)) {
                            misResponseTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));

                            String resp = misResponseTLVValue.getValue();
                            if (!resp.equals("00")) {
                                dataResult.bSuccess = false;
                                break;
                            }
                        }

                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.REF_NUMBER)) {
                            misRefTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.VOUCHER_NUMBER)) {
                            misVoucherTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.SERIAL_NUMBER)) {
                            misSerialTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.BATH_NUMBER)) {
                            misBatchTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.TRADE_MONEY)) {
                            misMoneyTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.MERCHANTS_NAME)) {
                            misMerNameTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.MAIN_ACCOUNT)) {
                            misCardNoTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.TRADE_TIME)) {
                            misDatetimeTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.OPERATOR_ID)) {
                            misOperIdTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.TERMINAL_ID)) {
                            misTermIdTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(), MisConstants.TagConstants.AUTH_ID)) {
                            misAuthIdTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                        if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
                                MisConstants.TagConstants.MERCHANTS_ENNAME)) {
                            misEnNameTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
                        }
                    }

                    // 凭证号是： ,将其上传到服务器保存
                    // if(misRefTLVValue != null) { //dataResult.bSuccess
                    // String voucher = ( misVoucherTLVValue!=null ?
                    // misVoucherTLVValue.getValue() : "");
                    // String serial = ( misSerialTLVValue!=null ?
                    // misSerialTLVValue.getValue() : "");
                    // String refNO = ( misRefTLVValue!=null ?
                    // misRefTLVValue.getValue() : "");
                    // String batchNO = ( misBatchTLVValue!=null ?
                    // misBatchTLVValue.getValue() : "");
                    // String money = ( misMoneyTLVValue!=null ?
                    // misMoneyTLVValue.getValue() : "");
                    // String mername = ( misMerNameTLVValue!=null ?
                    // misMerNameTLVValue.getValue() : "");
                    // String cardNo = ( misCardNoTLVValue!=null ?
                    // misCardNoTLVValue.getValue() : "");
                    // String dateTime = ( misDatetimeTLVValue!=null ?
                    // misDatetimeTLVValue.getValue() : "");
                    // String operId = ( misOperIdTLVValue!=null ?
                    // misOperIdTLVValue.getValue() : "");
                    // String termId = ( misTermIdTLVValue!=null ?
                    // misTermIdTLVValue.getValue() : "");
                    // String authId = ( misAuthIdTLVValue!=null ?
                    // misAuthIdTLVValue.getValue() : "");
                    // String enName = ( misEnNameTLVValue!=null ?
                    // misEnNameTLVValue.getValue() : "");
                    // //////////////////打印///////////////
                    // //String strPrintTest =
                    //
                    // "打印测试\r\n打印测试.\r\n打印测试..\r\n打印测试...\r\n打印测试....\r\n打印测试...........\r\n\r\n\r\n\r\n";
                    //
                    // PrintInfo pi=new PrintInfo();
                    // pi.setMchtEnName(enName);
                    // pi.setMchtName(mername);
                    // pi.setAmount(money);
                    // pi.setAuthNO(authId);
                    // pi.setBatchNO(batchNO);
                    // pi.setCardNO(cardNo);
                    // pi.setDatetime(dateTime);
                    // // pi.setKuantaiNO("0209");
                    // pi.setOperNO(operId);
                    // pi.setRefNO(refNO);
                    // pi.setTermNO(termId);
                    // pi.setVoucherNO(voucher);
                    // pi.setOrderNO(orderid);
                    // String strPrintTest=PrintUtils.print(pi);
                    //
                    // byte[] bytePrintTest = null;
                    // try {
                    // bytePrintTest = strPrintTest.getBytes("GBK");
                    // } catch (UnsupportedEncodingException e) {
                    // e.printStackTrace();
                    // dataResult = new MisDataResult();
                    // dataResult.bSuccess = false;
                    // dataResult.strMsg = "打印字符转码失败!";
                    // }
                    //
                    // if(dataResult.bSuccess) { //支付成功，打印小票
                    // dataResult = MisDataCenter.dataSwitch(
                    // mParentActivity,
                    // MisComm.getPrintCateringInfoArray(
                    // bytePrintTest).getEncoder());
                    // //notifyurl,transid传进去
                    // signType(5,true);
                    // }
                    //
                    // }
                }

                // ////////////////////////////////////

                // 退货
                /*
                 * if(dataResult.bSuccess){
				 * dataResult=MisDataCenter.dataSwitch(getActivity
				 * (),MisComm.getRetainGoodsArray
				 * ("000001",misRefTLVValue.getValue()).getEncoder()); }
				 */
                // MisDataResult
                //
                // dataResult = MisDataCenter.dataSwitch(ActNewPayment.this,
                // MisComm
                // .getConsumeRedoArray(strMoney).getEncoder());
                //

                boolean bRet = dataResult.bSuccess;
                Bundle bundle = new Bundle();
                Message msg = new Message();
                if (bRet) {
                    if (misVoucherTLVValue != null) {
                        bundle.putString("VOUCHER", misVoucherTLVValue.getValue());
                    }
                    msg.what = 0;
                } else
                    msg.what = 1;
                if (misRefTLVValue != null) {
                    bundle.putString("INFO",
                            dataResult.strMsg + "\r\n" + misRefTLVValue.getTagInfo() + ":" + misRefTLVValue.getValue());
                } else
                    bundle.putString("INFO", dataResult.strMsg);
                msg.setData(bundle);
                m_handler.sendMessage(msg);
            }
        };

        mCardThread.start();

        // 设定定时器
        mCardTimer = new Timer();
        mCardTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message timeOutMsg = new Message();
                timeOutMsg.what = TIME_OUT;
                m_handler.sendMessage(timeOutMsg);
            }
        }, TIME_LIMIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onActivityResult", "resultCode");
        if (resultCode == Activity.RESULT_CANCELED) {
            // Toast.makeText(this, data.getExtras().getString("reason"),
            // Toast.LENGTH_SHORT).show();
        }
        if (resultCode == -2) {
            Toast.makeText(ActNewPayment.this, data.getExtras().getString("reason"), Toast.LENGTH_SHORT).show();
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstUtil.REQUEST_CODE.PAY_TICKET_QR:
                    setResult(RESULT_CANCELED);
                    break;
                case ConstUtil.REQUEST_CODE.CAPTURE_TICKET_NEWPAYMENT:
                    String dynamicid = data.getStringExtra("dynamicid");
                    int payFrom = data.getIntExtra("CHECK_CODE_FROM", -1);
                    signCheckAli(dynamicid, payFrom);
                    break;
                case LANDI_POS_REQUEST_CODE: {
                    String amount = data.getStringExtra("amount");// 金额
                    String traceNo = data.getStringExtra("traceNo");// 流水号
                    mOutTradeNo = traceNo;
                    String referenceNo = data.getStringExtra("referenceNo");// 参考号
                    String cardNo = data.getStringExtra("cardNo");// 卡号
                    // Toast.makeText(
                    // getBaseContext(),
                    // "amount:" + amount + "\n" + "traceNo:" + traceNo + "\n"
                    // + "referenceNo:" + referenceNo + "\n"
                    // + "cardNo:" + cardNo, Toast.LENGTH_LONG).show();
                    signType(5, true);
                    break;
                }
                case LAKALA_POS_REQUEST_CODE: {
                    String amount = data.getStringExtra("amount");// 金额
                    String traceNo = data.getStringExtra("traceNo");// 流水号
                    mOutTradeNo = traceNo;
                    String referenceNo = data.getStringExtra("referenceNo");// 参考号
                    String cardNo = data.getStringExtra("cardNo");// 卡号
                    // Toast.makeText(
                    // getBaseContext(),
                    // "amount:" + amount + "\n" + "traceNo:" + traceNo + "\n"
                    // + "referenceNo:" + referenceNo + "\n"
                    // + "cardNo:" + cardNo, Toast.LENGTH_LONG).show();
                    signType(5, true);
                    break;
                }
                /*
                    从会员界面返回卡券信息
                 */
                case ConstUtil.REQUEST_CODE.PAY_REQUEST_VIP_INFO: {
                    //从短信发送SN码返回
                    if (data.hasExtra("USER_TEL")) {
                        mUserTel = data.getStringExtra("USER_TEL");
                        //Log.i("ActNewPayment_from_vicenter", mUserTel);
                        mFormatTelCoupon = "";
                        if (mFocusEditText == mEtTelCoupon) {
                            mFormatTemp = "";
                        }
                        int isShowMessageDialog = data.getIntExtra("SHOW_MESSAGE_DIALOG", 0);
                        //Log.i("onNewPayment_onActRESULT", Integer.toString(isShowMessageDialog));
                        if (isShowMessageDialog == 1) {
                            String message = "红包SN已发送至用户手机\n请在卡劵框中输入SN验证码";
                            showCouponNoticeMessage(message);
                            mEtTelCoupon.setText("");
                        } else {
                            mEtTelCoupon.setText(mUserTel);
                            mFormatTelCoupon = mUserTel;
                            if (mFocusEditText == mEtTelCoupon) {
                                mFormatTemp = mFormatTelCoupon;
                            }
                        }

                    } else if (data.hasExtra("USN_SN")) {   //从直接使用卡券返回
                        String tel = data.getStringExtra("USN_TEL");
                        String id = data.getStringExtra("USN_ID");
                        String sn = data.getStringExtra("USN_SN");
                        String couponName = data.getStringExtra("USN_NAME");
                        String value = data.getStringExtra("USN_VALUE");
                        String minprice = data.getStringExtra("USN_MINPRICE");
                        float couponValue = Float.parseFloat(value);
                        float couponMinPrice = Float.parseFloat(minprice);
                        //获取输入金额，复制给mSavaMoney
                        getSavePreMoney();
                        //标记卡券是否满足使用条件
                        boolean isCouponUes = ((mSaveMoney/100.0f) >= couponMinPrice) ? true : false;
                        setCoupon(isCouponUes, id, couponName, couponValue, sn, couponMinPrice, tel);

                    }
                    break;
                }

                /*
                    从扫描界面返回信息
                 */
                case ConstUtil.REQUEST_CODE.PAY_TO_SCAN_REQUEST: {
                    if (data.hasExtra("COUPON_SN")) {
                        String sn = data.getStringExtra("COUPON_SN");
                        if (mCouponUse) {
                            selfToastShow("已有一个红包正在使用中");
                        } else {
                            setCouponString(sn);
                        }
                    }
                    break;
                }
                case MILAI_PAY_REQUEST_CODE: {
                    printOrder();
                    printOrder();
                    finish();
                    break;
                }
            }
        } else if (resultCode == com.newland.jsums.paylib.utils.Key.RESULT_SUCCESS) {
            if (data != null) {
                NLResult payResult = (NLResult) data.getParcelableExtra("result");
                showResult(payResult);
            }
        } else {
            switch (requestCode) {
                case LANDI_POS_REQUEST_CODE: {
                    if (data != null) {
                        String reason = data.getStringExtra("reason");
                        if (reason != null) {
                            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "data为空", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    public void showCouponNoticeMessage(String message) {
        MyDialogUtil qDialog = new MyDialogUtil(this) {

            @Override
            public void onClickPositive() {
            }

            @Override
            public void onClickNagative() {
            }
        };
        qDialog.showCustomMessageOK(getString(R.string.notice), message, "知道了");
    }

    private OrderInfo lastOrderInfo = null;

    // SignUtils.verifySignData(getUnionPublicKey(), data,rslt.getSignData())
    public void showResult(NLResult rslt) {
        String resultMsg = "";
        StringBuilder sb = new StringBuilder();
        sb.append("返回码：" + rslt.getResultCode() + " \n");
        if (rslt.getResultCode() == 6000 && rslt.getData() != null) {
            // 有订单信息返回的时候需对期进行验签，防止数据被篡改
            ResultData data = rslt.getData();
            if (MPosSignatureVerify.verifySignDatatest(ActNewPayment.unionPublicKey, data, rslt.getSignData())) {
                // 验签成功
                sb.append("订单信息：" + data.toString() + "\n");
                // resultMsg = sb.toString();
                // 存入上次调用成功的信息
                if (rslt.getData() instanceof OrderInfo) {
                    lastOrderInfo = (OrderInfo) rslt.getData();
                    switch (Integer.parseInt(lastOrderInfo.getOrderStatus())) {
                        case 1: {
                            resultMsg = "收款成功！";
                            // String orderId=lastOrderInfo.getOrderNo();
                            // ActNewPayment.mNewPayment.updateMPosOrderStatus(orderId,
                            // 11);
                            clearMoney(); // 银联刷卡
                            break;
                        }
                    }
                }
            } else {// 验签失败
                resultMsg = "验签失败";
            }
        } else {// 调用失败
            sb.append("错误信息：" + rslt.getResultMsg() + "\n");
            resultMsg = sb.toString();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(resultMsg).setTitle("调用结果");
        if (resultMsg == "收款成功！") {
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    return;
                }
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }

    private EditText etTextPayCode = null;
    private Dialog mDia = null;

    private void showICCardDialog() {
        mDia = new Dialog(this, R.style.dialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.my_dialog_with_2editor, null);
        mDia.setContentView(v);
        TextView title = (TextView) v.findViewById(R.id.tv_dialogtitle);
        title.setText("IC刷卡提示框");
        etTextPayCode = (EditText) v.findViewById(R.id.pos_sign);
        etTextPayCode.setHint("请链接IC卡设备或输入卡号");
        etTextPayCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final EditText et_password = (EditText) v.findViewById(R.id.pos_password);
        et_password.setVisibility(View.GONE);
        Button btYes = (Button) v.findViewById(R.id.ok);
        etTextPayCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sign = s.toString();
                if (sign.contains("\n")) {
                    sign = sign.replace("\n", "");
                    mICCardId = sign;
                    signType(10, true);
                }
            }
        });

        btYes.setText("确认");
        btYes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String sign = etTextPayCode.getText().toString();
                if (sign.isEmpty()) {
                    Toast.makeText(ActNewPayment.this, "凭证不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    sign = sign.replace("\n", "");
                    mICCardId = sign;
                    signType(10, true);
                    // 将数据写到actNewPayment
                }
            }
        });
        Button btCancle = (Button) v.findViewById(R.id.cancel);
        btCancle.setText("取消");
        btCancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDia.dismiss();
            }
        });
        mDia.show();
    }

    // type 1 支付宝
    // type 2 微信
    // type 3 银联
    // type 4 现金
    // type 5 刷卡

    public void signCheckAli(String dynamicid, int from) {
        mDynamicid = dynamicid;
        Log.i("ActNewPayment", dynamicid + ":" + Integer.toString(from));
        if (from == -1) {
            Toast.makeText(getBaseContext(), "扫码来源异常。请重新发起扫描", Toast.LENGTH_LONG).show();
            return;
        } else if (from == 0) {
            signType(7, false);
        } else if (from == 1) {
            signType(12, false);
        }
    }

    private void signType(final int payType, final boolean cash) {

        mLoadingDialog = new CustomProgressDialog(this, "数据处理中");
        mLoadingDialog.show();

        new PostGetTask<JSONObject>(this) {
            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                String body = null;
                switch (payType) {
                    case 2:
                        body = "-消费 微信支付";
                        break;
                    case 3:
                        body = "-消费 银联支付";
                        break;
                    case 4:
                        body = "-消费 现金支付";
                        break;
                    case 5:
                        body = "-消费 POS刷卡支付";
                        break;
                    case 6:
                        body = "-消费 支付宝支付";
                        break;
                    case 7:
                        body = "-消费 支付宝支付";
                        break;
                    case 9:
                        body = "-消费 微信支付";
                        break;
                    case 10:
                        body = "-消费 IC刷卡支付";
                        break;
                    case 11:
                        body = "-消费 银联刷卡支付";
                        break;
                    case 12:
                        body = "-消费 微信刷卡";
                        break;
                    case 14:
                        body = "-消费 其他支付";
                        break;
                    case 15:
                        body = "-消费 支付宝支付";
                        break;
                }
                printPayType = body;
                JSONObject jParam = new JSONObject();
                jParam.put("uid", Cookies.getUserId());
                if (mTicketId <= 0) {
                    mTicketId = 0;
                }
                jParam.put("orderDishId", mTicketId);
                jParam.put("signType", payType);
                jParam.put("totalFee", mPayMoney);// totalfee
                jParam.put("shopId", Cookies.getShopId());// shopid
                jParam.put("token", Cookies.getToken());
                jParam.put("subject", mRcvShop + "-消费");
                jParam.put("body", mRcvShop + body + "<And>");
                if (mUserTel != null && mUserTel.length() == 11) {
                    jParam.put("mobile", mUserTel);
                }

                if (mCouponUse) {
                    jParam.put("couponSn", mCouponSn);
                }

                if (payType == 10) {
                    jParam.put("cardid", mICCardId);
                }

                JSONObject jRet = null;
                if (cash) {
                    if (payType == 5) { // 银行卡支付
                        if (Cookies.getShopType().contains("ldi")) {
                            jParam.put("transId", mTransId + mOutTradeNo);
                        } else if (Cookies.getShopType().contains("lkl")) {
                            jParam.put("transId", mTransId);
                        } else if (Cookies.getShopType().contains("bfu")) {
                            jParam.put("transId", mTransId + mVoucher);
                        }
                    } else if (payType == 11) { // 银联刷卡支付
                        jParam.put("transId", mTransId);
                    }
                    jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getSignCash(), jParam, true);
                } else {
                    if (payType == 7 || payType == 12) {
                        jParam.put("dynamicid", mDynamicid);
                    }
                    jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getPaySignUrl(), jParam, true);
                }
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject result) {

                if (result != null && exception == null) {
                    try {
                        if (result.getInt("result") == 1) {
                            if (cash) {
                                Toast.makeText(getBaseContext(), "收款成功", Toast.LENGTH_LONG).show();
                                if (payType == 10) {
                                    String feeRemain = result.getString("feeremain");
                                    showICCardResult(Float.toString((float) mPayMoney / 100.0f), feeRemain);
                                }
                                printOrder();
                                printOrder();

                                clearMoney(); // 现金等收银成功
                                finish();
                            } else {
                                Toast.makeText(getBaseContext(), "打印订单成功", Toast.LENGTH_LONG).show();
                                if (payType == 4) { // 现金支付
                                    ShowPayMessage("请确定是否收到正确数额现金", "已收到", "未收到", payType);
                                } else if (payType == 14) { // 其它支付
                                    ShowPayMessage("请确定是否收到正确数额款项", "已收到", "未收到", payType);
                                } else if (payType == 6 || payType == 9) { // 支付宝码，
                                    // 微信码
                                    String qr_code = result.getString("qrcode");
                                    int qtype = 1;
                                    if (payType == 9)
                                        qtype = 2;
                                    ActAliPay mQrPay = new ActAliPay(ActNewPayment.this, qr_code, qtype);
                                    mQrPay.showAtLocation(mEtMoney, failTip, failTip, failTip);
                                } else if (payType == 7 || payType == 12) { // 支付宝条码，微信刷卡
                                    String outTradeNo = result.getString("outTradeNo");
                                    if (mLoadingDialog.isShowing()) {
                                        mLoadingDialog.dismiss();
                                    }
                                    checkStatus(outTradeNo, payType);
                                    return;
                                } else if (payType == 5) { // 刷银行卡
                                    mTransId = result.getString("transid");
                                    mTransId = mTransId.replace("A", "");
                                    // Toast.makeText(mParentActivity, mTransId,
                                    // Toast.LENGTH_LONG).show();
                                    Log.i("shop types", Cookies.getShopType());
                                    if (Cookies.getShopType().contains("lkl")) {
                                        lklPosPay("消费", mPayMoney, mTransId);
                                    } else if (Cookies.getShopType().contains("ldi")) {
                                        landiPosPay("消费", mPayMoney, mTransId);
                                    } else {
                                        cardPay(mRcvShop + "-消费", mRcvShop + "-消费刷卡支付", Integer.toString(mPayMoney),
                                                mShopId, mTicketId, mTransId);
                                    }
                                } else if (payType == 11) { // 银联刷卡
                                    String amount = result.getString("amount").replace("A", "");
                                    String merchantNo = result.getString("mrchNo");
                                    String accessKeyId = result.getString("appAccessKeyId");
                                    String currency = result.getString("currency");
                                    // mTicketId =;
                                    String extOrderNo = result.getString("extOrderNo"); // Long.toString(mTicketId);
                                    String signatureString = result.getString("signData");
                                    String uniPublicKey = result.getString("uniPublicKey");
                                    callUniPayMPos(amount, accessKeyId, merchantNo, extOrderNo, currency,
                                            signatureString, uniPublicKey);
                                } else if (payType == 15) { // 支付宝转账码
                                    String pcode = result.getString("pcode");
                                    ActAliPay mQrPay = new ActAliPay(ActNewPayment.this, pcode, 3); // 支付宝转账码
                                    mQrPay.showAtLocation(mEtMoney, failTip, failTip, failTip);
                                }
                            }
                        } else {// 判断result
                            String msg = "";
                            if (result.has("msg")) {
                                msg = result.getString("msg");
                            }
                            Toast.makeText(getBaseContext(), "打印订单失败:" + msg, Toast.LENGTH_LONG).show();


                        }
                    } catch (JSONException ej) {// 判断异常
                        mLoadingDialog.setTitle("网络出现问题，数据解析失败，请重试");
                        Toast.makeText(getBaseContext(), "连接打印机出现问题", Toast.LENGTH_LONG).show();
                        if (mLoadingDialog.isShowing()) {
                            mLoadingDialog.dismiss();
                        }
                    }

                } else {
                    mLoadingDialog.setTitle("网络出现问题，打印失败，请重试");
                    Toast.makeText(getBaseContext(), "连接打印机出现问题", Toast.LENGTH_LONG).show();
                    if (cash) {
                        saveFailedToDataBase(payType);
                    }
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                }
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
        }.execute();
    }

    private void showICCardResult(String payMoney, String restMoney) {
        if (mDia.isShowing()) {
            mDia.dismiss();
        }
        final Dialog dia = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.mydialog_okdialogview, null);
        dia.setContentView(v);
        TextView title = (TextView) v.findViewById(R.id.dialog_title);
        title.setText("IC刷卡结果提示框");
        TextView dialog_message = (TextView) v.findViewById(R.id.dialog_message);
        dialog_message.setText("支付金额：" + payMoney + "\n" + "支付状态：成功" + "\n" + "卡内余额：" + restMoney);
        Button btCancle = (Button) v.findViewById(R.id.ok);
        btCancle.setText("知道了");
        btCancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dia.dismiss();
            }
        });
        dia.show();
    }

    private void callUniPayMPos(String amount, String appAccessKey, String merchNo, String extOrderNo, String currency,
                                String signatureString, String unionpublickey) {
        Bundle data = new Bundle();
        data.putString("amount", formateNumber(Float.parseFloat(amount)));
        data.putString("appAccessKey", appAccessKey);
        data.putString("merchNo", merchNo);
        data.putString("extOrderNo", extOrderNo);
        data.putString("currency", currency);
        data.putString("signatureString", signatureString);
        data.putString("unionPublickey", changePublicKey(unionpublickey));
        callUniPayMPosStart(1, data);
    }

    private String changePublicKey(String oringlePublicKey) {
        String finalPublicKey = "";
        oringlePublicKey = oringlePublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
        oringlePublicKey = oringlePublicKey.replace("-----END PUBLIC KEY-----", "");
        finalPublicKey = oringlePublicKey.replaceAll("\n", "");
        Log.i("ActNewPayment", finalPublicKey);
        return finalPublicKey;
    }

    public void callUniPayMPosStart(int type, Bundle data) {
        NLPay pay = NLPay.getInstance(); // 取一个支付实例
        String ext01 = "";
        String ext02 = "";
        String ext03 = "";
        if (type == 1) {// 消費，返回的orderstatus=1
            String amount = data.getString("amount");
            String exOrderNo = data.getString("extOrderNo");

            String publicUnionKey = data.getString("unionPublickey");
            ActNewPayment.unionPublicKey = publicUnionKey;
            ConsumeRequest request = new ConsumeRequest(); // 支付交易请求对象
            request.setAppAccessKeyId(data.getString("appAccessKey")); // 设置appSecretKey应用接入秘钥
            request.setMrchNo(data.getString("merchNo")); // 设置商户号
            request.setCurrency(data.getString("currency")); // 设置货币代码
            request.setAmount(amount); // 设置金额
            request.setExtOrderNo(exOrderNo); // 设置外部订单号
            request.setExt01(ext01);
            request.setExt02(ext02);
            request.setExt03(ext03);
            String signatureResult = data.getString("signatureString");
            // String signatureResultfFromSignUtil = SignUtils.signData(
            // PRIVATE_KEY, request);
            // Log.i("MainActivity_signData_by_SignUtil",
            // signatureResultfFromSignUtil);
            Log.i("MainActivity", signatureResult);
            request.setSignature(signatureResult);// 传入私钥生成签名数据,这个要放在最后
            try {
                pay.consume(this, request);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "您还没有安装  银联左收右付  请在“应用汇和360手机市场搜索下载”", Toast.LENGTH_LONG).show();
            }
            Log.i("debug", request.toString());
        }
    }

    private static String unionPublicKey = null;

//    private static WPosOpenRequest wPosOpenRequest;

    private void saveFailedToDataBase(int payType) {
        PaidFailed paidFailed = new PaidFailed();
        paidFailed.setId(0);
        paidFailed.setWaitorId(Cookies.getUserId());
        paidFailed.setUserTel(mUserTel);
        paidFailed.setUserName("");
        String transId = null;
        if (Cookies.getShopType().contains("ldi") || Cookies.getShopType().contains("lkl")) {
            transId = mTransId + mOutTradeNo;
        } else {
            transId = mTransId + mVoucher;
        }
        paidFailed.setTradeNo(transId);
        paidFailed.setSubject(Cookies.getShopName() + "-消费");
        paidFailed.setPayType(payType);
        if (mPayMoney > 1) {
            paidFailed.setTotalPrice(mPayMoney);
        } else {
            selfToastShow("缓存支付订单失败，非法支付金额");
            return;
        }
        paidFailed.setStatus(0);

        long time = System.currentTimeMillis();
        paidFailed.setCreateTime(time);
        paidFailed.setUpdateTime(time);
        paidFailed.setTicketNo(mTicketId);
        paidFailed.setShopId(Cookies.getShopId());
        paidFailed.setCouponSn(mCouponUse ? mCouponSn : "");
        Log.i("actnewpayment", "插入失败支付开始");
        PaymentProcessor.getInstance().insertPaidFailed(this, paidFailed);

//        wPosOpenRequest = new WPosOpenRequest(APPID, SECRET, TOKEN);
//        wPosOpenRequest.requestOpenApi(serviceKey, paramMap);
    }

    private void landiPosPay(String transName, int amount, String transId) {
        Intent in = new Intent();
        try {
            ComponentName cp = new ComponentName("com.landicorp.android.ccbpay",
                    "com.landicorp.android.ccbpay.MainActivity");
            in.setComponent(cp);
            in.putExtra("transName", transName);
            String amountString = String.format("%012d", amount);
            // Toast.makeText(getBaseContext(), amountString, Toast.LENGTH_LONG)
            // .show();
            in.putExtra("amount", amountString);
            in.putExtra("orderNo", transId);
            Log.i("dubug_startLandi",
                    "transName:" + transName + "\n" + "amount:" + amount + "\n" + "transId:" + transId);
            startActivityForResult(in, LANDI_POS_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "非LANDI设备支持店铺，请联系客服", Toast.LENGTH_LONG).show();
        }
    }

    private void lklPosPay(String transName, int amount, String transId) {
        Intent intent = new Intent();
        String realAmount = String.valueOf(((float) amount) / 100.0f);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMDDhhmmss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        try {
            ComponentName componet = new ComponentName("com.lkl.cloudpos.payment",
                    "com.lkl.cloudpos.payment.activity.MainMenuActivity");
            Bundle b = new Bundle();
            b.putString("msg_tp", "0200");// 报文类型
            b.putString("pay_tp", "0");// 支付方式0-银行卡 1-微信 2-支付宝 3-银联钱包
            b.putString("proc_tp", "00");// 消费类型 00消费 01授权
            b.putString("proc_cd", "000000");// 交易处理码 000000
            b.putString("amt", realAmount);// 金额
            b.putString("order_no", transId);// 订单号
            b.putString("appid", "com.meishipintu.assistant");// 包名
            b.putString("time_stamp", time);// 时间
            b.putString("order_info", transName);// 订单信息
            intent.setComponent(componet);
            intent.putExtras(b);
            Log.i("dubug_startLakala",
                    "transName:" + transName + "\n" + "amount:" + amount + "\n" + "transId:" + transId);
            startActivityForResult(intent, LAKALA_POS_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "非拉卡拉设备支持店铺，请联系客服", Toast.LENGTH_LONG).show();
        }
    }

    private void checkStatus(final String outTradeNo, final int payType) {
        printTradeNo = outTradeNo;
        m_progressDialog = new ProgressDialog(this);
        m_progressDialog.setMessage("等待用户支付，请稍候...");
        m_progressDialog.setCancelable(false);
        m_progressDialog.setCanceledOnTouchOutside(false);
        m_progressDialog.show();
        mAlipayScanerTimer = new Timer();
        mAlipayScanerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new PostGetTask<JSONObject>(ActNewPayment.this) {
                    @Override
                    protected JSONObject doBackgroudJob() throws Exception {
                        JSONObject jParam = new JSONObject();
                        jParam.put("outTradeNo", outTradeNo);
                        jParam.put("type", payType);//
                        jParam.put("token", Cookies.getToken());
                        jParam.put("uid", Cookies.getUserId());
                        jParam.put("shopId", Cookies.getShopId());
                        JSONObject jRet = null;
                        jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getAlipayScaner(), jParam, true);
                        timeTemp++;
                        return jRet;
                    }

                    @Override
                    protected void doPostJob(Exception exception, JSONObject result) {


                        try {
                            // Message timeOutMsg_1 = new Message();
                            if (result != null && exception == null) {
                                int resultCode = result.getInt("result");
                                if (resultCode == 1) {
                                    m_progressDialog.dismiss();
                                    Toast.makeText(getBaseContext(), "用户支付成功", Toast.LENGTH_LONG).show();
                                    // timeOutMsg_1.what = CHECK_TIME_OK;
                                    // m_handler_check
                                    // .sendMessage(timeOutMsg_1);
                                    mAlipayScanerTimer.cancel();
                                    if(result.getString("message").equals("ok")){
                                        printOrder();
                                        printOrder();
                                    }
                                    clearMoney(); // 条码与微信支付
                                    finish();
                                    return;
                                }else{
                                    Toast.makeText(getBaseContext(), "订单数据解析失败", Toast.LENGTH_LONG).show();
                                }
                            }
                            if (timeTemp == 9) {
                                // timeOutMsg_1.what = CHECK_TIME_OUT;
                                m_progressDialog.dismiss();
                                Toast.makeText(getBaseContext(), "支付超时", Toast.LENGTH_LONG).show();
                                // m_handler_check.sendMessage(timeOutMsg_1);
                                mAlipayScanerTimer.cancel();
                                return;
                            }
                        } catch (JSONException ej) {
                            Toast.makeText(getBaseContext(), "数据解析失败", Toast.LENGTH_LONG).show();


                        }
                    }
                }.execute();
            }
        }, 5 * 1000, 5 * 1000);
    }

    public void ShowPayMessage(String message, String positive, String negative, final int payType) {
        MyDialogUtil qDialog = new MyDialogUtil(this) {

            @Override
            public void onClickPositive() {

            }

            @Override
            public void onClickNagative() {
                signType(payType, true);
            }
        };
        qDialog.showCustomMessage(getString(R.string.notice), message, negative, positive);
    }

    // private void decode()
    // {
    // byte[] encoderData={0x02,0x00,0x68,(byte) 0x85, 0x01,0x30,0x30,0x30,
    // 0x30,0x30,0x32,0x00, 0x30,0x30,(byte) 0x8a,0x02,
    // 0x30,0x30,(byte) 0xff,(byte) 0x80, 0x08,(byte) 0xbd,(byte) 0xbb,(byte)
    // 0xd2,
    // (byte) 0xd7,(byte) 0xb3,(byte) 0xc9,(byte) 0xb9, (byte) 0xa6,(byte)
    // 0xff,(byte) 0x81,0x01,
    // 0x00,(byte) 0x9f,0x1c,0x08, 0x30,0x30,0x31,0x39,
    // 0x30,0x30,0x36,0x35, (byte) 0xff,0x45,0x0c,0x35,
    // 0x31,0x34,0x30,0x30, 0x39,0x32,0x33,0x37,
    // 0x30,0x30,0x36,(byte) 0xff, 0x24,0x10,0x36,0x32,
    // 0x32,0x35,0x37,0x36, 0x38,0x37,0x34,0x35,
    // 0x31,0x32,0x33,0x30, 0x34,0x30,(byte) 0xff,0x42,
    // 0x03,0x00,0x01,0x75, (byte) 0xff,0x43,0x03,0x00,
    // 0x01,0x75,(byte) 0xff,0x44, 0x03,0x00,0x00,0x01,
    // (byte) 0x9f,0x21,0x03,0x09};
    // ArrayList<MisTLV> array=MisTLV.decoderTLV(encoderData);
    // if(array!=null&&array.size()>0)
    // {
    // for(int i=0;i<array.size();i++){
    // MisTLV tlv=array.get(i);
    // Log.i("test_decode_tag",logByteToString( tlv.getmTag()));
    // Log.i("test_decode_length",logByteToString( tlv.getmLength()));
    // Log.i("test_decode_data",logByteToString( tlv.getmData()));
    // }
    // }
    // }
    private String logByteToString(byte[] dataArray) {
        String temp = "";
        if (dataArray != null && dataArray.length > 0) {
            for (int i = 0; i < dataArray.length; i++) {
                int v = dataArray[i] & 0xFF;
                temp += Integer.toHexString(v);
            }
        }
        return temp;
    }

    private Toast mSelfToast;
    private String oldMessage = "";

    private void selfToastShow(String message) {
        if (oldMessage.equals(message)) {
            return;
        }
        mSelfToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        LayoutInflater infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infalter.inflate(R.layout.layout_toast, null);
        TextView tvContent = (TextView) v.findViewById(R.id.tv_toast_content);
        tvContent.setText(message);
        mSelfToast.setGravity(Gravity.CENTER, 0, 0);
        mSelfToast.setView(v);
        oldMessage = message;
        mSelfToast.show();
    }

    /*
        匹配手机号码并验证
        此处验证用正则的find方法，是不是应该用matches？
     */
    private void verifyTelNative(String tel) {
        if (tel != null && tel.length() > 0) {
            tel = tel.replace(" ", "");
            if (tel.length() == 11) {
                String stringCompile = "^1[3|4|5|7|8][0-9]\\d{4,8}";
                Pattern pattern = Pattern.compile(stringCompile);
                Matcher matcher = pattern.matcher(tel);
                Boolean result = matcher.find();
                if (result) {
                    verifyTelFromNet(tel);
                } else {
                    selfToastShow("请输入正确号码");
                }
            } else {
                selfToastShow("请输入正确号码");
            }
        } else {
            selfToastShow("号码或验证码不能为空");
        }
    }

    private PostGetTask mGetVerifyTask;

    //验证手机号 - 通过接口验证
    private void verifyTelFromNet(final String tel) {
        mGetVerifyTask = new PostGetTask<JSONObject>(this) {

            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("shopId", Cookies.getShopId());
                jsonParams.put("mobile", tel);
                jsonParams.put("uid", Cookies.getUserId());
                jsonParams.put("token", Cookies.getToken());
                Log.i("test", "cookies.shopid:" + Cookies.getShopId());
                Log.i("test", "cookies.mobile:" + tel);
                Log.i("test", "cookies.uid:" + Cookies.getUserId());
                Log.i("test", "cookies.token:" + Cookies.getToken());
                JSONObject jRet = null;

                //使用HttpClient连接，是否需要替换为HttpUrlConnection或Volley？
                jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getVerifyTelUrl(), jsonParams, true);
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject result) {
                if (exception == null && result != null) {
                    try {
                        if (result.getInt("result") == 1) {
                            int items = result.getInt("items");
                            String name = result.getString("name");
                            int sex = 0;
                            try {
                                sex = result.getInt("sex");
                            } catch (JSONException e1) {
                                sex = 0;
                            }
                            JSONArray data = null;
                            try {
                                data = result.getJSONArray("list");
                            } catch (JSONException e) {
                                selfToastShow("解析红包数据错误");
                            }
                            String amount = result.getString("amount");
                            String average = result.getString("average");
                            Log.i("name:sex", name + ":" + Integer.toString(sex));
                            setData(tel, name, sex, items, amount, average, data);
                        }
                    } catch (JSONException e) {
                        selfToastShow("网络连接失败，请检查网络配置");
                    }
                }
            }
        };
        mGetVerifyTask.execute();
    }

    //获取数据后启动会员信息页面
    private void setData(String tel, String name, int sex, int items, String amount, String average,
                         JSONArray arrayJsonCoupon) throws JSONException {
        // Toast.makeText(getBaseContext(), "setData",
        // Toast.LENGTH_LONG).show();
        Intent in = new Intent();
        Bundle bd = new Bundle();
        in.setClass(ActNewPayment.this, ActVipCenter.class);
        in.putExtra("tel", tel);
        in.putExtra("items", items);
        in.putExtra("amount", amount);
        in.putExtra("average", average);
        if (name != null && name.length() > 0 && !name.equals("null")) {
            in.putExtra("name", name);
            Log.i("name", name);
        } else {
            in.putExtra("name", "暂无");
        }

        in.putExtra("sex", sex);
        in.putExtra("From", 1);
        ArrayList<Coupons> couponArray = null;
        if (arrayJsonCoupon != null && arrayJsonCoupon.length() > 0) {
            couponArray = new ArrayList<Coupons>();
            for (int i = 0; i < arrayJsonCoupon.length(); i++) {
                JSONObject coupon = arrayJsonCoupon.getJSONObject(i);
                try {
                    Coupons couponMode = new Coupons(coupon);
                    couponArray.add(i, couponMode);
                } catch (Exception e) {
                    Log.i("ActVerifyVipTel", "数据解析异常");
                    e.printStackTrace();
                }
            }
        }
        in.putExtra("dataArray", couponArray);
        this.startActivityForResult(in, ConstUtil.REQUEST_CODE.PAY_REQUEST_VIP_INFO);
    }

    /*
        验证卡券
     */
    private void getConpon(final String couponSn) {

        if (couponSn.length() == 0) {
            selfToastShow("红包SN码不能为空");
            return;
        }
        if (couponSn.length() > 12) {
            selfToastShow("红包SN码不正确");
            return;
        }
        mLoadingDialog = new CustomProgressDialog(this, "正在验证红包");
        mLoadingDialog.show();
        new PostGetTask<JSONObject>(this) {

            @Override
            protected JSONObject doBackgroudJob() throws Exception {
                JSONObject jParam = new JSONObject();
                jParam.put("uid", Cookies.getUserId());
                jParam.put("totalFee", mPayMoney);// totalfee
                jParam.put("shopId", Cookies.getShopId());// shopid
                jParam.put("token", Cookies.getToken());
                jParam.put("couponSn", couponSn);
                JSONObject jRet = null;
                jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getCouponVerifyUrl(), jParam, true);
                return jRet;
            }

            @Override
            protected void doPostJob(Exception exception, JSONObject result) {
                if (exception == null && result != null) {
                    try {
                        if (result.getInt("result") == 1) {
                            JSONObject jData = result.getJSONObject("couponData");
                            String couponId = jData.getString("couponId");
                            String couponName = jData.getString("couponName");
                            float couponValue = (float) jData.getDouble("couponValue");
                            float couponMinPrice = (float) jData.getDouble("minPrice");
                            String tel = jData.getString("mobile");
                            //获取输入金额，复制给mSavaMoney
                            getSavePreMoney();
                            //标记卡券是否满足使用条件
                            boolean isCouponUes = ((mSaveMoney/100.0f) >= couponMinPrice) ? true : false;
                            setCoupon(isCouponUes, couponId, couponName, couponValue, couponSn, couponMinPrice, tel);
                        } else {
                            if (result.has("msg")) {
                                String msg = result.getString("msg");
                                selfToastShow(msg);
                            }
                        }

                    } catch (JSONException e) {
                        selfToastShow("数据解析失败");
                    }
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                }
            }
        }.execute();
    }

    protected void finishAndAni() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn != null&&(isDeviceServiceLogined||Cookies.getShopType().contains("lkl"))) {
            getApplicationContext().unbindService(conn);
        }
        mPosdConnector.disconnect();
    }
}
