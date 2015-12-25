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
 * 注册类
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

				Toast.makeText(RegisterActivity.this, "注册新用户成功!",
						Toast.LENGTH_SHORT).show();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Bundle bundle = new Bundle();
				bundle.putString("activity", "register");
				Intent mainIntent = new Intent();
				mainIntent.setClass(RegisterActivity.this, DeviceDataViewActivity.class);
				mainIntent.putExtras(bundle);
				startActivity(mainIntent);// 打开新界面
				finish();
				break;
			case NO_WIFI:

				failAlert.setTitle("无法无线网络").setIcon(R.drawable.cloud_fail).setMessage("需要保持和本地设备在同一网络中");
				failAlert.create().show();
				break;
			case CMD_TIMEOUT:

				failAlert.setTitle("无法找到本地设备").setIcon(R.drawable.cloud_fail).setMessage("注册时需要绑定本地设备");
				failAlert.create().show();
				break;
			case USER_EXISTED:

				failAlert.setTitle(" 注册失败").setIcon(R.drawable.cloud_fail).setMessage("   用户已经存在");
				failAlert.create().show();
				break;
			case SERVER_EXCEPTION:

				failAlert.setTitle(" 注册失败").setIcon(R.drawable.cloud_fail).setMessage("   服务器异常");
				failAlert.create().show();
				break;
			default:

				failAlert.setTitle(" 注册失败").setIcon(R.drawable.cloud_fail).setMessage("   注册新用户失败");
				failAlert.create().show();
				break;

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序

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
		//显示SSID
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
	 * 注册 按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(noWifi){
				failAlert.setTitle("无法无线网络").setIcon(R.drawable.cloud_fail)
						.setMessage("需要保持和本地设备在同一网络中");
				failAlert.create().show();
				return;
			}
			userRegName = txtName.getText().toString();
			userRegPassword = txtPassword.getText().toString();
			String rePassword = txtrePassword.getText().toString();
			wifiPwd = txtWifipassword.getText().toString();

			if (userRegName.trim().isEmpty()||(!CheckEmailPhoneTools.isEmail(userRegName))) {
				Toast.makeText(getApplicationContext(), "请输入正确的邮箱地址作为用户名", Toast.LENGTH_SHORT).show();
				txtName.setFocusable(true);
				return;
			}
			if (userRegPassword.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			if (userRegPassword.length() < 6) {
				Toast.makeText(getApplicationContext(), "密码长度过短", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
			if(!rePassword.equals(userRegPassword)){
				Toast.makeText(getApplicationContext(), "两次输入密码不一致", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}

			//等待框
			dialogView = new MyDialogView(RegisterActivity.this);
			dialogView.showMyDialog("正在获取设备", "正在从服务器获取设备,请等待");

			new ConnectDevThread().start();
		}
	}


	//先配置设备连接WI-FI,再扫描本地设备获取本地设备ID.
	class ConnectDevThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();

			deviceInfo = new ConfigDevice(wifiPwd,switchIsHidden.isChecked(),IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE)),RegisterActivity.this);
			//检查是否有网络
			if(deviceInfo.getApSSid() == null){
				message.what = CMD_TIMEOUT;
				handler.sendMessage(message);
				return;
			}
			//执行配置线程
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

	//注册进程
	class registerUserThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			message.what = REGISTER_FAIL;
			Gson gson = new Gson();

			String[] paramsName = {"userName", "userPassword","deviceId", "devicePassword"};
			String[] paramsValue = {userRegName,userRegPassword,deviceInfo.getDeviceID(),deviceInfo.getDevicePwd()};

			//需要判断服务器是否开启
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
				//服务器程序异常
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
