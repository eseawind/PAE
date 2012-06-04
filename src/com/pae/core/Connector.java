package com.pae.core;

import android.widget.ArrayAdapter;

/*
 * ���ӽӿ�
 * ����������wifi���ӷ�ʽ�Ĺ�ͬ����
 */
public interface Connector {
	/*
	 * ����Ҫ�Ĺ��ܣ�������Ϣ ������Ͳ��ɹ���������
	 */
	public abstract void sendMessage(String message);

	// ����Ĭ������
	public abstract void doDefaultConnection();

	public abstract void openConnection(String address);

	public abstract void closeConnection();

	public abstract void reconnect();
	
	//�˳�ʱ�ر��豸
	public abstract void closeDevice();
}
