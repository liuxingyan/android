package com.testCarema.android;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class testCarema extends Activity {
	/** Called when the activity is first created. */
	private ImageView imageView;
	private OnClickListener imgViewListener;
	private Bitmap myBitmap;
	private byte[] mContent;
	private final String IMAGE_TYPE = "image/*";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		imageView = (ImageView) findViewById(R.id.imageView);
		imgViewListener = new OnClickListener() {
			public void onClick(View v) {
				final CharSequence[] items = { "相册", "拍照", "翻转" };
				AlertDialog dlg = new AlertDialog.Builder(testCarema.this)
						.setTitle("选择图片")
						.setItems(items, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								// 这里item是根据选择的方式，
								// 在items数组里面定义了两种方式，拍照的下标为1所以就调用拍照方法
								if (item == 1) {
									Intent getImageByCamera = new Intent(
											"android.media.action.IMAGE_CAPTURE");
									getImageByCamera.putExtra(
											MediaStore.EXTRA_OUTPUT,
											Uri.fromFile(new File(
													getExternalCacheDir()
															.toString()
															+ File.separator
															+ "test.jpg")));
									startActivityForResult(getImageByCamera, 1);
								} else if (item == 0) {
									Intent getImage = new Intent(
											Intent.ACTION_GET_CONTENT);
									getImage.addCategory(Intent.CATEGORY_OPENABLE);
									getImage.setType(IMAGE_TYPE);
//									getImage.putExtra("return-data", true);
									startActivityForResult(getImage, 0);
								} else if (item == 2) {
									Log.i("info", "图片状态"+imageView.getDrawable());
									if (imageView.getDrawable() != null) {
										Log.i("info", "翻转ing");
										// RotateAnimation animation=new
										// RotateAnimation(0, 90);
										// animation.setFillAfter(true);
										// animation.setDuration(5000);
										// imageView.startAnimation(animation);
										Matrix matrix = imageView.getImageMatrix();
										matrix.postRotate(90);
										myBitmap = rotateBitmap(myBitmap);
										imageView.setImageBitmap(myBitmap);
										Log.i("info", "翻转结束");
									}
								}
							}
						}).create();
				dlg.show();
			}
		};
		// 给imageView控件绑定点点击监听器
		imageView.setOnClickListener(imgViewListener);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		ContentResolver resolver = getContentResolver();
		/**
		 * 因为两种方式都用到了startActivityForResult方法， 这个方法执行完后都会执行onActivityResult方法，
		 * 所以为了区别到底选择了那个方式获取图片要进行判断，
		 * 这里的requestCode跟startActivityForResult里面第二个参数对应
		 */
		if (requestCode == 0) {
			try {
				// 获得图片的uri
				Uri originalUri = data.getData();

//				Bitmap bitmap = data.getExtras().getParcelable("data");
//				Log.i("info", "the width is " + bitmap.getWidth());
//				Log.i("info", "the height is " + bitmap.getHeight());

				// 将图片内容解析成字节数组
				mContent = readStream(resolver.openInputStream(Uri
						.parse(originalUri.toString())));
				// 将字节数组转换为ImageView可调用的Bitmap对象
				myBitmap = getPicFromBytes(mContent, null);

				// 把得到的图片绑定在控件上显示
				imageView.setImageBitmap(myBitmap);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (requestCode == 1) {
			try {
				super.onActivityResult(requestCode, resultCode, data);
				Options options = new BitmapFactory.Options();
				// options.inSampleSize=10;
				String cacheFilePath = getExternalCacheDir().toString()
						+ File.separator + "test.jpg";
				FileInputStream fis = new FileInputStream(cacheFilePath);
				Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
				fis.close();
				myBitmap = ResizeBitmap(bitmap, 800);
				// imageView.setImageBitmap(myBitmap);
				saveImage(myBitmap);
				// Bundle extras = data.getExtras();
				// myBitmap = (Bitmap) extras.get("data");
				// /* 创建文件 */
				// String filePath = getExternalFilesDir("").getPath()
				// + File.separator
				// + System.currentTimeMillis() + ".jpg";
				// File myCaptureFile = new File(filePath);
				// ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// BufferedOutputStream bos = new BufferedOutputStream(
				// new FileOutputStream(myCaptureFile));
				// myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				// mContent = baos.toByteArray();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 把得到的图片绑定在控件上显示
			imageView.setImageBitmap(myBitmap);
		}
	}

	public static Bitmap getPicFromBytes(byte[] bytes,
			BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;

	}

	public Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float temp = ((float) height) / ((float) width);
		int newHeight = (int) ((newWidth) * temp);
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		Configuration cf = this.getResources().getConfiguration();
		if (cf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.i("info", "横屏");
		} else if (cf.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.i("info", "竖屏");
			matrix.postRotate(90);
		}

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		bitmap.recycle();
		return resizedBitmap;
	}
	
	public Bitmap rotateBitmap(Bitmap bitmap){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		bitmap.recycle();
		return rotatedBitmap;
	}

	private void saveImage(Bitmap bm) {

		HashMap<Integer, Object> map = new HashMap<Integer, Object>();
		map.put(1, String.valueOf(System.currentTimeMillis()));
		map.put(2, new File(getExternalFilesDir("").getPath()));
		String strTempFile = (String) map.get(1);
		File myRecAudioDir = (File) map.get(2);

		try {
			File mediaFile = File.createTempFile(strTempFile, ".jpg",
					myRecAudioDir);

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(mediaFile));

			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.i("info", "界面改变");
	}

}