package com.seuic.android;

interface PrintBarCodeListener {
	void OnSuccess();
	void OnFail( int returnCode );
}
