package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.tools.MD5Tools;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.view.Menu;
import android.view.Window;
import android.util.Log;
/**
 * ��ӭ������
 * 
 * @author sl
 * 
 */
public class WelcomeActivity extends Activity {

	static final String TAG = "WelcomeActivity";
	private boolean isAutoLogin;
	static final int LOGIN_SUCCEED = 0;
	static final int LOGIN_ERROR = 1;

	static final int LONGIN_WAIT_TIME = 3000;

	ConfigService dbService;

	ServerReturnResult loginResult = new ServerReturnResult();
	long startTimestamp;

	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			if(msg.what == Cfg.REG_SUCCESS){
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("activity", "welcome");
				intent.putExtras(bundle);
				intent.setClass(WelcomeActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
			else {
				post(r);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_welcome);

		//�Ƿ���Ҫ�Զ���½,����Ҫ,�Զ���¼�ٻ�ӭ������е�¼���?
		dbService = new ConfigDao(WelcomeActivity.this.getBaseContext());
		isAutoLogin = dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")? true : false;

		if (isAutoLogin) {
			startTimestamp = System.currentTimeMillis();
			new AutoLoginThread().start();
		} else {
			handler.postDelayed(r, LONGIN_WAIT_TIME);// 3���رգ�����ת����ҳ��
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

			Cfg.userName = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
			Cfg.userPassword = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);

			if(Cfg.userName  == null || Cfg.userPassword == null) {
				return;
			}

			loginResult = LoginServer.LoginServerMethod();
			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = Cfg.REG_SUCCESS;
					break;
				case Cfg.CODE_PWD_ERROR:
					message.what = Cfg.REG_PWD_ERROR;
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = Cfg.REG_USER_EXISTED;
					break;
				//服务器程序异�?
				case Cfg.CODE_EXCEPTION:
					message.what = Cfg.REG_EXCEPTION;
					break;
				default:
					message.what = Cfg.REG_ERROR;
					break;
			}
			//��Ҫ�ȴ���ӭ���漸��
			while(System.currentTimeMillis() - startTimestamp < LONGIN_WAIT_TIME){

			}
			handler.sendMessage(message);
		}
	}
}
