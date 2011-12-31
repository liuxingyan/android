package com.testCarema.android;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

public class TestSp extends Activity {
	private ImageView imageView;
	private String filePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(TestSp.this, testCarema.class);
//				startActivityForResult(intent, 0);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("info", "start result");
		// TODO Auto-generated method stub

		Bundle bundle = data.getExtras();
		Log.i("info", "bundle is : " + bundle.get("filePath").toString());
		filePath = bundle.get("filePath").toString();
		Bitmap bm = getLoacalBitmap(filePath);
		imageView.setImageBitmap(bm);
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Configuration cf= this.getResources().getConfiguration();
		if(cf.orientation==Configuration.ORIENTATION_LANDSCAPE){
			Log.i("info", "横屏");
		}else if(cf.orientation==Configuration.ORIENTATION_PORTRAIT){
			Log.i("info", "竖屏");
		}
		if (filePath != null) {
			Bitmap bm = getLoacalBitmap(filePath);
			imageView.setImageBitmap(bm);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(TestSp.this);
			alertDialog.setTitle("提示")
					   .setMessage("是否退出程序？")
					   .setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}})
					   .setNegativeButton("取消", null)
					   .show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
