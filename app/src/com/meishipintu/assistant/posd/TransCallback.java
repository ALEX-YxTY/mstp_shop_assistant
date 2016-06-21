package com.meishipintu.assistant.posd;

import android.os.Handler;

/**
 * POSD回调基类
 * @author FengPeng
 *
 */
abstract class TransCallback implements Handler.Callback {
	/* Handler代码 */
	protected final static int WHAT_PROCESS = 100;
	protected final static int WHAT_FAILURE = 101;
	protected final static int WHAT_SUCCESS = 102;

	protected final Handler mHandler = new Handler(this);
	private boolean isWorking = false;

	protected void setWorkState(boolean value) {
		isWorking = value;
	}

	public boolean isWorking() {
		return isWorking;
	}
}