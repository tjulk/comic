/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.nodejs.comic.R;
import com.nodejs.comic.utils.ImageRequest.ImageRequestCallback;

public class AsyncImageView extends ImageView implements ImageRequestCallback {


	public static interface OnImageViewLoadListener {

		void onLoadingStarted(AsyncImageView imageView);

		void onLoadingEnded(AsyncImageView imageView, Bitmap image);

		void onLoadingFailed(AsyncImageView imageView, Throwable throwable);
	}

	private static final int IMAGE_SOURCE_UNKNOWN = -1;
	private static final int IMAGE_SOURCE_RESOURCE = 0;
	private static final int IMAGE_SOURCE_DRAWABLE = 1;
	private static final int IMAGE_SOURCE_BITMAP = 2;

	private int mImageSource;
	private Bitmap mDefaultBitmap;
	private Drawable mDefaultDrawable;
	private int mDefaultResId;
	private ScaleType mLoadScaleType;
	private ScaleType mDefScaleType;

	private String mUrl;
	private String mCacheFolder;
	private ImageRequest mRequest;
	private boolean mPaused;

	private Bitmap mBitmap;
	private OnImageViewLoadListener mOnImageViewLoadListener;
	private ImageProcessor mImageProcessor;
	private BitmapFactory.Options mOptions;
	
	private AlphaAnimation mAlphaAnimation;

	public AsyncImageView(Context context) {
		this(context, null);
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDefScaleType = getScaleType();
		initializeDefaultValues();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView, defStyle, 0);

		Drawable d = a.getDrawable(R.styleable.AsyncImageView_defaultSrc);
		if (d != null) {
			setDefaultImageDrawable(d);
		}

		final int inDensity = a.getInt(R.styleable.AsyncImageView_inDensity, -1);
		if (inDensity != -1) {
			setInDensity(inDensity);
		}
		int index = a.getInt(R.styleable.AsyncImageView_loadScaleType, -1);
        if (index >= 0) {
        	mLoadScaleType = getLoadScaleType(index);
        }
		// setUrlImage(a.getString(R.styleable.AsyncImageView_url));

		a.recycle();
		
		mAlphaAnimation = new AlphaAnimation(0, 1);
		mAlphaAnimation.setFillAfter(true);
		mAlphaAnimation.setDuration(500);
		setAnimation(mAlphaAnimation);
	}

	private void initializeDefaultValues() {
		mImageSource = IMAGE_SOURCE_UNKNOWN;
		mPaused = false;
	}

	public boolean isLoading() {
		return mRequest != null;
	}

	public boolean isLoaded() {
		return mRequest == null && mBitmap != null;
	}

	public void setPaused(boolean paused) {
		if (mPaused != paused) {
			mPaused = paused;
			if (!paused) {
				reload();
			}
		}
	}

	public void setInDensity(int inDensity) {
		if (mOptions == null) {
			mOptions = new BitmapFactory.Options();
			mOptions.inDither = true;
			mOptions.inScaled = true;
			mOptions.inTargetDensity = getContext().getResources().getDisplayMetrics().densityDpi;
		}

		mOptions.inDensity = inDensity;
	}

	public void setOptions(BitmapFactory.Options options) {
		mOptions = options;
	}

	public void reload() {
		reload(false);
	}

	public void reload(boolean force) {
		if (mRequest == null && !TextUtils.isEmpty(mUrl)) {

			// Prior downloading the image ... let's look in a cache !
			// TODO cyril: This is a synchronous call ... make it asynchronous
			mBitmap = null;
			if (!force) {
				mBitmap = Utility.getImageCache(getContext()).get(mUrl);
			}

			if (mBitmap != null) {
				setImageBitmap(mBitmap);
				return;
			}
			setDefaultImage();
			mRequest = new ImageRequest(mCacheFolder, mUrl, this, mImageProcessor, mOptions);
			mRequest.load(getContext());
		}
	}

	public void stopLoading() {
		if (mRequest != null) {
			mRequest.cancel();
			mRequest = null;
		}
		setImageResource(R.drawable.ic_launcher);
	}

	public void setOnImageViewLoadListener(OnImageViewLoadListener listener) {
		mOnImageViewLoadListener = listener;
	}

	public void setUrlImage(String cacheFolder, String url) {
		if (TextUtils.isEmpty(url)) {
			setDefaultImage();
			return;
		}

		if (mBitmap != null && url != null && url.equals(mUrl)) {
			return;
		}
		
		stopLoading();
		String tempUrl = Utility.isExistCache(cacheFolder, url);
		boolean isWiFiActive = Tools.isWiFiActive(getContext());
		if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isLoadImageWithoutWIFI", true) && tempUrl == null && !isWiFiActive) {
			setDefaultImage();
		} else {
			url = tempUrl == null ? url : tempUrl;
			mUrl = url;
			mCacheFolder = cacheFolder;
			if (TextUtils.isEmpty(mUrl)) {
				mBitmap = null;
				setDefaultImage();
			} else {
				if (!mPaused) {
					reload();
				} else {
					mBitmap = Utility.getImageCache(getContext()).get(mUrl);
					if (mBitmap != null) {
						setImageBitmap(mBitmap);
						return;
					} else {
						setDefaultImage();
					}
				}
			}
		}
	}

	public void setUrlImage(String url) {
		setUrlImage(null, url);
	}

	public void setDefaultImageBitmap(Bitmap bitmap) {
		if (mDefScaleType != null) {
			setScaleType(mDefScaleType);
		}
		mImageSource = IMAGE_SOURCE_BITMAP;
		mDefaultBitmap = bitmap;
		setDefaultImage();
	}

	public void setDefaultImageDrawable(Drawable drawable) {
		if (mDefScaleType != null) {
			setScaleType(mDefScaleType);
		}
		mImageSource = IMAGE_SOURCE_DRAWABLE;
		mDefaultDrawable = drawable;
		setDefaultImage();
	}

	public void setDefaultImageResource(int resId) {
		if (mDefScaleType != null) {
			setScaleType(mDefScaleType);
		}
		mImageSource = IMAGE_SOURCE_RESOURCE;
		mDefaultResId = resId;
		setDefaultImage();
	}

	public void setImageProcessor(ImageProcessor imageProcessor) {
		mImageProcessor = imageProcessor;
	}

	private void setDefaultImage() {
		if (mBitmap == null) {
			switch (mImageSource) {
			case IMAGE_SOURCE_BITMAP:
				setImageBitmap(mDefaultBitmap);
				break;
			case IMAGE_SOURCE_DRAWABLE:
				setImageDrawable(mDefaultDrawable);
				break;
			case IMAGE_SOURCE_RESOURCE:
				setImageResource(mDefaultResId);
				break;
			default:
				setImageDrawable(null);
				break;
			}
		}
	}
	
	public void setLoadScleType(ScaleType scaleType) {
		this.mLoadScaleType = scaleType;
	}
	
	private ScaleType getLoadScaleType(int index) {
		switch(index) {
		case 0:
			return ScaleType.MATRIX;
		case 1:
			return ScaleType.FIT_XY;
		case 2:
			return ScaleType.FIT_START;
		case 3:
			return ScaleType.FIT_CENTER;
		case 4:
			return ScaleType.FIT_END;
		case 5:
			return ScaleType.CENTER;
		case 6:
			return ScaleType.CENTER_CROP;
		case 7:
			return ScaleType.CENTER_INSIDE;
		}
		return null;
	}

	static class SavedState extends BaseSavedState {
		String url;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			url = in.readString();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(url);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	public void onImageRequestStarted(ImageRequest request) {
		if (mLoadScaleType != null) {
			setScaleType(mLoadScaleType);
		}
		if (mOnImageViewLoadListener != null) {
			mOnImageViewLoadListener.onLoadingStarted(this);
		}
	}

	public void onImageRequestFailed(ImageRequest request, Throwable throwable) {
		mRequest = null;
		if (mLoadScaleType != null) {
			setScaleType(mLoadScaleType);
		}
		setImageResource(R.drawable.load_error_preview);
		if (mOnImageViewLoadListener != null) {
			mOnImageViewLoadListener.onLoadingFailed(this, throwable);
		}
	}

	public void onImageRequestEnded(ImageRequest request, Bitmap image) {

		if (mDefScaleType != null) {
			setScaleType(mDefScaleType);
		}
		mBitmap = image;
		setImageBitmap(image);
		mAlphaAnimation.start();
		if (mOnImageViewLoadListener != null) {
			mOnImageViewLoadListener.onLoadingEnded(this, image);
		}
		mRequest = null;
	}

	public void onImageRequestCancelled(ImageRequest request) {
		mRequest = null;
		if (mLoadScaleType != null) {
			setScaleType(mLoadScaleType);
		}
		setImageResource(R.drawable.load_error_preview);
		if (mOnImageViewLoadListener != null) {
			mOnImageViewLoadListener.onLoadingFailed(this, null);
		}
	}
}
