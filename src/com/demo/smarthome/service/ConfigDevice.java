package com.demo.smarthome.service;

import android.content.Context;

import android.util.Log;

import com.demo.smarthome.tools.IpTools;
import com.demo.smarthome.tools.StrTools;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by leishi on 15/11/11.
 */
public class ConfigDevice {

//    static final String TAG = "ConfigDevice";
    static final String TAG = "MainActivity";
    static final int WAIT_RESULT  = 0;
    static final int FIND_DEVID = 2;
    static final int CMD_TIMEOUT = 6;

    Context context;
    private String wifiPwd;
    private boolean switchIsHidden;
    int result = WAIT_RESULT;

    boolean findDev = false;
    String myip;

    static String deviceId = "";
    static String devicePwd = "";

    public ConfigDevice(Context context) {
        this.context = context;
    }

    public ConfigDevice(String wifiPassword,boolean IsHidden,String myIP,Context context) {
        wifiPwd = wifiPassword;
        switchIsHidden = IsHidden;
        myip = myIP;
        this.context = context;
    }

    public void configDeviceThread(){
        new ConnectDevThread().start();
    }

    //先配置设备连接WI-FI,再扫描本地设备获取本地设备ID.
    class ConnectDevThread extends Thread {
        @Override
        public void run() {
            IEsptouchTask mConfigDevTask;
            //获取路由器SSID
            EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(context);
            //ssid是网络的ID,bssid是接入ap的mac
            String apSsid = mWifiAdmin.getWifiConnectedSsid();
            String apBssid = mWifiAdmin.getWifiConnectedBssid();

            //配置设备上网
            mConfigDevTask = new EsptouchTask(apSsid, apBssid, wifiPwd, switchIsHidden, context);
            IEsptouchResult result = mConfigDevTask.executeForResult();
            if (result.isSuc()) {
                //配置WI-FI后等待设备设置成lanstart模式
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            findDevice();
        }
    }

    public String getApSSid(){
        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(context);
        //ssid是网络的ID,bssid是接入ap的mac
        return mWifiAdmin.getWifiConnectedSsid();
    }

    public int getConfigResult(){
        return result;
    }

    public String getDeviceID(){
        if(findDev) {
            return deviceId;
        }else{
            return null;
        }
    }
    public String getDevicePwd(){
        if(findDev) {
            return devicePwd;
        }else{
            return null;
        }
    }

    //扫描本地设备
    private void findDevice(){
        findDev = false;
        Cfg.devScanClean();
        String ip = myip;
        if (ip.length() < 4) {
            ip = "192.168.1.255";
        }

        for (int i = 1; i < 255; i++) {

            if (findDev) {
                return;
            }
            new UDPThread(ip, i).start();
            try {
                Thread.sleep(Cfg.DEV_UDP_SEND_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (findDev) {
            return;
        }
        result = CMD_TIMEOUT;
    }

    class UDPThread extends Thread {
        String Hostip = "";
        String ip = "";
        int port = Cfg.DEV_UDP_SEND_PORT;

        public UDPThread(String ipStr, int i) {

            this.Hostip = ipStr;
            byte[] addr = IpTools.getIpV4Byte(ipStr);
            if (addr.length == 4) {
                addr[3] = (byte) (i);
                ip = IpTools.getIpV4StringByByte(addr, 0);
            }
        }

        public void run() {

            DatagramSocket dSocket = null;
            String msg ;
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);

            InetAddress local = null;
            try {
                local = InetAddress.getByName(ip); // 本机测试
                // local = InetAddress.getLocalHost(); // 本机测试
                System.out.println("local:" + local);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                dSocket = new DatagramSocket(); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
            } catch (SocketException e) {
                e.printStackTrace();
                return;
            }

            String localPort = dSocket.getLocalPort() + "";

            System.out.println("Hostip:" + Hostip + "  ip:" + ip
                    + "   localPort:" + localPort);

            msg = "RPL:\"" + Hostip + "\",\"" + localPort + "\"";

            int msg_len = msg == null ? 0 : msg.getBytes().length;
            DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
                    msg_len, local, port);

            try {

                // 发送设置为广播
                dSocket.setBroadcast(true);
                dSocket.setSoTimeout(10000);
                dSocket.send(dPacket);
                dSocket.receive(dp);
                String strInfo = new String(dp.getData(), 0, dp.getLength());
                System.out.println(strInfo);
                String str = strInfo;
                String[] tmp = str.split(":");
                for (String s : tmp) {
                    Log.i(TAG, "item1:" + s);
                }
                if (tmp.length >= 2) {
                    str = tmp[1];
                    tmp = str.split(",");
                    for (String s : tmp) {
                        Log.i(TAG, "item2:" + s);
                    }
                    if (tmp.length >= 2) {
                        Log.i(TAG, "tmp[0]:" + tmp[0]);
                        Log.i(TAG, "tmp[1]:" + tmp[1]);
                        String idStr = tmp[0].replace('"', ' ');
                        String passStr = tmp[1].replace('"', ' ');
                        Log.i(TAG, "idStr:" + idStr);
                        Log.i(TAG, "pasStrs:" + passStr);
                        StrTools.StrHexLowToLong(idStr);
                        StrTools.StrHexLowToLong(passStr);
                        StrTools.StrHexHighToLong(idStr);
                        StrTools.StrHexHighToLong(passStr);
                        // int id = Int
                        if (findDev) {
                            return;
                        }
                        deviceId = StrTools.StrHexLowToLong(idStr) + "";
                        devicePwd = StrTools.StrHexHighToLong(passStr) + "";
                        findDev = true;
                        result = FIND_DEVID;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            dSocket.close();
        }
    }
}
