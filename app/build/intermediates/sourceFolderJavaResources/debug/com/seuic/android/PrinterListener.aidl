package com.seuic.android;

interface PrinterListener {
	void OnPrintSuccess();
	void OnPrintFail( int returnCode );
}
