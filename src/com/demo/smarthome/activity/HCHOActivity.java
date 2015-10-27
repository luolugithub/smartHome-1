package com.demo.smarthome.activity;

import com.demo.smarthome.R;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.server.setServerURL;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.demo.smarthome.server.ServerReturnResult;
/**
 * ���ڼ�ȩ�豸ʹ��app
 *
 * @author sl
 *
 * create at 2015/09/23
 *
 */
public class HCHOActivity extends Activity {


	String userName;
	String userPassword;
	String deviceId;
	String devicePassword;
	EditText txtName = null;
	EditText txtPassword = null;
	EditText txtDevId = null;
	EditText txtDevPwd = null;
	Button submit;
	String jsonResult;
	ServerReturnResult getResult = new ServerReturnResult();


	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what)
			{
				case Cfg.REG_SUCCESS:
					Toast.makeText(HCHOActivity.this,"��¼�ɹ�",Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_hcho_dev_view);
		TextView title = (TextView) findViewById(R.id.titleHCHOView);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		txtName = (EditText) findViewById(R.id.username);
		txtPassword = (EditText) findViewById(R.id.userpassword);
		txtDevId = (EditText) findViewById(R.id.deviceid);
		txtDevPwd = (EditText) findViewById(R.id.devicepassword);

		submit = (Button) findViewById(R.id.registersubmit);
		submit.setOnClickListener(new registerUser());
	}



	class registerUser implements OnClickListener {
		@Override
		public void onClick(View v) {
			new registerUserThread().start();
		}
	}

	class registerUserThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			message.what = Cfg.REG_ERROR;
			Gson gson = new Gson();

			userName = txtName.getText().toString();
			userPassword = txtPassword.getText().toString();
			deviceId = txtDevId.getText().toString();
			devicePassword = txtDevPwd.getText().toString();

			String[] paramsName = {"userName","userPassword","deviceId", "devicePassword"};
			String[] paramsValue = {userName,userPassword,deviceId,devicePassword};

			setServerURL regiterUser= new setServerURL();

			jsonResult = regiterUser.sendParamToServer("register", paramsName, paramsValue);
			try {
				getResult = gson.fromJson(jsonResult
						, com.demo.smarthome.server.ServerReturnResult.class);
			}
			catch (JsonSyntaxException e){
				e.printStackTrace();
			}

			if(Cfg.debug)
			{
				Log.d("test",getResult.getMsg());
				Log.d("test",String.valueOf(getResult.getCode()));
			}

			switch (Integer.parseInt(getResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = Cfg.REG_SUCCESS;
					break;
				case Cfg.CODE_PWD_ERROR:
					message.what = Cfg.REG_PWD_ERROR;
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = Cfg.REG_USER_EXISTED;
					break;
				//�����������쳣
				case Cfg.CODE_EXCEPTION:
					message.what = Cfg.REG_EXCEPTION;
					break;
				default:
					message.what = Cfg.REG_ERROR;
					break;
			}
			handler.sendMessage(message);
		}

	}

}

	//��ȡ������Ϣ,�ѷ���
//	infoReslut Info = new infoReslut();
//
//	static final int GET_WEATHER_SUCCEED = 0;
//	static final int GET_WEATHER_FAIL 	 = 1;
//	static final int CITY_NAME_FAIL 	 = 2;
//	protected class infoReslut{
//		public  String errNum = "0";
//		public  String errMsg = "fail";
//		public  String city = "";//����������
//		public  String date = "";//��ǰ����
//		public  String time = "";//�¶ȷ���ʱ��ʱ��
//		public  String weather = "";//�������?
//		public  String temp = "";  //��ǰ�¶�
//		public  String l_tmp = "";//�����������?
//		public  String h_tmp = "";//�����������?
//		public  String WD = "";//����
//		public  String WS = "";//����
//		//protected static String sunrise = "";//�ճ�ʱ��
//		//protected static String sunset = "";//����ʱ��
//	}
//
//	Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			TextView infoRsult = (TextView) findViewById(R.id.enviInfoTextView);
//			switch (msg.what) {
//				case GET_WEATHER_SUCCEED:
//					infoRsult.setText("����"+Info.city+"����" + Info.weather+"\n��ֹ��"
//							+ Info.time + " �¶�"+Info.temp + "�ȣ�������£�?"
//							+ Info.h_tmp + "��������£�?" + Info.l_tmp);
//					infoRsult.setTextSize(30);
//					break;
//				case GET_WEATHER_FAIL:
//					infoRsult.setText("δ��ȡ��������Ϣ,��������");
//					infoRsult.setTextSize(30);
//					break;
//				case CITY_NAME_FAIL:
//					infoRsult.setText("��������ȷ�ĳ�������");
//					infoRsult.setTextSize(30);
//					break;
//				default:
//					break;
//			}
//		}
//	};
//
//	/**
//	 * ���������뵽���ַ�������ת����infoReslut����
//	 */
//	protected boolean toInfo(String jsonResult){
//		final String tempResult = jsonResult.replaceAll("\"","");//ȥ������˫����
//		String temp[];
//		temp = tempResult.split("errNum:");
//		Info.errNum = temp[1].substring(0, 1);
//		temp = tempResult.split("errMsg:");
//		Info.errMsg = temp[1].substring(0, temp[1].indexOf(","));
//		Info.errMsg  = decodeUnicode(Info.errMsg);
//		if(Info.errMsg.equals("success")) {
//			temp = tempResult.split("city:");
//			Info.city = temp[1].substring(0, temp[1].indexOf(","));
//			Info.city  = decodeUnicode(Info.city);//��Ҫ��unicodeת��ut8
//			temp = tempResult.split("date:");
//			Info.date = temp[1].substring(0, temp[1].indexOf(","));
//			temp = tempResult.split("time:");
//			Info.time = temp[1].substring(0, temp[1].indexOf(","));
//			temp = tempResult.split("weather:");
//			Info.weather = temp[1].substring(0, temp[1].indexOf(","));
//			Info.weather  = decodeUnicode(Info.weather);
//			temp = tempResult.split("temp:");
//			Info.temp = temp[1].substring(0, temp[1].indexOf(","));
//			temp = tempResult.split("l_tmp:");
//			Info.l_tmp = temp[1].substring(0, temp[1].indexOf(","));
//			temp = tempResult.split("h_tmp:");
//			Info.h_tmp = temp[1].substring(0, temp[1].indexOf(","));
//			temp = tempResult.split("WD:");
//			Info.WD = temp[1].substring(0, temp[1].indexOf(","));
//			Info.WD  = decodeUnicode(Info.WD);//��Ҫ��unicodeת��ut8
//			temp = tempResult.split("WS:");
//			Info.WS = temp[1].substring(0, temp[1].indexOf(","));
//			Info.WS  = decodeUnicode(Info.WS);//��Ҫ��unicodeת��ut8
//			return true;
//		}
//		return false;
//	}
//
//	class getEnvironmentFromInternetThread extends Thread {
//		@Override
//		public void run() {
//			Message message = new Message();
//			EditText cityInfo = (EditText) findViewById(R.id.cityName);
//
//			String httpArg = new String("cityname="+cityInfo.getText());
//
//			if(toInfo(requestAPI(Cfg.WEATHER_INFORMATION, httpArg))) {
//
//				message.what = GET_WEATHER_SUCCEED;
//
//			}
//			else {
//				if(Info.errMsg.equals("��������ȷ�ĳ���id/��������/����ƴ��")){
//					message.what = CITY_NAME_FAIL;
//				}
//				else {
//					message.what = GET_WEATHER_FAIL;
//				}
//			}
//			handler.sendMessage(message);
//		}
//	}
//	/**
//	 * �������Ӱٶ�����������Ϣ
//	 *
//	 * @param urlAll
//	 *            :����ӿ�?
//	 * @param httpArg
//	 *            :����
//	 * @return ���ؽ��?
//	 */
//	private  String requestAPI(String httpUrl, String httpArg) {
//		String result = null;
//		StringBuffer sbf = new StringBuffer();
//		httpUrl = httpUrl + "?" + httpArg;
//
//		try {
//			URL url = new URL(httpUrl);
//			HttpURLConnection connection = (HttpURLConnection) url
//					.openConnection();
//			connection.setRequestMethod("GET");
//			connection.setConnectTimeout(10000);
//			// ����apikey��HTTP header,����apikey�ǰ󶨰ٶ��˺ŵ�
//			connection.setRequestProperty("apikey",Cfg.APIKEY);
//			connection.connect();
//			InputStream is = connection.getInputStream();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//			String strRead = null;
//			while ((strRead = reader.readLine()) != null) {
//				sbf.append(strRead);
//				sbf.append("\r\n");
//			}
//
//			reader.close();
//			result = sbf.toString();
//		}
//		catch (SocketTimeoutException e) {
//			Info.errMsg = "fail";
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
//		setContentView(R.layout.activity_hcho_dev_view);
//		TextView title = (TextView) findViewById(R.id.titleHCHOView);
//		title.setClickable(true);
//		title.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				finish();
//			}
//
//		});
//
//		Button getEnviroment = (Button) findViewById(R.id.getEnvironment);
//		getEnviroment.setOnClickListener(new getEnviromentListener());
//	}
//
//	class getEnviromentListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			TextView environmentInfo = (TextView)findViewById(R.id.enviInfoTextView);
//			environmentInfo.setText("���Եȣ���Ϣ���ڻ�ȡ��");
//			new getEnvironmentFromInternetThread().start();
//		}
//	}
//	/**
//	 * �������ڽ�unicodeת����utf-8
//	 * @param String theString
//	 *            :unicode
//	 * @return String
//	 *			:UTF-8
//	 *
//	 */
//	private static String decodeUnicode(String theString) {
//		char aChar;
//		int len = theString.length();
//		StringBuffer outBuffer = new StringBuffer(len);
//		for (int x = 0; x < len;) {
//			aChar = theString.charAt(x++);
//			if (aChar == '\\') {
//				aChar = theString.charAt(x++);
//				if (aChar == 'u') {
//					// Read the xxxx
//					int value = 0;
//					for (int i = 0; i < 4; i++) {
//						aChar = theString.charAt(x++);
//						switch (aChar) {
//							case '0':
//							case '1':
//							case '2':
//							case '3':
//							case '4':
//							case '5':
//							case '6':
//							case '7':
//							case '8':
//							case '9':
//								value = (value << 4) + aChar - '0';
//								break;
//							case 'a':
//							case 'b':
//							case 'c':
//							case 'd':
//							case 'e':
//							case 'f':
//								value = (value << 4) + 10 + aChar - 'a';
//								break;
//							case 'A':
//							case 'B':
//							case 'C':
//							case 'D':
//							case 'E':
//							case 'F':
//								value = (value << 4) + 10 + aChar - 'A';
//								break;
//							default:
//								throw new IllegalArgumentException(
//										"Malformed   \\uxxxx   encoding.");
//						}
//
//					}
//					outBuffer.append((char) value);
//				} else {
//					if (aChar == 't')
//						aChar = '\t';
//					else if (aChar == 'r')
//						aChar = '\r';
//					else if (aChar == 'n')
//						aChar = '\n';
//					else if (aChar == 'f')
//						aChar = '\f';
//					outBuffer.append(aChar);
//				}
//			} else
//				outBuffer.append(aChar);
//		}
//		return outBuffer.toString();
//	}
//}