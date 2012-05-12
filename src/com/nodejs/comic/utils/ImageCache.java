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
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nodejs.comic.ComicApplication.OnLowMemoryListener;

public class ImageCache implements OnLowMemoryListener {

	private final HashMap<String, SoftReference<Bitmap>> mSoftCache;

	public ImageCache(Context context) {
		mSoftCache = new HashMap<String, SoftReference<Bitmap>>();
		Utility.getComicApplication(context).registerOnLowMemoryListener(this);
	}

	public static ImageCache from(Context context) {
		return Utility.getImageCache(context);
	}

	public Bitmap get(String url) {
		final SoftReference<Bitmap> ref = mSoftCache.get(url);
		if (ref == null) {
			if (url.startsWith(Utility.LOCAL_CACHE_PATH)) {
				return getDiskBitmap(url);
			}
			return null;
		}

		final Bitmap bitmap = ref.get();
		if (bitmap == null) {
			mSoftCache.remove(url);
		}

		return bitmap;
	}

	private Bitmap getDiskBitmap(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
		}

		return bitmap;
	}

	public void put(String url, Bitmap bitmap) {
		mSoftCache.put(url, new SoftReference<Bitmap>(bitmap));
	}

	public void flush() {
		mSoftCache.clear();
	}

	public boolean contain(String url) {
		final SoftReference<Bitmap> ref = mSoftCache.get(url);
		if (ref == null) {
			return false;
		}
		return ref.get() != null;
	}

	public void onLowMemoryReceived() {
		flush();
	}
}
