package com.demo.smarthome.activity;

import com.demo.smarthome.R;
import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.control.initPhoneConfig;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigService;
import com.demo.smarthome.service.HttpConnectService;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.NetworkStatusTools;
import com.demo.smarthome.tools.StrTools;
import com.demo.smarthome.tools.MD5Tools;
import com.demo.smarthome.updata.UpdataInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.app.Activity;
import android.content.Intent;
import android.util.Xml;
import android.view.Window;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * 
 * @author sl
 * 
 */
public class WelcomeActivity extends Activity {

	static final String TAG = "WelcomeActivity";
	private boolean isAutoLogin;
	static final int LOGIN_SUCCEED = 0;
	static final int LOGIN_ERROR = 1;

	static final int VERSION_HIGHEST = 2;
	static final int VERSION_UPDATA  = 3;

	static final int LONGIN_WAIT_TIME = 3000;

	static final int UPDATA_SUCCEED  = 4;
	static final int UPDATA_ERROR    = 5;

	static final int AUTO_LOGIN_NO_DEVID    = 6;

	static final int DIALOG_SHOW    = 8;
	static final int CONNECT_SERVER_FAIL    = 9;
	static final int FINISH		    = 10;

	UpdataInfo info = null;
	ConfigService dbService;
	AlertDialog.Builder failAlert;
	ServerReturnResult loginResult = new ServerReturnResult();
	long startTimestamp;


	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			Intent intent = new Intent();
			switch (msg.what){
				case LOGIN_SUCCEED:
					String tempDeviceType = dbService.getCfgByKey(Cfg.currentDeviceID);
					if(!tempDeviceType.isEmpty()){
						Cfg.currentDeviceType = tempDeviceType;
						if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_02L))
						{
							intent.setClass(WelcomeActivity.this, BGPM02LRealtimeDataActivity.class);
							startActivity(intent);
							finish();
						}else if(Cfg.currentDeviceType.equals(DeviceInformation.DEV_TYPE_BGPM_10))
						{
							intent.setClass(WelcomeActivity.this, BGPM10RealtimeDataActivity.class);
							startActivity(intent);
							finish();
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "请先绑定设备", Toast.LENGTH_SHORT).show();
						intent.setClass(WelcomeActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}

				break;
				case LOGIN_ERROR:
					post(r);
					break;
				case VERSION_HIGHEST:
					dbService = new ConfigDao(WelcomeActivity.this.getBaseContext());
					isAutoLogin = dbService.getCfgByKey(Cfg.KEY_AUTO_LOGIN).equals("true")? true : false;

					if (isAutoLogin) {
						new AutoLoginThread().start();
					} else {

						long wait_time = System.currentTimeMillis() - startTimestamp;
						wait_time = (wait_time > LONGIN_WAIT_TIME)?0:LONGIN_WAIT_TIME - wait_time;
						handler.postDelayed(r, wait_time);
					}
					break;
				case VERSION_UPDATA:
					updataVersionMothod();
					break;
				case UPDATA_ERROR:
					Toast.makeText(getApplicationContext(), "升级错误", Toast.LENGTH_SHORT).show();
					handler.postDelayed(r, 1000);
					break;
				case AUTO_LOGIN_NO_DEVID:
					Bundle bundle = new Bundle();
					bundle.putString("activity", "login");
					intent.putExtras(bundle);
					intent.setClass(WelcomeActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case DIALOG_SHOW:
					ProgressDialog.show(WelcomeActivity.this
							,"正在更新版本","...请稍等",false,true);
					break;
				case CONNECT_SERVER_FAIL:
					failAlert = new AlertDialog.Builder(WelcomeActivity.this);
					failAlert.setTitle("连接服务器错误").setIcon(R.drawable.cloud_fail).setMessage(StringRes.canNotConnetServer)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
					failAlert.create().show();

					break;
				case FINISH:
					finish();
					break;
				default:
					handler.postDelayed(r, 0);
					break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);
		ActivityControl.getInstance().addActivity(this);

		if(!NetworkStatusTools.isNetworkAvailable(WelcomeActivity.this)){
			failAlert = new AlertDialog.Builder(WelcomeActivity.this);
			failAlert.setTitle("无网络").setIcon(R.drawable.cloud_fail).setMessage("请连接网络")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityControl.getInstance().finishAllActivity();
						}
					});
			failAlert.create().show();
			return;
		}

		startTimestamp = System.currentTimeMillis();
		accessViewInit();
        //崩溃日志收集
        CrashReport.initCrashReport(getApplicationContext(), "d6a693f16b", false);
        new CheckVersionThread().start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	// 结束Activity&从栈中移除该Activity
		ActivityControl.getInstance().removeActivity(this);
	}
    //屏幕信息初始化
	void accessViewInit(){

		initPhoneConfig.initPhoneScreen(this);
		Cfg.isNavigationBar = initPhoneConfig.IsNavigationBar(this);
	}

	Runnable r = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, LoginActivity.class);

			startActivity(intent);
			finish();
		}
	};


	class AutoLoginThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = LOGIN_ERROR;

			String tempName = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
			String tempPwd = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);
			if(tempName != null && tempPwd != null){
				Cfg.userName = dbService.getCfgByKey(Cfg.KEY_USER_NAME);
				Cfg.userPassword = dbService.getCfgByKey(Cfg.KEY_PASS_WORD);
			}

			if(Cfg.userName.isEmpty() || Cfg.userPassword.isEmpty()) {
				handler.sendMessage(message);
				return;
			}
			Cfg.currentDeviceID = dbService.getCfgByKey(Cfg.KEY_DEVICE_ID);

			loginResult = LoginServer.LoginServerMethod();
			switch (Integer.parseInt(loginResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					if(Cfg.currentDeviceID.isEmpty()) {
						message.what = AUTO_LOGIN_NO_DEVID;
						dbService.SaveSysCfgByKey(Cfg.KEY_DEVICE_ID,"");
					}else{
						message.what = LOGIN_SUCCEED;
						for(int i = 0;i<Cfg.devInfo.length;i++)
						{
							if(dbService.getCfgByKey(Cfg.devInfo[i]).isEmpty()) {
								if(!LoginServer.getDeviceType(Cfg.devInfo[i])){
									//handler.sendMessage(message);
									return;
								}
								else{
									dbService.SaveSysCfgByKey(Cfg.devInfo[i],Cfg.deviceType);
								}
							}
						}

					}
					break;
				default:
					message.what = LOGIN_ERROR;
					break;
			}
			//至少等待3秒
			while(System.currentTimeMillis() - startTimestamp < LONGIN_WAIT_TIME){

			}
			handler.sendMessage(message);
		}
	}


	class CheckVersionThread extends Thread {
		@Override
		public void run() {
				Message msg = new Message();
				msg.what = VERSION_HIGHEST;

				try {

				getVersionName();

				String path = "http://" + StringRes.updateXmlUrl;


				URL url = new URL(path);
				HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15000);
				conn.setReadTimeout(15000);
				InputStream is =conn.getInputStream();
				info =  getUpdataInfo(is);
				if(!info.getVersion().equals(Cfg.versionNumber)) {
					msg.what = VERSION_UPDATA;
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.what = CONNECT_SERVER_FAIL;
			}
			handler.sendMessage(msg);
		}
	}

	/*
     *
     */
	private String getVersionName()  throws Exception{

		PackageManager packageManager = getPackageManager();

		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		Cfg.versionNumber = packInfo.versionName;
		return packInfo.versionName;
	}


	public static UpdataInfo getUpdataInfo(InputStream is) throws Exception{
		XmlPullParser  parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int type = parser.getEventType();
		UpdataInfo info = new UpdataInfo();
		while(type != XmlPullParser.END_DOCUMENT ){
			switch (type) {
				case XmlPullParser.START_TAG:
					if("version".equals(parser.getName())){
						info.setVersion(parser.nextText());
					}else if ("url".equals(parser.getName())){
						info.setUrl(parser.nextText());
					}else if ("description".equals(parser.getName())){
						info.setDescription(parser.nextText());
					}
					break;
			}
			type = parser.next();
		}
		return info;
	}
	protected void updataVersionMothod(){
		AlertDialog.Builder builer = new AlertDialog.Builder(this) ;
		builer.setTitle("发现新版本");
		builer.setMessage(info.getDescription());

		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "update");
				downLoadApk();
			}
		});

		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				msg.what = FINISH;
				handler.sendMessage(msg);
			}
		});
		AlertDialog dialog = builer.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	protected void downLoadApk() {

		final ProgressDialog pd;
		pd = new  ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载");
		pd.show();
		new Thread(){
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = UPDATA_ERROR;
				try {
					String ApkUrl = "http://" + StringRes.serverIP +":"+ StringRes.serverPort
							+info.getUrl();
					File file = getFileFromServer(ApkUrl, pd);
					pd.dismiss();

					Message message = new Message();
					message.what = DIALOG_SHOW;
					handler.sendMessage(message);

					sleep(3000);
					installApk(file);

				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(msg);
				}

			}}.start();
	}


	protected void installApk(File file) {

		Intent intent = new Intent();

		intent.setAction(Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivity(intent);
	}

	public static File getFileFromServer(String path, ProgressDialog pd) throws Exception{

		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			URL url = new URL(path);
			HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);

			pd.setMax(conn.getContentLength());
			InputStream is = conn.getInputStream();
			File file = new File(Environment.getExternalStorageDirectory(), "Begood.apk");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len ;
			int total=0;
			while((len =bis.read(buffer))!=-1){
				fos.write(buffer, 0, len);
				total+= len;

				pd.setProgress(total);
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		}
		else{
			return null;
		}
	}
}
