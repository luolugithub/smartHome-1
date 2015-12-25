package com.demo.smarthome.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.smarthome.R;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.iprotocol.IProtocol;
import com.demo.smarthome.protocol.Msg;
import com.demo.smarthome.protocol.PlProtocol;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigDevice;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.tools.IpTools;
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
 * 主界面类
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity {

	//后台转到前台
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
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

	static final int WAIT_RESULT  = 0;
	static final int FIND_DEVID = 2;
	static final int CMD_TIMEOUT = 6;

	static final int FIND_DEV_SUCCEED = 0X10;
	static final int FIND_DEV_TIMEOUT = 0X11;

	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();

	MyDialogView dialogView;
	Intent tempIntent;
	ConfigService dbService;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			failAlert = new AlertDialog.Builder(MainActivity.this);
			switch (msg.what) {

			case GET_DEV_SUCCEED:
				dialogView.closeMyDialog();
				getDevList();
				break;
			case GET_DEV_ERROR:

				break;

			case BUTTON_DELETE:
				dialogView.closeMyDialog();
				Toast.makeText(getApplicationContext(), "成功删除设备", Toast.LENGTH_SHORT)
						.show();
				finish();
				tempIntent = new Intent(MainActivity.this, MainActivity.class);
				startActivity(tempIntent);
				break;

			case DELETE_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "删除设备失败！", Toast.LENGTH_SHORT)
						.show();

				break;
			case SERVER_CONNECT_ERROR:

				break;
			case FIND_DEV_SUCCEED:
				dialogView.closeMyDialog();
				//如果该设备已经存在
				if(Cfg.devInfo != null) {
					for (String devID : Cfg.devInfo) {
						if (deviceInfo.getDeviceID().equals(devID)) {
							failAlert.setTitle(" 添加失败").setIcon(R.drawable.cloud_fail).setMessage("   没有新的本地设备");
							failAlert.create().show();
							return;
						}
					}
				}
				failAlert.setTitle(" 添加本地设备").setMessage("   是否添加本地设备\n  设备ID:"+ deviceInfo.getDeviceID())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						//等待框
						dialogView = new MyDialogView(MainActivity.this);
						dialogView.showMyDialog("添加设备到云端", "正在添加本地设备到云端,请等待");
						new addDeviceThread().start();
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

				failAlert.setTitle(" 添加失败").setIcon(R.drawable.cloud_fail).setMessage("   无法找到本地设备");
				failAlert.create().show();
					break;
			case ADD_DEV_SUCCED:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "添加设备成功", Toast.LENGTH_SHORT)
						.show();
				finish();
				tempIntent = new Intent(MainActivity.this, MainActivity.class);
				startActivity(tempIntent);
				break;
			default:
				break;

			}
		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_main);

		TextView title = (TextView) findViewById(R.id.titleMain);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});

		btnRefresh = (Button) findViewById(R.id.setupBtnRefresh);
		btnRefresh.setOnClickListener(new BtnRefreshOnClickListener());

		btnAddDev = (Button) findViewById(R.id.mainBtnAddDev);
		btnAddDev.setOnClickListener(new BtnAddDevOnClickListener());

		listView = (ListView) findViewById(R.id.devListView);

		dbService = new ConfigDao(MainActivity.this.getBaseContext());
		//等待框
		dialogView = new MyDialogView(MainActivity.this);
		dialogView.showMyDialog("正在获取设备", "正在从服务器获取设备,请等待");
		new GetDevThread().start();

	}

	//捕捉back键
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

		if(Cfg.devInfo == null) {
			Toast.makeText(MainActivity.this, "请添加设备", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		//如果没有选择过设备,让他选择.
		if(Cfg.currentDeviceID.isEmpty())
		{
			Toast.makeText(MainActivity.this, "请选择设备", Toast.LENGTH_SHORT)
					.show();
		}
		for (String devID : Cfg.devInfo) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id", devID);
			item.put("name", "未定义");
			data.add(item);
		}
		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		SimpleAdapter adapter = new MySimpleAdapter(this, data,
				R.layout.devitem, new String[] { "id", "name"},
				new int[] { R.id.devId, R.id.devName});
		// 实现列表的显示
		listView.setAdapter(adapter);
		// 删除分割线
		listView.setDivider(null);

		listView.setOnItemClickListener(new ItemClickListener());
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}


	//将设备列表储存到Cfg.devInfo静态变量中
	class GetDevThread extends Thread {
		@Override
		public void run() {

			Message message = new Message();

			if((getResult = LoginServer.LoginServerMethod())==null) {
				message.what = SERVER_CONNECT_ERROR;
				handler.sendMessage(message);
				return;
			}
			message.what = GET_DEV_SUCCEED;

			handler.sendMessage(message);
		}
	}

	/**
	 * 刷新 按钮监听事件
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
					.setTitle("请输入路由器密码");
			myDialog.setView(layout);
			myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetPassword = (EditText) layout.findViewById(R.id.apPassword);
					wifiPassword = userSetPassword.getText().toString();
					SSIDisHidden = ((Switch)layout.findViewById(R.id.IsHiddenSSID)).isChecked();
					dialog.dismiss();

					//等待框
					dialogView = new MyDialogView(MainActivity.this);
					dialogView.showMyDialog("正在添加设备", "正在扫描本地智能硬件,请等待");
					//扫描设备
					new ConnectDevThread().start();
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
	//先配置设备连接WI-FI,再扫描本地设备获取本地设备ID.
	class ConnectDevThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();

			deviceInfo = new ConfigDevice(wifiPassword,SSIDisHidden, IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE)),MainActivity.this);
			//执行配置线程
			deviceInfo.configDeviceThread();
			while(true){

				if(deviceInfo.getConfigResult() == WAIT_RESULT){
					continue;
				}
				if(deviceInfo.getConfigResult() == FIND_DEVID){
					message.what = FIND_DEV_SUCCEED;
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

			String[] paramsName = {"userName","deviceId","devicePassword"};
			String[] paramsValue = {Cfg.userName,deviceInfo.getDeviceID(),deviceInfo.getDevicePwd()};

			setServerURL addDevSet= new setServerURL();

			if((jsonResult = addDevSet.sendParamToServer("addDeviceForUser", paramsName, paramsValue)).isEmpty()){
				message.what = Cfg.SERVER_CANT_CONNECT;
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
					message.what = ADD_DEV_SUCCED;
					break;
				default:
					message.what = ADD_DEV_FAIL;
					break;
			}
			handler.sendMessage(message);
		}

	}

	// 获取点击事件
	private final class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;
			HashMap<String, Object> data = (HashMap<String, Object>) listView
					.getItemAtPosition(position);

			String devId = (String) data.get("id");

			if (devId == null) {
				Toast.makeText(getApplicationContext(), "请重新选择设备", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// 跳转到设置界面
			Cfg.currentDeviceID = devId;
			//保存选择设备
			dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID,Cfg.currentDeviceID);

			Intent tempIntent = new Intent();
			tempIntent.setClass(MainActivity.this, DeviceDataViewActivity.class);
			startActivity(tempIntent);

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
					message.what = BUTTON_DELETE;
					message.arg1 = mPosition;

					HashMap<String, Object> data1 = (HashMap<String, Object>) listView
							.getItemAtPosition(message.arg1);
					final String deleteDevId = (String) data1.get("id");

					Log.i(TAG, "ItemClickListener devId：" + deleteDevId);

					if (deleteDevId == null) {

						message.what = DELETE_ERROR;
						handler.sendMessage(message);

						return;
					}
					//弹出"确定删除"警示框
					AlertDialog.Builder deleteAlert = new AlertDialog.Builder(MainActivity.this);
					deleteAlert.setTitle("  确定删除该设备?");
					deleteAlert.setIcon(R.drawable.delete_alert);

					deleteAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							//等待框
							dialogView = new MyDialogView(MainActivity.this);
							dialogView.showMyDialog("删除设备中", "正在从服务器删除设备,请等待");

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

			//需要判断服务器是否开启
			if((jsonResult = removeUser.sendParamToServer("removeDeviceById", paramsName, paramsValue)).isEmpty()){
				message.what = SERVER_CONNECT_ERROR;
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

}
