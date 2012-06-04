package com.pae.view;

import java.util.List;

import com.pae.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * wifi�豸�б�
 * 
 * @author Administrator
 * 
 */
public class WifiDeviceListActivity extends Activity {

	private static final String TAG = "Bluetooth";// ����̨���

	private WifiManager wifiManager = null;
	private WifiConfiguration wifiConfiguraiton = null;
	private WifiInfo wifiInfo = null;
	private List<ScanResult> scanResult;

	private ArrayAdapter<String> newDeviceAdapter;
	private ArrayAdapter<String> configuratedDeviceAdapter;// �Ѿ����õ�����

	private ListView configureddeviceList;
	private ListView newDeviceList;
	private Button scanButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		
		initWifi(this);
		initComponents();
	}

	public void initComponents() {
		scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(listener);

		configuratedDeviceAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		configureddeviceList = (ListView) findViewById(R.id.paired_devices);
		configureddeviceList.setAdapter(configuratedDeviceAdapter);
		configureddeviceList.setOnItemClickListener(itemListener);

		newDeviceAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		newDeviceList = (ListView) findViewById(R.id.paired_devices);
		newDeviceList.setAdapter(newDeviceAdapter);
		newDeviceList.setOnItemClickListener(itemListener);

		/**
		 * ��ʼ�������õ�����
		 */
		List<WifiConfiguration> configuratedDevice = wifiManager
				.getConfiguredNetworks();
		if (configuratedDevice.size() > 0) {
			for (WifiConfiguration wc : configuratedDevice) {
				configuratedDeviceAdapter.add(wc.SSID + "\n" + wc.BSSID);
			}
		} else {
			// ���ر���
			findViewById(R.id.title_paired_devices).setVisibility(View.GONE);
		}

	}

	private void openWifi() {
		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);
	}

	public void initWifi(Context context) {
		if (wifiManager == null)
			wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
		this.openWifi();
		// ͬ����ע��㲥������
		IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		this.registerReceiver(receiver, filter);

	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				// �Ȱ�loadingDiologȡ��
				loadingDialog.cancel();
				scanResult = wifiManager.getScanResults();
				for (int i = 0; i < scanResult.size(); i++) {
					newDeviceAdapter.add(scanResult.get(i).toString());
				}
			}
		}
	};

	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			showLoadingDialog();
			wifiManager.startScan();
		}
	};

	private OnItemClickListener itemListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
			//ȡ�ð�������һ��
			ScanResult sc = scanResult.get(position);
			
		}

	};
	private LoadingDialog loadingDialog;

	private void showLoadingDialog() {
		loadingDialog = new LoadingDialog(this, "���ڼ����У����Ժ�...");
		loadingDialog.show();
	}
	
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
