package com.nodejs.comic.handler;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.text.TextUtils;

import com.nodejs.comic.R;
import com.nodejs.comic.handler.ComicContent.DownLoadInfo;
import com.nodejs.comic.handler.ComicContent.DownLoadInfoColumn;
import com.nodejs.comic.models.DownLoadInfoStructure;
import com.nodejs.comic.services.DownLoadService;

public class DownLoadHandler {
	private static DownLoadHandler mInstance;
	private ArrayList<DownLoadInfoStructure> mDownloadInfo = new ArrayList<DownLoadInfoStructure>();
	private ArrayList<DownLoadInfoStructure> mCompletedInfo = new ArrayList<DownLoadInfoStructure>();
	private OnInfoChangedListener mOnInfoChanged;
	private ProgressDialog mProgressDialog;
	public static boolean mFlag = true;

	public static final int OPERATE_STARTALLDOWNLOADS = 0;
	public static final int OPERATE_PAUSEALLDOWNLOADS = 1;
	public static final int OPERATE_CLEARALLDOWNLOADING = 2;
	public static final int OPERATE_DELETEALLCOMPLETED = 3;

	public interface OnInfoChangedListener {
		public void onChanged();
	}

	public DownLoadHandler(Context ctx) {
	}

	public String getFilePath(DownLoadInfoStructure info) {
		if (info == null) {
			return null;
		}
		String url = info.key;
		String path = info.storageDir + info.packageName + info.key.substring(url.indexOf("=") + 1, url.length());
		return path;
	}

	public synchronized static DownLoadHandler getDownLoadHander(Context ctx) {
		if (mInstance == null) {
			mInstance = new DownLoadHandler(ctx);
		}
		return mInstance;
	}

	public void setOnInfoChanged(OnInfoChangedListener listener) {
		this.mOnInfoChanged = listener;
	}

	public void notifyObservers() {
		if (mOnInfoChanged != null) {
			mOnInfoChanged.onChanged();
		}
	}

	public void addItem(DownLoadInfoStructure info) {
		if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_COMPLETED) {
			if (mDownloadInfo.contains(info))
				deleteDownLoadInfo(info.key);
			if (!mCompletedInfo.contains(info))
				addCompletedItem(info);
		} else {
			if (mCompletedInfo.contains(info))
				deleteCompletedInfo(info.key);
			if (!mDownloadInfo.contains(info)) {
				mDownloadInfo.add(info);
			} else {
				deleteDownLoadInfo(info.key);
				mDownloadInfo.add(info);
			}
			notifyObservers();
		}
	}

	private void addCompletedItem(DownLoadInfoStructure info) {
		mCompletedInfo.add(info);
		notifyObservers();
	}

	public void setAllInfo(ArrayList<DownLoadInfoStructure> downloadInfo) {
		if (downloadInfo != null) {
			mCompletedInfo.clear();
			mDownloadInfo.clear();
			for (DownLoadInfoStructure structure : downloadInfo) {
				if (structure.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_COMPLETED) {
					mCompletedInfo.add(structure);
				} else {
					mDownloadInfo.add(structure);
				}
			}
		}

	}

	public void setDownloadInfo(ArrayList<DownLoadInfoStructure> downloadInfo) {
		this.mDownloadInfo = downloadInfo;
	}

	public void setCompletedInfo(ArrayList<DownLoadInfoStructure> completedInfo) {
		this.mCompletedInfo = completedInfo;
	}

	public ArrayList<DownLoadInfoStructure> getAllInfo() {
		return mDownloadInfo;
	}

	public int getInfoListSize(int groupPosition) {
		if (groupPosition == 0) {
			return mDownloadInfo.size();
		} else {
			return mCompletedInfo.size();
		}
	}

	public ArrayList<DownLoadInfoStructure> getAllCompletedInfo() {
		return mCompletedInfo;
	}

	public ArrayList<String> getAllPackageNames() {
		ArrayList<String> allPackageNames = new ArrayList<String>();
		for (DownLoadInfoStructure structure : mDownloadInfo) {
			allPackageNames.add(structure.packageName);
		}
		for (DownLoadInfoStructure structure : mCompletedInfo) {
			allPackageNames.add(structure.packageName);
		}
		return allPackageNames;
	}

	public void pauseAllDownloading() {
		ArrayList<DownLoadInfoStructure> infos = new ArrayList<DownLoadInfoStructure>();
		infos.addAll(mDownloadInfo);
		for (DownLoadInfoStructure info : infos) {
			if (!mFlag) {
				return;
			}
			info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
		}
	}

	public void deleteAllCompletedSoftware(ContentResolver contentResolver) {
		ArrayList<DownLoadInfoStructure> infos = new ArrayList<DownLoadInfoStructure>();
		infos.addAll(mCompletedInfo);
		for (DownLoadInfoStructure info : infos) {
			if (!mFlag) {
				return;
			}
			contentResolver.delete(DownLoadInfo.CONTENT_URI, DownLoadInfoColumn.ITEM_KEY + " = ?",
					new String[] { info.key });
			deleteFile(info);
			mCompletedInfo.remove(info);
		}
	}

	public void clearAllDownloading(ContentResolver contentResolver) {
		ArrayList<DownLoadInfoStructure> infos = new ArrayList<DownLoadInfoStructure>();
		infos.addAll(mDownloadInfo);
		for (DownLoadInfoStructure info : infos) {
			if (!mFlag) {
				return;
			}
			info.setDownLoadState(DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE);
			contentResolver.delete(DownLoadInfo.CONTENT_URI, DownLoadInfoColumn.ITEM_KEY + " = ?",
					new String[] { info.key });
			deleteFile(info);
			mDownloadInfo.remove(info);
		}
	}

	public void deleteFile(DownLoadInfoStructure info) {
		File file = new File(getFilePath(info));
		if (file.exists()) {
			file.delete();
		}
	}

	public void startAllDownloading(Context context) {
		ArrayList<DownLoadInfoStructure> infos = new ArrayList<DownLoadInfoStructure>();
		infos.addAll(mDownloadInfo);
		for (DownLoadInfoStructure structure : infos) {
			if (!mFlag) {
				return;
			}
			if (structure.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE
					|| structure.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_NOT_BEGIN) {
				Intent i = new Intent(context, DownLoadService.class);
				i.putExtra(DownLoadService.ITEM_KEY, structure.key);
				i.putExtra(DownLoadService.ITEM_PACKAGENAME, structure.packageName);
				i.putExtra(DownLoadService.ITEM_NAME, structure.name);
				context.startService(i);
			}
		}
	}

	public int getDownLoadInfoSize() {
		return mDownloadInfo.size();
	}

	public DownLoadInfoStructure getDownLoadInfo(int position) {
		return mDownloadInfo.get(position);
	}

	public DownLoadInfoStructure getDownLoadInfo(int groupPosition, int childPosition) {
		if (groupPosition == 0) {
			return mDownloadInfo.get(childPosition);
		} else if (groupPosition == 1) {
			return mCompletedInfo.get(childPosition);
		} else {
			return null;
		}
	}

	private void showProgressDialog(Context context, int operateId) {
		mProgressDialog = new ProgressDialog(context);
		switch (operateId) {
		case OPERATE_STARTALLDOWNLOADS:
			mProgressDialog.setTitle(R.string.operate_download_all_software);
			break;
		case OPERATE_PAUSEALLDOWNLOADS:
			mProgressDialog.setTitle(R.string.operate_pause_all_software);
			break;
		case OPERATE_CLEARALLDOWNLOADING:
			mProgressDialog.setTitle(R.string.operate_clear_all_software);
			break;
		case OPERATE_DELETEALLCOMPLETED:
			mProgressDialog.setTitle(R.string.operate_delete_completed_software);
			break;
		}
		mProgressDialog.setMessage(context.getString(R.string.operate_wait));
		mProgressDialog.setButton(context.getString(R.string.button_cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mFlag = false;
				progressHandler.post(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						notifyObservers();
					}
				});
			}
		});
		mProgressDialog.show();
	}

	private Handler progressHandler = new Handler() {
	};

	private void closeProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	public void startOperate(final Context context, final int operateId) {
		mFlag = true;
		showProgressDialog(context, operateId);
		new Thread(new Runnable() {
			@Override
			public void run() {
				switch (operateId) {
				case OPERATE_STARTALLDOWNLOADS:
					startAllDownloading(context);
					break;
				case OPERATE_PAUSEALLDOWNLOADS:
					pauseAllDownloading();
					break;
				case OPERATE_CLEARALLDOWNLOADING:
					clearAllDownloading(context.getContentResolver());
					break;
				case OPERATE_DELETEALLCOMPLETED:
					deleteAllCompletedSoftware(context.getContentResolver());
					break;
				}
				progressHandler.post(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						notifyObservers();
					}
				});
			}
		}).start();
	}

	public DownLoadInfoStructure getDownLoadInfoByKey(String key) {
		ArrayList<DownLoadInfoStructure> infos = new ArrayList<DownLoadInfoStructure>();
		infos.addAll(mDownloadInfo);
		for (DownLoadInfoStructure structure : infos) {
			if (structure.key.equals(key)) {
				return structure;
			}
		}
		for (DownLoadInfoStructure structure : mCompletedInfo) {
			if (structure.key.equals(key)) {
				return structure;
			}
		}
		return null;
	}

	public DownLoadInfoStructure getDownLoadInfoByPackageName(String packageName) {
		for (DownLoadInfoStructure structure : mDownloadInfo) {
			if (structure.packageName.equals(packageName)) {
				return structure;
			}
		}
		for (DownLoadInfoStructure structure : mCompletedInfo) {
			if (structure.packageName.equals(packageName)) {
				return structure;
			}
		}
		return null;
	}

	public void deleteDownLoadInfo(int position) {
		mDownloadInfo.remove(position);
	}

	public void deleteDownLoadInfo(String key) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		for (int i = 0; i < mDownloadInfo.size(); i++) {
			if (mDownloadInfo.get(i).key.equals(key)) {
				mDownloadInfo.remove(i);
				return;
			}
		}
	}

	public void deleteDownLoadInfoByPackageName(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return;
		}
		for (int i = 0; i < mDownloadInfo.size(); i++) {
			if (mDownloadInfo.get(i).packageName.equals(packageName)) {
				mDownloadInfo.remove(i);
				notifyObservers();
				return;
			}
		}
		notifyObservers();
	}

	public void deleteCompletedInfo(String key) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		for (int i = 0; i < mCompletedInfo.size(); i++) {
			if (mCompletedInfo.get(i).key.equals(key)) {
				mCompletedInfo.remove(i);
				return;
			}
		}
		notifyObservers();
	}

	public void deleteCompletedInfoByPackageName(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return;
		}
		for (int i = 0; i < mCompletedInfo.size(); i++) {
			if (mCompletedInfo.get(i).packageName.equals(packageName)) {
				mCompletedInfo.remove(i);
				notifyObservers();
				return;
			}
		}
	}

	public void clearAllInfo() {
		mDownloadInfo.clear();
	}

	public boolean isDownLoadInfoEmpty() {
		if (mDownloadInfo == null) {
			return true;
		} else if (mDownloadInfo.size() == 0) {
			return true;
		}
		return false;
	}

	public boolean checkExistByKey(String key) {
		for (DownLoadInfoStructure info : getAllInfo()) {
			if (key.equals(info.key)) {
				return true;
			}
		}
		return false;
	}

	public boolean isInfoDownLoadComplete(String key) {
		for (DownLoadInfoStructure info : getAllInfo()) {
			if (key.equals(info.key)) {
				if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_COMPLETED) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	public boolean isInfoDownLoadDowning(String key) {
		for (DownLoadInfoStructure info : getAllInfo()) {
			if (key.equals(info.key)) {
				if (info.getDownLoadState() == DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING) {
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	public synchronized void infoSoftByState() {
		ArrayList<DownLoadInfoStructure> notBeginInfos = new ArrayList<DownLoadInfoStructure>();
		ArrayList<DownLoadInfoStructure> waitInfos = new ArrayList<DownLoadInfoStructure>();
		ArrayList<DownLoadInfoStructure> pausedInfos = new ArrayList<DownLoadInfoStructure>();
		ArrayList<DownLoadInfoStructure> downingInfos = new ArrayList<DownLoadInfoStructure>();
		for (DownLoadInfoStructure downloadInfoStructure : mDownloadInfo) {
			switch (downloadInfoStructure.getDownLoadState()) {
			case DownLoadInfoStructure.DOWNLOAD_STATE_NOT_BEGIN:
				notBeginInfos.add(downloadInfoStructure);
				break;
			case DownLoadInfoStructure.DOWNLOAD_STATE_WAIT:
				waitInfos.add(downloadInfoStructure);
				break;
			case DownLoadInfoStructure.DOWNLOAD_STATE_PAUSE:
				pausedInfos.add(downloadInfoStructure);
				break;
			case DownLoadInfoStructure.DOWNLOAD_STATE_DOWNING:
				downingInfos.add(downloadInfoStructure);
				break;
			}
		}
		mDownloadInfo.clear();
		mDownloadInfo.addAll(downingInfos);
		mDownloadInfo.addAll(waitInfos);
		mDownloadInfo.addAll(pausedInfos);
		mDownloadInfo.addAll(notBeginInfos);
	}

}
