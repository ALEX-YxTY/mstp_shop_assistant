package com.meishipintu.assistant.ui;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.app.MsptApplication;
import com.meishipintu.assistant.mpos.ActMPosSetting;
import com.meishipintu.assistant.orderdish.ActSelectTable;
import com.meishipintu.assistant.ui.auth.ActBindShop;
import com.meishipintu.assistant.ui.auth.ActChangePwd;
import com.milai.asynctask.PostGetTask;
import com.meishipintu.core.ui.ActAboutUs;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.Des2;
import com.milai.download.DownloadMgr;
import com.meishipintu.core.utils.MyDialogUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class ActSetting extends FragmentActivity {

	public long mTableId = 0;
	public String mTableName = null;
	public static String LOGIN_INFO_FILE = "account_info";
	private CheckBox check_table = null;
	private SharedPreferences settings = null;
	private TextView setting_table = null;
	private Editor mEditor = null;
	private int mWaitorType = -1;
	private int mBoundResult = 0;
	private EditText mEtPassword = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settting);
		settings = MsptApplication.getInstance().getSharedPreferences(
				LOGIN_INFO_FILE, Context.MODE_PRIVATE);
		mEditor = settings.edit();
		mWaitorType = Cookies.getWaitorType();
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.settings));
		Button bt = (Button) findViewById(R.id.btn_back);
		bt.setOnClickListener(ll);

		
		check_table = (CheckBox) findViewById(R.id.Check_table);
		setting_table = (TextView) findViewById(R.id.textViewtable);
		if (settings.getBoolean("settable", false)) {
			check_table.setChecked(true);
			setting_table.setText("已绑定"
					+ settings.getString("tableName", "未绑定桌号"));
		} else {
			setting_table.setText("未绑定桌号");
			check_table.setChecked(false);
		}
		check_table.setOnClickListener(ll);

		findViewById(R.id.rl_about).setOnClickListener(ll);
		findViewById(R.id.rl_clear).setOnClickListener(ll);
		findViewById(R.id.rl_check_update).setOnClickListener(ll);
		findViewById(R.id.rl_change_pwd).setOnClickListener(ll);
		findViewById(R.id.rl_bind_shop).setOnClickListener(ll);
		findViewById(R.id.rl_mpos).setOnClickListener(ll);
		
		int shopCategory=Cookies.getShopCategory();
		if(shopCategory!=1)
		{
			findViewById(R.id.rl_table).setVisibility(View.GONE);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private OnClickListener ll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {

			case R.id.rl_about:
				intent.setClass(ActSetting.this, ActAboutUs.class);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.rl_clear:
				showDialogClear();
				break;
			case R.id.rl_change_pwd:
				intent.setClass(ActSetting.this, ActChangePwd.class);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;

			case R.id.rl_check_update:
				doCheckUpdate();
				break;

			case R.id.rl_bind_shop:
				intent.setClass(ActSetting.this, ActBindShop.class);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;

			case R.id.rl_mpos:
				intent.setClass(ActSetting.this, ActMPosSetting.class);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.Check_table: {
				showBoundPassword();
			}
				break;
			default:
				break;
			}

		}
	};

	protected void showDialogClear() {

		MyDialogUtil closeDialog = new MyDialogUtil(ActSetting.this) {

			@Override
			public void onClickPositive() {
				clearCache();
			}

			@Override
			public void onClickNagative() {

			}
		};
		closeDialog.showCustomMessage(getString(R.string.notice),
				getString(R.string.prompt_clear_cache),
				getString(R.string.confirm), getString(R.string.cancel));
	}

	private void showBoundPassword() {

		final String password = Des2.decodeValue("meishipintu",
				Cookies.getPassword());
		mEtPassword = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(ActSetting.this);
		builder.setTitle("请输入密码")
				.setView(mEtPassword)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String et_pwd = mEtPassword.getText().toString();
						if (et_pwd.equals(password)) {
							mBoundResult = 1;
							boundTable();
						} else {
							Toast.makeText(getBaseContext(), "密码错误！请联系服务员更改绑定",
									Toast.LENGTH_LONG).show();
							if (check_table.isChecked()) {
								check_table.setChecked(false);
							} else {
								check_table.setChecked(true);
							}
							mBoundResult = 0;
						}
					}

				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mBoundResult = 0;
						if (check_table.isChecked()) {
							check_table.setChecked(false);
						} else {
							check_table.setChecked(true);
						}
					}
				}).show();
	}

	private void boundTable() {
		if (check_table.isChecked() == true) {
			// 选定桌号并存储
			Intent intent = new Intent();
			intent.setClass(ActSetting.this, ActSelectTable.class);
			startActivityForResult(intent, ConstUtil.REQUEST_CODE.SELECT_TABLE);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		} else {
			setting_table.setText("未绑定桌号");
			mEditor.putBoolean("settable", false);

		}
		mEditor.commit();
		Toast.makeText(getBaseContext(), "请重新进入点菜页面刷新桌号绑定状态", Toast.LENGTH_LONG)
				.show();
	}

	protected void clearCache() {
		new PostGetTask<Void>(this, R.string.clearing_cache,
				R.string.clear_cache_failed, true, true, false) {

			@Override
			protected Void doBackgroudJob() throws Exception {
				try {
					File cacheDir = new File(
							DownloadMgr.getDownloadCacheDirPath(MsptApplication.getInstance()));
					MsptApplication.getInstance().removeAllDBs();//清除数据库数据，当菜单发生变化时需要重新获取
					if (cacheDir.exists())
						if (cacheDir.isDirectory())
							for (File child : cacheDir.listFiles())
								if (child.isFile())
									child.delete();
					File cacheDir2 = new File(ConstUtil.getUploadCacheDirPath());
					if (cacheDir2.exists())
						if (cacheDir2.isDirectory())
							for (File child : cacheDir2.listFiles())
								if (child.isFile())
									child.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void doPostJob(Exception exception, Void result) {
				Toast.makeText(ActSetting.this,
						R.string.clear_cache_successfully, Toast.LENGTH_SHORT)
						.show();
			}
		}.execute();
	}

	private void doCheckUpdate() {
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
				// TODO Auto-generated method stub
				 switch (updateStatus) {
			        case UpdateStatus.Yes: // has update
			            UmengUpdateAgent.showUpdateDialog(ActSetting.this, updateInfo);
			            break;
			        case UpdateStatus.No: // has no update
			        {
			            String verName = null;
			            try {
			                verName = MsptApplication.getInstance().getPackageManager()
			                        .getPackageInfo(MsptApplication.getInstance().getPackageName(), 0).versionName;
			            } catch (NameNotFoundException e) {
			                e.printStackTrace();
			            }	
			            Toast.makeText(ActSetting.this, "已是最新版本，版本号："+verName, Toast.LENGTH_SHORT).show();
			        }
			            break;
			        case UpdateStatus.NoneWifi: // none wifi
			            Toast.makeText(ActSetting.this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
			            break;
			        case UpdateStatus.Timeout: // time out
			            Toast.makeText(ActSetting.this, "检查更新超时", Toast.LENGTH_SHORT).show();
			            break;
			        }
			}
		});
		UmengUpdateAgent.forceUpdate(this);
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

	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);

	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		switch (arg1) {
		case RESULT_OK: {
			try {
				mTableName = arg2.getStringExtra("tableName");
				mTableId = arg2.getLongExtra("tableNoId", 0);
				mEditor = settings.edit();

				if (mTableName != null && mTableId != 0) {
					mEditor.putBoolean("settable", true);
					mEditor.putLong("tableNoId", mTableId);
					mEditor.putString("tableName", mTableName);
					mEditor.commit();
					setting_table.setText("已绑定"
							+ settings.getString("tableName", "未绑定桌号"));
					check_table.setChecked(true);
				} else {
					mEditor.putBoolean("settable", false);
					mEditor.putLong("tableNoId", 0);
					mEditor.putString("tableName", null);
					mEditor.commit();
					check_table.setChecked(false);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			break;
		default:
			mEditor.putBoolean("settable", false);
			mEditor.putLong("tableNoId", 0);
			mEditor.putString("tableName", null);
			check_table.setChecked(false);
			mEditor.commit();
			break;
		}
	}

}
