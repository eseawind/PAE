package com.pae.app;

import com.pae.R;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.KeyCode;
import com.pae.core.PaeApplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * PPT�ķ�ӳ�Ļ�����ݼ�
 * ���ţ�F5     �ӵ�ǰҳ�����˳���ESC    ��һҳ �� ���ͷ     ��һҳ�� �Ҽ�ͷ       �������ָ�룺 Ctrl+H
 */
public class PPTActivity extends Activity {

	private static final String TAG = "PPT";

	// ��ʼ���ؼ�
	private ImageButton playBt;
	private ImageButton playCurrentBt;
	private ImageButton escBt;
	private ImageButton nextPageBt;
	private ImageButton forePageBt;
	private ImageButton homeBt ; 

	/**
	 * ���Ķ���
	 */
	private Connector connector; // ���ӷ�ʽ�ӿڣ�������ʼ����������ӷ�ʽ��������ֻ������������Ϣ
	private PaeApplication application; // ȫ�ֶ����ô˶���������socket����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ppt);

		// �ȷ����û���
		initComponents();

		/**
		 * ��ȫ�ֱ�����ȡ��socket�����Խ���socketͨ��
		 */
		application = (PaeApplication) getApplication();
		connector = application.getConnector();

		// �ȷ����û���
		connector.sendMessage("client|" + Config.CLIENT_NAME);

		/**
		 * ������Ļ����
		 */
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void initComponents() {
		playBt = (ImageButton) findViewById(R.id.bt_play);
		playCurrentBt = (ImageButton) findViewById(R.id.bt_playCurrent);
		escBt = (ImageButton) findViewById(R.id.bt_esc);
		nextPageBt = (ImageButton) findViewById(R.id.bt_next);
		forePageBt = (ImageButton) findViewById(R.id.bt_fore);
		homeBt = (ImageButton) findViewById(R.id.ppt_home);
		
		playBt.setOnClickListener(listener);
		playCurrentBt.setOnClickListener(listener);
		escBt.setOnClickListener(listener);
		nextPageBt.setOnClickListener(listener);
		forePageBt.setOnClickListener(listener);
		homeBt.setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.bt_play:
				connector.sendMessage(KeyCode.KEY_F5 + "");
				break;
			case R.id.bt_playCurrent:
				// ��������Ҫ�� �� + ����������
				connector.sendMessage(KeyCode.KEY_SHIFT + "+" + KeyCode.KEY_F5);
				break;
			case R.id.bt_esc:
				connector.sendMessage(KeyCode.KEY_ESC + "");
				break;
			case R.id.bt_next:
				connector.sendMessage(KeyCode.KEY_RIGHT_ARROW + "");
				break;
			case R.id.bt_fore:
				connector.sendMessage(KeyCode.KEY_LEFT_ARROW + "");
				break;
			case R.id.ppt_home:
				backHome();
				break;
			default:
				break;
			}
		}
	};
	
	public void backHome()
	{
		finish();
	}
	/**
	 * ��ֹ�����������й��ܼ�
	 */
	@Override
	public void onBackPressed() {
		
	}
	
	
	//�����ֻ�������û���õ��İ���
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
}
