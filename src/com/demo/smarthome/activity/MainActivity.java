package com.demo.smarthome.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.smarthome.R;
import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.iprotocol.IProtocol;
import com.demo.smarthome.protocol.Msg;
import com.demo.smarthome.protocol.PlProtocol;
import com.demo.smarthome.server.DeviceDataResult;
import com.demo.smarthome.server.DeviceDataSet;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigDevice;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.IpTools;
import com.demo.smarthome.tools.NetworkStatusTools;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.DialogInterface;
import android.view.LayoutInflater;
/**
 * ��������
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		new GetDevThread().start();
	}

	Button btnRefresh = null;
	Button btnAddDev = null;

	ListView listView;
	private final String TAG = "MainActivity";

	String wifiPassword;
	boolean SSIDisHidden = false;

	ConfigDevice deviceInfo;
	AlertDialog.Builder failAlert;

	static final int GET_DEV_SUCCEED = 0;
	static final int GET_DEV_ERROR = 1;
	static final int BUTTON_DELETE = 2;
	static final int DELETE_ERROR = 5;
	static final int SERVER_CONNECT_ERROR = 6;
	static final int ADD_DEV_SUCCED 		= 7;
	static final int ADD_DEV_FAIL 		    = 8;
	static final int GET_DEV_TYPE_SUCCEED	= 9;
	static final int GET_DEV_TYPE_ERROR		= 10;

	static final int WAIT_RESULT  = 0;
	static final int FIND_DEVID = 2;
	static final int CMD_TIMEOUT = 6;

	static final int FIND_DEV_SUCCEED = 0X20;
	static final int FIND_DEV_TIMEOUT = 0X21;

	private enum getTypeFunc {
		addDevice , clickList
	}
	getTypeFunc getTypeFor;
	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	MyDialogView dialogView;
	Intent tempIntent;
	ConfigService dbService;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_DEV_SUCCEED:
				dialogView.closeMyDialog();
				getDevList();
				break;
			case GET_DEV_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(getApplicationContext(), "请重新登录", Toast.LENGTH_SHORT)
						.show();
				tempIntent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(tempIntent);
				finish();
				break;

			case BUTTON_DELETE:
				dialogView.closeMyDialog();
				Toast.makeText(getApplicationContext(), "删除设备成功", Toast.LENGTH_SHORT)
						.show();
				finish();
				tempIntent = new Intent(MainActivity.this, MainActivity.class);
				startActivity(tempIntent);
				break;

			case DELETE_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "删除设备失败", Toast.LENGTH_SHORT)
						.show();

				break;
			case SERVER_CONNECT_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "删除设备失败,"+ StringRes.canNotConnetServer, Toast.LENGTH_SHORT)
						.show();
				break;
			case FIND_DEV_SUCCEED:
				dialogView.closeMyDialog();

				if(Cfg.devInfo != null) {
					for (String devID : Cfg.devInfo) {
						if (deviceInfo.getDeviceID().equals(devID)) {
							failAlert.setTitle("警告").setIcon(R.drawable.warning_01).setMessage("请勿重复绑定设备");
							failAlert.create().show();
							return;
						}
					}
				}
				failAlert.setTitle("发现设备").setMessage("发现设备 ID:"+ deviceInfo.getDeviceID())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						dialogView.showMyDialog("添加设备", "正在添加设备,请稍等");
						Log.d(TAG,"add device alert view,thread "+Thread.currentThread().getName());
						getTypeFor = getTypeFunc.addDevice;

						new getDeviceType().start();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				failAlert.create().show();
					break;
			case FIND_DEV_TIMEOUT:
				dialogView.closeMyDialog();

				failAlert.setTitle("错误").setIcon(R.drawable.error_01).setMessage("请检查设备是否为搜索网络模式");
				failAlert.create().show();
					break;
			case ADD_DEV_SUCCED:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "添加设备成功", Toast.LENGTH_SHORT)
						.show();
				if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_02L))
				{
					tempIntent = new Intent(MainActivity.this, BGPM02LRealtimeDataActivity.class);

				}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_08))
				{
					tempIntent = new Intent(MainActivity.this, BGPM08RealtimeDataActivity.class);
				}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_10))
				{
					tempIntent = new Intent(MainActivity.this, BGPM10RealtimeDataActivity.class);
				}

				startActivity(tempIntent);
				finish();
				break;
			case ADD_DEV_FAIL:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "添加设备失败,请重新添加", Toast.LENGTH_SHORT)
						.show();
					break;
			case GET_DEV_TYPE_SUCCEED:
				if(getTypeFor == getTypeFunc.addDevice) {
					new addDeviceThread().start();
				}else {
					if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_02L))
					{
						tempIntent = new Intent(MainActivity.this, BGPM02LRealtimeDataActivity.class);

					}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_08))
					{
						tempIntent = new Intent(MainActivity.this, BGPM10RealtimeDataActivity.class);
					}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_10))
					{
						tempIntent = new Intent(MainActivity.this, BGPM10RealtimeDataActivity.class);
					}
					startActivity(tempIntent);
					finish();
				}
				break;
			case GET_DEV_TYPE_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "请重新绑定", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;

			}
		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ActivityControl.getInstance().addActivity(this);
		TextView title = (TextView) findViewById(R.id.titleMain);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		}) ;

		btnRefresh = (Button) findViewById(R.id.setupBtnRefresh);
		btnRefresh.setOnClickListener(new BtnRefreshOnClickListener());

		btnAddDev = (Button) findViewById(R.id.mainBtnAddDev);
		btnAddDev.setOnClickListener(new BtnAddDevOnClickListener());

		listView = (ListView) findViewById(R.id.devListView);

		dbService = new ConfigDao(MainActivity.this.getBaseContext());

		failAlert = new AlertDialog.Builder(MainActivity.this);

		dialogView = new MyDialogView(MainActivity.this);
		dialogView.showMyDialog("读取数据", "...请等待");
		new GetDevThread().start();

	}

	@Override
	public void onBackPressed(){
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if(bundle != null){
			if (bundle.getString("activity").equals("login")) {
				intent.setClass(MainActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}
		else {
			super.onBackPressed();
		}
	}

	private void getDevList() {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		if(Cfg.devInfo == null || Cfg.devInfo.length == 0) {
			Toast.makeText(MainActivity.this, "无绑定设备,请绑定设备", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if(Cfg.currentDeviceID.isEmpty())
		{
			Toast.makeText(MainActivity.this, "请绑定设备", Toast.LENGTH_SHORT)
					.show();
		}
		for (String devID : Cfg.devInfo) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id", devID);
			if(dbService.getCfgByKey(devID).equals(DeviceInformation.DEV_TYPE_BGPM_02L))
			{
				item.put("name", "PM2.5监测仪");
			}
			else if(dbService.getCfgByKey(devID).equals(DeviceInformation.DEV_TYPE_BGPM_08))
			{
				item.put("name", "多功能甲醛监测仪");
			}
			else if(dbService.getCfgByKey(devID).equals(DeviceInformation.DEV_TYPE_BGPM_10))
			{
				item.put("name", "多功能空气监测仪");
			}
			else{
				item.put("name", "未知");
			}
			data.add(item);
		}

		SimpleAdapter adapter = new MySimpleAdapter(this, data,
				R.layout.devitem, new String[] { "id", "name"},
				new int[] { R.id.devId, R.id.devName});

		listView.setAdapter(adapter);

		listView.setDivider(null);

		listView.setOnItemClickListener(new ItemClickListener());
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}



	class GetDevThread extends Thread {
		@Override
		public void run() {

			Message message = new Message();
			getResult = LoginServer.LoginServerMethod();
			if(getResult.getCode().equals(String.valueOf(Cfg.SERVER_CANT_CONNECT))) {
				message.what = SERVER_CONNECT_ERROR;
				handler.sendMessage(message);
				return;
			}else if(getResult.getCode().equals(String.valueOf(Cfg.USERNAME_EXCEPTION))){
				message.what = GET_DEV_ERROR;
				handler.sendMessage(message);
				return;
			}
			message.what = GET_DEV_SUCCEED;

			handler.sendMessage(message);
		}
	}

	/**
	 *
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRefreshOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
			Intent intent = new Intent(MainActivity.this, MainActivity.class);
			startActivity(intent);
		}
	}


	class BtnAddDevOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {

			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			final View layout = inflater.inflate(R.layout.add_device_wifi_password, null);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("请输入WI-FI密码");
			myDialog.setView(layout);
			myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetPassword = (EditText) layout.findViewById(R.id.apPassword);
					wifiPassword = userSetPassword.getText().toString();
					SSIDisHidden = ((Switch)layout.findViewById(R.id.IsHiddenSSID)).isChecked();
					dialog.dismiss();

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
					alertDialog.setTitle("注意").setMessage("请确定已经将设备设置成搜索网络模式")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									if(!NetworkStatusTools.isWifi(MainActivity.this)){
										Toast.makeText(MainActivity.this, "请连接先WI-FI", Toast.LENGTH_SHORT)
												.show();
										return;
									}
									dialogView.showMyDialog("注册", "正在扫描设备,请稍等");

									new ConnectDevThread().start();
								}
							});
					alertDialog.create().show();
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

	class ConnectDevThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();

			deviceInfo = new ConfigDevice(wifiPassword,SSIDisHidden, IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE)),MainActivity.this);

			if(!NetworkStatusTools.isNetworkAvailable(MainActivity.this)){
				message.what = FIND_DEV_TIMEOUT;
				handler.sendMessage(message);
				return;
			}

			deviceInfo.configDeviceThread();
			while(true){

				if(deviceInfo.getConfigResult() == WAIT_RESULT){
					continue;
				}
				if(deviceInfo.getConfigResult() == FIND_DEVID){
					message.what = FIND_DEV_SUCCEED;
					Log.d(TAG,"FIND_DEV_SUCCEED");
				}else{
					message.what = FIND_DEV_TIMEOUT;
				}
				handler.sendMessage(message);
				break;
			}
		}
	}
	class addDeviceThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			message.what = ADD_DEV_FAIL;

			Gson gson = new Gson();

			String[] paramsName = {"userName", "deviceId", "devicePassword"};
			String[] paramsValue = {Cfg.userName, deviceInfo.getDeviceID(), deviceInfo.getDevicePwd()};

			setServerURL addDevSet = new setServerURL();

			if ((jsonResult = addDevSet.sendParamToServer("addDeviceForUser", paramsName, paramsValue)).isEmpty()) {
				message.what = SERVER_CONNECT_ERROR;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult, ServerReturnResult.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				message.what = ADD_DEV_FAIL;
				handler.sendMessage(message);
				return;
			}


			switch (Integer.parseInt(getResult.getCode())) {
				case Cfg.CODE_SUCCESS:

					Cfg.currentDeviceID = deviceInfo.getDeviceID();
					dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID, Cfg.currentDeviceID);
					message.what = ADD_DEV_SUCCED;
					Log.d(TAG,"ADD_DEV_SUCCED");
					handler.sendMessage(message);
					break;
				default:
					message.what = ADD_DEV_FAIL;
					handler.sendMessage(message);
					break;
			}

		}
	}

	class getDeviceType extends Thread {
		@Override
		public void run () {
			Message message = new Message();

			if(getTypeFor == getTypeFunc.addDevice){
				if (deviceInfo.getDeviceID().isEmpty()) {
					message.what = GET_DEV_TYPE_ERROR;
					handler.sendMessage(message);
					return;
				}

				if(!LoginServer.getDeviceType(deviceInfo.getDeviceID())){
					message.what = GET_DEV_TYPE_ERROR;
					handler.sendMessage(message);
					return;
				}
			}else {
				if(Cfg.currentDeviceID.isEmpty()){
					message.what = GET_DEV_TYPE_ERROR;
					handler.sendMessage(message);
					return;
				}
				if(!LoginServer.getDeviceType(Cfg.currentDeviceID)){
					message.what = GET_DEV_TYPE_ERROR;
					handler.sendMessage(message);
					return;
				}
			}
			dbService.SaveSysCfgByKey(deviceInfo.getDeviceID(), Cfg.deviceType);
			Cfg.currentDeviceType = Cfg.deviceType;
			message.what = GET_DEV_TYPE_SUCCEED;
			handler.sendMessage(message);
			return;
		}
	}
	private final class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;

			@SuppressWarnings("unchecked")
			HashMap<String, Object> data = (HashMap<String, Object>) listView
					.getItemAtPosition(position);

			String devId = (String) data.get("id");

			if (devId == null) {
				Toast.makeText(getApplicationContext(), "设备出现错误,请删除该设备", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			Cfg.currentDeviceID = devId;
			dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID,Cfg.currentDeviceID);

			getTypeFor = getTypeFunc.clickList;
			Cfg.currentDeviceType = dbService.getCfgByKey(devId);
			if(Cfg.currentDeviceType.isEmpty())
			{
				Toast.makeText(getApplicationContext(), "该设备出现错误,请删除该设备", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_02L))
			{
				tempIntent = new Intent(MainActivity.this, BGPM02LRealtimeDataActivity.class);

			}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_08))
			{
				tempIntent = new Intent(MainActivity.this, BGPM08RealtimeDataActivity.class);
			}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_10))
			{
				tempIntent = new Intent(MainActivity.this, BGPM10RealtimeDataActivity.class);
			}
			startActivity(tempIntent);
			finish();
		}
	}


	class MySimpleAdapter extends SimpleAdapter {


		public MySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final int mPosition = position;
			convertView = super.getView(position, convertView, parent);

			ImageView buttonDelete = (ImageView) convertView
					.findViewById(R.id.devDelete);
			buttonDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Message message = new Message();
					message.what = DELETE_ERROR;
					message.arg1 = mPosition;

					@SuppressWarnings("unchecked")
					HashMap<String, Object> data1 = (HashMap<String, Object>) listView
							.getItemAtPosition(message.arg1);

					final String deleteDevId = (String) data1.get("id");

					if (deleteDevId == null) {

						message.what = DELETE_ERROR;
						handler.sendMessage(message);

						return;
					}

					AlertDialog.Builder deleteAlert = new AlertDialog.Builder(MainActivity.this);
					deleteAlert.setTitle("确定删除该设备?");
					deleteAlert.setIcon(R.drawable.delete_alert);

					deleteAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialogView.showMyDialog("删除", "正在删除设备中");

							new DelDevThread(deleteDevId).start();
							return;
						}
					});

					deleteAlert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							return;
						}
					});
					deleteAlert.show();
				}
			});
			return convertView;
		}

	}

	class DelDevThread extends Thread {
		String id;

		public DelDevThread(String strId) {
			id = strId;

		}

		@Override
		public void run() {

			if (id.isEmpty()) {
				return;
			}

			Message message = new Message();
			message.what = DELETE_ERROR;
			Gson gson = new Gson();

			String[] paramsName = {"userName","deviceId"};
			String[] paramsValue = {Cfg.userName,id};

			setServerURL removeUser= new setServerURL();

			if((jsonResult = removeUser.sendParamToServer("removeDeviceById", paramsName, paramsValue)).isEmpty()){
				message.what = SERVER_CONNECT_ERROR;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, ServerReturnResult.class);
			}
			catch (JsonSyntaxException e){
				e.printStackTrace();
			}


			switch (Integer.parseInt(getResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = BUTTON_DELETE;
					if(id.equals(dbService.getCfgByKey(Cfg.KEY_DEVICE_ID))){
						dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID,"");
					}
					break;
				default:
					message.what = DELETE_ERROR;
					break;
			}
			handler.sendMessage(message);

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束Activity&从栈中移除该Activity
		ActivityControl.getInstance().removeActivity(this);
	}

}
