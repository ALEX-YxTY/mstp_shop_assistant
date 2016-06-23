package com.meishipintu.assistant.dao;

public interface NetCallBack<T> {
	void onSuccess(T data);

	void onError(String error);
}
