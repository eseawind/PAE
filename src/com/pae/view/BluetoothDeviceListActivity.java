/**
 * 
 */
package com.pae.view;

import java.util.Set;

import com.pae.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Administrator
 * 
 */
public class BluetoothDeviceListActivity extends Activity {

	private static final String TAG = "Bluetooth";// ����̨���
	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter btAdapter;

	// ��ƥ���豸�б�
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	// �·��ֵ��豸�б�
	private ArrayAdapter<String> newDevicesArrayAdapter;

	private LoadingDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ��ʾ�豸�б���
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		setResult(Activity.RESULT_CANCELED);

		// ��ȡ���������豸
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		if (btAdapter == null) {
			Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_SHORT);
			return;
		}
		if (!btAdapter.isEnabled())
			btAdapter.enable();
		// ע�������
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(receiver, filter);

		// ͨ��findViewById�ҵ�activity�еĿؼ�
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doScan();
				// ����ɨ�谴ť����ֹ�ٰ�
				v.setVisibility(View.GONE);
			}
		});

		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		newDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		ListView pairedList = (ListView) findViewById(R.id.paired_devices);
		pairedList.setAdapter(pairedDevicesArrayAdapter);
		pairedList.setOnItemClickListener(deviceClickListener);

		ListView newDeviceList = (ListView) findViewById(R.id.new_devices);
		newDeviceList.setAdapter(newDevicesArrayAdapter);
		newDeviceList.setOnItemClickListener(deviceClickListener);

		// ��ʼ����ƥ���б�
		/**
		 * ��ִ��device discovery֮ǰ�����������Ե��豸�б��в鿴��Ҫ���ֵ��豸�Ƿ��Ѿ����ڡ�
		 * ͨ������getBondedDevices()�������Ի�ô����Ѿ���Ե��豸��BluetoothDevice����
		 */
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			findViewById(R.id.title_paired_devices).setVisibility(View.GONE);
		}
	}
     //��Ա���� ���̳нӿ��࣬��ʼ��һ��ʵ��
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				newDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// setProgressBarIndeterminateVisibility(false);
				progressDialog.cancel();
				setTitle(R.string.select_device);
				if (newDevicesArrayAdapter.getCount() == 0) {
					Toast.makeText(BluetoothDeviceListActivity.this,
							"�Բ���û���ҵ����豸", Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	private OnItemClickListener deviceClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
			btAdapter.cancelDiscovery();
			// Get the device MAC address, which is the last 17 chars in the
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			// ����һ����Ϣ���������������豸��ַ���ͻ�������
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// �ر��������
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	public void doScan() {
		// ���ԭ�����б�
		newDevicesArrayAdapter.clear();
		// ���ý�����
		// setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.select_device);

		findViewById(R.id.title_new_devices);

		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		// ��ʼɨ��
		showLoadingDialog();

		btAdapter.startDiscovery();
	}

	private void showLoadingDialog() {
		progressDialog = new LoadingDialog(this, "���������豸�����Ժ�...");
		progressDialog.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
		}
		// Unregister broadcast listeners
		this.unregisterReceiver(receiver);
	}
     //////////////???????????????????????????????????
	//////////////////////////////////////////////////
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// ����һ����Ϣ���������������豸��ַ���ͻ�������
		Intent intent = new Intent();
		// �ر��������
		setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}

}
