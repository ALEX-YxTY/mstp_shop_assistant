package com.meishipintu.assistant.mpos;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.epos.bertlv.MisComm;
import com.epos.bertlv.MisDataResult;
import com.epos.bertlv.MisDataCenter;
import com.epos.utilstools.MPosConfig;
import com.meishipintu.assistant.mpos.BtNotificationBroadcastReceiver;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;

public class ActBluetoothConfig extends Activity{		
	public static		Activity 					mActivity;
	
	private final int 					REQUEST_BT_DISCOVER =0;	
	
	private final int					CONST_CONFIRM_BLUETOOLTH_ALTERDIALOG=0;
	
	private Spinner 					m_spinner_Device;
	private ArrayAdapter<String> 		m_adapter_Device;
	
	private Button 						m_btn_Device;
	private Button 						m_btn_Init;
	private ProgressDialog 				m_progressDialog;
	@SuppressLint({ "HandlerLeak", "HandlerLeak" })
	private Handler m_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			m_progressDialog.dismiss();	
			String strInfo=msg.getData().getString("INFO");
			switch(msg.what){
			case 0:
				startMain();
				break;
			case 1:				
				showDialog(-1, "提示",strInfo);		 //"蓝牙通讯失败,请检查!"		
				break;			
			}
		}
	};
	
	private void processThread() {

		m_progressDialog =new ProgressDialog(ActBluetoothConfig.this);
		m_progressDialog.setMessage("正在测试蓝牙通讯,请稍候...");
		m_progressDialog.setCancelable(false);
		m_progressDialog.setCanceledOnTouchOutside(false);
		m_progressDialog.show();
		new Thread() {
			public void run() {				
				MisDataResult dataResult=MisDataCenter.dataSwitch(ActBluetoothConfig.this,MisComm.getConnectArray().getEncoder());
				//MisDataResult dataResult=new MisDataResult();
				//MisDataResult dataResult=dataSwitch(mActivity,new byte[]{0x02,0x32,0x30,0x31,0x33,0x31,0x32,0x32,0x39,0x31,0x38,0x32,0x34,0x00,0x13,0x7e,0x12,0x00,0x00,0x00,0x05,(byte) 0xd2,0x0e,0x00,0x01,0x01,0x00,0x00,0x03,(byte) 0xa0});
				boolean bRet=dataResult.bSuccess;
				Bundle bundle=new Bundle();				
				Message msg = new Message();
				if(bRet){					
					msg.what =0;
				}
				else
					msg.what =1;
				
				//String strTest=MisDataCenter.dataSwitchTest(m_activity, new byte[]{0x31,0x32});
				//msg.what =1;
				//bundle.putString("INFO",strTest);
				bundle.putString("INFO",dataResult.strMsg);
				msg.setData(bundle);
				m_handler.sendMessage(msg);
				//m_handler.sendEmptyMessage(0);
			}
		}.start();
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_bluetooth_config);
        //
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction(); 
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                Log.w("", "Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;       
            }
        }
        mActivity=this;
        
		findViewById(R.id.btn_back).setOnClickListener(mClkListener);
        //
        m_btn_Device= (Button) this.findViewById(R.id.button_Device);
        m_btn_Device.setOnClickListener(new button_Device_OnClickListener());
        
        m_adapter_Device = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        m_adapter_Device.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        m_spinner_Device = (Spinner) this.findViewById(R.id.spinner_Device);
        m_spinner_Device.setAdapter(m_adapter_Device);
        m_spinner_Device.setOnItemSelectedListener(new spinner_Device_OnItemSelectedListener());
        
        m_btn_Init= (Button) this.findViewById(R.id.button_Init);
        m_btn_Init.setOnClickListener(new button_Init_OnClickListener());
        
        //
        if(BluetoothAdapter.STATE_ON==BluetoothAdapter.getDefaultAdapter().getState()){
        	updateBtMacView();
        }
        
    }
    
	private OnClickListener mClkListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {						
			switch (v.getId()) {
							
			case R.id.btn_back:
				finishAndAni();
				break;			
			}
		}
	};
	
    class button_Device_OnClickListener implements OnClickListener {
		public void onClick(View v) {
			Intent intent;
			intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
			startActivityForResult(intent, REQUEST_BT_DISCOVER);			
		}
	}
    public class spinner_Device_OnItemSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,long arg3) {
			/*String strDevice=parent.getItemAtPosition(pos).toString();
			String[] btInfo = strDevice.split("\n");
			Cookies.saveBluetoothAddr(btInfo[0], btInfo[1]);
			*/
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// nothing to do
		}
	}
    class button_Init_OnClickListener implements OnClickListener {
		public void onClick(View v) {
			
			if(BluetoothAdapter.STATE_ON!=BluetoothAdapter.getDefaultAdapter().getState()){
				showDialog(CONST_CONFIRM_BLUETOOLTH_ALTERDIALOG, "提示", "请打开蓝牙设备!");
				return;
			}
			else{
				 //updateBtMacView();	 
			}
			if(Spinner.INVALID_POSITION==m_spinner_Device.getSelectedItemPosition()){
				showDialog(-1, "提示", "请选择蓝牙设备!");
				return;
			}
			else{
				String strDevice=m_spinner_Device.getSelectedItem().toString();
				String[] btInfo = strDevice.split("\n");
				Cookies.saveBluetoothAddr(btInfo[0], btInfo[1]);
				MPosConfig.m_strMAC=btInfo[1];  //设置蓝牙地址
			}
			processThread();
			//startMain();
			/*Intent intent;
			intent = new Intent(); 
			intent.setClass(getApplicationContext(),ElectricSignatureActivity.class);
			startActivity(intent);
			*/
		}
	}
    void showDialog(int nConfirmDialogID,String strTitle, String strMessage) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(strTitle);
		builder.setMessage(strMessage);
		switch(nConfirmDialogID){
        case -1:
        	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {					
					dialog.dismiss();
					return;
				}
			});
        	break;
        case CONST_CONFIRM_BLUETOOLTH_ALTERDIALOG:
        	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent;
					intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
					startActivityForResult(intent, REQUEST_BT_DISCOVER);
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
    

	private void updateBtMacView(){
	    int nDefaultSel=0,nPos=0;	    
	    m_adapter_Device.clear();
	    Set<BluetoothDevice> btBondedDevs = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
	    if(btBondedDevs==null) return;	    	    
	    for (BluetoothDevice bondedDev : btBondedDevs){
	    	m_adapter_Device.add(bondedDev.getName() + "\n" + bondedDev.getAddress());
	    	if(bondedDev.getAddress().equals(Cookies.getBluetoothMac()))
	        	nDefaultSel=nPos;
	        nPos++;	        
	        
	    }	
	    m_adapter_Device.notifyDataSetChanged();
	    
	    m_spinner_Device.setSelection(nDefaultSel);
	    
	}

	private void startMain(){
		/*
		Intent intent = new Intent();
		intent.setClass(mActivity,UserManagerActivity.class);
		Bundle bundle = new Bundle(); 
        bundle.putBoolean("Start", true);
        intent.putExtras(bundle);
        */
		//
		if(MPosConfig.m_strPosCmd.equals(MobilePosActionBroadcastReceiver.POS_PRINTCATERING)){
			Intent intent = new Intent("pos_android.intent.action.my_broadcast");
			//intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);//必须加，如果农行银行交易程序终止，会启动不起来
			intent.putExtra("POSCMD", MPosConfig.m_strPosCmd);				  	  //交易指令
		    intent.putExtra("DATA",MPosConfig.m_bytePosData);
		    sendBroadcast(intent);			
		}
		else{
			if(MPosConfig.m_strPosCmd.equals(MobilePosActionBroadcastReceiver.POS_PRINTSTATUS)){
				//((MainApp)getApplication()).m_strMAC="111111";
				Intent intent = new Intent("pos_android.intent.action.my_broadcast");
				intent.putExtra("POSCMD", MPosConfig.m_strPosCmd);				  	  //交易指令
			    intent.putExtra("DATA",MPosConfig.m_bytePosData);
			    sendBroadcast(intent);			
			}
		}
		//
		ComponentName component=new ComponentName(this, BtNotificationBroadcastReceiver.class);
	    getPackageManager()
	        .setComponentEnabledSetting(component,
	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	    
		Toast.makeText(ActBluetoothConfig.this, "蓝牙启动成功",	Toast.LENGTH_SHORT).show();
		
		finishAndAni();		
	}
	
	protected void finishAndAni() {
		//setResult(RESULT_OK);
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {	
		case REQUEST_BT_DISCOVER:	    			
	        updateBtMacView();	              
	        break;
		}
	}

	public static void showByteInfo(String strTag,byte [] byteInfo){
		String strDisp="";
		for(int i=0;i<byteInfo.length;i++){
			strDisp=strDisp+String.format("%2X", byteInfo[i]).replace(" ","0")+" ";
		}
		Log.e(strTag, strDisp);
	}
	//

}
