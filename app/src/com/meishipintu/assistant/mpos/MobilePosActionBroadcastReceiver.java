package com.meishipintu.assistant.mpos;

import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.epos.bertlv.MisComm;
import com.epos.bertlv.MisDataResult;
import com.epos.bertlv.MisTLV;
import com.epos.bertlv.MisDataCenter;
import com.epos.bluetoothutils.RFComm;
import com.epos.utilstools.MPosConfig;
import com.meishipintu.assistant.app.Cookies;

public class MobilePosActionBroadcastReceiver extends BroadcastReceiver {
	public final static String		POS_CONSUMECMD="CONSUME";
	public final static String		POS_PRINTCATERING="PRINT";	
	public final static String		POS_PRINTSTATUS="PRINT_STATUS";
	public final static String 		BT_DISCONNECT="BT_DISCONNECT";
	/**
	 * 生产打印测试
	 */
	public final static String		POS_PRINTCATERING_TEST="PRINT_TEST";
	@Override
	public void onReceive(Context context, Intent intent) {
		if(!intent.getExtras().containsKey("POSCMD")&&!intent.getExtras().containsKey("DATA"))
			return;
		MPosConfig.m_strPosCmd=intent.getExtras().getString("POSCMD");
		MPosConfig.m_bytePosData=intent.getExtras().getByteArray("DATA");
		
		Log.e("POSCMD",MPosConfig.m_strPosCmd);		
		if (MPosConfig.m_strMAC.equals("")) {
			//add 生产测试增加 2014/03/31 add by ccf
			if((MPosConfig.m_strPosCmd.equals(POS_PRINTCATERING_TEST))&&!Cookies.getBluetoothMac().equals("")){
				Toast.makeText(context,"正在获取打印机状态...", Toast.LENGTH_SHORT).show();
				PrintAsyncTask asyncTask=new  PrintAsyncTask(context);
				asyncTask.execute("3");
			}//add end
			else{
			intent.setClass(context, ActBluetoothConfig.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
					| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			}
		} else {			
			if(MPosConfig.m_strPosCmd.equals(POS_CONSUMECMD)){  //束搏注掉
				//intent.setClass(context, MainActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
				//		| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				//		| Intent.FLAG_ACTIVITY_NEW_TASK);
				//context.startActivity(intent);
			}
			else{
			
				if(MPosConfig.m_strPosCmd.equals(POS_PRINTCATERING)){
					Toast.makeText(context,"正在发送打印数据...", Toast.LENGTH_SHORT).show();
					PrintAsyncTask asyncTask=new  PrintAsyncTask(context);
					asyncTask.execute("1");
				}
				else{
					if(MPosConfig.m_strPosCmd.equals(POS_PRINTSTATUS)){
						Toast.makeText(context,"正在获取打印机状态...", Toast.LENGTH_SHORT).show();
						PrintAsyncTask asyncTask=new  PrintAsyncTask(context);
						asyncTask.execute("2");
					}
					//add 生产测试增加 2014/03/31 add by ccf
					else{
						if(MPosConfig.m_strPosCmd.equals(POS_PRINTCATERING_TEST)){
							Toast.makeText(context,"正在获取打印机状态...", Toast.LENGTH_SHORT).show();
							PrintAsyncTask asyncTask=new  PrintAsyncTask(context);
							asyncTask.execute("3");
						}
						else{
							if(MPosConfig.m_strPosCmd.equals(BT_DISCONNECT)){
								PrintAsyncTask asyncTask=new  PrintAsyncTask(context);
								asyncTask.execute("4");
							}
						}
					}
					//end
				}
			}
		}		
	}
	/**
	 * String 1打印 2打印状态 3 打印测试
	 * @author user
	 *
	 */
	public  class PrintAsyncTask extends AsyncTask<String, Integer, Void>{
		Context context;
		public PrintAsyncTask(Context context) {
			this.context=context;
		}

		@Override
		protected Void doInBackground(String... params) {
			String strAction=params[0];
			if(strAction==null) return null;
			MisDataResult dataResult=null;
			int nAction=Integer.valueOf(strAction);
			switch(nAction){
				case 1:
					byte[] bytePrintTest = MPosConfig.m_bytePosData;
					dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintCateringInfoArray(bytePrintTest).getEncoder());
					Log.e("打印结果", dataResult.strMsg);
					break;
				case 2:
					dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintInfoArray().getEncoder());
					if(dataResult.bSuccess==false){
						SystemClock.sleep(1000*6);
						dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintInfoArray().getEncoder());
					}
					String strTLVValue="";
					byte[] byteData=null;
					//byteData=new byte[]{0x01,0x02};
					if(dataResult.tlvList!=null){
						int n=dataResult.tlvList.size();
						for(int i=0;i<n;i++){
							MisTLV misTLV=dataResult.tlvList.get(i);
							if(Arrays.equals(misTLV.getmTag(), new byte[]{(byte) 0xFF,(byte) 0x84})){
							strTLVValue="返回值:    "+String.format("%02X", misTLV.getmData()[0]);
							Log.e("打印机状态", strTLVValue);
							byteData=misTLV.getmData();
							break;	
							}									
						}
					}
					Intent intent = new Intent("pos_android.intent.action.printstatus_broadcast");
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);				
					intent.putExtra("POSCMD", POS_PRINTSTATUS);
					if(byteData!=null){						
					    intent.putExtra("DATA", byteData);
					}
					else{
						intent.putExtra("DATA", new byte[]{(byte) 0xFF});
					}
					context.sendBroadcast(intent);
					break;
				case 3:				
					if(Cookies.getBluetoothMac().equals("")){
						Log.e("错误","蓝牙配置信息无内容!");
						return null;
					}
					if(MPosConfig.m_strMAC.equals("")){
						MPosConfig.m_strMAC=Cookies.getBluetoothMac();
					}
					dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintInfoArray().getEncoder());										
					String strTLVValueTest="";
					byte[] byteDataTest=null;
					//byteData=new byte[]{0x01,0x02};
					if(dataResult.tlvList!=null){
						int n=dataResult.tlvList.size();
						for(int i=0;i<n;i++){
							MisTLV misTLV=dataResult.tlvList.get(i);
							if(Arrays.equals(misTLV.getmTag(), new byte[]{(byte) 0xFF,(byte) 0x84})){
							strTLVValueTest="返回值:    "+String.format("%02X", misTLV.getmData()[0]);
							Log.e("打印机状态", strTLVValueTest);
							byteDataTest=misTLV.getmData();
							break;	
							}									
						}
					}
					Intent intentTest = new Intent("pos_android.intent.action.printstatus_broadcast");
					intentTest.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);				
					intentTest.putExtra("POSCMD", POS_PRINTSTATUS);
					if(byteDataTest!=null){						
					    intentTest.putExtra("DATA", byteDataTest);
					}
					else{
						intentTest.putExtra("DATA", new byte[]{(byte) 0xFF});
						//140402 add
						intentTest.putExtra("ERROR",RFComm.mErrInfo);//增加了这行，输出错误
					}
					context.sendBroadcast(intentTest);					
					break;
				case 4:
					MPosConfig.m_rfcComm.colse();
					break;
			}
			MPosConfig.m_strPosCmd="";
			return null;
		}		
	 }
	/*
	public  class PrintStatusAsyncTask extends AsyncTask<Void, Integer, Void>{
		Context context;
		public PrintStatusAsyncTask(Context context) {
			this.context=context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			MisDataResult dataResult=null;
			dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintInfoArray().getEncoder());
			int n=dataResult.tlvList.size();
			String strTLVValue="";
			byte[] byteData=null;
			//byteData=new byte[]{0x01,0x02};			
			for(int i=0;i<n;i++){
				MisTLV misTLV=dataResult.tlvList.get(i);
				if(Arrays.equals(misTLV.getmTag(), new byte[]{(byte) 0xFF,(byte) 0x84})){
					strTLVValue="返回值:    "+String.format("%02X", misTLV.getmData()[0]);
					Log.e("打印机状态", strTLVValue);
					byteData=misTLV.getmData();
					break;	
				}									
			}			
			if(byteData!=null){
				
				Intent intent = new Intent("pos_android.intent.action.printstatus_broadcast");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);				
				intent.putExtra("POSCMD", POS_PRINTSTATUS);
			    intent.putExtra("DATA", byteData);
			    context.sendBroadcast(intent);
			}			
			return null;
		}		
	 }
	*/
	//打印测试同步任务,2014/03/31 add by ccf
	/*
	public  class PrintAsyncTask_ForTest extends AsyncTask<Void, Integer, Void>{
		Context context;
		public PrintAsyncTask_ForTest(Context context) {
			this.context=context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			MisDataResult dataResult=null;			
			MainApp mainApp = (MainApp) context.getApplicationContext();
			if(mainApp==null) return null;
			NonVolatile nv =NonVolatile.getInstance(context); 
			nv.load();
			if(nv.bluetoothMac.equals("")){
				Log.e("错误","蓝牙配置信息无内容!");
				return null;
			}
			if(mainApp.m_strMAC.equals("")){
				mainApp.m_strMAC=nv.bluetoothMac;
			}
			byte[] bytePrintTest = mainApp.m_bytePosData;
			dataResult=MisDataCenter.dataSwitch(context,MisComm.getPrintCateringInfoArray(bytePrintTest).getEncoder());
			Log.e("打印结果", dataResult.strMsg);
			return null;
		}		
	 }
	*/
	
}
