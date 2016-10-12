package com.meishipintu.assistant.ui.pay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterTeamList;
import com.meishipintu.assistant.app.Cookies;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;

public class ActTeamList extends Activity {

	private static String TEAM_LIST_FILE = "teamlist_info";
	private List<Map<String, Object>> mTeamListData = null;
	private ListView mTeamListView = null;
	private int mListViewSize = 0;
	private GridView mGridPeopleList = null;
	private List<HashMap<String, Object>> peopleNumList = null;
	private int mPeople = 0;
	private int mStatus = 0;
	private String[] mReduceString = null;//
	private int mPrintResult = 0;

	private AdapterTeamList adapterTeamList = null;
	private SimpleAdapter mPeopleNumAdapter = null;

	private Button mBtGetNum = null;

	public static ActTeamList mTeamList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_payteam);
		mTeamList = this;
		RadioGroup rg = (RadioGroup) findViewById(R.id.rg_pay_num);
		rg.setOnCheckedChangeListener(ll);
		rg.check(R.id.rb_status1);
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getResources().getString(R.string.team_num));

		mTeamListData = getTeamListData(mStatus);
		adapterTeamList = new AdapterTeamList(this, mTeamListData);
		adapterTeamList.notifyDataSetChanged();
		mTeamListView = (ListView) findViewById(R.id.llv_teamlist);
		mTeamListView.setAdapter(adapterTeamList);

		findViewById(R.id.tv_clear).setOnClickListener(cl);
		findViewById(R.id.tv_getnum).setOnClickListener(cl);
		findViewById(R.id.btn_back).setOnClickListener(cl);
	}

	private List<Map<String, Object>> getTeamListData(int status) {
		String[] stringObject = null;
		List<Map<String, Object>> listData = null;
		listData = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences spTeamList = this.getSharedPreferences(
				TEAM_LIST_FILE, Context.MODE_PRIVATE);
		String teamList = spTeamList.getString("teamList", "");
		if (teamList.length() > 0) {
			stringObject = stringAnalytical(teamList, ";");

			for (int i = 0; i < stringObject.length; i++) {
				String objectTeam = stringObject[i];
				String[] paraTeam = stringAnalytical(objectTeam, ",");
				if (paraTeam.length > 0) {
					if (status == 0) {
						map = new HashMap<String, Object>();
						map.put("teamNum", paraTeam[0]);
						map.put("peopleNum", paraTeam[1]);
						map.put("time", paraTeam[2]);
						listData.add(map);
					} else if (status == 1) {
						if (Integer.parseInt(paraTeam[1]) <= 4) {
							map = new HashMap<String, Object>();
							map.put("teamNum", paraTeam[0]);
							map.put("peopleNum", paraTeam[1]);
							map.put("time", paraTeam[2]);
							listData.add(map);
						}
					} else if (status == 2) {

						if (Integer.parseInt(paraTeam[1]) <= 8
								&& Integer.parseInt(paraTeam[1]) >= 4) {
							map = new HashMap<String, Object>();
							map.put("teamNum", paraTeam[0]);
							map.put("peopleNum", paraTeam[1]);
							map.put("time", paraTeam[2]);
							listData.add(map);
						}
					} else if (status == 3) {
						if (Integer.parseInt(paraTeam[1]) >= 8) {
							map = new HashMap<String, Object>();
							map.put("teamNum", paraTeam[0]);
							map.put("peopleNum", paraTeam[1]);
							map.put("time", paraTeam[2]);
							listData.add(map);
						}
					}

				}
			}

		} else {
			map = new HashMap<String, Object>();
			map.put("teamNum", "");
			map.put("peopleNum", "");
			map.put("time", "无人排队");
			listData.add(map);
		}
		mListViewSize = listData.size();
		return listData;

	}

	private String[] stringAnalytical(String str, String divisionChar) {
		String string[];
		int i = 0;
		StringTokenizer tokenizer = new StringTokenizer(str, divisionChar);
		string = new String[tokenizer.countTokens()];// 动态的决定数组的长度
		while (tokenizer.hasMoreTokens()) {
			string[i] = new String();
			string[i] = tokenizer.nextToken();
			i++;
		}
		return string;// 返回字符串数组
	}

	private void clearList() {
		SharedPreferences spTeamList = this.getSharedPreferences(
				TEAM_LIST_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spTeamList.edit();
		editor.putString("teamList", "");
		editor.putString("end", "0");
		editor.putString("start", "0");
		editor.putInt("reduceNum", 0);
		editor.putInt("reducePeoNum", 0);
		editor.commit();
//		mTeamListView.removeAllViews();
		mTeamListView.setVisibility(View.INVISIBLE);
	}

	private void  addTeamListNum(int peopleNum) {
		SharedPreferences spTeamList = this.getSharedPreferences(
				TEAM_LIST_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spTeamList.edit();
		String teamList = spTeamList.getString("teamList", "");

		int end = 0;
		if (teamList.length() == 0) {
			clearList();
			++end;
		} else {
			String string = spTeamList.getString("end", "0");
			end = Integer.parseInt(string);
			++end;
		}
		
		mPeople = 0;
		int tabletype = 0;
		int reduceNum = 0;
		int reducePeo = 0;
		String reduceString = "";
		reduceNum = spTeamList.getInt("reduceNum", 0);
		reducePeo = spTeamList.getInt("reducePeoNum", 0);
		if (reducePeo <= 4 && reducePeo > 0) {
			reduceString = "A" + reduceNum + "号";
		} else if (reducePeo > 4 && reducePeo <= 8) {
			reduceString = "B" + reduceNum + "号";
		} else if (reducePeo > 8) {
			reduceString = "C" + reduceNum + "号";
		}
		String tableNumString = "";
		if (peopleNum <= 4 && peopleNum > 0) {
			tableNumString = "A" + Integer.toString(end);
		} else if (peopleNum > 4 && peopleNum <= 8) {
			tabletype = 1;
			tableNumString = "B" + Integer.toString(end);
		} else if (peopleNum > 8) {
			tabletype = 2;
			tableNumString = "C" + Integer.toString(end);
		}
		printGetNum(Cookies.getUserId(), Cookies.getShopId(),tableNumString,reduceString,Integer.toString(peopleNum),Integer.toString(mListViewSize),tabletype,editor);
	}
	
	private void addToLocal(int peopleNum)
	{
		SharedPreferences spTeamList = getSharedPreferences(
				TEAM_LIST_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spTeamList.edit();
		String teamList = spTeamList.getString("teamList", "");
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 设置日期格式
		String date = df.format(new Date());// new Date()为获取当前系统时间;
		date = df.format(new Date());// new Date()为获取当前系统时间
		
		int end = 0;
		if (teamList.length() == 0) {
			clearList();
			++end;
		} else {
			String string = spTeamList.getString("end", "0");
			end = Integer.parseInt(string);
			++end;
		}
		
		teamList = teamList + Integer.toString(end) + ","
				+ Integer.toString(peopleNum) + "," + date + ";";
		editor.putString("teamList", teamList);
		editor.putString("end", Integer.toString(end));
		editor.commit();
		refreshTeamList();
	}
	
	public void reduceTeamListNumFromNet(final String team, final String teamNum)
	{
		new PostGetTask<JSONObject>(this, R.string.loading,
				R.string.fail_reduce_teamnum, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("shopId", Cookies.getShopId());
				jParam.put("token", Cookies.getToken());
				jParam.put("number", teamNum);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getNetTeamNumReduce(), jParam, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				// TODO Auto-generated method stub
				if (result != null && exception == null) {
					try {
						int resultCode = result.getInt("result");
						if (resultCode == 1) {
							reduceTeamListNum(team);
						} else {
							Toast.makeText(ActTeamList.this, "销号失败，请重试",
									Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e1) {
						Toast.makeText(ActTeamList.this, "销号失败，请检查网络",
								Toast.LENGTH_LONG).show();
					}
				}

			}
		}.execute();
	}

	public void reduceTeamListNum(String team) {
		String[] stringObject = null;
		String[] paraTeam = null;
		List<Map<String, Object>> listData = null;
		listData = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences spTeamList = this.getSharedPreferences(
				TEAM_LIST_FILE, Context.MODE_PRIVATE);
		String teamList = spTeamList.getString("teamList", "");
		if (teamList.length() > 0) {
			String reduceString = teamList.replace(team, "");
			Editor editor = spTeamList.edit();
			editor.putString("teamList", reduceString);

			stringObject = stringAnalytical(team, ";");
			if (stringObject.length > 0) {
				String objectTeam = stringObject[0];
				paraTeam = stringAnalytical(objectTeam, ",");
				editor.putInt("reduceNum", Integer.parseInt(paraTeam[0]));
				editor.putInt("reducePeoNum", Integer.parseInt(paraTeam[1]));
			}
			editor.commit();
		}
		refreshTeamList();
	}

	private OnCheckedChangeListener ll = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			switch (checkedId) {
			case R.id.rb_status1:
				mStatus = 0;
				refreshTeamList();
				break;
			case R.id.rb_status2:
				mStatus = 1;
				refreshTeamList();
				break;
			case R.id.rb_status3:
				mStatus = 2;
				refreshTeamList();
				break;
			case R.id.rb_status4:
				mStatus = 3;
				refreshTeamList();
				break;
			}

		}
	};

	private OnClickListener cl = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tv_clear:{			
				AlertDialog alt=new AlertDialog.Builder(ActTeamList.this)
				.setTitle("确认清除所有选号？")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						clearAllNum();
					}
				}).show();
				}
				break;
			case R.id.tv_getnum: {
				showAddDialog();
			}
				break;
			case R.id.btn_back: {
				finishAndAni();
			}
				break;
			}
		}
	};

	private void showAddDialog() {
		peopleNumList = new ArrayList<HashMap<String, Object>>();
		for (int i = 1; i <= 9; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (i < 9) {
				map.put("pNum", Integer.toString(i));
			} else {
				map.put("pNum", "8人以上");
			}
			peopleNumList.add(map);
		}
		mPeopleNumAdapter = new SimpleAdapter(this, peopleNumList,
				R.layout.people_num, new String[] { "pNum" },
				new int[] { R.id.tv_people });
		final AlertDialog alert_people = new AlertDialog.Builder(this).create();
		alert_people.show();

		LayoutInflater inflater = LayoutInflater.from(this);
		View view_people = inflater
				.inflate(R.layout.layout_dialog_peonum, null);
		alert_people.getWindow().setContentView(view_people);
		mGridPeopleList = (GridView) view_people
				.findViewById(R.id.list_get_paynum);
		mGridPeopleList.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		mGridPeopleList.setAdapter(mPeopleNumAdapter);
		// alert_people.getWindow().setContentView(mGridPeopleList);

		mGridPeopleList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mPeople = position + 1;
				for (int i = 0; i < 9; i++) {
					View v = parent.getChildAt(i);
					TextView tv_peo = (TextView) v.findViewById(R.id.tv_people);
					if (i == position) {
						tv_peo.setSelected(true);
					} else {
						tv_peo.setSelected(false);
					}
				}
				// 生成一个列表
			}
		});
		mBtGetNum = (Button) view_people.findViewById(R.id.btn_getnum);
		mBtGetNum.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mPeople == 0) {
					Toast.makeText(getBaseContext(), "请选择人数",
							Toast.LENGTH_SHORT).show();
				} else {
					addTeamListNum(mPeople);
					refreshTeamList();
					// refreshPeopleList();
					alert_people.dismiss();
				}
				

			}
		});
	}

	public void refreshTeamList() {
		mTeamListData = getTeamListData(mStatus);
		adapterTeamList = new AdapterTeamList(this, mTeamListData);
		adapterTeamList.notifyDataSetChanged();
		mTeamListView = (ListView) findViewById(R.id.llv_teamlist);
		mTeamListView.setVisibility(View.VISIBLE);
		mTeamListView.setAdapter(adapterTeamList);
	}

	public void printGetNum(final String uid,final long shopId,final String number,
			final String curnumber,final String peoplecnt,final String prevtable,final int tabletype,final Editor editor) {
		new PostGetTask<JSONObject>(this, R.string.loading,
				R.string.fail_get_print_teamnum, true, true, false) {
			@Override
			protected JSONObject doBackgroudJob() throws Exception {				
				JSONObject jParam = new JSONObject();
				jParam.put("uid", uid);
				jParam.put("shopId", shopId);
				jParam.put("number", number);//桌号
				jParam.put("curnumber",curnumber);//当前叫到的号码
				jParam.put("peoplecnt", peoplecnt);//人数
				jParam.put("prevtable", prevtable);//前面还有多少桌
				jParam.put("tabletype", tabletype);
				jParam.put("token", Cookies.getToken());
				JSONObject jRet = null;
		        jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getNetPrintUrl(), jParam, true);
				return jRet;
			}
			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (result != null && e == null) {
					try {
						if (result.getInt("result") == 1) {
							addToLocal(Integer.parseInt(peoplecnt));
							Toast.makeText(getBaseContext(), "打印成功", Toast.LENGTH_SHORT).show();
						}else if(result.getInt("result") != 1)
						{
							String msg="打印失败";
							if(result.has("msg"))
							{
								msg=result.getString("msg");	
							}
							Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
						}
						
					} catch (JSONException e1) {
						e1.printStackTrace();
						mPrintResult=0;
					}

				}
			}
		}.execute();
	}

	private void clearAllNum() {
		new PostGetTask<JSONObject>(this, R.string.loading,
				R.string.fail_clear_teamnum, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("shopId", Cookies.getShopId());
				jParam.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getNetTeamNumClear(), jParam, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				// TODO Auto-generated method stub
				if (result != null && exception == null) {
					try {
						int resultCode = result.getInt("result");
						if (resultCode == 1) {
							clearList();
						} else {
							Toast.makeText(ActTeamList.this, "清空排号失败，请重试",
									Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e1) {
						Toast.makeText(ActTeamList.this, "清空排号失败，请检查网络",
								Toast.LENGTH_LONG).show();
					}
				}

			}
		}.execute();
	}
	
	protected void finishAndAni() {
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
