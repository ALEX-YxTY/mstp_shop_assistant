package com.seuic.android;
import com.seuic.android.PrinterListener;
import com.seuic.android.PrintQRCodeListener;
import com.seuic.android.PrintBarCodeListener;
import com.seuic.android.PrintImageListener;

interface Printer {
	int startPrint( PrinterListener listener, in byte[] print_data );
	int setGray( byte greyval );
	int printQRCode( PrintQRCodeListener listener, String str, int qr_size, int qr_level );
	int printBarCode( PrintBarCodeListener listener, String str, int type, int width, int height );
	int printImage( PrintImageListener listener, in byte[] image_data, int width, int height );
}
