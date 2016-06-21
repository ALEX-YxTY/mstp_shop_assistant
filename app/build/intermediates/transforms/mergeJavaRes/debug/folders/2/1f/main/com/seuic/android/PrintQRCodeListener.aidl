package com.seuic.android;

interface PrintQRCodeListener {
	void OnSuccess();
	void OnFail( int returnCode );
}
