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

	TextView title = null;
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
				//������˺�ֻ����һ̨�豸(�����Ѿ������豸),ֱ�ӽ���ʵʱ�豸���ݽ���,����Ҫȥ�����豸����
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
					startActivity(intent);// ���½���
				}
				break;
			case PASSWORD_ERROR:

				Toast.makeText(LoginActivity.this, "�������", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_SUCCEED:

				Toast.makeText(LoginActivity.this, "�������뵽����ɹ�!", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_ERROR:

				Toast.makeText(LoginActivity.this, "�û�������!", Toast.LENGTH_SHORT)
						.show();

				break;
			case SEND_PWD2EMAIL_EXCEPTION:

				Toast.makeText(LoginActivity.this, "��������쳣!", Toast.LENGTH_SHORT)
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
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ע��˳��
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
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

		//��ʾ�汾��
		textVersion = (TextView) findViewById(R.id.versionNumber);
		textVersion.setText("v" + Cfg.versionNumber);

		dbService = new ConfigDao(LoginActivity.this.getBaseContext());

		//�����ݿ���ȡ���û������� �Զ���¼
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

		//�һ����빦��
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

	//�һ�����
	class clickTextForgetPwd implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
			final View layout = inflater.inflate(R.layout.forget_password, null);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("������ע���������");
			myDialog.setView(layout);
			myDialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetName = (EditText) layout.findViewById(R.id.userMailName);
					userMailName = userSetName.getText().toString();
					if (userMailName.isEmpty() ||(!CheckEmailPhoneTools.isEmail(userMailName))) {
						Toast.makeText(LoginActivity.this, "��������ȷ��ע����������", Toast.LENGTH_SHORT)
								.show();
						userSetName.setFocusable(true);
						return;
					}
					dialog.dismiss();
					//�ȴ���
					dialogView = new MyDialogView(LoginActivity.this);
					dialogView.showMyDialog("�����һ�����", "���ڷ����һ���������,��ȴ�");
					new forgetPwd().start();
				}
			});
			myDialog.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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
	 * ��¼��ť������
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
				Toast.makeText(getApplicationContext(), "�������û���", Toast.LENGTH_SHORT).show();
				txtName.setFocusable(true);
				return;
			}
			if (password.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "����������", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			//�ȴ���
			dialogView = new MyDialogView(LoginActivity.this);
			dialogView.showMyDialog("��¼��", "������֤�û���Ϣ,��ȴ�");

			Cfg.userName = name;
			Cfg.userPassword =password;
			new LoginThread().start();
		}
	}

	/**
	 * ע�ᰴť������
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
				//�����������쳣
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
