package com.pae.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.pae.PaeActivity;
import com.pae.R;
import com.pae.app.AppConfig;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * ���������ࣺ --> ��װ�����Ļ������� �����������裺1, ��ȡ���������豸 ; 2, �����豸 ��3 ,�������� -->
 * �ɽӿ�openConnection(address) �������ӣ�ͨ����ȡ���������MAC��ַʹ�� PS: ����Զ���豸
 * ��Խ���BtDeviceListActivity��ʵ��
 * 
 * ���ӳɹ�������Ϣ��handler���͵�������
 * 
 * @author Administrator
 */
public class BluetoothConnector implements Connector {

	// ���debug��Ϣ
	private static final String TAG = "BluetoothCore";
	// ��������������
	private final BluetoothAdapter btAdapter;

	// ��ƥ���豸�б���������Ĭ������ʱ��ƥ���б�
	private Set<BluetoothDevice> pairedDevices;

	/**
	 * ������״̬�����������ж�����״̬���Խ�����Ӧ����
	 */
	private static final int BT_NONE = 0; // ����״̬
	private static final int BT_CONNECTING = 1; // ����������
	private static final int BT_CONNECTED = 2; // ������
	private int state; // ��ǰ״̬

	// ���������߳�
	private ConnectThread connectThread;
	// ���������ӽ���
	private ConnectedThread connectedThread;

	// ����ͬ�߳���Ϣ���
	private final Handler handler;

	private BluetoothDevice serverDevice = null;
	// ���浱ǰ���ӵ��豸���Ա����Ӷ�ʧʱ��������
	private BluetoothDevice connectedDevice = null;

	public BluetoothConnector(Context context, Handler handler) {

		// ��handler�������̼߳����Ϣ
		this.handler = handler;
		// ��ȡ���������豸
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		// �ȴ�����
		openBtWithoutRequest();
		// ȡ����ƥ���豸�б�
		try {
			pairedDevices = btAdapter.getBondedDevices();
			// ��������״̬
			state = BT_NONE;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openBtWithoutRequest() {
		// ֱ�Ӵ�����
		if (!btAdapter.isEnabled())
		{
			btAdapter.enable();
			//�ȴ���������
//			try {
//    			Thread.sleep(2000);
//    		} catch (InterruptedException e) {
//    			// nothing
//    		}
		}
	}

	public void disableBluetooth() {
		if (btAdapter.isEnabled()) {
			btAdapter.disable();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// nothing
			}
		}
	}

	/**
	 * ����ƥ���豸�в���Ĭ�Ϸ�����PAESERVER
	 */
	public BluetoothDevice getServerFromBondedList() {

		if (pairedDevices.size() == 0)
			return null;
		else {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals(AppConfig.SERVERNAME)) {
					return device;
				}
			}
			return null;
		}
	}

	/**
	 * ����Ĭ�����ӣ����ҷ��������ƣ��ҵ��������������豸 �������ʧ������Handler������Ϣ����ʾ�豸�б����û�ѡ��
	 */
	public void doDefaultConnection() {
		openBtWithoutRequest();
		/**
		 * ֻ����ƥ���б��в���
		 */
		if ((serverDevice = getServerFromBondedList()) != null) {
			connectDevice(serverDevice);
		} else {
			handler.obtainMessage(PaeActivity.DEFAULT_CONNECTION_FAIL)
					.sendToTarget();
		}

	}

	/**
	 * �����߳̽������� �������� ͨ��connectThread���ӳɹ�������connectedThread�������ӳɹ���socket
	 * 
	 * @param device
	 */
	private void connectDevice(BluetoothDevice device) {

		ConnectThread thread = new ConnectThread(device);
		thread.start();
		Log.d(TAG, "����������");
		setState(BT_CONNECTING);
	}

	/**
	 * ���ķ������õ�MAC��ַ�������� ���ӳɹ����ͨ��handler������Ϣ��������
	 */
	public void openConnection(String address) {

		// �ȴ�����
		openBtWithoutRequest();
		Log.d(TAG, "��������ַ�ǣ�address:" + address);
		// ��ͨ��MAC��ַ���ӷ�����
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		connectedDevice = device; // ���������ӵĶ����Ա�reconnect

		connectDevice(device);
	}

	public void sendMessage(String message) {
		// ���ж��Ƿ������ӣ��ڽ����κβ������������Ĳ���ǰ��Ҫ���
		if (state != BT_CONNECTED) {
			return;
		}
		if (message.length() > 0) {
			byte[] ms = message.getBytes();
			write(ms);
		}
	}

	public void write(byte[] bytes) {
		ConnectedThread t;
		synchronized (this) {
			if (state != BT_CONNECTED)
				return;
			t = connectedThread;
		}
		t.write(bytes);
	}

	public void closeConnection() {
		// �ͷ��߳�
		closeExistedConnection();
	}

	/**
	 * ���Ӷ�ʧ������
	 */
	public void reconnect() {
		if (BT_NONE == state) {
			connectDevice(connectedDevice);
		}
	}

	// -----һ����ʹ���߳�������ָ���������豸���ڲ��ࣩ----//
	private class ConnectThread extends Thread {
		private BluetoothDevice device;
		private BluetoothSocket socket;

		public ConnectThread(BluetoothDevice btDevice) {

			device = btDevice;
			BluetoothSocket tmp = null;

			try {
				// ������UUID����������
				tmp = device.createRfcommSocketToServiceRecord(Config.uuid);
			} catch (IOException e) {
				connectionFail();
			}
			socket = tmp;
		}

		@Override
		public void run() {
			try {
				Log.d(TAG, "��ʼ��������");
				socket.connect();
				/**
				 * ���ӳɹ��󣬹������socketͨ��
				 */
				manageConnection(socket);

			} catch (IOException e) {
				Log.d(TAG, "����ʧ��");
				// ͨ��handler������Ϣ��UI������ʾ�����Ϣ
				connectionFail();
				try {
					socket.close();
				} catch (IOException e1) {
				}

			}
			synchronized (BluetoothConnector.this) {
				connectThread = null;
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private BluetoothSocket socket = null;
		private InputStream is = null;
		private OutputStream os = null; // ֻ��Ҫд��Ϣ����

		public ConnectedThread(BluetoothSocket msocket) {
			socket = msocket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			is = tmpIn;
			os = tmpOut;
		}

		@Override
		public void run() {
			// ��������̣������ַ�������������Ҫ̫��
			byte[] buffer = new byte[512];
			// ��¼�����˶��ٸ��ַ�
			int bytes;
			while (true) {
				try {
					// Read from the InputStream
					bytes = is.read(buffer);
					Log.d(TAG, "��ȡ����Ϣ ��" + buffer);

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				os.write(buffer);
				Log.d(TAG, "��Ϣ��" + buffer + " ���ͳɹ�");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "��Ϣ��" + buffer + " ����ʧ��");
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

	}

	/**
	 * ���������ӵ�Socket����ͨ��
	 * 
	 * @param socket
	 */
	public void manageConnection(BluetoothSocket socket) {

		closeExistedConnection();

		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		/**
		 * �������ӳɹ���Ϣ��������
		 */
		connectionSuccess();
	}

	/**
	 * ������������ͨ��handler�������淢��������Ϣ�����ӳɹ�������ʧ�������Ӷ�ʧ
	 */
	public void connectionFail() {
		Log.e(TAG, "����ʧ��");
		setState(BT_NONE);
		handler.obtainMessage(PaeActivity.CONNECTION_FAIL).sendToTarget();
	}

	public void connectionLost() {
		Log.e(TAG, "���Ӷ�ʧ");
		setState(BT_NONE);
		handler.obtainMessage(PaeActivity.CONNECTION_LOST).sendToTarget();
	}

	public void connectionSuccess() {
		Log.d(TAG, "���ӳɹ�");
		setState(BT_CONNECTED);
		handler.obtainMessage(PaeActivity.CONNECTION_SUCCESS).sendToTarget();
	}

	/**
	 * ��synchronized��state���м�������ֹ����߳�ͬʱ�޸�״̬
	 * 
	 * @param mstate
	 */
	private synchronized void setState(int mstate) {
		state = mstate;
	}

	private synchronized int getState() {
		return state;
	}

	/**
	 * �ر������Ѿ����ڵ��߳�
	 */
	private void closeExistedConnection() {
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		setState(BT_NONE);
	}

	public void closeDevice() {
		// �ر�����
		disableBluetooth();
	}

}
