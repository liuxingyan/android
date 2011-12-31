/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.testCarema.android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

// ----------------------------------------------------------------------

public class CameraPreview extends Activity {
	private Preview mPreview;
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Create our Preview view and set it as the content of our activity.
		mPreview = new Preview(this);
		setContentView(mPreview);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mPreview.surfaceCreated(mPreview.mHolder);

		mPreview.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("info", "m on click");
				mPreview.mCamera.takePicture(null, null, new PictureCallback() {

					public void onPictureTaken(byte[] data, Camera camera) {
						// TODO Auto-generated method stub
						try {
							Log.i("info", "call back");
							Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
									data.length);
							/* 创建文件 */
							String filePath = getExternalFilesDir("").getPath()
									+ File.separator
									+ System.currentTimeMillis() + ".jpg";
							File myCaptureFile = new File(filePath);
							try {
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(myCaptureFile));
								/* 采用压缩转档方法 */
								bm.compress(Bitmap.CompressFormat.JPEG, 100,
										bos);

								/* 调用flush()方法，更新BufferStream */
								bos.flush();

								/* 结束OutputStream */
								bos.close();

								/* 将拍照下来且保存完毕的图文件，显示出来 */
								Intent intent = getIntent();
								intent.putExtra("filePath", filePath);
								intent.setClass(CameraPreview.this,
										TestSp.class);
								setResult(0, intent);
								CameraPreview.this.finish();
							} catch (Exception e) {
								e.printStackTrace();

								Log.e("error", e.getMessage());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

	}

	// private final class MyPictureCallback implements PictureCallback{
	// public void onPictureTaken(byte[] data, Camera camera) {
	//
	//
	// }
	//
	// }

}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("info", "on click");
			}
		});
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		parameters.setFocusMode("auto");
		parameters.setPictureFormat(PixelFormat.JPEG);
		Configuration cf= this.getResources().getConfiguration();
		if(cf.orientation==Configuration.ORIENTATION_LANDSCAPE){
			Log.i("info", "横屏");
			parameters.setPictureSize(800, 600);
			parameters.set("orientation", "landscape");
			parameters.setRotation(0);
		}else if(cf.orientation==Configuration.ORIENTATION_PORTRAIT){
			Log.i("info", "竖屏");
			parameters.setPictureSize(600, 800);
			parameters.set("orientation", "portrait");
			parameters.setRotation(90);
		}
		

		mCamera.setParameters(parameters);
		mCamera.startPreview();

	}

}
