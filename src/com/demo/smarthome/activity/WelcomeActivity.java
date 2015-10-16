package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.tools.MD5Tools;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.view.Menu;
import android.view.Window;
import android.util.Log;
/**
 * 欢迎界面类
 * 
 * @author sl
 * 
 */
public class WelcomeActivity extends Activity {

	static final String TAG = "WelcomeActivity";
	private boolean isAutoLogin;
	static final int LOGIN_SUCCEED = 0;
	static final int LOGIN_ERROR = 1;

	ConfigService dbService;
	String name = "";
	String password = "";

	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			if(msg.what == LOGIN_SUCCEED){
				Intent intent = new Intent();
				intent.setClass(WelcomeActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
			else if(msg.what == LOGIN_ERROR){
				post(r);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_welcome);

		//是否需要自动登陆,如需要,自动登录再欢迎界面进行登录验证
		dbService = new ConfigDao(WelcomeActivity.this.getBaseContext());
		isAutoLogin = dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")? true : false;
		if(isAutoLogin){

			new AutoLoginThread().start();
		}
		else {
			handler.postDelayed(r, 3000);// 3秒后关闭，并跳转到主页面
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

	Runnable r = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, LoginActivity.class);

			startActivity(intent);
			finish();
		}
	};


	class AutoLoginThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = LOGIN_ERROR;

			name = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
			password = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);

			if(password.isEmpty()||name.isEmpty())
			{
				return;
			}

			Log.v(TAG, "AutoLoginThread start..");
			String md5Pass;
			if (password.length() >= 20) {
				md5Pass = password;
			} else {
				md5Pass = MD5Tools.string2MD5(password).toUpperCase();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String info = HttpConnectService.userLogin(name, (md5Pass));

			if (info.length() > 10) {
				String[] text = info.split(":");

				if (text.length == 4) {
					int index = 1;
					Cfg.torken = text[index++];
					Cfg.userId = StrTools.hexStringToBytes(StrTools
							.strNumToBig(text[index++]));// id 要倒序
					Cfg.passWd = StrTools.hexStringToBytes(StrTools
							.strNumToHex(text[index++]));

					Cfg.userName = name;
					message.what = LOGIN_SUCCEED;
					Log.v(TAG, "welcomeActivity Cfg.torken:" + Cfg.torken);
					Log.v(TAG, "welcomeActivity Cfg.userId:" + Cfg.userId);
					Log.v(TAG, "welcomeActivity Cfg.userName:" + Cfg.userName);
					Log.v(TAG, "welcomeActivity Cfg.passWd:" + Cfg.passWd);
				}
			}
			handler.sendMessage(message);
		}
	}
}
