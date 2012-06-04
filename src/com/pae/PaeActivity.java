package com.pae;

import com.pae.app.AppConfig;
import com.pae.app.GameHandleActivity;
import com.pae.app.PPTActivity;
import com.pae.core.BluetoothConnector;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.PaeApplication;
import com.pae.core.WifiConnector;
import com.pae.util.Util;
import com.pae.view.AboutDialog;
import com.pae.view.BluetoothDeviceListActivity;
import com.pae.view.LoadingDialog;
import com.pae.view.WifiDeviceListActivity;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PaeActivity extends Activity implements OnClickListener {

	private static final String TAG = "MAIN_ACTIVITY";

	/**
	 * ȫ�ֱ������������ݸ��Ӷ���
	 */
	private PaeApplication application;
	/**
	 * ����Ӧ�õİ�ť
	 */
	private ImageButton gameHandleBt;
	private ImageButton pptBt;
	private ImageButton movieBt;
	private ImageButton musicBt;
	private ImageButton painterBt;
	private ImageButton penBt;

	// ���õ�ǰ��Ӧ��ID�����ĳ��Ӧ�õİ�ťʱ�ͻᱻ����Ϊ��Ӧ�õı�ţ������AppConfig�����ж���
	private int selectedAppID;

	/**
	 * ���������ӹ���
	 */
	private Connector connector;

	/**
	 * �����request Code �����ڶ�Ӧ���ص����ݴ���
	 */
	public static final int REQUEST_BLUETOOTH_DEIVCE_LIST = 1;
	public static final int REQUEST_WIFI_DEIVCE_LIST = 2;
	/**
	 * handler���¼������,��㶨�壬���ָ��¼�����
	 */
	public static final int DEFAULT_CONNECTION_FAIL = 0;
	public static final int WIFI_DEFAULT_CONNECTION_FAIL = 5;
	public static final int CONNECTION_FAIL = 1;
	public static final int CONNECTION_LOST = 2;
	public static final int CONNECTION_SUCCESS = 3;
	public final static int REQUEST_DEVICE_LIST = 4;
	public final static int REQUEST_WIFI_SETTING = 5;
	/**
	 * �ȴ��Ի���
	 */
	public LoadingDialog loadingDialog;

	/** Activity ��� */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// ����Activity���ü�������
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/**
		 * ���ȫ�ֱ�����ʵ�����ں������Զ�application���get,set�������в���
		 */
		application = (PaeApplication) getApplication();
		/**
		 * ��ʼ���ؼ�
		 */
		initComponents();
	}

	// ��ʼ���ؼ�
	private void initComponents() {
		gameHandleBt = (ImageButton) findViewById(R.id.bt_gamehandle);
		gameHandleBt.setOnClickListener(this);
		pptBt = (ImageButton) findViewById(R.id.bt_ppt);
		pptBt.setOnClickListener(this);
		movieBt = (ImageButton) findViewById(R.id.bt_movie);
		movieBt.setOnClickListener(this);
		musicBt = (ImageButton) findViewById(R.id.bt_music);
		musicBt.setOnClickListener(this);
		painterBt = (ImageButton) findViewById(R.id.bt_painter);
		painterBt.setOnClickListener(this);
		penBt = (ImageButton) findViewById(R.id.bt_writer);
		penBt.setOnClickListener(this);
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.bt_gamehandle:
			selectedAppID = AppConfig.GAMEHANDLE;
			initChoiceDialog();
			break;
		case R.id.bt_ppt:
			selectedAppID = AppConfig.PPT;
			initChoiceDialog();// ����ѡ��ʽ�Ի���
			break;

		default:
			application.showToastShort("���ڿ����У������ڴ�");
			break;
		}
	}

	/**
	 * ���ӷ�ʽѡ��Ի���
	 */
	public void initChoiceDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.connection_choice_title)
				.setIcon(R.drawable.connectway)
				// �������õ�ѡ�б�ť
				.setSingleChoiceItems(AppConfig.CHOICE, 0, chooseDialogListener)
				.setNegativeButton("ȡ��", null).create();// ����ѡ��ʽ�Ի���
		// ���öԻ���͸����
		Util.setAlphaDialog(dialog, (float) 0.6);
		dialog.show();
	}

	/**
	 * ����ѡ��Ի���ļ�����
	 */
	private DialogInterface.OnClickListener chooseDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			// which��0��ʼ���������ѡ������ѡ�����Լ������
			case AppConfig.BT_CONNECTION:
				dialog.dismiss();// ȡ���Ի���
				showLoadingDialog("���ڽ����Զ����ӣ����Ժ�...");// ���Ͻ���Ĭ������
				autoBluetoothConnection();
				break;
			case AppConfig.WIFI_CONNECTION:
				dialog.dismiss();
				showWifiSetting();
				break;
			default:
				break;
			}
		}
	};

	public void showWifiSetting() {
		startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),
				REQUEST_WIFI_SETTING);
	}

	/**
	 * ����WIFI���ӣ���ֱ�ӽ��о��������ӣ� ��Ҫ�û������Ӻ�Wifi
	 */
	public void makeWifiConnection() {

		String address = Util.getLocalGateWay(this);
		application.showToastShort("address: " + address);
		// �����������ӷ�ʽ
		connector = new WifiConnector(this, handler);
		connector.openConnection(address);
	}

	/**
	 * ��ʾ�����б�󣬽��û�ѡ���������ַ����������OnActivityForResult�н��գ������ø÷���
	 * 
	 * @param data
	 *            ������ݵ�intent
	 */
	protected void makeBluetoothConnection(Intent data) {
		// ���մ��ص�������ַ
		Log.d(TAG, "���ڽ�����������");
		String address = data.getExtras().getString(
				BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// �����������ӷ�ʽ
		connector = new BluetoothConnector(this, handler);// ����ʵ������ʵ����
		connector.openConnection(address);
	}

	/**
	 * �����Զ�����
	 */
	public void autoBluetoothConnection() {
		// �����������ӷ�ʽ
		connector = new BluetoothConnector(this, handler);
		connector.doDefaultConnection();
	}

	/**
	 * �Զ�����ʧ�ܺ���ʾ�豸�б�
	 */
	protected void showBlueToothDeviceList() {
		Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
		startActivityForResult(intent, REQUEST_BLUETOOTH_DEIVCE_LIST);
	}

	/**
	 * �ֻ��ײ��˵���ť
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	/**
	 * ����˵�����ѡ���¼�
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_exit:
			// �˳�����
			finish();
			break;
		case R.id.option_about:
			// �������ڶԻ���
			showAboutDialog();
			break;
		default:
			break;
		}
		return false;
	}

	private void showAboutDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.about)
				// �������õ�ѡ�б�ť
				.setMessage(Config.ABOUT_US)
				.setIcon(android.R.drawable.sym_action_chat)
				.setNegativeButton("ȷ��", null).create();// ����ѡ��ʽ�Ի���
		// ���öԻ���͸����
		Util.setAlphaDialog(dialog, (float) 0.6);
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// ����ѡ��Ի����ѡ���ж�����������WiFi����
		showLoadingDialog("���������У����Ժ�...");
		switch (requestCode) {
		case REQUEST_BLUETOOTH_DEIVCE_LIST:
			if (resultCode == Activity.RESULT_OK) {
				makeBluetoothConnection(intent);
			} else {
				loadingDialog.cancel();
			}
			break;
		case REQUEST_WIFI_SETTING:
			if (resultCode == Activity.RESULT_CANCELED) {
				loadingDialog.cancel();
				makeWifiConnection();
			}
		default:
			// loadingDialog.cancel();
			break;
		}
	}

	/**
	 * ��ʾ�ȴ��Ի���
	 */
	private void showLoadingDialog(String msg) {
		loadingDialog = new LoadingDialog(this, msg);// ʵ��������
		loadingDialog.show();
	}

	/**
	 * ʵʱ�������������ͻ�������Ϣ
	 */
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// �����յ�ʲô��Ϣ����Ҫ�Ȱ�loadingDialog��ȡ��
			loadingDialog.cancel();
			switch (msg.what) {
			// Ĭ������ʧ�ܣ���ʾ�����б����û�ѡ�������豸
			case DEFAULT_CONNECTION_FAIL:
				// ȡ���ȴ��Ի���
				application.showToastShort("����Ĭ�Ϸ�����ʧ�ܣ����ڼ����豸�б�");
				showBlueToothDeviceList();
				break;
			case CONNECTION_FAIL:
				application.showToastShort("����ʧ�ܣ������³���");
				showBlueToothDeviceList();
				break;
			case CONNECTION_LOST:
				loadingDialog.cancel();
				application.showToastShort("���Ӷ�ʧ��������������");
				// connector.reconnect();
				break;
			// ���ӳɹ�������Ӧ�ý���
			case CONNECTION_SUCCESS:
				loadingDialog.cancel();
				startApp();
				// startGameHandle();
				break;
			case WIFI_DEFAULT_CONNECTION_FAIL:
				application.showToastShort("����Ĭ�Ϸ�����ʧ��");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * �ڵ��ĳ��Ӧ�õİ�ťʱ�ὫselectedAppID����Ϊ��Ӧ�õ�ID�� Ȼ���������selectedAppID����ָ��Ӧ��
	 */
	private void startApp() {
		Log.d("selected APPID", selectedAppID + "");
		// �Ƚ����ĵ����������ú�
		application.setConnector(connector);// ���ǰ������������ò���ת

		Intent intent = new Intent();
		switch (selectedAppID) {
		case AppConfig.GAMEHANDLE:
			intent.setClass(this, GameHandleActivity.class);
			break;
		case AppConfig.PPT:
			intent.setClass(this, PPTActivity.class);
			break;
		default:
			break;
		}
		startActivity(intent); // ʹ�õ�ǰ����������һ������
		// finish();
	}

	/**
	 * ����finish()ʱִ��
	 */
	@Override
	protected void onDestroy() {
		if (connector != null) {
			// connector.closeDevice();
		}
		super.onDestroy();
		System.exit(0);
	}

	/**
	 * ��Ļ�ر�ʱ����
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/**
	 * ��Ļ����ʱ����
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/**
	 * �����ֻ����ؼ�ʱ�Ĳ���
	 */
	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

}