package com.pae.view;

import com.pae.R;

import android.app.AlertDialog;
import android.content.Context;

public class AboutDialog extends AlertDialog {

	private String title = "��������" ; 
	private String content = "�������ǵ�����" ; 
	
	
	public AboutDialog(Context context) {
		super(context);
		
		this.setTitle(title) ; 
		this.setMessage(content);
		setContentView(R.layout.aboutus);
		// TODO Auto-generated constructor stub
	}
}
