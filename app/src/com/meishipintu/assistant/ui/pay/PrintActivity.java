package com.meishipintu.assistant.ui.pay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;
import com.lkl.cloudpos.util.Debug;
import com.meishipintu.assistant.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/18 0018.
 */
public class PrintActivity extends FragmentActivity {
    private AidlPrinter printerDev = null;
    public static final String LKL_SERVICE_ACTION = "lkl_cloudpos_mid_service";
    //设别服务连接桥
    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            Log.d("aaa", name + "" + serviceBinder);
            Debug.d("aidlService服务连接成功");
            if(serviceBinder != null){	//绑定成功
                AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
                onDeviceConnected(serviceManager);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Debug.d("AidlService服务断开了");
        }
    };
    //绑定服务
    public void bindService(){
        Intent intent = new Intent();
        intent.setAction(LKL_SERVICE_ACTION);
        intent.setPackage(getPackageName());
        boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if(flag){
            Log.d("aaa", "服务绑定成功");
            Debug.d("服务绑定成功");
            Log.d("aaa",conn+"123");
        }else{
            Log.d("aaa", "服务绑定失败");
            Debug.d("服务绑定失败");
            Log.d("aaa",conn+"123");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.activity_printtext);
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        bindService();
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unbindService(conn);
    }
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        // TODO Auto-generated method stub
        try {
            printerDev = AidlPrinter.Stub.asInterface(serviceManager.getPrinter());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 获取打印机状态
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午2:18:47
     */
    public void getPrintState(View v){
        try {
            int printState = printerDev.getPrinterState();

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 打印文本
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午2:19:28
     */
    @SuppressWarnings("serial")
    public void printText(View v){
        try {
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    add(new PrintItemObj("默认打印数据测试"));
                    add(new PrintItemObj("默认打印数据测试"));
                    add(new PrintItemObj("默认打印数据测试"));
                    add(new PrintItemObj("打印数据字体放大",24));
                    add(new PrintItemObj("打印数据字体放大",24));
                    add(new PrintItemObj("打印数据字体放大",24));
                    add(new PrintItemObj("打印数据加粗",8,true));
                    add(new PrintItemObj("打印数据加粗",8,true));
                    add(new PrintItemObj("打印数据加粗",8,true));
                    add(new PrintItemObj("打印数据左对齐测试",8,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {

                }

                @Override
                public void onError(int arg0) throws RemoteException {

                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 打印位图
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午2:39:33
     */
    public void printBitmap(View v){
        try {
            InputStream ins = this.getAssets().open("bmp/canvas.bmp");
            Bitmap bmp = BitmapFactory.decodeStream(ins);
            this.printerDev.printBmp(30, bmp.getWidth(), bmp.getHeight(), bmp, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {

                }

                @Override
                public void onError(int arg0) throws RemoteException {

                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }





}
