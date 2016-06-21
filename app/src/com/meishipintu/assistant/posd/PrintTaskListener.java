package com.meishipintu.assistant.posd;

import com.seuic.android.PrintBarCodeListener;
import com.seuic.android.PrintImageListener;
import com.seuic.android.PrintQRCodeListener;
import com.seuic.android.PrinterListener;

import android.os.Message;
import android.os.RemoteException;

public abstract class PrintTaskListener extends TransCallback implements BaseListener<Byte> {

	/* 打印文字监听接口 */
	private final PrinterListener.Stub mAIDLTextListener = new PrinterListener.Stub() {

		@Override
		public void OnPrintSuccess() throws RemoteException {
			mHandler.obtainMessage(WHAT_SUCCESS).sendToTarget();
		}

		@Override
		public void OnPrintFail(int returnCode) throws RemoteException {
			mHandler.obtainMessage(WHAT_FAILURE, returnCode, -1).sendToTarget();
		}
	};

	/* 打印图片监听接口 */
	private final PrintImageListener.Stub mAIDLImageListener = new PrintImageListener.Stub() {

		@Override
		public void OnSuccess() throws RemoteException {
			mHandler.obtainMessage(WHAT_SUCCESS).sendToTarget();
		}

		@Override
		public void OnFail(int returnCode) throws RemoteException {
			mHandler.obtainMessage(WHAT_FAILURE, returnCode, -1).sendToTarget();
		}
	};

	/* 打印二维码监听接口 */
	private final PrintQRCodeListener.Stub mAIDLQRCodeListener = new PrintQRCodeListener.Stub() {

		@Override
		public void OnSuccess() throws RemoteException {
			mHandler.obtainMessage(WHAT_SUCCESS).sendToTarget();
		}

		@Override
		public void OnFail(int returnCode) throws RemoteException {
			mHandler.obtainMessage(WHAT_FAILURE, returnCode, -1).sendToTarget();
		}
	};

	/* 打印条形码监听接口 */
	private final PrintBarCodeListener.Stub mAIDLBarCodeListener = new PrintBarCodeListener.Stub() {

		@Override
		public void OnSuccess() throws RemoteException {
			mHandler.obtainMessage(WHAT_SUCCESS).sendToTarget();
		}

		@Override
		public void OnFail(int returnCode) throws RemoteException {
			mHandler.obtainMessage(WHAT_FAILURE, returnCode, -1).sendToTarget();
		}
	};

	public PrinterListener.Stub getAIDLTextListener() {
		return mAIDLTextListener;
	}

	public PrintImageListener.Stub getAIDLImageListener() {
		return mAIDLImageListener;
	}

	public PrintQRCodeListener.Stub getAIDLQRCodeListener() {
		return mAIDLQRCodeListener;
	}

	public PrintBarCodeListener.Stub getAIDLBarCodeListener() {
		return mAIDLBarCodeListener;
	}

	public static final String PRINT_FAILED = "print failed.";
	public static final String PRINT_OUT_OF_PAPER = "printer is out of paper.";
	public static final String PRINT_LOW_POWER = "low power can not be printed.";

	public interface QueueTask {
		public boolean onQueueTask();
	}

	private QueueTask mQueueTask;

	protected void setQueueTask(QueueTask task) {
		mQueueTask = task;
	}

	@Override
	public boolean handleMessage(Message message) {
		switch (message.what) {
		case WHAT_SUCCESS:
			boolean last = true;
			if (null != mQueueTask) {
				last = mQueueTask.onQueueTask();
			}
			if (last) {
				onSuccess((byte) 0);
			}
			break;
		case WHAT_FAILURE:
			String errmsg = PRINT_FAILED;
			int errcode = message.arg1;
			if (-1 == errcode) {
				errmsg = PRINT_LOW_POWER;
			} else if (-2 == errcode) {
				errmsg = PRINT_OUT_OF_PAPER;
			}
			onFailure(errcode, errmsg);
			break;
		default:
			break;
		}
		return false;
	}
}