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
 * ��������
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity {

	//��̨ת��ǰ̨
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
				Toast.makeText(getApplicationContext(), "�ɹ�ɾ���豸", Toast.LENGTH_SHORT)
						.show();
				finish();
				tempIntent = new Intent(MainActivity.this, MainActivity.class);
				startActivity(tempIntent);
				break;

			case DELETE_ERROR:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "ɾ���豸ʧ�ܣ�", Toast.LENGTH_SHORT)
						.show();

				break;
			case SERVER_CONNECT_ERROR:

				break;
			case FIND_DEV_SUCCEED:
				dialogView.closeMyDialog();
				//������豸�Ѿ�����
				if(Cfg.devInfo != null) {
					for (String devID : Cfg.devInfo) {
						if (deviceInfo.getDeviceID().equals(devID)) {
							failAlert.setTitle(" ���ʧ��").setIcon(R.drawable.cloud_fail).setMessage("   û���µı����豸");
							failAlert.create().show();
							return;
						}
					}
				}
				failAlert.setTitle(" ��ӱ����豸").setMessage("   �Ƿ���ӱ����豸\n  �豸ID:"+ deviceInfo.getDeviceID())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						//�ȴ���
						dialogView = new MyDialogView(MainActivity.this);
						dialogView.showMyDialog("����豸���ƶ�", "������ӱ����豸���ƶ�,��ȴ�");
						new addDeviceThread().start();
					}
				}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				failAlert.create().show();
					break;
			case FIND_DEV_TIMEOUT:
				dialogView.closeMyDialog();

				failAlert.setTitle(" ���ʧ��").setIcon(R.drawable.cloud_fail).setMessage("   �޷��ҵ������豸");
				failAlert.create().show();
					break;
			case ADD_DEV_SUCCED:
				dialogView.closeMyDialog();
				Toast.makeText(MainActivity.this, "����豸�ɹ�", Toast.LENGTH_SHORT)
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
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
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
		//�ȴ���
		dialogView = new MyDialogView(MainActivity.this);
		dialogView.showMyDialog("���ڻ�ȡ�豸", "���ڴӷ�������ȡ�豸,��ȴ�");
		new GetDevThread().start();

	}

	//��׽back��
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
			Toast.makeText(MainActivity.this, "������豸", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		//���û��ѡ����豸,����ѡ��.
		if(Cfg.currentDeviceID.isEmpty())
		{
			Toast.makeText(MainActivity.this, "��ѡ���豸", Toast.LENGTH_SHORT)
					.show();
		}
		for (String devID : Cfg.devInfo) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id", devID);
			item.put("name", "δ����");
			data.add(item);
		}
		// ����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���
		SimpleAdapter adapter = new MySimpleAdapter(this, data,
				R.layout.devitem, new String[] { "id", "name"},
				new int[] { R.id.devId, R.id.devName});
		// ʵ���б����ʾ
		listView.setAdapter(adapter);
		// ɾ���ָ���
		listView.setDivider(null);

		listView.setOnItemClickListener(new ItemClickListener());
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}


	//���豸�б��浽Cfg.devInfo��̬������
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
	 * ˢ�� ��ť�����¼�
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
					.setTitle("������·��������");
			myDialog.setView(layout);
			myDialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText userSetPassword = (EditText) layout.findViewById(R.id.apPassword);
					wifiPassword = userSetPassword.getText().toString();
					SSIDisHidden = ((Switch)layout.findViewById(R.id.IsHiddenSSID)).isChecked();
					dialog.dismiss();

					//�ȴ���
					dialogView = new MyDialogView(MainActivity.this);
					dialogView.showMyDialog("��������豸", "����ɨ�豾������Ӳ��,��ȴ�");
					//ɨ���豸
					new ConnectDevThread().start();
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
	//�������豸����WI-FI,��ɨ�豾���豸��ȡ�����豸ID.
	class ConnectDevThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();

			deviceInfo = new ConfigDevice(wifiPassword,SSIDisHidden, IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE)),MainActivity.this);
			//ִ�������߳�
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

	// ��ȡ����¼�
	private final class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;
			HashMap<String, Object> data = (HashMap<String, Object>) listView
					.getItemAtPosition(position);

			String devId = (String) data.get("id");

			if (devId == null) {
				Toast.makeText(getApplicationContext(), "������ѡ���豸", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// ��ת�����ý���
			Cfg.currentDeviceID = devId;
			//����ѡ���豸
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

					Log.i(TAG, "ItemClickListener devId��" + deleteDevId);

					if (deleteDevId == null) {

						message.what = DELETE_ERROR;
						handler.sendMessage(message);

						return;
					}
					//����"ȷ��ɾ��"��ʾ��
					AlertDialog.Builder deleteAlert = new AlertDialog.Builder(MainActivity.this);
					deleteAlert.setTitle("  ȷ��ɾ�����豸?");
					deleteAlert.setIcon(R.drawable.delete_alert);

					deleteAlert.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							//�ȴ���
							dialogView = new MyDialogView(MainActivity.this);
							dialogView.showMyDialog("ɾ���豸��", "���ڴӷ�����ɾ���豸,��ȴ�");

							new DelDevThread(deleteDevId).start();
							return;
						}
					});

					deleteAlert.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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

			//��Ҫ�жϷ������Ƿ���
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
