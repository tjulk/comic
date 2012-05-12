/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import java.util.concurrent.Future;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nodejs.comic.utils.ImageLoader.ImageLoaderCallback;

public class ImageRequest {

	public static interface ImageRequestCallback {

		void onImageRequestStarted(ImageRequest request);

		void onImageRequestFailed(ImageRequest request, Throwable throwable);

		void onImageRequestEnded(ImageRequest request, Bitmap image);

		void onImageRequestCancelled(ImageRequest request);
	}

	private static ImageLoader sImageLoader;

	private Future<?> mFuture;
	private String mUrl;
	private String mCacheFolder;
	private ImageRequestCallback mCallback;
	private ImageProcessor mBitmapProcessor;
	private BitmapFactory.Options mOptions;

	public ImageRequest(String cacheFolder, String url, ImageRequestCallback callback) {
		this(null, url, callback, null);
	}

	public ImageRequest(String cacheFolder, String url, ImageRequestCallback callback, ImageProcessor bitmapProcessor) {
		this(null, url, callback, bitmapProcessor, null);
	}

	public ImageRequest(String cacheFolder, String url, ImageRequestCallback callback, ImageProcessor bitmapProcessor,
			BitmapFactory.Options options) {
		mUrl = url;
		mCacheFolder = cacheFolder;
		mCallback = callback;
		mBitmapProcessor = bitmapProcessor;
		mOptions = options;
	}

	public void setImageRequestCallback(ImageRequestCallback callback) {
		mCallback = callback;
	}

	public String getUrl() {
		return mUrl;
	}

	public void load(Context context) {
		if (mFuture == null) {
			if (sImageLoader == null) {
				sImageLoader = new ImageLoader(context);
			}
			mFuture = sImageLoader.loadImage(mCacheFolder, mUrl, new InnerCallback(), mBitmapProcessor, mOptions);
		}
	}

	public void cancel() {
		if (!isCancelled()) {
			mFuture.cancel(false);
			if (mCallback != null) {
				mCallback.onImageRequestCancelled(this);
			}
		}
	}

	public final boolean isCancelled() {
		return mFuture.isCancelled();
	}

	private class InnerCallback implements ImageLoaderCallback {

		public void onImageLoadingStarted(ImageLoader loader) {
			if (mCallback != null) {
				mCallback.onImageRequestStarted(ImageRequest.this);
			}
		}

		public void onImageLoadingEnded(ImageLoader loader, Bitmap bitmap) {
			if (mCallback != null && !isCancelled()) {
				mCallback.onImageRequestEnded(ImageRequest.this, bitmap);
			}
			mFuture = null;
		}

		public void onImageLoadingFailed(ImageLoader loader, Throwable exception) {
			if (mCallback != null && !isCancelled()) {
				mCallback.onImageRequestFailed(ImageRequest.this, exception);
			}
			mFuture = null;
		}
	}

}
