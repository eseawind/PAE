package com.pae.core;

/**
 * Wifi���ӵĺ����࣬�����������UDP����
 * ͨ��Connector�ӿ�����ʼ��һ��UDP����
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import com.pae.PaeActivity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WifiConnector implements Connector {

	private static final String TAG = "BluetoothCore";// ���debug��Ϣ
	private ConnectThread connectThread = null;
	private ConnectedThread connectedThread = null; // �����Ѿ����ӵ��߳�
	private final Handler handler;
	
	private String serverIp = "" ; 

	public WifiConnector(Context context, Handler handler) {
		this.handler = handler;
	}

	/**
	 * �����������̵߳�UDP socket������Ϣ��ͳһ��sendMessage����
	 */
	public void sendMessage(String message) {
		ConnectedThread t;
		synchronized (this) {
			t = connectedThread;
		}
		t.write(message);
	}

	public void openConnection(String address) {
		/**
		 * �����û��Ѿ���ϵͳ��������Wifi��ֱ�ӽ��о�����ͨ��
		 * ����address�Ƿ������˵�IP��ַ������û���õ�
		 * ��������IP��ַ��˿ں�ֻ���ڽ������ݵ�ʱ�������
		 */
		serverIp = address ; 
		connectThread = new ConnectThread();
		connectThread.start();
		Log.d(TAG, "����������");
	}

	/**
	 * ʹ��UDPͨ�ţ��������ճ������ 
	 * ���������ӳɹ���û���ã���ΪUDP�������ӷ�ʽ�ģ�����ֻ����ָ��������ָ���˿ڷ������� ��������������������
	 */
	class ConnectThread extends Thread {
		// ����udp���ӵ�socket
		private DatagramSocket das = null;

		public void run() {
			try {
				Log.d(TAG, "����������");
				das = new DatagramSocket();
				// �������淢�����ӳɹ�����Ϣ
				connectedThread = new ConnectedThread(das);
				connectedThread.start();
				Log.d(TAG, "���ӳɹ�");
				sendMessageToUI(PaeActivity.CONNECTION_SUCCESS);
			} catch (Exception e) {
				// �������淢������ʧ�ܵ���Ϣ
				Log.d(TAG, "����ʧ��");
				sendMessageToUI(PaeActivity.CONNECTION_FAIL);
			}
		}
		public void cancel() {
			try {
				das.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * ���������ӵ�udp socket , ��������udp���� ������䣺�������ݱ���Ȼ���� datagramPacket = new
	 * DatagramPacket(bytes[], length,hostIp, port);
	 * DatagramSocket.send(datagramPacket);
	 */
	class ConnectedThread extends Thread {
		// ʹ��UDP����
		private DatagramSocket das; // UDPЭ���Socket
		private DatagramPacket p; // UDP���ݱ�
		private InetAddress ipAddress; // ������IP��ַ

		public ConnectedThread(DatagramSocket sc) {
			das = sc;
			try {
				// �����ǽ�IP��ַת���ɹ̶���InetAddress��ʽ
				ipAddress = InetAddress.getByName(serverIp);
			} catch (UnknownHostException e) {
				// ���IP��ַ��ʽ�����򱨴�
				e.printStackTrace();
			}
		}

		public void write(String msg) {
			int msg_length = msg.length();
			byte[] messageByte = msg.getBytes();
			/**
			 * �����ǽ���һ��UDP���ݱ� ԭ���ǣ�DatagramPacket(byte[] data, int length,
			 * InetAddress host, int port) data��ָҪ���͵����ݣ�ת�����ֽ��� lengthָ���ݵĳ���
			 * hostָҪ���ն˵�IP��ַ port����ָ�����ն�Ӧ�ó���Ķ˿ںţ��������˻�����˶˿ڣ��Ӷ�������Ϣ
			 */
			p = new DatagramPacket(messageByte, msg_length, ipAddress,
					Config.SERVER_PORT);
			try {
				das.send(p);
			} catch (IOException e) {
				// ����UDP���ص㣬������Ϣʧ�ܣ��������κδ���
			}
		}

		public void cancel() {
			try {
				das.close();
			} catch (Exception e) {
				// �������˲�����ͻ��˶Ͽ����ӣ�ֻ�ܿͻ����Լ��ر��Լ�
			}

		}
	}
	
	/**
	 * �ڱ�Ҫ��ʱ���������淢����Ϣ
	 * @param msg
	 */
	public void sendMessageToUI(int msg) {
		handler.obtainMessage(msg).sendToTarget();
	}
	/**
	 * �ر������Ѿ����ڵ��߳�
	 */
	private void closeExistedConnection() {
		if (connectThread != null) {
			connectThread.cancel();
			connectedThread = null;
		}
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}
	public void closeConnection() {
		closeExistedConnection();
	}
	/**
	 * ���漸�����Ǽ̳нӿڵĺ���������Ҫ�õ�
	 */
	public void doDefaultConnection() {

	}
	public void reconnect() {

	}

	public void closeDevice() {
	}

}
