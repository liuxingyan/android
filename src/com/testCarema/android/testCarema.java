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
				final CharSequence[] items = { "���", "����", "��ת" };
				AlertDialog dlg = new AlertDialog.Builder(testCarema.this)
						.setTitle("ѡ��ͼƬ")
						.setItems(items, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								// ����item�Ǹ���ѡ��ķ�ʽ��
								// ��items�������涨�������ַ�ʽ�����յ��±�Ϊ1���Ծ͵������շ���
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
									Log.i("info", "ͼƬ״̬"+imageView.getDrawable());
									if (imageView.getDrawable() != null) {
										Log.i("info", "��תing");
										// RotateAnimation animation=new
										// RotateAnimation(0, 90);
										// animation.setFillAfter(true);
										// animation.setDuration(5000);
										// imageView.startAnimation(animation);
										Matrix matrix = imageView.getImageMatrix();
										matrix.postRotate(90);
										myBitmap = rotateBitmap(myBitmap);
										imageView.setImageBitmap(myBitmap);
										Log.i("info", "��ת����");
									}
								}
							}
						}).create();
				dlg.show();
			}
		};
		// ��imageView�ؼ��󶨵���������
		imageView.setOnClickListener(imgViewListener);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		ContentResolver resolver = getContentResolver();
		/**
		 * ��Ϊ���ַ�ʽ���õ���startActivityForResult������ �������ִ����󶼻�ִ��onActivityResult������
		 * ����Ϊ�����𵽵�ѡ�����Ǹ���ʽ��ȡͼƬҪ�����жϣ�
		 * �����requestCode��startActivityForResult����ڶ���������Ӧ
		 */
		if (requestCode == 0) {
			try {
				// ���ͼƬ��uri
				Uri originalUri = data.getData();

//				Bitmap bitmap = data.getExtras().getParcelable("data");
//				Log.i("info", "the width is " + bitmap.getWidth());
//				Log.i("info", "the height is " + bitmap.getHeight());

				// ��ͼƬ���ݽ������ֽ�����
				mContent = readStream(resolver.openInputStream(Uri
						.parse(originalUri.toString())));
				// ���ֽ�����ת��ΪImageView�ɵ��õ�Bitmap����
				myBitmap = getPicFromBytes(mContent, null);

				// �ѵõ���ͼƬ���ڿؼ�����ʾ
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
				// /* �����ļ� */
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
			// �ѵõ���ͼƬ���ڿؼ�����ʾ
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
			Log.i("info", "����");
		} else if (cf.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.i("info", "����");
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
		Log.i("info", "����ı�");
	}

}