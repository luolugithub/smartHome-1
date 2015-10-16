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
		import android.widget.EditText;
		import android.widget.ImageView;
		import android.widget.ProgressBar;
		import android.widget.SeekBar;
		import android.widget.SeekBar.OnSeekBarChangeListener;
		import android.widget.TextView;
		import android.widget.Toast;
		import java.io.BufferedReader;
		import java.io.InputStreamReader;
		import java.io.InputStream;
		import java.net.SocketTimeoutException;
		import java.net.URL;
		import java.net.HttpURLConnection;
		import java.util.concurrent.TimeoutException;

/**
 * 用于甲醛设备使用app
 *
 * @author sl
 *
 * create at 2015/09/23
 *
 */
public class HCHOActivity extends Activity {

	infoReslut Info = new infoReslut();

	static final int GET_WEATHER_SUCCEED = 0;
	static final int GET_WEATHER_FAIL 	 = 1;
	static final int CITY_NAME_FAIL 	 = 2;
	protected class infoReslut{
		public  String errNum = "0";
		public  String errMsg = "fail";
		public  String city = "";//城市名中文
		public  String date = "";//当前日期
		public  String time = "";//温度发布时的时间
		public  String weather = "";//天气情况
		public  String temp = "";  //当前温度
		public  String l_tmp = "";//今日最低气温
		public  String h_tmp = "";//今日最高气温
		public  String WD = "";//风向
		public  String WS = "";//风力
		//protected static String sunrise = "";//日出时间
		//protected static String sunset = "";//日落时间
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			TextView infoRsult = (TextView) findViewById(R.id.enviInfoTextView);
			switch (msg.what) {
				case GET_WEATHER_SUCCEED:
					infoRsult.setText("今天"+Info.city+"天气" + Info.weather+"\n截止到"
							+ Info.time + " 温度"+Info.temp + "度，最高气温："
							+ Info.h_tmp + "，最低气温：" + Info.l_tmp);
					infoRsult.setTextSize(30);
					break;
				case GET_WEATHER_FAIL:
					infoRsult.setText("未获取到天气信息,请检查网络");
					infoRsult.setTextSize(30);
					break;
				case CITY_NAME_FAIL:
					infoRsult.setText("请输入正确的城市名称");
					infoRsult.setTextSize(30);
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 函数将申请到的字符串数据转换到infoReslut类中
	 */
	protected boolean toInfo(String jsonResult){
		final String tempResult = jsonResult.replaceAll("\"","");//去掉所有双引号
		String temp[];
		temp = tempResult.split("errNum:");
		Info.errNum = temp[1].substring(0, 1);
		temp = tempResult.split("errMsg:");
		Info.errMsg = temp[1].substring(0, temp[1].indexOf(","));
		Info.errMsg  = decodeUnicode(Info.errMsg);
		if(Info.errMsg.equals("success")) {
			temp = tempResult.split("city:");
			Info.city = temp[1].substring(0, temp[1].indexOf(","));
			Info.city  = decodeUnicode(Info.city);//需要从unicode转成ut8
			temp = tempResult.split("date:");
			Info.date = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("time:");
			Info.time = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("weather:");
			Info.weather = temp[1].substring(0, temp[1].indexOf(","));
			Info.weather  = decodeUnicode(Info.weather);
			temp = tempResult.split("temp:");
			Info.temp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("l_tmp:");
			Info.l_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("h_tmp:");
			Info.h_tmp = temp[1].substring(0, temp[1].indexOf(","));
			temp = tempResult.split("WD:");
			Info.WD = temp[1].substring(0, temp[1].indexOf(","));
			Info.WD  = decodeUnicode(Info.WD);//需要从unicode转成ut8
			temp = tempResult.split("WS:");
			Info.WS = temp[1].substring(0, temp[1].indexOf(","));
			Info.WS  = decodeUnicode(Info.WS);//需要从unicode转成ut8
			return true;
		}
		return false;
	}

	class getEnvironmentFromInternetThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			EditText cityInfo = (EditText) findViewById(R.id.cityName);

			String httpArg = new String("cityname="+cityInfo.getText());

			if(toInfo(requestAPI(Cfg.WEATHER_INFORMATION, httpArg))) {

				message.what = GET_WEATHER_SUCCEED;

			}
			else {
				if(Info.errMsg.equals("请输入正确的城市id/城市名称/城市拼音")){
					message.what = CITY_NAME_FAIL;
				}
				else {
					message.what = GET_WEATHER_FAIL;
				}
			}
			handler.sendMessage(message);
		}
	}
	/**
	 * 函数将从百度申请天气信息
	 *
	 * @param urlAll
	 *            :请求接口
	 * @param httpArg
	 *            :参数
	 * @return 返回结果
	 */
	private  String requestAPI(String httpUrl, String httpArg) {
		String result = null;
		StringBuffer sbf = new StringBuffer();
		httpUrl = httpUrl + "?" + httpArg;

		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(10000);
			// 填入apikey到HTTP header,其中apikey是绑定百度账号的
			connection.setRequestProperty("apikey",Cfg.APIKEY);
			connection.connect();
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}

			reader.close();
			result = sbf.toString();
		}
		catch (SocketTimeoutException e) {
			Info.errMsg = "fail";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
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

		Button getEnviroment = (Button) findViewById(R.id.getEnvironment);
		getEnviroment.setOnClickListener(new getEnviromentListener());
	}

	class getEnviromentListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			TextView environmentInfo = (TextView)findViewById(R.id.enviInfoTextView);
			environmentInfo.setText("请稍等，信息正在获取中");
			new getEnvironmentFromInternetThread().start();
		}
	}
	/**
	 * 函数用于将unicode转换成utf-8
	 * @param String theString
	 *            :unicode
	 * @return String
	 *			:UTF-8
	 *
	 */
	private static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								value = (value << 4) + aChar - '0';
								break;
							case 'a':
							case 'b':
							case 'c':
							case 'd':
							case 'e':
							case 'f':
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A':
							case 'B':
							case 'C':
							case 'D':
							case 'E':
							case 'F':
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default:
								throw new IllegalArgumentException(
										"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
}