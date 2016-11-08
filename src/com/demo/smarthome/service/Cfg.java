package com.demo.smarthome.service;

import java.util.ArrayList;
import java.util.List;

import com.demo.smarthome.device.Dev;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Ӧ�ó���ȫ�ֱ��� ��
 * @author Administrator
 *
 */
public class Cfg extends Application  {

	public final static String VERSION ="10001";
	

	public static String savePath="//sdcard//myImage/";
	public static boolean debug = true;

	//用户界面宽和高度
	public static int widthPixels = 1080;
	public static int heightPixels = 1080;
	//是否存在虚拟按键
	public static boolean isNavigationBar = false;
	public final static String SendBoardCastName ="com.demo.smarthome.service.socketconnect";
	
	
	public final static String KEY_PASS_WORD ="password";
	public final static String KEY_USER_NAME ="username";
	public final static String KEY_AUTO_LOGIN ="autoLogin";

	public final static String KEY_DEVICE_ID  ="deviceId";

	//  ������ַ  cloud.ai-thinker.com
	//	cloud.ai-thinker.com
	//	admin
	//	admin_!@#*()
	//  ���Ե�ַ tangdengan.xicp.net
//	public final static String WEBSERVICE_SERVER_URL="http://182.139.160.79:8020/service/s.asmx";
	public final static String WEBSERVICE_SERVER_URL="http://cloud.ai-thinker.com/service/s.asmx";

	public final static String TCP_SERVER_URL="cloud.ai-thinker.com";
//	public final static String WEBSERVICE_SERVER_URL="http://tangdengan.xicp.net:8020/service/s.asmx";
//	public final static String TCP_SERVER_URL="tangdengan.xicp.net";
	//��ѯ������API�ĵ�ַ
	public final static String WEATHER_INFORMATION="http://apis.baidu.com/apistore/weatherservice/cityname";
//	public final static String TCP_SERVER_URL="182.139.160.79";
	public final static int  TCP_SERVER_PORT=6009;

	//���ڷ��������ش���
	public final static int REG_SUCCESS 			= 0;
	public final static int REG_ERROR   			= 1;
	public final static int SERVER_CANT_CONNECT    	= 6;
//	public final static int REG_PWD_ERROR   		= 7;
//	public final static int REG_USER_EXISTED    	= 8;
//	public final static int REG_EXCEPTION   		= 9;
	public final static int USERNAME_EXCEPTION   	= 10;

	public final static int CODE_SUCCESS 		= 000;
	public final static int CODE_NULL_CODE 		= 222;
	public final static int CODE_PWD_ERROR   	= 777;
	public final static int CODE_USER_EXISTED   = 888;
	public final static int CODE_EXCEPTION   	= 999;

	public static String[] devInfo;
	//
	//
	public static String currentDeviceID = "";
	public static String currentDeviceType = "";
	public static String deviceType	= "";
	public static String versionNumber = "";
	//The time(millisecond) of auto freshing of real-time data of device
	public static int autoFreshTime = 5000;
	public static int outlineTime = 20000;

	//send verification code interval
	public static long sendVerficationCodeInterval = 60*1000;

//	public final static String DEV_UDP_IPADDR="192.168.5.88"; //192.168.1.255
	public final static int  DEV_UDP_SEND_PORT=2468;
	public final static int  DEV_UDP_SEND_DELAY=100;
	public final static int  DEV_UDP_READ_DELAY=15;

	public final static int  WAIT_WIFI_LANSTART_TIME=10;

	public final static String DEV_UDP_IPADDR="192.168.4.1";
	public final static int  DEV_UDP_PORT=8001;

	public static byte[] userId= new byte[0];
	public static String userName="";
	public static String userPassword="";
	public static long id=0;
	public static byte[] passWd= new byte[0];
	public static String torken = "";
	public static byte[] tcpTorken = new byte[0];

	public static String historyType = "";

	public static boolean isLogin=false;
	public static boolean isSubmitDev=false;
	public static boolean isDeleteDev=false;


	public static boolean register=false;
	public  static String regUserName ="";
	public  static String regUserPass ="";
	
	public static List<Dev> listDev=new  ArrayList<Dev>();
	private static List<Dev> listDevScan=new  ArrayList<Dev>();//ɨ�����?

	
//	public static final int timeDelayDef = 50; // ������ʱʱ�� ms
//	public static final int timeOutDef = 30 * (1000 / timeDelayDef); // ���ʱʱ��
//																		// 20��
//	public static final int timeReSendTimeDef = 5 * (1000 / timeDelayDef); // �����ط���ʱʱ��

	public static Dev getDevById(String id) {
		Dev dev = null;
		for (Dev d : listDev) {
			if (d.getId().equals(id)) {
				dev = d;
				break;
			}
		}
		return dev;
	}

	
	public static Dev getDevScan() {
		Dev dev = null;
		for (Dev d : listDevScan) {
			dev = d;
			listDevScan.remove(d);
			break;
			
		}
		return dev;
	}
	public static void putDevScan(Dev dev) {
		if(dev == null){
			return;
		}
		for (Dev d : listDev) {
			if (d.getId().equals(dev.getId())) {
				return;
			}
		}
		for (Dev d : listDevScan) {
			if (d.getId().equals(dev.getId())) {
				return;
			}
		}
		listDevScan.add(dev);
		return ;
	}

	public static void devScanClean() {
		listDevScan.clear();
		return ;
	}
}
