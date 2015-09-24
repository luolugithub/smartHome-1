package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.activity.ScanActivity.StartUDPThread;
import com.demo.smarthome.activity.ScanActivity.UDPThread;
import com.demo.smarthome.device.Dev;
import com.demo.smarthome.iprotocol.IProtocol;
import com.demo.smarthome.protocol.MSGCMD;
import com.demo.smarthome.protocol.MSGCMDTYPE;
import com.demo.smarthome.protocol.Msg;
import com.demo.smarthome.protocol.PlProtocol;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.SocketService;
import com.demo.smarthome.service.SocketService.SocketBinder;
import com.demo.smarthome.tools.IpTools;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.view.SlipButton;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 设备显示类
 * 
 * @author Administrator
 * 
 */
public class DevViewActivity extends Activity {
	Dev dev = null;
	TextView title = null;
	// TextView txtId = null;
	TextView txtName = null;
	TextView devViewLampStat = null; // lamp pic
	TextView txtOnLine = null;
	// TextView txtState = null;

	// Button btnOn;
	// Button btnOff;
	// Button btnRefresh;

	SeekBar devViewSeekLampR;
	SeekBar devViewSeekLampG;
	SeekBar devViewSeekLampB;
	// TextView devViewLblLampRVal;
	// TextView devViewLblLampGVal;
	// TextView devViewLblLampBVal;
	ProgressBar devViewProgBarCmdInfo;
	// ImageView btnOpen;

	ImageView btnOn;
	ImageView btnOff;

	int cmdSendCount = 0;
	boolean btnIsOpen = false;
	IProtocol protocol = new PlProtocol();

	private String TAG = "DevViewActivity";
	static final int SEND_SUCCEED = 0;
	static final int SEND_ERROR = 1;
	static final int REFRESH_START = 4;
	static final int TIME_OUT = 5;

	static final int CMD_SUCCEEDT = 6;
	static final int CMD_TIMEOUT = 7;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			devViewLampChagne();
			// txtState.setText((dev.isLightState() ? "开  灯" : "关  灯"));
			switch (msg.what) {
			case SEND_SUCCEED:
				Toast.makeText(DevViewActivity.this, "开关灯成功....",
						Toast.LENGTH_SHORT).show();
				uiEnable(true);
				// if(dev.isLightState()){
				// devViewLampStat.setBackgroundResource(R.drawable.light_on);
				// }else{
				// devViewLampStat.setBackgroundResource(R.drawable.light_off);
				// }
				break;
			case SEND_ERROR:
				Toast.makeText(DevViewActivity.this, "开关灯失败！",
						Toast.LENGTH_SHORT).show();
				uiEnable(true);
				// if(dev.isLightState()){
				// devViewLampStat.setBackgroundResource(R.drawable.light_on);
				// }else{
				// devViewLampStat.setBackgroundResource(R.drawable.light_off);
				// }
				break;
			case REFRESH_START:

				break;
			case CMD_SUCCEEDT:
				Toast.makeText(DevViewActivity.this, "命令执行成功！",
						Toast.LENGTH_SHORT).show();
				uiEnable(true);
				lamValChange();
				break;
			case CMD_TIMEOUT:
				Toast.makeText(DevViewActivity.this, "命令执行失败！",
						Toast.LENGTH_SHORT).show();
				uiEnable(true);
				lamValChange();
				break;
			case TIME_OUT:
				// Toast.makeText(DevViewActivity.this, "开始发送查询指令！",
				// Toast.LENGTH_SHORT).show();
				// new RefreshThread().start();
				break;

			default:
				break;

			}
		}

		private void lamValChange() {
			// TODO Auto-generated method stub
			if (dev.getLampRVal() != devViewSeekLampR.getProgress()) {
				devViewSeekLampR.setProgress(dev.getLampRVal());
			}
			if (dev.getLampGVal() != devViewSeekLampG.getProgress()) {
				devViewSeekLampG.setProgress(dev.getLampGVal());
			}
			if (dev.getLampBVal() != devViewSeekLampB.getProgress()) {
				devViewSeekLampB.setProgress(dev.getLampBVal());
			}
		}

	};

	SocketBinder socketBinder;
	SocketService socketService;
	boolean isBinderConnected = false;

	IntentFilter intentFilter = null;
	SocketIsConnectReceiver socketConnectReceiver = new SocketIsConnectReceiver();

	private class SocketIsConnectReceiver extends BroadcastReceiver {// 继承自BroadcastReceiver的子类
		@Override
		public void onReceive(Context context, Intent intent) {// 重写onReceive方法

			if (intent.getBooleanExtra("conn", false)) {

				Log.i(TAG, "socket连接成功。");
			} else {
				Log.i(TAG, "socket连接失败。");
			}
		}
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(TAG, "=============onServiceConnected");
			socketBinder = (SocketBinder) service;
			socketService = socketBinder.getService();
			socketService.myMethod();// ipAddr, port

			isBinderConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxonServiceDisconnected");
			isBinderConnected = false;
			socketBinder = null;
			socketService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_dev_view);
		TextView title = (TextView) findViewById(R.id.titleDevView);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		Intent intent = this.getIntent();
		String devId = intent.getStringExtra("devId");
		dev = Cfg.getDevById(devId);
		Log.i(TAG, "=============devId:" + devId);
		Log.i(TAG, "=============dev:" + dev);
		if (dev == null) {
			return;
		}
		/*
		title = (TextView) findViewById(R.id.titleDevView);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		*/

		// txtId = (TextView) findViewById(R.id.devViewTxtId);
		txtName = (TextView) findViewById(R.id.devViewTxtName);
		devViewLampStat = (TextView) findViewById(R.id.devViewLampStat);
		// txtState = (TextView) findViewById(R.id.devViewTxtState);

		txtOnLine = (TextView) findViewById(R.id.devViewTxtOnLine);

		txtName.setTextSize(24);

		devViewProgBarCmdInfo = (ProgressBar) findViewById(R.id.devViewProgBarCmdInfo);

		// txtState.setTextSize(24);

		// btnOn = (Button) findViewById(R.id.devViewBtnOn);
		// btnOn.setOnClickListener(new BtnOpenOnChangeListener());
		// btnOff = (Button) findViewById(R.id.devViewBtnOff);
		// btnOff.setOnClickListener(new BtnOffOnChangeListener());
		// btnRefresh = (Button) findViewById(R.id.devViewBtnRefresh);
		// btnRefresh.setOnClickListener(new BtnRefreshOnChangeListener());

		devViewSeekLampR = (SeekBar) findViewById(R.id.devViewSeekLampR);
		devViewSeekLampG = (SeekBar) findViewById(R.id.devViewSeekLampG);
		devViewSeekLampB = (SeekBar) findViewById(R.id.devViewSeekLampB);

		devViewSeekLampR
				.setOnSeekBarChangeListener(new LampROnSeekBarChangeListener());
		devViewSeekLampG
				.setOnSeekBarChangeListener(new LampGOnSeekBarChangeListener());
		devViewSeekLampB
				.setOnSeekBarChangeListener(new LampBOnSeekBarChangeListener());

		// devViewLblLampRVal = (TextView)
		// findViewById(R.id.devViewLblLampRVal);
		// devViewLblLampGVal = (TextView)
		// findViewById(R.id.devViewLblLampGVal);
		// devViewLblLampBVal = (TextView)
		// findViewById(R.id.devViewLblLampBVal);

		// btnOpen = (ImageView) findViewById(R.id.devViewBtnState);
		// btnOpen.setOnClickListener( new BtnOpenOnClickListener());

		btnOn = (ImageView) findViewById(R.id.devViewBtnOn);
		btnOn.setOnClickListener(new BtnOpenOnChangeListener());

		btnOff = (ImageView) findViewById(R.id.devViewBtnOff);
		btnOff.setOnClickListener(new BtnOffOnChangeListener());

		// devViewBtnState.setOnChangeListener(new
		// com.demo.smarthome.view.OnChangedListener()
		// {
		// public void OnChanged(boolean CheckState)
		//
		// {
		//
		// // TODO Auto-generated method stub
		//
		// String s;
		//
		// if(devViewBtnState.isNowChoose())
		//
		// {
		//
		// Toast.makeText(DevViewActivity.this, "发送开灯指令", Toast.LENGTH_SHORT)
		// .show();
		//
		// new OpenThread().start();
		// // devViewLampStat.setBackgroundResource(R.drawable.light_on);
		// //devViewLampStat.setBackground(R.drawable.on);
		//
		// }else{
		//
		// Toast.makeText(DevViewActivity.this, "发送关灯指令", Toast.LENGTH_SHORT)
		// .show();
		// new OffThread().start();
		// // devViewLampStat.setBackgroundResource(R.drawable.light_off);
		//
		// }
		// }
		//
		// });
		// devViewSeekLampR.setMax(99);
		// devViewSeekLampG.setProgress(50);
		// devViewSeekLampG.setMax(99);
		// devViewSeekLampB.setProgress(50);
		// devViewSeekLampB.setMax(99);
		//
		// txtId.setText(dev.getId());
		txtName.setText(dev.getTemp() + "");
		// txtState.setText((dev.isLightState() ? "开  灯" : "关  灯"));

		// bind....
		try {
			if (!isBinderConnected) {
				bindService();
			}
		} catch (Exception e) {

		}

		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.SendBoardCastName);
		this.registerReceiver(socketConnectReceiver, intentFilter);

		new RefreshThread().start();
		new ChangeTempThread().start();
		new ChangeLampTempThread().start();
		cmdSendCount = 0;
		devViewLampChagne();
		dev.isDataChange(false);
	}

	private void devViewLampChagne() {
		// TODO Auto-generated method stub
		if (dev.isOnLine()) {
			txtOnLine.setText("在线");
		} else {
			txtOnLine.setText("不在线");

		}
		txtName.setText(dev.getTemp() + "");
		if (dev.isLightState()) {
			devViewLampStat.setBackgroundResource(R.drawable.light_on);
			btnIsOpen = true;
			// btnOpen.setBackgroundResource(R.drawable.on);
		} else {
			btnIsOpen = false;
			devViewLampStat.setBackgroundResource(R.drawable.light_off);
			// btnOpen.setBackgroundResource(R.drawable.off);
		}
		if (dev.getLampRVal() != devViewSeekLampR.getProgress()) {
			devViewSeekLampR.setProgress(dev.getLampRVal());
		}
		if (dev.getLampGVal() != devViewSeekLampG.getProgress()) {
			devViewSeekLampG.setProgress(dev.getLampGVal());
		}
		if (dev.getLampBVal() != devViewSeekLampB.getProgress()) {
			devViewSeekLampB.setProgress(dev.getLampBVal());
		}

	}

	private void bindService() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(DevViewActivity.this, SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dev_view, menu);
		return true;
	}

	/**
	 * 刷新按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRefreshOnChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}
			Toast.makeText(DevViewActivity.this, "发送查询指令", Toast.LENGTH_SHORT)
					.show();

			new RefreshThread().start();
		}

	}

	/**
	 * 开灯按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnOpenOnChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}
			Toast.makeText(DevViewActivity.this, "发送开灯指令", Toast.LENGTH_SHORT)
					.show();

			new StartBtnOpenThread(true).start();
			// new OpenThread().start();
		}

	}

	/**
	 * 关灯按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnOffOnChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}

			Toast.makeText(DevViewActivity.this, "发送关灯指令", Toast.LENGTH_SHORT)
					.show();
			// new OffThread().start();

			new StartBtnOpenThread(false).start();
		}

	}

	/**
	 * 读灯状态命令
	 * 
	 * @author Administrator
	 * 
	 */
	class ChangeTempThread extends Thread {

		@Override
		public void run() {

			Log.v("SendLigheStateThread", "SendLigheStateThread start..");

			while (true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message message = new Message();
				message.what = TIME_OUT;
				handler.sendMessage(message);

				if (dev.isDataChange()) {
					return;
				}
				new RefreshThread().start();
				cmdSendCount++;
				if (cmdSendCount >= 3) {
					return;
				}
			}
		}

	}
	
	class ChangeLampTempThread extends Thread {

		@Override
		public void run() {

			Log.v("SendLigheStateThread", "SendLigheStateThread start..");

			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dev.runStep();
				Message message = new Message();
				message.what = TIME_OUT;
				handler.sendMessage(message);

			
			}
		}

	}

	/**
	 * 发送开灯命令
	 * 
	 * @author Administrator
	 * 
	 */
	class OpenThread extends Thread {

		@Override
		public void run() {
			if (!isBinderConnected) {
				return;
			}
			Log.v("OpenThread", "OpenThread start..");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] data;
			data = new String("LIGHT:1").getBytes();
			Msg msg = new Msg();
			msg.setId(StrTools.byteToSwapByte(StrTools
					.hexStringToBytes(StrTools.strNumToHex(dev.getId()))));
			msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xAA));
			msg.setCmd(MSGCMD.valueOf((byte) 0xFF));
			msg.setTorken(dev.getTorken());
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);

			socketService.socketSendMessage(msg);

			// 查询灯
			data = new String("LIGHT:?").getBytes();
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);
			Log.i(TAG, "         data:" + StrTools.bytesToHexString(data));
			Log.i(TAG,
					"sendData:" + StrTools.bytesToHexString(msg.getSendData()));
			socketService.socketSendMessage(msg);

		}
	}

	class SrartLampValThread extends Thread {
		int lamp = 0; // 1 lampR 2 lampG 3 lampB
		int val = 0; // 0--99

		public SrartLampValThread(int id, int val) {
			this.lamp = id;
			this.val = val;
		}

		public void run() {

		}
	}

	class SetLampValThread extends Thread {
		int lamp = 0; // 1 lampR 2 lampG 3 lampB
		int val = 0; // 0--99
		// int lampVal=0;
		int lampRealVal = 0;
		String str;

		public SetLampValThread(int id, int val) {
			this.lamp = id;
			this.val = val;
		}

		@Override
		public void run() {
			if (!isBinderConnected) {
				return;
			}
			Log.v("OpenThread", "OpenThread start..");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (val < 0) {
				val = 0;
			}
			if (val > 99) {
				val = 99;
			}
			byte[] data;
			if (1 == lamp) {
				str = "LIGHTR:" + val;
			} else if (2 == lamp) {
				str = "LIGHTG:" + val;
			} else if (3 == lamp) {
				str = "LIGHTB:" + val;
			} else {
				return;
			}
			data = str.getBytes();
			Msg msg = new Msg();
			msg.setId(StrTools.byteToSwapByte(StrTools
					.hexStringToBytes(StrTools.strNumToHex(dev.getId()))));
			msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xAA));
			msg.setCmd(MSGCMD.valueOf((byte) 0xFF));
			msg.setTorken(dev.getTorken());
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);
			boolean isCmdOk = false;
			for (int i = 0; i < 10; i++) {
				// if(isCmdOk){
				// Message message = new Message();
				// message.what = CMD_SUCCEEDT;
				// handler.sendMessage(message);
				// return;
				// }
				socketService.socketSendMessage(msg);//
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (1 == lamp) {
					lampRealVal = dev.getLampRVal();
					// lampVal = devViewSeekLampR.getProgress();
				} else if (2 == lamp) {
					lampRealVal = dev.getLampGVal();
					// lampVal = devViewSeekLampG.getProgress();
				} else if (3 == lamp) {
					lampRealVal = dev.getLampBVal();
					// lampVal = devViewSeekLampB.getProgress();
				}
				if (lampRealVal == val) {
					Message message = new Message();
					message.what = CMD_SUCCEEDT;
					handler.sendMessage(message);
					return;
				}
			}
			Message message = new Message();
			message.what = CMD_TIMEOUT;
			handler.sendMessage(message);

			// socketService.socketSendMessage(msg);//
			// // 查询灯
			// data = new String("LIGHT:?").getBytes();
			// msg.setData(data);
			// msg.setDataLen(data.length);
			// protocol.MessageEnCode(msg);
			// Log.i(TAG, "         data:" + StrTools.bytesToHexString(data));
			// Log.i(TAG,
			// "sendData:" + StrTools.bytesToHexString(msg.getSendData()));
			// socketService.socketSendMessage(msg);//

		}
	}

	/**
	 * 发送关灯命令
	 * 
	 * @author Administrator
	 * 
	 */
	class OffThread extends Thread {

		@Override
		public void run() {
			if (!isBinderConnected) {
				return;
			}
			Log.v("OffThread", "OffThread start..");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] data;
			data = new String("LIGHT:0").getBytes();
			Msg msg = new Msg();
			msg.setId(StrTools.byteToSwapByte(StrTools
					.hexStringToBytes(StrTools.strNumToHex(dev.getId()))));
			msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xAA));
			msg.setCmd(MSGCMD.valueOf((byte) 0xFF));
			msg.setTorken(dev.getTorken());
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);

			socketService.socketSendMessage(msg);//

			// 查询灯
			data = new String("LIGHT:?").getBytes();
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);
			Log.i(TAG, "         data:" + StrTools.bytesToHexString(data));
			Log.i(TAG,
					"sendData:" + StrTools.bytesToHexString(msg.getSendData()));
			socketService.socketSendMessage(msg);//

		}
	}

	class RefreshThread extends Thread {

		@Override
		public void run() {
			if (!isBinderConnected) {
				return;
			}
			Log.v("RefreshThread", "RefreshThread start..");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] data;
			data = new String("TEMP:?").getBytes();
			Msg msg = new Msg();
			msg.setId(StrTools.byteToSwapByte(StrTools
					.hexStringToBytes(StrTools.strNumToHex(dev.getId()))));
			msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xAA));
			msg.setCmd(MSGCMD.valueOf((byte) 0xFF));
			msg.setTorken(dev.getTorken());
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);

			socketService.socketSendMessage(msg);

			// 查询灯
			data = new String("LIGHT:?").getBytes();
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);
			Log.i(TAG, "         data:" + StrTools.bytesToHexString(data));
			Log.i(TAG,
					"sendData:" + StrTools.bytesToHexString(msg.getSendData()));
			socketService.socketSendMessage(msg);

		}
	}

	class SendLigheStateThread extends Thread {

		@Override
		public void run() {
			if (!isBinderConnected) {
				return;
			}
			Message message = new Message();
			message.what = SEND_ERROR;

			Log.v("SendLigheStateThread", "SendLigheStateThread start..");
			int delay = 50;
			int reSendTime = 1;
			while (true) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reSendTime--;
				if (reSendTime <= 0) {// send
					reSendTime = 10;
					byte[] data = new byte[0];
					Msg msg = new Msg();
					msg.setId(StrTools.byteToSwapByte(StrTools
							.hexStringToBytes(StrTools.strNumToHex(dev.getId()))));
					msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xAA));
					msg.setCmd(MSGCMD.valueOf((byte) 0xFF));
					msg.setTorken(dev.getTorken());
					msg.setData(data);
					msg.setDataLen(data.length);
					protocol.MessageEnCode(msg);

					Log.i(TAG,
							"         data:" + StrTools.bytesToHexString(data));
					Log.i(TAG,
							"sendData:"
									+ StrTools.bytesToHexString(msg
											.getSendData()));
					socketService.socketSendMessage(msg);//

					// 查询灯

					data = new String("LIGHT:?").getBytes();
					msg.setData(data);
					msg.setDataLen(data.length);
					protocol.MessageEnCode(msg);
					Log.i(TAG,
							"         data:" + StrTools.bytesToHexString(data));
					Log.i(TAG,
							"sendData:"
									+ StrTools.bytesToHexString(msg
											.getSendData()));
					socketService.socketSendMessage(msg);//
				}

				delay--;
				if (delay <= 0) {
					message.what = SEND_ERROR;
					handler.sendMessage(message);
					break;
				}
			}
		}
	}

	class LampROnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			// System.out.println("LampR:"+arg0.getProgress()+"  arg1:"+arg1+
			// "  arg2:"+arg2);

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampRStart:"+arg0.getProgress());

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampRStop:"+arg0.getProgress());
			// devViewLblLampRVal.setText( arg0.getProgress()+"");

			uiEnable(false);
			int val = arg0.getProgress();
			Toast.makeText(DevViewActivity.this, "开始发送命令！", Toast.LENGTH_SHORT)
					.show();
			new SetLampValThread(1, arg0.getProgress()).start();
			val = arg0.getProgress();
		}
	}

	class LampGOnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			// System.out.println("LampG:"+arg0.getProgress()+"  arg1:"+arg1+
			// "  arg2:"+arg2);

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampGStart:"+arg0.getProgress());

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampGStop:"+arg0.getProgress());
			// devViewLblLampGVal.setText( arg0.getProgress()+"");

			uiEnable(false);
			Toast.makeText(DevViewActivity.this, "开始发送命令！", Toast.LENGTH_SHORT)
					.show();
			new SetLampValThread(2, arg0.getProgress()).start();
		}
	}

	class LampBOnSeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			// System.out.println("LampB:"+arg0.getProgress()+"  arg1:"+arg1+
			// "  arg2:"+arg2);

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampBStart:"+arg0.getProgress());

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			// System.out.println("LampBStop:"+arg0.getProgress());
			// devViewLblLampBVal.setText( arg0.getProgress()+"");

			uiEnable(false);
			Toast.makeText(DevViewActivity.this, "开始发送命令！", Toast.LENGTH_SHORT)
					.show();
			new SetLampValThread(3, arg0.getProgress()).start();
		}
	}

	private void uiEnable(boolean b) {
		// if(b){
		// devViewProgBarCmdInfo.setVisibility(View.INVISIBLE);
		// devViewLampChagne();
		// }else{
		// devViewProgBarCmdInfo.setVisibility(View.VISIBLE);
		// }
		// devViewSeekLampR.setEnabled(b);
		// devViewSeekLampG.setEnabled(b);
		// devViewSeekLampB.setEnabled(b);
		// btnOpen.setEnabled(b);
	}

	class BtnOpenOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			if (dev == null) {
				return;
			}
			if (!btnIsOpen) {
				Toast.makeText(DevViewActivity.this, "发送开灯指令",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DevViewActivity.this, "发送关灯指令",
						Toast.LENGTH_SHORT).show();
			}

			uiEnable(false);
			new StartBtnOpenThread(btnIsOpen).start();

		}

	}

	class StartBtnOpenThread extends Thread {
		boolean open = false;

		public StartBtnOpenThread(boolean open) {
			this.open = open;
		}

		public void run() {
			dev.isDataChange(false);
			for (int i = 0; i < 3; i++) {
				if (open) {
					new OpenThread().start();
				} else {
					new OffThread().start();
				}
				try {
					sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((dev.isDataChange()) && (dev.isLightState() == open)) {

					Message message = new Message();
					message.what = CMD_SUCCEEDT;
					handler.sendMessage(message);
					return;
				}
			}
			try {
				sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// if(dev.isLightState() == open){

			Message message = new Message();
			message.what = CMD_TIMEOUT;
			handler.sendMessage(message);

			// }

		}

	}
}
