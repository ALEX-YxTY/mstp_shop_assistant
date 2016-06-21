package com.meishipintu.assistant.posd;

import android.os.RemoteException;
/**
 * 负责终端打印
 * 
 * @author FengPeng
 * 
 */
public class PosdTerminalTrans {
	/* 属性 */
	private final PosdConnector mPosdConnector;
	private final static PosdTerminalTrans instance = new PosdTerminalTrans();

	/* 状态 */
	public static enum TRANS_STATE {
		FREE, CARD_READING, PIN_INPUTING, TRANS_FINISHING, PRINTING, BUSY
	}

	private PosdTerminalTrans() {
		mPosdConnector = PosdConnector.getInstance();
	}

	public static PosdTerminalTrans build() {
		return instance;
	}
	
	/* 打印**** */
	public void startPrint(PrintAction action, PrintTaskListener listener) {
		int result = -101;
		try {
			if (null == mPosdConnector.printer) {
				listener.onFailure(-1, "printer is null");
				return;
			}
			switch (action.getPrintType()) {
			case TEXT:
				result = mPosdConnector.printer.startPrint(listener.getAIDLTextListener(), action.getPrintBytes());
				PosdLog.v("print ret(TEXT):" + result);
				break;
			case IMAGE:
				result = mPosdConnector.printer.printImage(listener.getAIDLImageListener(), action.getImageBytes(),
						action.getImageWidth(), action.getImageHeight());
				PosdLog.v("print ret(IMAGE):" + result);
				break;
			case QRCODE:
				result = mPosdConnector.printer.printQRCode(listener.getAIDLQRCodeListener(), action.getCodeStr(),
						action.getQRSize(), action.getQRLevel());
				PosdLog.v("print ret(QRCODE):" + result);
				break;
			case BARCODE:
				result = mPosdConnector.printer.printBarCode(listener.getAIDLBarCodeListener(), action.getCodeStr(),
						action.getBarType(), action.getBarWidth(), action.getBarHeight());
				PosdLog.v("print ret(BARCODE):" + result);
				break;
			default:
				break;
			}
			if (0 != result) {
				String errmsg = PrintTaskListener.PRINT_FAILED;
				int errcode = -3;
				if (-1 == result) {
					errcode = -2;
					errmsg = PrintTaskListener.PRINT_OUT_OF_PAPER;
				} else if (-2 == result) {
					errcode = -1;
					errmsg = PrintTaskListener.PRINT_LOW_POWER;
				}
				listener.onFailure(errcode, errmsg);
			}
		} catch (RemoteException e) {
			listener.onFailure(-1, "remote exception");
			e.printStackTrace();
		}
	}
	
	public void startPrint(final PrintAction[] action, final PrintTaskListener listener){
		listener.setQueueTask(new PrintTaskListener.QueueTask() {
			int index = 0;
			@Override
			public boolean onQueueTask() {
				if(++index==action.length){
					return true;
				}
				startPrint(action[index], listener);
				return false;
			}
		});
		startPrint(action[0], listener);
	}
}