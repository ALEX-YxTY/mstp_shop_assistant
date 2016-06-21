package com.meishipintu.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.utils.StringUtil;

public class ActWebView extends Activity {
    private WebView mWebView;
    private String mLink = null;
    private String mErrorLink = "file:///android_asset/error.html";    
    String mTitle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Intent in = getIntent();
       
        if (in.hasExtra("linkTitle")){
        	mTitle = in.getStringExtra("linkTitle");
        }
        if (in.hasExtra("link")){
        	mLink = in.getStringExtra("link");
        }
        final TextView tvTilte = (TextView) findViewById(R.id.tv_title);
        tvTilte.setText(mTitle);
        
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.requestFocus();
        if (!StringUtil.isNullOrEmpty(mLink))
            mWebView.loadUrl(mLink);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            
            //网页加载失败时调用，隐藏加载提示旋转进度条
            @Override
            public void onReceivedError(WebView view, int errorCode,String description,String failingUrl) {
            	super.onReceivedError(view, errorCode, description, failingUrl);
            	view.loadUrl(mErrorLink);
            }             

        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) { // 这里是设置activity的标题， 也可以根据自己的需求做一些其他的操作
                    tvTilte.setText(mTitle);
                } else {
                    tvTilte.setText(getString(R.string.loading) + newProgress + "%");

                }
            }

        });
        findViewById(R.id.btn_back).setOnClickListener(listener);
    }

    private OnClickListener listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_back:
//                goBack();
            	finish();
                break;
            }
        }

        private void goBack() {
            if (mWebView.canGoBack()&&!mWebView.getUrl().equals(mErrorLink))
                mWebView.goBack();
            else
                finish();
        };
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()&&!mWebView.getUrl().equals(mErrorLink))
                mWebView.goBack();
            else
                finish();
            return true;
        }
        return false;
    }

}
