package com.meishipintu.assistant.posd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.seuic.android.PosdService;
import com.seuic.android.Printer;
/**
 * PosdConnector负责维护PosdService连接及生命周期管理
 * 
 * @author FengPeng
 * 
 */
public class PosdConnector {
	/* AIDL接口 */
	protected PosdService posdService = null;
	protected Printer printer = null;
	/* 监听器 */
	private BaseListener<String> lStartServiceListener;
	/* 属性 */
	private boolean connected;
	private Context mContext;
	private final static PosdConnector instance = new PosdConnector();

	private PosdConnector(){};
	public static PosdConnector getInstance() {
		return instance;
	};

	private ServiceConnection posdConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			connected = false;
			if (posdConnection != null) {
				printer = null;
			} else {
				if (lStartServiceListener != null) {
					lStartServiceListener.onFailure(-1, "posdservice disconnect failed");
					lStartServiceListener = null;
				}
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			posdService = PosdService.Stub.asInterface(binder);
			if (posdConnection != null) {
				try {
					printer = Printer.Stub.asInterface(posdService.getPrinter());
					connected = true;
					if (lStartServiceListener != null) {
						lStartServiceListener.onSuccess("posdservice bind success");
						lStartServiceListener = null;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					if (lStartServiceListener != null) {
						lStartServiceListener.onFailure(-1, "posdservice bind failed=" + e.getMessage());
						lStartServiceListener = null;
					}
				}
			} else {
				if (lStartServiceListener != null) {
					lStartServiceListener.onFailure(-1, "posdservice bind failed");
					lStartServiceListener = null;
				}
			}
		}
	};

	public void connect(Context context, BaseListener<String> listener) {
		if (connected) {
			return;
		}
		mContext = context;
		lStartServiceListener = listener;
		int type = Context.BIND_AUTO_CREATE;
		Boolean ret = mContext.bindService(new Intent("com.seuic.android.PosdService"), posdConnection, type);
		if (!ret) {
			if (lStartServiceListener != null) {
				lStartServiceListener.onFailure(-1, "bindService failed");
				lStartServiceListener = null;
			}
		}
	}

	public void disconnect() {
		if (!connected) {
			return;
		}
		connected = false;
		mContext.unbindService(posdConnection);
	}

	public Boolean connected() {
		return connected;
	}
}