package com.meishipintu.assistant.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.meishipintu.assistant.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2016/3/29 0029.
 */
public class RegisterActivity extends Activity implements View.OnClickListener{
    private Button btn_back;
    private ProgressBar myProgressBar;
    private WebView wb_shop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn_back= (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        myProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
        wb_shop = (WebView) findViewById(R.id.wb_shop);
        wb_shop.getSettings().setJavaScriptEnabled(true);

        wb_shop.setWebViewClient(new MyWebViewClient());
        wb_shop.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (100 == newProgress) {
                    myProgressBar.setVisibility(View.GONE);
                } else {
                    myProgressBar.setProgress(newProgress);
                }
            }
        });
        wb_shop.loadUrl("http://fucai.milaipay.com/download/test.html");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
        }
    }
    private class MyWebViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wb_shop.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wb_shop.reload();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wb_shop.loadData("", "text/html", "utf-8");
    }
}
