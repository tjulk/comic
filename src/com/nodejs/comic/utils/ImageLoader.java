/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class ImageLoader {

	private static final String TAG = "ImageLoader";

	public static interface ImageLoaderCallback {

		void onImageLoadingStarted(ImageLoader loader);

		void onImageLoadingEnded(ImageLoader loader, Bitmap bitmap);

		void onImageLoadingFailed(ImageLoader loader, Throwable exception);
	}

	private static final int ON_START = 0x100;
	private static final int ON_FAIL = 0x101;
	private static final int ON_END = 0x102;

	private static ImageCache sImageCache;
	private static ExecutorService sExecutor;
	private static BitmapFactory.Options sDefaultOptions;
	private static AssetManager sAssetManager;

	public ImageLoader(Context context) {
		if (sImageCache == null) {
			sImageCache = Utility.getImageCache(context);
		}
		if (sExecutor == null) {
			sExecutor = Utility.getExecutor(context);
		}
		if (sDefaultOptions == null) {
			sDefaultOptions = new BitmapFactory.Options();
			sDefaultOptions.inDither = true;
			sDefaultOptions.inScaled = true;
			sDefaultOptions.inDensity = DisplayMetrics.DENSITY_MEDIUM;
			sDefaultOptions.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
		}
		sAssetManager = context.getAssets();
	}

	public Future<?> loadImage(String cacheFolder, String url, ImageLoaderCallback callback) {
		return loadImage(cacheFolder, url, callback, null);
	}

	public Future<?> loadImage(String cacheFolder, String url, ImageLoaderCallback callback,
			ImageProcessor bitmapProcessor) {
		return loadImage(cacheFolder, url, callback, bitmapProcessor, null);
	}

	public Future<?> loadImage(String cacheFolder, String url, ImageLoaderCallback callback,
			ImageProcessor bitmapProcessor, BitmapFactory.Options options) {
		return sExecutor.submit(new ImageFetcher(cacheFolder, url, callback, bitmapProcessor, options));
	}

	public void loadImageToCache(String cacheFolder, String url) {
		if (!sImageCache.contain(url)) {
			loadImage(cacheFolder, url, null);
		}
	}

	private class ImageFetcher implements Runnable {

		private String mUrl;
		private ImageHandler mHandler;
		private ImageProcessor mBitmapProcessor;
		private BitmapFactory.Options mOptions;

		public ImageFetcher(String cacheFolder, String url, ImageLoaderCallback callback,
				ImageProcessor bitmapProcessor, BitmapFactory.Options options) {
			mUrl = url;
			mHandler = new ImageHandler(cacheFolder, url, callback);
			mBitmapProcessor = bitmapProcessor;
			mOptions = options;
		}

		public void run() {

			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

			final Handler h = mHandler;
			Bitmap bitmap = null;
			Throwable throwable = null;

			h.sendMessage(Message.obtain(h, ON_START));

			try {

				if (TextUtils.isEmpty(mUrl)) {
					throw new Exception("The given URL cannot be null or empty");
				}

				InputStream inputStream = null;

				if (mUrl.startsWith(Environment.getExternalStorageDirectory().getPath())) {
					inputStream = new FileInputStream(new File(mUrl.trim()));
				} else if (mUrl.startsWith("file:///android_asset/")) {
					inputStream = sAssetManager.open(mUrl.replaceFirst("file:///android_asset/", ""));
				} else {
					inputStream = new URL(mUrl).openStream();
				}

				bitmap = BitmapFactory.decodeStream(inputStream, null, (mOptions == null) ? sDefaultOptions : mOptions);

				if (mBitmapProcessor != null && bitmap != null) {
					final Bitmap processedBitmap = mBitmapProcessor.processImage(bitmap);
					if (processedBitmap != null) {
						bitmap = processedBitmap;
					}
				}

			} catch (Exception e) {
				LogUtil.LOGE(TAG, "Error while fetching image");
				throwable = e;
			}

			if (bitmap == null) {
				if (throwable == null) {
					throwable = new Exception("Skia image decoding failed");
				}
				h.sendMessage(Message.obtain(h, ON_FAIL, throwable));
			} else {
				h.sendMessage(Message.obtain(h, ON_END, bitmap));
			}
		}
	}

	private class ImageHandler extends Handler {

		private String mUrl;
		private String mCacheFolder;
		private ImageLoaderCallback mCallback;

		private ImageHandler(String cacheFolder, String url, ImageLoaderCallback callback) {
			mUrl = url;
			mCacheFolder = cacheFolder;
			mCallback = callback;
		}

		@Override
		public void handleMessage(final Message msg) {

			switch (msg.what) {

			case ON_START:
				if (mCallback != null) {
					mCallback.onImageLoadingStarted(ImageLoader.this);
				}
				break;

			case ON_FAIL:
				if (mCallback != null) {
					mCallback.onImageLoadingFailed(ImageLoader.this, (Throwable) msg.obj);
				}
				break;

			case ON_END:

				final Bitmap bitmap = (Bitmap) msg.obj;
				sImageCache.put(mUrl, bitmap);
				if (mUrl.startsWith("http://") && bitmap != null) {
					Utility.saveBmpToSd(bitmap, mCacheFolder, mUrl);
				}

				if (mCallback != null) {
					mCallback.onImageLoadingEnded(ImageLoader.this, bitmap);
				}
				break;

			default:
				super.handleMessage(msg);
				break;
			}
		};
	}

}
