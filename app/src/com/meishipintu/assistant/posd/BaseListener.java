package com.meishipintu.assistant.posd;

public interface BaseListener<T> {
	public void onSuccess(T response);
	public void onFailure(int errcode, String errmsg);
}
