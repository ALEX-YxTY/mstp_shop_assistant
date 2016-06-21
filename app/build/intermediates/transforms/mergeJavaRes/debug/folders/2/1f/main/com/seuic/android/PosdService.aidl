package com.seuic.android;

interface PosdService {
	String getServiceName();
	String getMcuInfo();
	int getMcuStatus();
	boolean isBatteryLow();
	IBinder getMagCardReader();
	IBinder getICCardReader();
	IBinder getPrinter();
	IBinder getPinpad();
	IBinder getSuperPinpad();
	IBinder getFactoryFunc();
	IBinder getTerminalInfo();
	IBinder getServerCert();
	IBinder getTermCert();
	IBinder getHash();
	boolean isMcuUpdateing();
}
