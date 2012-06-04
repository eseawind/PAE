package com.pae.core;

import java.net.Socket;

import com.pae.view.LoadingDialog;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * ȫ�ֱ����࣬�������ݸ��Ӷ��� ��Ҫ��AndroidMainfest.xml�������Ӧ��application
 * 
 * @author Administrator
 * 
 */
public class PaeApplication extends Application {

	/**
	 * ȫ��socket����
	 */
	public Connector connector;

	private static PaeApplication paeApplication;


	public static PaeApplication getInstance() {
		return paeApplication;
	}
	/**
	 * �������ӹ���
	 */
	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}


	@Override
	public Context getApplicationContext() {
		// TODO Auto-generated method stub
		return super.getApplicationContext();
	}
	
	/**
	 * �����࣬ͨ��Toast
	 * @param msg
	 */
	//PaeActivity Handler
	public void showToastShort(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	public void showToastLong(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
	/**
	 * ͨ�ü��ؽ�����
	 */
	public LoadingDialog loadingDialog;
	public void showLoadingDialog(String msg) {
		loadingDialog = new LoadingDialog(getApplicationContext(), msg);
		loadingDialog.show();
	}
	public void cancelLoadingDialog()
	{
		if( loadingDialog!=null )
			loadingDialog.cancel();
	}

}
