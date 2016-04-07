package com.demo.smarthome.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.dao.DevDao;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigDevice;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.CheckEmailPhoneTools;
import com.demo.smarthome.tools.IpTools;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.R;
import com.demo.smarthome.view.MyDialogView;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;
import com.demo.smarthome.service.ConfigService;
import android.view.KeyEvent;
/**
 * ע����
 * 
 * @author Administrator
 * 
 */
public class RegisterActivity extends Activity {
	EditText txtName = null;
	EditText txtPassword = null;
	EditText txtrePassword = null;
	EditText txtWifipassword = null;
	Switch  switchIsHidden;
	TextView apSSID;
	MyDialogView dialogView;

	boolean noWifi = false;

	AlertDialog.Builder failAlert;

	String userRegName = "";
	String userRegPassword = "";
	String wifiPwd = "";

	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	static final int WAIT_RESULT  = 0;
	static final int FIND_DEVID = 2;
	static final int NO_WIFI    = 3;
	static final int CMD_TIMEOUT = 6;


	final static int REGISTER_SUCCESS 		= 0x10;
	final static int USER_EXISTED  			= 0x11;
	final static int SERVER_EXCEPTION   	= 0x12;
	final static int REGISTER_FAIL   		= 0x19;

	ConfigDevice deviceInfo;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialogView.closeMyDialog();
			super.handleMessage(msg);

			switch (msg.what) {

			case REGISTER_SUCCESS:
				ConfigService dbService = new ConfigDao(RegisterActivity.this.getBaseContext());
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, userRegName);
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, userRegPassword);
				dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, deviceInfo.getDeviceID());
				dbService.SaveSysCfgByKey(Cfg.KEY_AUTO_LOGIN , "true");
				Cfg.userName = userRegName;
				Cfg.userPassword = userRegPassword;
				Cfg.currentDeviceID = deviceInfo.getDeviceID();

				dialogView.closeMyDialog();

				Toast.makeText(RegisterActivity.this, "ע�����û��ɹ�!",
						Toast.LENGTH_SHORT).show();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Bundle bundle = new Bundle();
				bundle.putString("activity", "register");
				Intent mainIntent = new Intent();
				mainIntent.setClass(RegisterActivity.this, DeviceRealtimeDataActivity.class);
				mainIntent.putExtras(bundle);
				startActivity(mainIntent);// ���½���
				finish();
				break;
			case NO_WIFI:

				failAlert.setTitle("�����������").setIcon(R.drawable.cloud_fail).setMessage("��Ҫ���ֺͱ����豸��ͬһ������");
				failAlert.create().show();
				break;
			case CMD_TIMEOUT:

				failAlert.setTitle("�޷��ҵ������豸").setIcon(R.drawable.cloud_fail).setMessage("ע��ʱ��Ҫ�󶨱����豸");
				failAlert.create().show();
				break;
			case USER_EXISTED:

				failAlert.setTitle(" ע��ʧ��").setIcon(R.drawable.cloud_fail).setMessage("   �û��Ѿ�����");
				failAlert.create().show();
				break;
			case SERVER_EXCEPTION:

				failAlert.setTitle(" ע��ʧ��").setIcon(R.drawable.cloud_fail).setMessage(StringRes.canNotConnetServer);
				failAlert.create().show();
				break;
			default:

				failAlert.setTitle(" ע��ʧ��").setIcon(R.drawable.cloud_fail).setMessage("   ע�����û�ʧ��");
				failAlert.create().show();
				break;

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��

		setContentView(R.layout.activity_register);
		TextView title = (TextView) findViewById(R.id.titleRegister);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				finish();
			}
		});

		txtName = (EditText) findViewById(R.id.registerTxtName);
		txtPassword = (EditText) findViewById(R.id.registerTxtPassword);
		txtrePassword = (EditText) findViewById(R.id.againPassword);
		txtWifipassword = (EditText) findViewById(R.id.wifiPassword);
		switchIsHidden = (Switch) findViewById(R.id.wifiIsHidden);
		//��ʾSSID
		apSSID = (TextView)findViewById(R.id.wifiSSID);
		ConfigDevice forApSSID= new ConfigDevice(RegisterActivity.this);
		if(forApSSID.getApSSid() == null){
			noWifi = true;
		}
		else{
			apSSID.setText(forApSSID.getApSSid());
		}
		Button btnSetup = (Button) findViewById(R.id.registerBtnReg);
		btnSetup.setOnClickListener(new BtnRegOnClickListener());

		failAlert = new AlertDialog.Builder(RegisterActivity.this);


	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.register, menu);
//		return true;
//	}

	/**
	 * ע�� ��ť������
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(noWifi){
				failAlert.setTitle("�޷���������").setIcon(R.drawable.cloud_fail)
						.setMessage("��Ҫ���ֺͱ����豸��ͬһ������");
				failAlert.create().show();
				return;
			}
			userRegName = txtName.getText().toString();
			userRegPassword = txtPassword.getText().toString();
			String rePassword = txtrePassword.getText().toString();
			wifiPwd = txtWifipassword.getText().toString();

			if (userRegName.trim().isEmpty()||(!CheckEmailPhoneTools.isEmail(userRegName))) {
				Toast.makeText(getApplicationContext(), "��������ȷ�������ַ��Ϊ�û���", Toast.LENGTH_SHORT).show();
				txtName.setFocusable(true);
				return;
			}
			if (userRegPassword.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "����������", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			if (userRegPassword.length() < 6) {
				Toast.makeText(getApplicationContext(), "���볤�ȹ���", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			if(!rePassword.equals(userRegPassword)){
				Toast.makeText(getApplicationContext(), "�����������벻һ��", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}

			//�ȴ���
			dialogView = new MyDialogView(RegisterActivity.this);
			dialogView.showMyDialog("���ڻ�ȡ�豸", "���ڴӷ�������ȡ�豸,��ȴ�");

			new ConnectDevThread().start();
		}
	}


	//�������豸����WI-FI,��ɨ�豾���豸��ȡ�����豸ID.
	class ConnectDevThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();

			deviceInfo = new ConfigDevice(wifiPwd,switchIsHidden.isChecked(),IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE)),RegisterActivity.this);
			//����Ƿ�������
			if(deviceInfo.getApSSid() == null){
				message.what = NO_WIFI;
				handler.sendMessage(message);
				return;
			}
			//ִ�������߳�
			deviceInfo.configDeviceThread();
			while(true){

				if(deviceInfo.getConfigResult() == WAIT_RESULT){
					continue;
				}
				if(deviceInfo.getConfigResult() == FIND_DEVID){
					new registerUserThread().start();
				}else{
					message.what = CMD_TIMEOUT;
					handler.sendMessage(message);
				}
				break;
			}
		}
	}

	//ע�����
	class registerUserThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			message.what = REGISTER_FAIL;
			Gson gson = new Gson();

			String[] paramsName = {"userName", "userPassword","deviceId", "devicePassword"};
			String[] paramsValue = {userRegName,userRegPassword,deviceInfo.getDeviceID(),deviceInfo.getDevicePwd()};

			//��Ҫ�жϷ������Ƿ���
			if((jsonResult = new setServerURL().sendParamToServer("register", paramsName, paramsValue)).isEmpty()){
				message.what = SERVER_EXCEPTION;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			}
			catch (JsonSyntaxException e){
				e.printStackTrace();
			}

			switch (Integer.parseInt(getResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = REGISTER_SUCCESS;
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = USER_EXISTED;
					break;
				//�����������쳣
				case Cfg.CODE_EXCEPTION:
					message.what = SERVER_EXCEPTION;
					break;
				default:
					message.what = REGISTER_FAIL;
					break;
			}
			handler.sendMessage(message);
		}
	}
}
