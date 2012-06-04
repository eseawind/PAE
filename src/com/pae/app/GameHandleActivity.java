package com.pae.app;

import com.pae.PaeActivity;
import com.pae.R;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.PaeApplication;
import com.pae.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ��Ϸ�ֱ�Ӧ�õ�Activity ���ڲ�ͬ�û���ʹ�����ײ�ͬ���������û�ѡ��
 * 
 * @author Administrator
 * 
 */
public class GameHandleActivity extends Activity implements OnTouchListener,
		OnClickListener {

	private static final String TAG = "Bluetooth";
	/**
	 * �����ؼ�
	 */
	private ImageButton dir_left;
	private ImageButton dir_right;
	private ImageButton dir_up;
	private ImageButton dir_down;

	private ImageButton fun_circle; // ԲȦ
	private ImageButton fun_triangle; // ������
	private ImageButton fun_x; // ��
	private ImageButton fun_square; // ������

	private ImageButton fun_r1;
	private ImageButton fun_r2;
	private ImageButton fun_l1;
	private ImageButton fun_l2;

	private ImageButton fun_select;
	private ImageButton fun_start;

	private ImageButton fun_home; // �ص������棬ͬʱ�˳�Ӧ��

	private TextView tips;

	/**
	 * ���Ķ���
	 */
	private Connector connector; // ���ӷ�ʽ�ӿڣ�������ʼ����������ӷ�ʽ��������ֻ������������Ϣ
	private PaeApplication application; // ȫ�ֶ����ô˶���������socket����

	private Vibrator vibrator; // ����
	// ��ģʽ��ʱ��Ϊ����,{1000,5000}��ʾ��1�룬��5�룻����vibrator.vibrate(pattern,
	// repeat);�ڶ�������Ϊ0��ʾ�ظ���-1��ʾ���ظ�
	private long[] pattern = { 1000, 5000 };

	// ������Ӧ��
	private SensorManager sensorMgr;
	private Sensor sensor;

	private String message = ""; // ���͵���Ϣ

	// handler���ڴ�����Ϣ���������棬����û���õ�
	private final Handler handler = new Handler();

	private boolean isLongPress = false;
	private LongPressThread thread;

	/*
	 * ��ʼ������
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamehandle);
		initComponents();
		// ��ϵͳ�����л�ȡ�����ķ���
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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

		// �̲߳������У����������¼�
		thread = new LongPressThread();
		thread.start();
	}

	/**
	 * ��ʼ����ť
	 */
	public void initComponents() {
		dir_left = (ImageButton) findViewById(R.id.left);
		dir_left.setOnTouchListener(this);
		dir_right = (ImageButton) findViewById(R.id.right);
		dir_right.setOnTouchListener(this);
		dir_up = (ImageButton) findViewById(R.id.up);
		dir_up.setOnTouchListener(this);
		dir_down = (ImageButton) findViewById(R.id.down);
		dir_down.setOnTouchListener(this);

		fun_circle = (ImageButton) findViewById(R.id.circle);
		fun_circle.setOnTouchListener(this);
		fun_square = (ImageButton) findViewById(R.id.square);
		fun_square.setOnTouchListener(this);
		fun_triangle = (ImageButton) findViewById(R.id.triangle);
		fun_triangle.setOnTouchListener(this);
		fun_x = (ImageButton) findViewById(R.id.x);
		fun_x.setOnTouchListener(this);

		fun_l1 = (ImageButton) findViewById(R.id.l1);
		fun_l1.setOnTouchListener(this);
		fun_l2 = (ImageButton) findViewById(R.id.l2);
		fun_l2.setOnTouchListener(this);
		fun_r1 = (ImageButton) findViewById(R.id.r1);
		fun_r1.setOnTouchListener(this);
		fun_r2 = (ImageButton) findViewById(R.id.r2);
		fun_r2.setOnTouchListener(this);

		fun_home = (ImageButton) findViewById(R.id.home);
		fun_home.setOnClickListener(this);
		fun_start = (ImageButton) findViewById(R.id.start);
		fun_start.setOnClickListener(this);
		fun_select = (ImageButton) findViewById(R.id.select);
		fun_select.setOnClickListener(this);

		tips = (TextView) findViewById(R.id.gamehandletips);
	}

	// �����������������������ť�����¼���һֱ���͸ð�����Ϣ
	public boolean onTouch(View v, MotionEvent event) {
		doButtonTouchEvent(v.getId(), event.getAction()
				& MotionEvent.ACTION_MASK);

		return true;
	}

	// �жϰ�ť
	private void doButtonTouchEvent(int buttonId, int action) {
		switch (buttonId) {
		case R.id.up:
			message = GameHandleConfig.DIR_UP;
			doButtonTouchAction(action, message);
			break;
		case R.id.down:
			message = GameHandleConfig.DIR_DOWN;
			doButtonTouchAction(action, message);
			break;
		case R.id.left:
			message = GameHandleConfig.DIR_LEFT;
			doButtonTouchAction(action, message);
			break;
		case R.id.right:
			message = GameHandleConfig.DIR_RIGHT;
			doButtonTouchAction(action, message);
			break;
		// ������ļ�Ҫ��һ��
		case R.id.circle:
			vibrator.vibrate(500);
			message = GameHandleConfig.FUN_CIRCLE;
			doButtonTouchAction(action, message);
			break;
		case R.id.square:
			message = GameHandleConfig.FUN_SQUARE;
			doButtonTouchAction(action, message);
			break;
		case R.id.triangle:
			message = GameHandleConfig.FUN_TRIANGLE;
			doButtonTouchAction(action, message);
			break;
		case R.id.x:
			message = GameHandleConfig.FUN_X;
			doButtonTouchAction(action, message);
			break;
		case R.id.l1:
			message = GameHandleConfig.FUN_L1;
			doButtonTouchAction(action, message);
			break;
		case R.id.l2:
			message = GameHandleConfig.FUN_L2;
			doButtonTouchAction(action, message);
			break;
		case R.id.r1:
			message = GameHandleConfig.FUN_R1;
			doButtonTouchAction(action, message);
			break;
		case R.id.r2:
			message = GameHandleConfig.FUN_R2;
			doButtonTouchAction(action, message);
			break;
		default:
			break;
		}
	}

	// �жϴ����¼�
	private void doButtonTouchAction(int action, String msg) {
		message = msg;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// connector.sendMessage(message);
			isLongPress = true;
			break;
		case MotionEvent.ACTION_UP:
			// connector.sendMessage(message);
			isLongPress = false;
			break;
		default:
			break;
		}
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.home:
			backHome();
			break;
		case R.id.select:
			// ���select����������Ӧ
			openSensor();
			break;
		case R.id.start:
			// ���select����������Ӧ
			connector.sendMessage(GameHandleConfig.FUN_START);
			break;
		default:
			break;
		}
	}

	private void backHome() {
		finish();
	}

	private boolean isSensorOpen = false;
	private void openSensor() {
		if (!isSensorOpen) {
			// ��ü��ٴ�������ʵ��������������Ӧ
			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			// ע����ٴ���������3������Ϊ���ľ�ȷ��
			sensorMgr.registerListener(sensorlistener, sensor,
					SensorManager.SENSOR_DELAY_UI);//�ֱ���Ӧ������
			isSensorOpen = true;
			tips.setText("������Ӧ�ѿ���");
		} else {
			sensorMgr.unregisterListener(sensorlistener);
			isSensorOpen = false;
			tips.setText("������Ӧ�ѹر�");
		}
	}

	protected void closeSensor() {
		if (isSensorOpen) {
			sensorMgr.unregisterListener(sensorlistener);
			isSensorOpen = false;
		}
	}

	/**
	 * ���ﴦ���������ٶȵ��¼�
	 */
	// ������Ӧ���ж�����
	private final static double minRight = 2;// �����ߵ���Сֵ�����ֻ�̧��Ҫ��Y����2 , ��y >2
	private final static double maxLeft = -2; // ������ʱ��Y�����ֵҪС��-2����ʾ������б��y<-2
	private final static double minUp = 2; // ��ǰ��ʱ����Сֵ�����Բ���������Ϊ����������� -1 <x < 5
	private final static double minDown = 5; // �ֻ���ǰ������45�����ʾ����ߡ���x>5
	private SensorEventListener sensorlistener = new SensorEventListener() {
		// ������Ϸ�ֱ���Ļһֱ���ϣ�Z�ᳯ�ϣ�����0��������ֻ����x����y���ֵ
		public void onSensorChanged(SensorEvent event) {
			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			// float z = event.values[SensorManager.DATA_Z];
			// ����Ҫ������ϼ�
			// ��Ļ������̧��ʱ��ǰ��������w , ��ʱ y > 0

			// �����ӵļ�����ǰ�棬�����Ͳ�����Ϊ����̫������º���Ĳ���ִ��
			// ��ǰ��ʱ����Сֵ�����Բ���������Ϊ����������� -1 <x < 5
			if (x > minUp && x < minDown && y > minRight) // �ֻ���̧��������̧��ͬ����ʾ������
				connector.sendMessage(GameHandleConfig.DIR_RIGHT);
			else if (x > minUp && x < minDown && y < maxLeft)// �ֻ���̧��������̧��ͬ����ʾ������
				connector.sendMessage(GameHandleConfig.DIR_LEFT);
			else if (x > minUp && x < minDown)// ΢��̧��������45�ȣ�����ǰ��
				connector.sendMessage(GameHandleConfig.DIR_UP);
			else if (x > minDown)// ����̧����45�ȱ�ʾ�����
				connector.sendMessage(GameHandleConfig.DIR_DOWN);
			else if (y > minRight) //
				connector.sendMessage(GameHandleConfig.DIR_RIGHT);
			else if (y < maxLeft)
				connector.sendMessage(GameHandleConfig.DIR_LEFT);
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	@Override
	public void onBackPressed() {
		closeSensor();
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDestroy() {
		if (null != vibrator) {
			vibrator.cancel();
		}
		closeSensor();
		connector.closeConnection();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		if (null != vibrator) {
			vibrator.cancel();
		}
		closeSensor();
		connector.closeConnection();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeSensor();
	}

	// ���һ���ڲ��߳����������¼����ɿ��¼�
	class LongPressThread extends Thread {
		@Override
		public void run() {
			// ���̲߳�������
			while (true) {
				// ������ĳ����ťʱ�����Ϸ��͸���Ϣ
				if (isLongPress) {
					connector.sendMessage(message);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
