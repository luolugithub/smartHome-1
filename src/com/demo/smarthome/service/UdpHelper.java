package com.demo.smarthome.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import android.net.wifi.WifiManager;
import android.util.Log;

public class UdpHelper implements Runnable {
	public Boolean IsThreadDisable = false;//
	private static WifiManager.MulticastLock lock;
	InetAddress mInetAddress;

	public UdpHelper(WifiManager manager) {
		this.lock = manager.createMulticastLock("UDPwifi");
	}

	public void StartListen() {
		//
		Integer port = 8903;
		//
		byte[] message = new byte[100];
		try {
			//
			DatagramSocket datagramSocket = new DatagramSocket(port);
			datagramSocket.setBroadcast(true);
			DatagramPacket datagramPacket = new DatagramPacket(message,
					message.length);
			try {
				while (!IsThreadDisable) {

					this.lock.acquire();

					datagramSocket.receive(datagramPacket);
					String strMsg = new String(datagramPacket.getData()).trim();
					Log.d("UDP Demo", datagramPacket.getAddress()
							.getHostAddress().toString()
							+ ":" + strMsg);
					this.lock.release();
				}
			} catch (IOException e) {// IOException
				e.printStackTrace();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	public static void send(String message) {
		message = (message == null ? "Hello IdeasAndroid!" : message);
		int server_port = 8904;
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		InetAddress local = null;
		try {
			local = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int msg_length = message.length();
		byte[] messageByte = message.getBytes();
		DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,
				server_port);
		try {

			s.send(p);
			s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		StartListen();
	}
}
