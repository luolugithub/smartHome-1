package com.demo.smarthome.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
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
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.demo.smarthome.tools.CheckEmailPhoneTools;
/**
 * ��¼��
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
	String name = "";
	String password = "";
	String userMailName ="";
	boolean isLogin = false;
	private static final String TAG = "LoginActivity";
	ConfigService dbService;
	static final int LOGIN_SUCCEED = 0;
	static final int PASSWORD_ERROR = 1;
	static final int SEND_PWD2EMAIL_SUCCEED 	= 2;
	static final int SEND_PWD2EMAIL_ERROR 		= 3;
	static final int SEND_PWD2EMAIL_EXCEPTION 	= 4;
	static final int SERVER_ERROR = 7;

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

					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, DeviceRealtimeDataActivity.class);
					startActivity(intent);
					finish();
				}
				else {
					Bundle bundle = new Bundle();
					bundle.putString("activity", "login");
					Intent intent = new Intent();
					intent.putExtras(bundle);
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
				}
				break;
			case PASSWORD_ERROR:

				Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_SUCCEED:

				Toast.makeText(LoginActivity.this, "发送成功,请到邮箱中查收", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_ERROR:

				Toast.makeText(LoginActivity.this, "发送失败,请验证邮箱名后再次发送", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_EXCEPTION:

				Toast.makeText(LoginActivity.this, "发送失败,请验证邮箱名后再次发送", Toast.LENGTH_SHORT)
						.show();

				break;
			case SERVER_ERROR:
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

				dialogView = new MyDialogView(LoginActivity.this);
				dialogView.showMyDialog("正在登录", "验证明用户名密码,请等待");

				Cfg.userName = name;
				Cfg.userPassword =password;
				new LoginThread().start();
			}
			return false;
		}
	};

	class clickTextForgetPwd implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
			final View layout = inflater.inflate(R.layout.forget_password, null);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("请输入用户名(仅支持邮箱)");
			myDialog.setView(layout);
			myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetName = (EditText) layout.findViewById(R.id.userMailName);
					userMailName = userSetName.getText().toString();
					if (userMailName.isEmpty() ||(!CheckEmailPhoneTools.isEmail(userMailName))) {
						Toast.makeText(LoginActivity.this, "请输入正确的邮箱名", Toast.LENGTH_SHORT)
								.show();
						userSetName.setFocusable(true);
						return;
					}
					dialog.dismiss();

					dialogView = new MyDialogView(LoginActivity.this);
					dialogView.showMyDialog("找回密码", "...正在找回密码");
					new forgetPwd().start();
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


	/**
	 * ע�ᰴť������
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



	/**
	 * ��¼
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
	 * �һ�����
	 *
	 * @author Administrator
	 *
	 */
	class forgetPwd extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = SEND_PWD2EMAIL_EXCEPTION;

			Gson gson = new Gson();
			String type = "mail";

			String[] paramsName = {"userName", "type"};
			String[] paramsValue = {userMailName, type};


			if ((jsonResult = new setServerURL().sendParamToServer("findPassword", paramsName, paramsValue)).isEmpty()) {
				message.what = Cfg.SERVER_CANT_CONNECT;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			}


			switch (Integer.parseInt(getResult.getCode())) {
				case Cfg.CODE_SUCCESS:
					message.what = SEND_PWD2EMAIL_SUCCEED;
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = SEND_PWD2EMAIL_ERROR;
					break;
				default:
					message.what = SEND_PWD2EMAIL_EXCEPTION;
					break;
			}
			handler.sendMessage(message);
		}
	}

}
