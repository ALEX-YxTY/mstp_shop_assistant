package com.meishipintu.assistant.mpos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.Toast;
import com.epos.utilstools.MPosConfig;

public class BtNotificationBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
			switch (state) {
            case BluetoothAdapter.STATE_OFF:
            	Toast.makeText(context,"蓝牙被关闭,断开连接", Toast.LENGTH_SHORT).show();
            	MPosConfig.m_rfcComm.colse();
                break;            
            case BluetoothAdapter.STATE_ON:            	
            	Toast.makeText(context,"蓝牙连接打开,恢复连接", Toast.LENGTH_SHORT).show();            	
            	connectAsyncTask asyncTask=new  connectAsyncTask(context);
            	asyncTask.execute();
                break;            
            }
			return;
		}
		
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
           
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        	//Toast.makeText(context,"蓝牙已连接", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        	
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
        	
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
        	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        	if(btAdapter==null){
        		MPosConfig.m_rfcComm.colse();
        		Toast.makeText(context,"未发现蓝牙设备", Toast.LENGTH_SHORT).show();
        		return;
        	}
        	else{
        		if (!btAdapter.isEnabled()) {
        			MPosConfig.m_rfcComm.colse();
        			Toast.makeText(context,"蓝牙被关闭,请打开蓝牙", Toast.LENGTH_SHORT).show();        			
        			return;
        	    }        		
        		Toast.makeText(context,"蓝牙连接断开,重新连接", Toast.LENGTH_SHORT).show();
        		MPosConfig.m_rfcComm.colse();
        		
            	connectAsyncTask asyncTask=new  connectAsyncTask(context);
            	asyncTask.execute();
        	}        	
        }
	}
	public  class connectAsyncTask extends AsyncTask<Void, Void, Integer>{
		Context context;
		public connectAsyncTask(Context context) {
			this.context=context;
		}
		@Override
		protected Integer doInBackground(Void... params) {
			SystemClock.sleep(1000*1);					
			boolean bSuccess=false;
			if(!MPosConfig.m_strMAC.equals(""))
				bSuccess=MPosConfig.m_rfcComm.connect(MPosConfig.m_strMAC);			
			if(bSuccess)
				return 0;
			else
				return -1;
		}
		@Override
		protected void onPostExecute(Integer result) {
			if(result==0)
				Toast.makeText(context,"蓝牙重新连接成功", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(context,"蓝牙重新连接失败", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}		
	 }

}
