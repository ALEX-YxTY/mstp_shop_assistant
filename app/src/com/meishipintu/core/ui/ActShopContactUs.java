package com.meishipintu.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.core.utils.ConstUtil;
import com.umeng.analytics.MobclickAgent;

public class ActShopContactUs extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_contact_us);
        TextView tv = (TextView) findViewById(R.id.tv_title);
        tv.setText(getString(R.string.shop_contact_us));
        findViewById(R.id.btn_back).setOnClickListener(ll);
        findViewById(R.id.rl_tel).setOnClickListener(ll);
        findViewById(R.id.rl_email).setOnClickListener(ll);
        findViewById(R.id.rl_visit_website).setOnClickListener(ll);
    }

    private OnClickListener ll = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_back:
            	finishAndAni();
                break;
            case R.id.rl_tel:
                try {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL,

                    Uri.parse("tel:" + ConstUtil.getServiceTel().replace("-", "")));
                    startActivity(phoneIntent);
                } catch (Exception e) {
                    Toast.makeText(ActShopContactUs.this, R.string.missing_tel_application, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rl_email:
                try {
                    Intent data = new Intent(Intent.ACTION_SENDTO);
                    data.setType("plain/text");
                    data.setData(Uri.parse("mailto:" + ConstUtil.getServiceMailbox()));
                    Intent mailer = Intent.createChooser(data, getString(R.string.choose_send_email));
                    data.putExtra(Intent.EXTRA_SUBJECT, "米来商户问题反馈");  
                    data.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(mailer);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActShopContactUs.this, R.string.missing_mail_application, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rl_visit_website:
                try {
                    Uri uri = Uri.parse(ConstUtil.getCompanyWebsite());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ActShopContactUs.this, R.string.missing_browser_application, Toast.LENGTH_SHORT)
                            .show();
                }

                break;

            default:
                break;
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    
    @Override
	public void onBackPressed() {
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
