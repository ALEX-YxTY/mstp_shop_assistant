package com.meishipintu.core.utils;

import java.util.List;

import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;

import android.graphics.Bitmap;
import android.os.RemoteException;

public class PrintUtils {

	public static void printText(AidlPrinter printerDev, List<PrintItemObj> list, AidlPrinterListener listener) {
		try {
			printerDev.printText(list, listener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void printBitmap(AidlPrinter printerDev, Bitmap bmp, AidlPrinterListener listener) {
		try {
			printerDev.printBmp(30, bmp.getWidth(), bmp.getHeight(), bmp, listener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
