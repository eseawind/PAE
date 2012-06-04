package com.pae.util;

import com.pae.view.LoadingDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.Window;
import android.view.WindowManager;

/**
 * �����࣬�ռ����õķ���
 * 
 * @author Administrator
 * 
 */
public class Util {

	/**
	 * ��÷�����IP��ַ
	 
	public static String getLocalIPAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return parseIPAddress(ipAddress);
	}
*/
	public static String getLocalGateWay(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();//�����Ժ󣬻��wifi��Ϣ
		int ipAddress = wifiInfo.getIpAddress();
		return parseGateWayAddress(ipAddress);
	}

	/**
	 * 
	 * @param intIp
	 *            ��ʽ����ÿһλ��2���ƴ������ģ���32λ ͨ����������
	 * @return
	 */
	public static String parseGateWayAddress(long intIp) {
		StringBuilder sb = new StringBuilder();
		sb.append(intIp & 0xFF).append(".");
		intIp = intIp >> 8;
		sb.append(intIp & 0xFF).append(".");
		intIp = intIp >> 8;
		sb.append(intIp & 0xFF).append(".");
		// �������һλ��1
		sb.append("1");
		return sb.toString();
	}

	/**
	 * 
	 * @param intIp
	 *            ��ʽ����ÿһλ��2���ƴ������ģ���32λ ͨ����������
	 * @return
	 * 
	 *         public static String parseIPAddress(long intIp) { StringBuilder
	 *         sb = new StringBuilder(); sb.append(intIp & 0xFF).append(".");
	 *         intIp = intIp >> 8; sb.append(intIp & 0xFF).append("."); intIp =
	 *         intIp >> 8; sb.append(intIp & 0xFF).append("."); intIp = intIp >>
	 *         8; sb.append(intIp & 0xFF).append("."); return sb.toString(); }
	 */
	/**
	 * ���öԻ����͸����
	 * 
	 * @param dialog
	 * @param alpha
	 */
	public static void setAlphaDialog(Dialog dialog, float alpha) {
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = alpha;
	}

	/**
	 * ������Ļ����
	 */
	public static void setScreenOn(Activity activity) {
		activity.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

}
