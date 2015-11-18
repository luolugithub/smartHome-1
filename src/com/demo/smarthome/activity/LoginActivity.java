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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.smarthome.device.Dev;
import com.demo.smarthome.server.setServerURL;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.demo.smarthome.tools.CheckEmailPhoneTools;
/**
 * 登录类
 * 
 * @author Administrator
 * 
 */
public class LoginActivity extends Activity {
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// if(Cfg.register){
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// txtName.setText(Cfg.regUserName);
		// txtPassword.setText(Cfg.regUserPass);
		// new LoginThread().start();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// new LoginThread().start();
		// }
	}

	TextView title = null;
	TextView forgetPassword = null;

	EditText txtName = null;
	EditText txtPassword = null;

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
	ProgressDialog dialogView;
	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			dialogView.dismiss();
			// dataToui();
			switch (msg.what) {
			case LOGIN_SUCCEED:
				isLogin = true;
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, txtName.getText()
						.toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, txtPassword
						.getText().toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_AUTO_LOGIN
						, String.valueOf(isAtuoLogin.isChecked()));

				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, MainActivity.class);
				startActivity(intent);// 打开新界面

				break;
			case PASSWORD_ERROR:

				Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_SUCCEED:

				Toast.makeText(LoginActivity.this, "发送密码到邮箱成功!", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_ERROR:

				Toast.makeText(LoginActivity.this, "用户不存在!", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_EXCEPTION:

				Toast.makeText(LoginActivity.this, "邮箱服务异常!", Toast.LENGTH_SHORT)
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 注意顺序
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_login);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);
		title = (TextView) findViewById(R.id.titlelogin);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		txtName = (EditText) findViewById(R.id.loginTxtName);
		txtPassword = (EditText) findViewById(R.id.loginTxtPassword);
		isAtuoLogin = (CheckBox) findViewById(R.id.rememberUser);
		forgetPassword = (TextView) findViewById(R.id.forgetPassword);

		Button btnOk = (Button) findViewById(R.id.loginBtnOk);
		btnOk.setOnClickListener(new BtnOkOnClickListener());
		Button btnReg = (Button) findViewById(R.id.loginBtnReg);
		btnReg.setOnClickListener(new BtnRegOnClickListener());

		dbService = new ConfigDao(LoginActivity.this.getBaseContext());

		//从数据库中取出用户名密码

		txtName.setText(dbService.getCfgByKey(Cfg.KEY_USER_NAME));
		txtPassword.setText(dbService.getCfgByKey(Cfg.KEY_PASS_WORD));

		if(dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")) {
			isAtuoLogin.setChecked(true);
		}
		else {
			isAtuoLogin.setChecked(false);
		}

		//找回密码功能
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	//找回密码
	class clickTextForgetPwd implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
			final View layout = inflater.inflate(R.layout.forget_password, null);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("请输入注册的邮箱名");
			myDialog.setView(layout);
			myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetName = (EditText) layout.findViewById(R.id.userMailName);
					userMailName = userSetName.getText().toString();
					if (userMailName.isEmpty() ||(!CheckEmailPhoneTools.isEmail(userMailName))) {
						Toast.makeText(LoginActivity.this, "请输入正确的注册邮箱名！", Toast.LENGTH_SHORT)
								.show();
						userSetName.setFocusable(true);
						return;
					}
					dialog.dismiss();
					//等待框
					dialogView = new ProgressDialog(LoginActivity.this);
					dialogView.setTitle("正在找回密码");
					dialogView.setMessage("正在发送找回密码请求,请等待");
					//点击等待框以外等待框不消失
					dialogView.setCanceledOnTouchOutside(false);
					dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
						}
					});
					dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
							"请等待...", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					dialogView.show();
					dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
							.setEnabled(false);
					//扫描设备时屏蔽返回键
					dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								return true;
							}
							return false;
						}
					});
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
	 * 登录按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnOkOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
			if (name.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
				txtName.setFocusable(true);
				return;
			}
			if (password.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			//等待框
			dialogView = new ProgressDialog(LoginActivity.this);
			dialogView.setTitle("登录中");
			dialogView.setMessage("正在验证用户信息,请等待");
			//点击等待框以外等待框不消失
			dialogView.setCanceledOnTouchOutside(false);
			dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
				}
			});
			dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
					"请等待...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			dialogView.show();
			dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
			//扫描设备时屏蔽返回键
			dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						Log.d(TAG, "click back");
						return true;
					}
					return false;
				}
			});

			Cfg.userName = name;
			Cfg.userPassword =password;
			new LoginThread().start();

		}

	}

	/**
	 * 注册按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, RegisterActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putInt("type", 2);
//			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	/**
	 * 设置按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnSetupOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, SetupDevActivity.class);
			startActivity(intent);

		}

	}

	/**
	 * 登录
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
				//服务器程序异常
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
	 * 登录
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
