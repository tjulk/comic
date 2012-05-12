/* --------------------------------------------------------------------------------------------- 
 * 
 * Capital Alliance Software Confidential Proprietary 
 * (c) Copyright CAS 201{x}, All Rights Reserved 
 * 
 * ---------------------------------------------------------------------------------------------- 
 */

package com.nodejs.comic.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import com.nodejs.comic.MainActivity;
import com.nodejs.comic.R;
import com.nodejs.comic.handler.DownLoadHandler;
import com.nodejs.comic.handler.ComicContent.DownLoadInfo;
import com.nodejs.comic.handler.ComicContent.DownLoadInfoColumn;
import com.nodejs.comic.handler.ComicContent.DownloadStatusInfo;
import com.nodejs.comic.models.DownLoadInfoStructure;
import com.nodejs.comic.utils.BufferedRandomAccessFile;
import com.nodejs.comic.utils.LogUtil;
import com.nodejs.comic.utils.TipUtil;
import com.nodejs.comic.utils.Tools;
import com.nodejs.comic.utils.Utility;

public class DownLoadService extends Service {
	protected static final String TAG = "DownLoadService ";

	private static final int MSG_GET_FILE_INFO_SUCCESS = 0;
	private static final int MSG_GET_FILE_INFO_FAILED = 1;
	private static final int MSG_REFRESH_DOWNLOAD_LIST = 2;
	private static final int MSG_REFRESH_NOTIFICATION = 3;

	private static final int BUFFERED_SIZE = 1024 * 5;

	private static final int TIME_REFRESHNOTIFY = 4000; // ms

	public static final String ITEM_KEY = "itemKey";
	public static final String ITEM_PACKAGENAME = "itemPackageName";
	public static final String ITEM_NAME = "itemName";

	public static final int INFO_IS_NEW = 1;
	public static final int INFO_NOT_NEW = -1;

	public static final int NOTIFICATION_ID = 4231221;

	public static final int THREADCOUNT = 2;

	public static final Object mLock = new Object();
	// Thread pool

	private ExecutorService mExecutorService = Executors.newFixedThreadPool(THREADCOUNT);

	private String mItemKey;
	private String mItemPackageName;
	private String mItemName;

	private ContentResolver mContentResolver;
	private DownLoadHandler mDownLoadHandler;
	// notification
	private NotificationManager mNfManager;
	private Notification mNotification;
	private Intent mNfIntent;
	private int mPreState = -100;

	private static ArrayList<DownLoadInfoStructure> datas = new ArrayList<DownLoadInfoStructure>();
	private static boolean isRefreshNotify = true;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_GET_FILE_INFO_SUCCESS:
				break;
			case MSG_GET_FILE_INFO_FAILED:
				break;
			case MSG_REFRESH_DOWNLOAD_LIST:
				mDownLoadHandler.notifyObservers();
				break;
			case MSG_REFRESH_NOTIFICATION:
				refreshAllNotifycation();
				break;
			}
		}

	};

	private void refreshAllNotifycation() {

		new TimeThread().start();
		if (datas.size() == 0) {
			stopNotification();
		} else {
			if (isRefreshNotify) {
				updateNotification();
			}
		}

	};

	class TimeThread extends Thread {
		public void run() {
			isRefreshNotify = false;
			try {
				Thread.sleep(TIME_REFRESHNOTIFY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			isRefreshNotify = true;
		}
	};

	public static void startDownLoadService(Context ctx, String itemKey, String itemName, String packageName) {
		Intent i = new Intent(ctx, DownLoadService.class);
		i.putExtra(DownLoadService.ITEM_KEY, itemKey);
		if (itemName != null) {
			i.putExtra(DownLoadService.ITEM_NAME, itemName);
		}
		if (packageName != null) {
			i.putExtra(DownLoadService.ITEM_PACKAGENAME, packageName);
		}
		ctx.startService(i);
	}

	public static void stopDownLoadService(Context ctx) {
		Intent i = new Intent(ctx, DownLoadService.class);
		ctx.stopService(i);
	}

	public void onCreate() {
		super.onCreate();
		mContentResolver = getContentResolver();
		mDownLoadHandler = DownLoadHandler.getDownLoadHander(this);
		initNotification();
	}

	private void initNotification() {
		mNfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNfManager.cancelAll();
		mNotification = new Notification();
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotification.icon = android.R.drawable.stat_sys_download;
		mNfIntent = new Intent(getApplicationContext(), MainActivity.class);
		mNfIntent.putExtra("index", 1);
		mNotification.setLatestEventInfo(this, "", getString(R.string.download_completed_percent, 0) + "%",
				PendingIntent.getActivity(getApplicationContext(), 0, mNfIntent, PendingIntent.FLAG_ONE_SHOT));
	}

	private void updateNotification() {
		float molecular = 0f;
		float denominator = 0f;

		String title = "";
		int size = datas.size();
		for (int i = 0; i < size; i++) {
			DownLoadInfoStructure info = datas.get(i);
			if (info != null && info.wholeSize > 0) {
				title += info.name + ",";
				molecular += info.curSize;
				denominator += info.wholeSize;
			}
		}
		int percent = 0;
		if (denominator != 0) {
			percent = (int) (molecular * 100 / denominator);
		}
		if (title.length() > 0) {
			title = title.substring(0, title.length() - 1);
		}
		mNotification.setLatestEventInfo(this, title, getString(R.string.download_completed_percent, percent) + "%",
				PendingIntent.getActivity(getApplicationContext(), 0, mNfIntent, PendingIntent.FLAG_ONE_SHOT));
		mNfManager.notify(NOTIFICATION_ID, mNotification);
	}

	private void stopNotification() {
		System.out.println("==========datas.size()= " + datas.size() + " =======datas[0] =  datas[1]");
		if (mNfManager != null && datas.size() <= 0) {
			mNfManager.cancel(NOTIFICATION_ID);
		}
	}

	private int getNotificationIndex(String packageName) {
		int size = datas.size();
		for (int i = 0; i < size; i++) {
			if (packageName != null && packageName.equals(datas.get(i).packageName)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.LOGD(TAG, "download service  ");
		if(!Utility.sdcardStatus()){
			TipUtil.showMessageByShort(R.string.error_sdcard_no);
			return 0;
		}
		mItemKey = intent.getStringExtra(ITEM_KEY);
		mItemPackageName = intent.getStringExtra(ITEM_PACKAGENAME);
		mItemName = intent.getStringExtra(ITEM_NAME);
		if (mItemKey == null) {
			return 0;
		} else {
			for (DownLoadInfoStructure info : mDownLoadHandler.getAllInfo()) {
				if (mItemKey.equals(info.key)) {
					LogUtil.LOGD(TAG, "The handler has this info alreaday. ");
					if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE) {
						LogUtil.LOGD(TAG, "The handler has this info alreaday. and pause ");
						info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_WAIT);
						// new DownTask().execute(info);
						startDownloadTask(info);
						return 0;
					} else if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_NOT_BEGIN) {
						LogUtil.LOGD(TAG, "The handler has this info alreaday.-------and begin---");
						info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_WAIT);
						// new DownTask().execute(info);
						startDownloadTask(info);
						return 0;
					} else {
						return 0;
					}
				}
			}
		}

		Cursor c = mContentResolver.query(DownLoadInfo.CONTENT_URI, null, DownLoadInfoColumn.ITEM_KEY + " = ?",
				new String[] { mItemKey }, null);
		if (c == null || c.getCount() <= 0) {
			LogUtil.LOGD(TAG, "The handler has not this info.-----This info is not in the db. ");
			DownLoadInfoStructure info = new DownLoadInfoStructure(this, mItemKey, mItemPackageName, null, 0, -1,
					mItemName, DownLoadInfoStructure.DOWNLOAD_STATE_WAIT);
			mDownLoadHandler.addItem(info);
			startDownloadTask(info);
			// new DownTask().execute(info);
		} else {
			LogUtil.LOGD(TAG, "The handler has not this info.-----This info is in the db. ");
			c.moveToFirst();
			int curSize = c.getInt(DownLoadInfo.CONTENT_ITEM_CUR_SIZE_COLUMN);
			if (curSize <= 0) {
				curSize = 0;
			}
			DownLoadInfoStructure info = new DownLoadInfoStructure(c, this);
			mDownLoadHandler.addItem(info);
			startDownloadTask(info);
			// new DownTask().execute(info);
		}
		c.close();
		return 0;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		for (DownLoadInfoStructure info : mDownLoadHandler.getAllInfo()) {
			if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING
					|| info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_WAIT) {
				info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
			}
		}
	}

	private void bindNotify(int index, DownLoadInfoStructure info) {
		if (index != -1) {
			datas.remove(index);
			datas.add(index, info);
		} else {
			if (datas.size() < 2) {
				datas.add(info);
			}
		}
	}

	private void unBindNotify(int index) {
		if (index != -1) {
			datas.remove(index);
			stopNotification();
		}
	}

	private boolean nIsNew = false;

	private void startDownloadTask(final DownLoadInfoStructure info) {
		nIsNew = false;
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_WAIT) {
					info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING);
				} else if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE) {
					mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
					mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
					mHandler.sendEmptyMessage(MSG_REFRESH_NOTIFICATION);
					return;
				}
				mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
				mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
				Cursor c = mContentResolver.query(DownLoadInfo.CONTENT_URI, null, DownLoadInfoColumn.ITEM_KEY + " = ?",
						new String[] { info.key }, null);
				nIsNew = c.getCount() <= 0;
				c.close();
				File directory = new File(info.storageDir);
				if (!directory.exists() || !directory.isDirectory()) {
					directory.mkdirs();
				}
				final File file = new File(mDownLoadHandler.getFilePath(info));
				BufferedRandomAccessFile randfile = null;
				try {
					if (!file.exists()) {
						info.curSize = 0;
						file.createNewFile();
					} else if (info.curSize != file.length()) {
						info.curSize = 0;
						file.delete();
						file.createNewFile();
					}
					randfile = new BufferedRandomAccessFile(file, "rw", BUFFERED_SIZE);
					randfile.seek(info.curSize);
					String restartKey = "";
					if (info.curSize != 0)
						restartKey = "&action=restart";
					HttpURLConnection conn = openConnection(info, restartKey);
					String serverLastModifiedDate = conn.getHeaderField("Last-Modified");
					if (info.wholeSize > 0 && info.lastModifiedDate != null && serverLastModifiedDate != null) {
						Date localMoifiedDate = new Date(info.lastModifiedDate);
						Date serverMoifiedDate = new Date(serverLastModifiedDate);
						if (localMoifiedDate.before(serverMoifiedDate)) {
							info.lastModifiedDate = serverLastModifiedDate;
							info.curSize = 0;
							file.delete();
							file.createNewFile();
							conn.disconnect();
							randfile = new BufferedRandomAccessFile(file, "rw", BUFFERED_SIZE);
							randfile.seek(info.curSize);
							conn = openConnection(info, restartKey);
							info.wholeSize = conn.getContentLength();
							mHandler.post(new Runnable() {
								public void run() {
									TipUtil.showMessageByShort(R.string.error_file_not_compare);
								}
							});
						}
					} else {
						info.wholeSize = conn.getContentLength();// 获取文件大小
						info.lastModifiedDate = serverLastModifiedDate;
					}
					if (info.wholeSize <= 0) {
						throw new Exception("Can't get file size!");
					} else {
						if (!Tools.SDCardHaveEnoughSpace(info.wholeSize - info.curSize)) {
							mHandler.post(new Runnable() {
								public void run() {
									info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
									unBindNotify(getNotificationIndex(info.packageName));
									TipUtil.showMessageByShort(R.string.error_sdcard_not_haveEnoughSpace);
									mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
									mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
								}
							});
							return;
						}
					}
					InputStream input = conn.getInputStream();
					byte[] buffer = new byte[BUFFERED_SIZE];
					int len = -1;
					if (info.wholeSize == info.curSize) {
						randfile.close();
						conn.disconnect();
					}
					while (true) {
						if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE) {
							unBindNotify(getNotificationIndex(info.packageName));
							storeInfo(info);
							randfile.close();
							conn.disconnect();
							mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
							mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
							return;
						}
						if ((len = input.read(buffer)) == -1) {
							break;
						} else {
							bindNotify(getNotificationIndex(info.packageName), info);
							mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
							mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
							mHandler.sendEmptyMessage(MSG_REFRESH_NOTIFICATION);
							randfile.write(buffer, 0, len);
							info.curSize = file.length();
						}
					}
					if (info.curSize == info.wholeSize) {
						PackageManager packageManager = getPackageManager();
						PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(),
								PackageManager.GET_ACTIVITIES);
						if (packageInfo == null) {
							mHandler.post(new Runnable() {
								public void run() {
									info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
									file.delete();
									deleteInfo(info);
								}
							});
						} else {
							String packageName = packageInfo.packageName;
							initSoftwareStatus(packageName);
							mHandler.post(new Runnable() {
								public void run() {
									info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_COMPLETED);
									unBindNotify(getNotificationIndex(info.packageName));
									installApk(info);
								}

							});

						}
						mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
						mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
						mHandler.sendEmptyMessage(MSG_REFRESH_NOTIFICATION);
					}
					storeInfo(info);
					randfile.close();
					conn.disconnect();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
					unBindNotify(getNotificationIndex(info.packageName));
					mHandler.post(new Runnable() {
						public void run() {
							TipUtil.showMessageByShort(R.string.error_time_out);
							mHandler.removeMessages(MSG_REFRESH_DOWNLOAD_LIST);
							mHandler.sendEmptyMessage(MSG_REFRESH_DOWNLOAD_LIST);
						}
					});
					if (info.curSize <= 0) {
						info.curSize = file.length();
					}
					storeInfo(info);
					try {
						if (randfile != null)
							randfile.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		});
	}

	private void installApk(DownLoadInfoStructure info) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.fromFile(new File(mDownLoadHandler.getFilePath(info))), MimeTypeMap.getSingleton()
				.getMimeTypeFromExtension("apk"));
		startActivity(i);
	}

	private void deleteInfo(DownLoadInfoStructure info) {
		mContentResolver.delete(DownLoadInfo.CONTENT_URI, DownLoadInfoColumn.ITEM_KEY + " = ?",
				new String[] { info.key });
	}

	private HttpURLConnection openConnection(DownLoadInfoStructure info, String restartKey)
			throws MalformedURLException, IOException {
		System.out.println("==============url = " + info.key + restartKey);
		HttpURLConnection conn = (HttpURLConnection) new URL(info.key + restartKey).openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", "Internet Explorer");
		conn.setRequestProperty("Range", "bytes=" + info.curSize + "-");
		conn.setConnectTimeout(Tools.READ_TIME_OUT);
		conn.setReadTimeout(Tools.READ_TIME_OUT);
		return conn;
	}

	private void initSoftwareStatus(String packageName) {
		packageName = "package:" + packageName;
		ContentValues values = DownloadStatusInfo.toContentValues(packageName);
		mContentResolver.insert(DownloadStatusInfo.CONTENT_URI, values);
	}

	private void storeInfo(DownLoadInfoStructure info) {
		ContentValues values = DownLoadInfo.toContentValues(info);
		if (nIsNew) {
			mContentResolver.insert(DownLoadInfo.CONTENT_URI, values);
		} else {
			mContentResolver.update(DownLoadInfo.CONTENT_URI, values, DownLoadInfoColumn.ITEM_KEY + " = ?",
					new String[] { info.key });
		}
	}

}
