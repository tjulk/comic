/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.updates;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;

public class UpdateUtility {

	public static boolean isSdcardMounted() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}

	public static boolean hasEnoughSpace(long required) {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			File path = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(path.getPath());
			long blockSize = statfs.getBlockSize();
			long availaBlock = statfs.getAvailableBlocks();
			return availaBlock * blockSize > required;
		} else {
			return false;
		}
	}

	public static boolean hasInternet(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected() || !info.isAvailable()) {
			return false;
		}
		return true;
	}

	public static boolean isWiFiActive(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static String getVersionCode(Context context) {
		String versionCode = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionCode = pi.versionCode + "";
		} catch (PackageManager.NameNotFoundException e) {
			versionCode = "";
		}
		return versionCode;
	}
}
