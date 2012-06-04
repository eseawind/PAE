package com.pae.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

public class LoadingDialog extends ProgressDialog {

	public LoadingDialog(Context context, String message) {
		super(context);
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		setMessage(message);
		setCancelable(true);
	}
	/**
	 * 
	 * @param context 
	 * @param message ��ʾ����Ϣ
	 * @param alpha   ͸����
	 */
	public LoadingDialog(Context context, String message , float alpha)
	{
		super(context);
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		setMessage(message);
		setCancelable(true);
		// ����͸����
		Window window = this.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = alpha;
	}
	

}
