package com.demo.smarthome.activity;

import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.demo.smarthome.tools.CheckEmailPhoneTools;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 *
 * 
 * @author Administrator
 * 
 */
public class LoginActivity extends Activity {

	TextView forgetPassword = null;

	EditText txtName = null;
	EditText txtPassword = null;
	TextView textVersion = null;
	CheckBox isAtuoLogin = null;
	TextView sendCodeAgain = null;
	String name = "";
	String password = "";
	String userPhoneName ="";
	String newUserPassword = "";
	boolean isLogin = false;
	//if sending verification code is successful
	boolean isSendCodeSuccessful = false;
	View forgetPwdlayout;
	private static final String TAG = "LoginActivity";
	ConfigService dbService;
	static final int LOGIN_SUCCEED = 0;
	static final int PASSWORD_ERROR = 1;
	static final int BASE_TYPE_LOGIN_SUCCEED	= 5;
	static final int BASE_TYPE_LOGIN_FAILED		= 6;
	static final int SERVER_ERROR = 7;
	static final int JUMP_MAINACTIVITY = 8			;
	static final int USER_EXISTED		 		= 10;
	static final int USER_NOT_EXISTED		 	= 11;
	static final int CODE_ERROR 				= 12;
	static final int NOT_PHONENUMBER		 	= 13;

	ServerReturnResult loginResult = new ServerReturnResult();
	MyDialogView dialogView;
	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			dialogView.closeMyDialog();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			switch (msg.what) {
			case LOGIN_SUCCEED:
				isLogin = true;
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, txtName.getText()
						.toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, txtPassword
						.getText().toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_AUTO_LOGIN
						, String.valueOf(isAtuoLogin.isChecked()));

				if(Cfg.devInfo.length == 1){
					dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID
							, Cfg.devInfo[0]);
					Cfg.currentDeviceID = Cfg.devInfo[0];
				}else {
					Cfg.currentDeviceID = dbService.getCfgByKey(Cfg.KEY_DEVICE_ID);
				}

				if(!Cfg.currentDeviceID.isEmpty()){

					new getDeviceType().start();
				}else
				{
					new getAllDeviceType().start();
				}
				break;
			case PASSWORD_ERROR:

				Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT)
						.show();

				break;
			case USER_EXISTED:
				sendVerifactionCode();
				break;
			case USER_NOT_EXISTED:
				Toast.makeText(LoginActivity.this, "该手机号没有注册过", Toast.LENGTH_SHORT)
						.show();
				break;

			case BASE_TYPE_LOGIN_SUCCEED:

				if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_02L))
				{
					intent.setClass(LoginActivity.this, BGPM02LRealtimeDataActivity.class);
					startActivity(intent);
					finish();
				}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_10))
				{
					intent.setClass(LoginActivity.this, BGPM10RealtimeDataActivity.class);
					startActivity(intent);
					finish();
				}else {
					bundle.putString("activity", "login");

					intent.putExtras(bundle);
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
				break;

			case BASE_TYPE_LOGIN_FAILED:
					Cfg.currentDeviceID = null;


					bundle.putString("activity", "login");

					intent.putExtras(bundle);
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					break;
			case JUMP_MAINACTIVITY:
				bundle.putString("activity", "login");

				intent.putExtras(bundle);
				intent.setClass(LoginActivity.this, MainActivity.class);
				startActivity(intent);
				break;
			case SERVER_ERROR:
				Toast.makeText(LoginActivity.this, "服务器故障", Toast.LENGTH_SHORT)
						.show();
				break;
			case CODE_ERROR:
				String codeErrorMessage = (String)msg.obj;
				Toast.makeText(LoginActivity.this,codeErrorMessage, Toast.LENGTH_SHORT)
						.show();
				break;
				case NOT_PHONENUMBER:
					Toast.makeText(LoginActivity.this,userPhoneName, Toast.LENGTH_SHORT)
							.show();
					break;
			default:
				break;

			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		txtName.setText(dbService.getCfgByKey(Cfg.KEY_USER_NAME));
		txtPassword.setText(dbService.getCfgByKey(Cfg.KEY_PASS_WORD));
		if(dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("false")) {
			isAtuoLogin.setChecked(false);
		}
		else {
			isAtuoLogin.setChecked(true);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		ActivityControl.getInstance().addActivity(this);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);

		txtName = (EditText) findViewById(R.id.loginTxtName);
		txtPassword = (EditText) findViewById(R.id.loginTxtPassword);
		isAtuoLogin = (CheckBox) findViewById(R.id.rememberUser);
		forgetPassword = (TextView) findViewById(R.id.forgetPassword);

		Button btnOk = (Button) findViewById(R.id.loginBtnOk);
		btnOk.setOnTouchListener(loginTouch);
		Button btnReg = (Button) findViewById(R.id.loginBtnReg);
		btnReg.setOnTouchListener(regsiterTouch);

		textVersion = (TextView) findViewById(R.id.versionNumber);
		textVersion.setText("v" + Cfg.versionNumber);

		dbService = new ConfigDao(LoginActivity.this.getBaseContext());
		dialogView = new MyDialogView(LoginActivity.this);
		//短信验证码
		SMSSDK.initSDK(LoginActivity.this, StringRes.SMSKEY,StringRes.SMSSECRET);
		SMSSDK.registerEventHandler(eh); //注册短信回调

		String tempName = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
		String tempPwd = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);
		if(tempName == null || tempPwd == null) {
			txtName.setText(dbService.getCfgByKey(Cfg.KEY_USER_NAME));
			txtPassword.setText(dbService.getCfgByKey(Cfg.KEY_PASS_WORD));
		}

		if (dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("false")) {
			isAtuoLogin.setChecked(false);
		} else {
			isAtuoLogin.setChecked(true);
		}

		forgetPassword.setClickable(true);
		forgetPassword.setOnClickListener(new clickTextForgetPwd());

		name = txtName.getText().toString();
		password = txtPassword.getText().toString();
		if (name.trim().isEmpty()) {
			txtName.setFocusable(true);
			return;
		}
		if (password.trim().isEmpty()) {
			txtPassword.setFocusable(true);
			return;
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.login, menu);
//		return true;
//	}
	/**
	 *
	 *
	 * @author Administrator
	 *
	 */
	private View.OnTouchListener loginTouch = new View.OnTouchListener() {

		public boolean onTouch(View view, MotionEvent event) {
			int iAction = event.getAction();
			if (iAction == MotionEvent.ACTION_DOWN) {
				view.setBackgroundResource(R.drawable.login_light);
			} else if (iAction == MotionEvent.ACTION_UP) {
				view.setBackgroundResource(R.drawable.login);
				name = txtName.getText().toString();
				password = txtPassword.getText().toString();
				if (name.trim().isEmpty()) {
					Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
					txtName.setFocusable(true);
					return false;
				}
				if (password.trim().isEmpty()) {
					Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
					txtPassword.setFocusable(true);
					return false;
				}

				dialogView.showMyDialog("正在登录", "验证用户名密码,请等待");

				Cfg.userName = name;
				Cfg.userPassword =password;
				new LoginThread().start();
			}
			return false;
		}
	};

	private class clickTextForgetPwd implements OnClickListener {
		@Override
		public void onClick(View arg0) {

			LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
			forgetPwdlayout = inflater.inflate(R.layout.forget_password, null);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("请输入注册手机号");
			myDialog.setView(forgetPwdlayout);
			sendCodeAgain = (TextView) forgetPwdlayout.findViewById(R.id.sendCodeAgainButton);
			sendCodeAgain.setClickable(true);
			sendCodeAgain.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText userSetName = (EditText) forgetPwdlayout.findViewById(R.id.userPhoneName);
					userPhoneName = userSetName.getText().toString();
					new isUserExist().start();
				}
			});

			myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetName = (EditText) forgetPwdlayout.findViewById(R.id.userPhoneName);
					userPhoneName = userSetName.getText().toString();
					if (userPhoneName.isEmpty() ||(!CheckEmailPhoneTools
							.isPhoneNumber(userPhoneName))) {
						Toast.makeText(LoginActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT)
								.show();
						userSetName.setFocusable(true);
						return;
					}
					String verificationCode = ((EditText)forgetPwdlayout.findViewById
							(R.id.verificationCode)).getText().toString();
					newUserPassword = ((EditText) forgetPwdlayout.findViewById(R.id.changeUserPassword))
							.getText().toString();
					String rePassword = ((EditText) forgetPwdlayout.findViewById(R.id.againPassword))
							.getText().toString();
					if (newUserPassword.trim().isEmpty() || (newUserPassword.length() < 6)) {
						Toast.makeText(getApplicationContext(), "密码至少为六位"
								, Toast.LENGTH_SHORT).show();
						txtPassword.setFocusable(true);
						return;
					}

					if(!rePassword.equals(newUserPassword)){
						Toast.makeText(getApplicationContext(), "两次填写的密码不一致"
								, Toast.LENGTH_SHORT).show();
						txtPassword.setFocusable(true);
						return;
					}
					dialog.dismiss();

					if(isSendCodeSuccessful)
					{
						dialogView.showMyDialog("更改密码中", "...请等待");
						SMSSDK.submitVerificationCode(StringRes.ChinaCode
								,userPhoneName,verificationCode);
					}
					else
					{
						Toast.makeText(getApplicationContext()
								, "验证码验证失败", Toast.LENGTH_SHORT).show();
					}

				}
			});
			myDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
									int which) {
					dialog.dismiss();
				}
			});
			myDialog.create().show();
		}
	}
	/*
        * send verification code
        *
        * */
	private void sendVerifactionCode()
	{

		//send Code
		SMSSDK.getVerificationCode(StringRes.ChinaCode, txtName.getText().toString());

		sendCodeAgain.setClickable(false);
		sendCodeAgain.setTextColor(ContextCompat.getColor
				(LoginActivity.this, R.color.sbc_header_text));
		new CountDownTimer(Cfg.sendVerficationCodeInterval, 1000) {
			public void onTick(long millisUntilFinished) {
				sendCodeAgain.setText("再次发送("+ millisUntilFinished/1000+")");
			}
			public void onFinish() {
				sendCodeAgain.setClickable(true);
				sendCodeAgain.setTextColor(ContextCompat.getColor
						(LoginActivity.this, R.color.blue_50));
				sendCodeAgain.setText("发送验证码");
			}
		}.start();
	}

	EventHandler eh=new EventHandler(){

		@Override
		public void afterEvent(int event, int result, Object data) {
			//回调完成
			if (result == SMSSDK.RESULT_COMPLETE) {

				//提交验证码成功
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
					new userChangePwdThread().start();
					//获取验证码成功
				}else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
					isSendCodeSuccessful = true;
				}
			}else{
				((Throwable)data).printStackTrace();
				if(isSendCodeSuccessful == true)
				{
					isSendCodeSuccessful = false;
					Message message = new Message();
					message.what = CODE_ERROR;
					message.obj = ((Throwable)data).getMessage();
					handler.sendMessage(message);
				}

			}
		}
	};

	/**
	 *
	 * @author Administrator
	 *
	 */
	private View.OnTouchListener regsiterTouch = new View.OnTouchListener() {

		public boolean onTouch(View view, MotionEvent event) {


			int iAction = event.getAction();
			if (iAction == MotionEvent.ACTION_DOWN) {
				view.setBackgroundResource(R.drawable.register_light);
			} else if (iAction == MotionEvent.ACTION_UP) {
				view.setBackgroundResource(R.drawable.register);
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, RegisterActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putInt("type", 2);
//			intent.putExtras(bundle);
				startActivity(intent);
			}
			return false;
		}
	};
	private class getDeviceType extends Thread {
		@Override
		public void run () {
			Message message = new Message();
			if(Cfg.currentDeviceID.isEmpty()){
				message.what = BASE_TYPE_LOGIN_FAILED;
				handler.sendMessage(message);
				return;
			}
			if(!LoginServer.getDeviceType(Cfg.currentDeviceID)){
				message.what = BASE_TYPE_LOGIN_FAILED;
				handler.sendMessage(message);
				return;
			}
			dbService.SaveSysCfgByKey(Cfg.currentDeviceID, Cfg.deviceType);
			Cfg.currentDeviceType = Cfg.deviceType;
			message.what = BASE_TYPE_LOGIN_SUCCEED;
			handler.sendMessage(message);
			return;
		}
	}
	private class getAllDeviceType extends Thread {
		@Override
		public void run () {
			Message message = new Message();

			for(int i = 0;i< Cfg.devInfo.length;i++) {
				String type = LoginServer.getType(Cfg.devInfo[i]);
				if (type == null ) {
					message.what = BASE_TYPE_LOGIN_FAILED;
					handler.sendMessage(message);
					return;
				}
				dbService.SaveSysCfgByKey(Cfg.devInfo[i],type);
			}
			message.what = JUMP_MAINACTIVITY;
			handler.sendMessage(message);
			return;
		}
	}
	/**
	 * 
	 * @author Administrator
	 * 
	 */
	class LoginThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = Cfg.REG_SUCCESS;

			if((loginResult = LoginServer.LoginServerMethod())==null) {
				message.what = SERVER_ERROR;
				handler.sendMessage(message);
				return;
			}

			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = LOGIN_SUCCEED;
					break;
				case Cfg.CODE_PWD_ERROR:
					message.what = PASSWORD_ERROR;
					break;
				case Cfg.CODE_USER_EXISTED:
					break;

				case Cfg.CODE_EXCEPTION:
					break;
				default:
					message.what = Cfg.REG_ERROR;
					break;
			}

			handler.sendMessage(message);
		}
	}

	/**
	 *
	 * @author Administrator
	 *
	 */
	class isUserExist extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = USER_NOT_EXISTED;
			Gson gson = new Gson();

			if (userPhoneName.isEmpty()||
					!CheckEmailPhoneTools.isPhoneNumber(userPhoneName)) {
				message.what = NOT_PHONENUMBER;
				handler.sendMessage(message);
				return;
			}

			String[] paramsName = {"userName"};
			String[] paramsValue = {userPhoneName};

			if ((jsonResult = new setServerURL().sendParamToServer
					("isUserExist", paramsName, paramsValue)).isEmpty()) {
				message.what = Cfg.SERVER_CANT_CONNECT;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				message.what = Cfg.SERVER_CANT_CONNECT;
				handler.sendMessage(message);
				return;
			}


			switch (Integer.parseInt(getResult.getCode())) {
				case Cfg.CODE_SUCCESS:
					message.what = USER_EXISTED;
					break;
				default:
					message.what = USER_NOT_EXISTED;
					break;
			}
			handler.sendMessage(message);
		}
	}
	class userChangePwdThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = SERVER_ERROR;
			Gson gson = new Gson();

			String[] paramsName = {"userName","userPassword"};
			String[] paramsValue = {userPhoneName,newUserPassword};

			if ((jsonResult = new setServerURL().sendParamToServer("updatePassword", paramsName, paramsValue)).isEmpty()) {
				message.what = Cfg.SERVER_CANT_CONNECT;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				message.what = Cfg.SERVER_CANT_CONNECT;
				handler.sendMessage(message);
				return;
			}


			switch (Integer.parseInt(getResult.getCode())) {
				case Cfg.CODE_SUCCESS:
					Cfg.userName = userPhoneName;
					Cfg.userPassword =newUserPassword;
					new LoginThread().start();
					break;
				default:
					message.what = SERVER_ERROR;
					break;
			}
			handler.sendMessage(message);
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//注销短信验证回调函数
		SMSSDK.unregisterEventHandler(eh);
		// 结束Activity&从栈中移除该Activity
		ActivityControl.getInstance().removeActivity(this);
	}

}
