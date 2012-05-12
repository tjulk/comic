/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;

import com.nodejs.comic.models.Catalog;
import com.nodejs.comic.models.SoftwareInfo;
import com.nodejs.comic.updates.UpdateUtility;
import com.nodejs.comic.utils.ImageCache;
import com.nodejs.comic.utils.LogUtil;
import com.nodejs.comic.utils.Utility;

 
public class ComicApplication extends Application {

	private String TAG = "MarketApplication";

	public static final String BASE_UPDATE_URL = "http://app.pekall.com/market/app/isupdated?";
	// http://app.pekall.com/market/upload.jsp
	private static ComicApplication mContext;
	public MyPreference mPreference;
	public String mUpdateUrl;
	private ExecutorService mExecutorService;
	private ImageCache mImageCache;
	private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
	private HashMap<String, String> sCache;
	private Catalog mCatalog;
	private HashMap<String, ArrayList<SoftwareInfo>> mCacheSoftware;
	private HashMap<String, Integer> mCatalogState;
	private HashMap<String, onConfigChanged> mConfigs = new HashMap<String, onConfigChanged>();

	private String mModel;
	private String mCompany;
	private int mOSVersion;
	private String mLocale;
	private String mPreLocale;
	private OnCacheChangedListener mCacheChangedListener;
	public static boolean mIsDownloading = false;
	private static boolean mIsFirst = true;

	public interface OnCacheChangedListener {
		public void onCacheChange();
	}

	private void initGlobals() {
		mContext = this;
	}

	public static Context getContext() {
		return mContext;
	}

	public void notifyObservers() {
		if (mCacheChangedListener != null) {
			mCacheChangedListener.onCacheChange();
		}
	}

	public void setOnCacheChangedListener(OnCacheChangedListener cacheChangedListener) {
		mCacheChangedListener = cacheChangedListener;
	}

	public ComicApplication() {
		initGlobals();
		mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.LOGD(TAG, "=== MarketApplication create ===");
		mPreference = new MyPreference(this).getPreference();
		mUpdateUrl = BASE_UPDATE_URL + "packageName=" + getPackageName() + "&versionCode="
				+ UpdateUtility.getVersionCode(this);
		// mUpdateUrl = BASE_UPDATE_URL + "packageName=com.pekall.weather"
		// + "&versionCode=" + UpdateUtility.getVersionCode(this);
		mModel = Build.MODEL;
		if(!"xmm2231ff1_0".equals(mModel))
			mModel = "HUAWEI T8301";
		mCompany = Build.MANUFACTURER;
		mOSVersion = Build.VERSION.SDK_INT;
		mPreLocale = mLocale = Utility.getLanguage(getResources());
		if (mCacheSoftware == null) {
			mCacheSoftware = new HashMap<String, ArrayList<SoftwareInfo>>();
		}
		if (mCatalogState == null) {
			mCatalogState = new HashMap<String, Integer>();
		}
		// CrashHandler crashHandler = CrashHandler.getInstance();
		// crashHandler.init(getApplicationContext());
		// crashHandler.sendPreviousReportsToServer();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mLocale = Utility.getLanguage(getResources());
		if (!mPreLocale.equals(mLocale) && !mIsFirst && mConfigs.size() > 0) {
			request();
		}
		mIsFirst = false;
	}

	public void setConfigs(String key, onConfigChanged config) {
		if (!mConfigs.containsKey(key)) {
			mConfigs.put(key, config);
		}
	}
	
	public void clearConfigs(){
		if(mConfigs != null){
			mConfigs.clear();
		}
	}

	public void request() {
		mPreLocale = mLocale;
		Intent intent = new Intent();
		intent.setAction("ExitApp");
		this.sendBroadcast(intent);
		clearCache();
		clearConfigs();
		intent = new Intent(this, CartoonActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(intent);
	}

	public interface onConfigChanged {
		public void onRequest();
	}

	private void getInstalledSoftware() {
		ArrayList<SoftwareInfo> installedList = new ArrayList<SoftwareInfo>();
		PackageManager packageManager = getPackageManager();
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		SoftwareInfo softwareInfo;
		for (PackageInfo info : packageInfos) {
			if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				softwareInfo = new SoftwareInfo();
				String packageName = info.packageName;
				int versionCode = info.versionCode;
				String versionName = info.versionName;
				String name = (String) info.applicationInfo.loadLabel(packageManager);
				softwareInfo.setPackageName(packageName);
				softwareInfo.setVersionCode(versionCode);
				softwareInfo.setVersionName(versionName);
				softwareInfo.setName(name);
				installedList.add(softwareInfo);
			}
		}
		putSoftwareList(getInstallCatalog(), installedList);
	}

	public class MyPreference {

		private static final String IS_SHOW_DISCLAIMER = "isShowDisclaimer";
		private static final String CHECKUPDATE_STR = "isAutoCheckUpdate";
		private static final String CHECKUPDATE_PRE_TIME = "checkUpdatePreTime";
		private static final String DOWNLOAD_USE_WIFI_ONLY_STR = "isOnlyUseWIFI";
		private static final String DOWNLOAD_AUTO_UPDATE = "isAutoUpdate";
		private static final String DOWNLOAD_CHECK_INTERVAL = "checkInterval";

		public boolean mIsShowDisclaimer;
		public boolean mIsAutoCheckUpdate;
		public boolean mIsCheckUpdateInOneDay;
		public boolean mIsOnlyUseWIFI;
		public String mCheckIntrval;
		public boolean mIsAutoUpdate;

		public MyPreference preference;

		private Context context;

		public MyPreference getPreference() {
			if (preference == null) {
				preference = new MyPreference(context);
			} else {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
				preference.mIsAutoCheckUpdate = pref.getBoolean(CHECKUPDATE_STR, true);
				preference.mIsOnlyUseWIFI = pref.getBoolean(DOWNLOAD_USE_WIFI_ONLY_STR, true);
				preference.mIsAutoUpdate = pref.getBoolean(DOWNLOAD_AUTO_UPDATE, false);
				preference.mCheckIntrval = pref.getString(DOWNLOAD_CHECK_INTERVAL, "1");
				preference.mIsCheckUpdateInOneDay = isCheckUpdateInOneDay(pref);
				preference.mIsShowDisclaimer = pref.getBoolean(IS_SHOW_DISCLAIMER, true);
			}
			return preference;
		}

		private MyPreference(Context context) {
			this.context = context;
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

			mIsAutoCheckUpdate = pref.getBoolean(CHECKUPDATE_STR, true);
			mIsOnlyUseWIFI = pref.getBoolean(DOWNLOAD_USE_WIFI_ONLY_STR, true);
			mIsAutoUpdate = pref.getBoolean(DOWNLOAD_AUTO_UPDATE, false);
			mCheckIntrval = pref.getString(DOWNLOAD_CHECK_INTERVAL, "1");
			mIsCheckUpdateInOneDay = isCheckUpdateInOneDay(pref);
			mIsShowDisclaimer = pref.getBoolean(IS_SHOW_DISCLAIMER, true);
		}

		private boolean isCheckUpdateInOneDay(SharedPreferences pref) {
			boolean result = false;
			long currentTime = System.currentTimeMillis();
			if (pref.contains(CHECKUPDATE_PRE_TIME)) {
				long preTime = pref.getLong(CHECKUPDATE_PRE_TIME, 0);
				if (currentTime - 24 * 60 * 60 * 1000 <= preTime && currentTime > preTime) {
					result = true;
				}
			}
			return result;
		}

		public void setUpdateTime() {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ComicApplication.this);
			pref.edit().putLong(CHECKUPDATE_PRE_TIME, System.currentTimeMillis()).commit();

		}
	}

	public static boolean ismIsDownloading() {
		return mIsDownloading;
	}

	public static void setmIsDownloading(boolean mIsDownloading) {
		ComicApplication.mIsDownloading = mIsDownloading;
	}

	public static interface OnLowMemoryListener {
		public void onLowMemoryReceived();
	}

	private static final int CORE_POOL_SIZE = 1;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
		}
	};

	public ExecutorService getExecutor() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
		}
		return mExecutorService;
	}

	public ImageCache getImageCache() {
		if (mImageCache == null) {
			mImageCache = new ImageCache(this);
		}
		return mImageCache;
	}

	public HashMap<String, String> getDataCache() {
		if (sCache == null) {
			sCache = new HashMap<String, String>();
		}
		return sCache;
	}

	public Class<?> getHomeActivityClass() {
		return null;
	}

	public Intent getMainApplicationIntent() {
		return null;
	}

	public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(listener));
		}
	}

	public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			int i = 0;
			while (i < mLowMemoryListeners.size()) {
				final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
				if (l == null || l == listener) {
					mLowMemoryListeners.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	public void onLowMemory() {
		super.onLowMemory();
		int i = 0;
		while (i < mLowMemoryListeners.size()) {
			final OnLowMemoryListener listener = mLowMemoryListeners.get(i).get();
			if (listener == null) {
				mLowMemoryListeners.remove(i);
			} else {
				listener.onLowMemoryReceived();
				i++;
			}
		}
	}

	public void setCatalog(Catalog catalog) {
		this.mCatalog = catalog;
	}

	public Catalog getCatalog() {
		return mCatalog;
	}

	public String getmModel() {
		return mModel;
	}

	public String getmCompany() {
		return mCompany;
	}

	public int getmOSVersion() {
		return mOSVersion;
	}

	public String getmLocale() {
		return mLocale;
	}

	public synchronized void putSoftwareList(Catalog catalog, ArrayList<SoftwareInfo> softwares) {
		mCacheSoftware.put(catalog.getId(), softwares);
		notifyObservers();
	}

	public ArrayList<SoftwareInfo> getSoftwareList(Catalog catalog) {
		ArrayList<SoftwareInfo> ret = mCacheSoftware.get(catalog.getId());
		ret = ret == null ? new ArrayList<SoftwareInfo>() : ret;
		if (ret.size() <= 0 && catalog.getId().equals(getInstallCatalog().getId())) {
			getInstalledSoftware();
			ret = mCacheSoftware.get(catalog.getId());
		}
		return ret;
	}

	public synchronized void putCatalogLoadState(Catalog catalog, int mState) {
		mCatalogState.put(catalog.getId(), mState);
	}

	public int getCatalogLoadState(Catalog catalog) {
		Integer ret = mCatalogState.get(catalog.getId());
		return ret == null ? 0 : ret;
	}

	public void clearCache() {
		if (mCacheSoftware != null) {
			mCacheSoftware.clear();
		}
		if (mCatalogState != null) {
			mCatalogState.clear();
		}
	}

	public Catalog getInstallCatalog() {
		Catalog installed = new Catalog();
		installed.setId("installed");
		return installed;
	}

	public boolean isSoftwareUpdate(ArrayList<SoftwareInfo> installedList, SoftwareInfo info) {
		for (SoftwareInfo installedInfo : installedList) {
			if (installedInfo.getPackageName().equals(info.getPackageName()) && installedInfo.isHasSoftwareUpdate()) {
				return true;
			}
			if (installedInfo.getPackageName().equals(info.getPackageName())
					&& installedInfo.getVersionCode() < info.getVersionCode()) {
				return true;
			}
		}
		return false;
	}

	public boolean isSoftwareInstalled(ArrayList<SoftwareInfo> installedList, SoftwareInfo info) {
		for (SoftwareInfo installedInfo : installedList) {
			if (installedInfo.isNetSoftwareInstalled(info)) {
				return true;
			}
		}
		return false;
	}

}
