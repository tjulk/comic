package com.nodejs.comic.models;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;

import com.nodejs.comic.handler.DownLoadHandler;
import com.nodejs.comic.handler.ComicContent.DownLoadInfo;
import com.nodejs.comic.handler.ComicContent.DownLoadInfoColumn;

public class DownLoadInfoStructure {

	public static final String MARKET_MAIN_DIR = "/sdcard/PekallMarket/";
	public static final int DOWNLOAD_STATE_NOT_BEGIN = -1;
	public static final int DOWNLOAD_STATE_WAIT = 0;
	public static final int DOWNLOAD_STATE_PAUSE = 1;
	public static final int DOWNLOAD_STATE_DOWNING = 2;
	public static final int DOWNLOAD_STATE_COMPLETED = 3;

	public String name;

	public String key;

	public String packageName;

	public String storageDir;

	public long curSize;

	public long wholeSize;

	public String lastModifiedDate;

	public String icon;

	private int downLoadState;

	private DownLoadHandler mDownLoadHandler;

	public synchronized void setDownLoadState(int state) {
		this.downLoadState = state;
		if (mDownLoadHandler != null) {
			if (this.downLoadState == DOWNLOAD_STATE_COMPLETED) {
				mDownLoadHandler.addItem(this);
			}
			mDownLoadHandler.infoSoftByState();
		}
	}

	public synchronized int getDownLoadState() {
		return downLoadState;
	}

	public DownLoadInfoStructure(Cursor c) {
		this.key = c.getString(DownLoadInfo.CONTENT_ITEM_KEY_COLUMN);
		this.packageName = c.getString(DownLoadInfo.CONTENT_ITEM_PACKAGENAME_COLUMN);
		this.curSize = c.getInt(DownLoadInfo.CONTENT_ITEM_CUR_SIZE_COLUMN);
		this.wholeSize = c.getInt(DownLoadInfo.CONTENT_ITEM_WHOLE_SIZE_COLUMN);
		this.name = c.getString(DownLoadInfo.CONTENT_ITEM_NAME_COLUMN);
		this.storageDir = MARKET_MAIN_DIR;
		this.lastModifiedDate = c.getString(DownLoadInfo.CONTENT_ITEM_LASTMODIFIEDDATE_COLUMN);
		this.icon = c.getString(DownLoadInfo.CONTENT_ITEM_ICON_COLUMN);
		if (curSize == wholeSize) {
			setDownLoadState(DOWNLOAD_STATE_COMPLETED);
		} else {
			setDownLoadState(DOWNLOAD_STATE_PAUSE);
		}
	}

	public DownLoadInfoStructure(Cursor c, Context ctx) {
		mDownLoadHandler = DownLoadHandler.getDownLoadHander(ctx);
		this.key = c.getString(DownLoadInfo.CONTENT_ITEM_KEY_COLUMN);
		this.packageName = c.getString(DownLoadInfo.CONTENT_ITEM_PACKAGENAME_COLUMN);
		this.curSize = c.getInt(DownLoadInfo.CONTENT_ITEM_CUR_SIZE_COLUMN);
		this.wholeSize = c.getInt(DownLoadInfo.CONTENT_ITEM_WHOLE_SIZE_COLUMN);
		this.name = c.getString(DownLoadInfo.CONTENT_ITEM_NAME_COLUMN);
		this.storageDir = MARKET_MAIN_DIR;
		this.lastModifiedDate = c.getString(DownLoadInfo.CONTENT_ITEM_LASTMODIFIEDDATE_COLUMN);
		this.icon = c.getString(DownLoadInfo.CONTENT_ITEM_ICON_COLUMN);
		if (curSize == wholeSize) {
			setDownLoadState(DOWNLOAD_STATE_COMPLETED);
		} else {
			setDownLoadState(DOWNLOAD_STATE_PAUSE);
		}
	}

	public DownLoadInfoStructure(Context ctx, String key, String packageName, String iconUrl, int curSize,
			int wholeSize, String name, int downloadState) {
		mDownLoadHandler = DownLoadHandler.getDownLoadHander(ctx);
		this.key = key;
		this.name = name;
		this.packageName = packageName;
		this.storageDir = MARKET_MAIN_DIR;
		this.curSize = curSize;
		this.wholeSize = wholeSize;
		this.icon = iconUrl;
		setDownLoadState(downloadState);
	}

	public DownLoadInfoStructure(Context ctx, SoftwareInfo info, int downloadState) {
		mDownLoadHandler = DownLoadHandler.getDownLoadHander(ctx);
		this.key = info.getApkUrl();
		this.name = info.getName();
		this.packageName = info.getPackageName();
		this.storageDir = MARKET_MAIN_DIR;
		this.curSize = 0;
		this.wholeSize = 0;
		this.icon = info.getIcon();
		setDownLoadState(downloadState);
	}

	public DownLoadInfoStructure(Parcel p) {

	}

	@Override
	public String toString() {
		return "storageDir ==== > " + storageDir + "  name ====> " + key;
	}

	public static int getDownLoadState(Activity ctx, String itemKey) {
		for (DownLoadInfoStructure info : DownLoadHandler.getDownLoadHander(ctx).getAllInfo()) {
			if (info.key.equals(itemKey) && info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING) {
				return DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING;
			}
		}

		// File f = new File(DownLoadInfoStructure.TRAVEL_SUBWAY_APK_PATH);
		// if(!f.exists() || !f.isFile()){
		// int num = ctx.getContentResolver().delete(DownLoadInfo.CONTENT_URI,
		// DownLoadInfoColumn.ITEM_KEY + " = ?",
		// new String[] { itemKey });
		// LogUtil.LOGD(TAG, "delete number in main menu ====== > " + num);
		// return DownLoadInfoStructure.DOWNLOAD_STATE_NOT_BEGIN;
		// }

		Cursor c = ctx.managedQuery(DownLoadInfo.CONTENT_URI, null, DownLoadInfoColumn.ITEM_KEY + " = ?",
				new String[] { itemKey }, null);
		try {
			if (c == null || c.getCount() <= 0) {
				return DownLoadInfoStructure.DOWNLOAD_STATE_NOT_BEGIN;
			} else {
				c.moveToFirst();
				int curSize = c.getInt(DownLoadInfo.CONTENT_ITEM_CUR_SIZE_COLUMN);
				int wholeSize = c.getInt(DownLoadInfo.CONTENT_ITEM_WHOLE_SIZE_COLUMN);
				if (curSize == wholeSize) {
					return DownLoadInfoStructure.DOWNLOAD_STATE_COMPLETED;
				} else {
					return DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE;
				}
			}
		} finally {
			if (c != null)
				c.close();
		}

	}

	private String byteFormat(long dataByte) {
		DecimalFormat df = new DecimalFormat("###.##");
		float f;
		if (dataByte < 1024 * 1024) {
			f = (float) ((float) dataByte / (float) 1024);
			return (df.format(new Float(f).doubleValue()) + " KB");
		} else {
			f = (float) ((float) dataByte / (float) (1024 * 1024));
			return (df.format(new Float(f).doubleValue()) + " MB");
		}
	}

	public String getLoadingStr() {
		return byteFormat(curSize) + "/" + byteFormat(wholeSize);
	}

	public String getPercentByStr() {
		DecimalFormat df = new DecimalFormat("0.00");
		if (wholeSize == 0) {
			wholeSize = -1;
		}
		float percent = 100 * curSize / (float) wholeSize;
		if (percent <= 0) {
			return "0";
		} else {
			return df.format(percent);
		}
	}

	public int getPercent() {
		if (wholeSize == 0) {
			wholeSize = -1;
		}
		int percent = (int) (100 * curSize / wholeSize);
		if (percent <= 0) {
			return 0;
		} else {
			return percent;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownLoadInfoStructure other = (DownLoadInfoStructure) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
