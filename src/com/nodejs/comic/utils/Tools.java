package com.nodejs.comic.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.nodejs.comic.R;
import com.nodejs.comic.models.DownLoadInfoStructure;
import com.nodejs.comic.services.DownLoadService;

public class Tools {
	private static final String TAG = "DownLoad: ";
	public static final int BUF_SIZE = 1024;
	public static final int READ_TIME_OUT = 20 * 1000;
	private static ProgressDialog mProgress;
	private static final long MINI_SDCARD_LEFT_BYTE = 10 * 1000000;
  
	public static boolean isEmpty(String str) {
		if (str == null || str.length() <= 0) {
			return true;
		} else {
			return false;
		}
	}

	public static File copyFile(InputStream in, String newFilePath) {
		File newFile = new File(newFilePath);
		try {
			if (!newFile.getParentFile().exists()) {
				newFile.getParentFile().mkdirs();
			}
			if (!newFile.exists()) {
				newFile.createNewFile();
			} else if (newFile.isDirectory()) {
				newFile.delete();
				newFile.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(newFile);

			byte[] buffer = new byte[BUF_SIZE];
			int length = -1;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();

			return newFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static boolean isWifiConnected(Context context) {
		return getNetworkState(context, ConnectivityManager.TYPE_WIFI) == State.CONNECTED;
	}

	private static boolean isMobileConnected(Context context) {
		return getNetworkState(context, ConnectivityManager.TYPE_MOBILE) == State.CONNECTED;
	}

	private static State getNetworkState(Context context, int networkType) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getNetworkInfo(networkType);

		return info == null ? null : info.getState();
	}

	public static void showProgressDialog(Context ctx, String msg, boolean cancelable) {
		if (mProgress != null) {
			mProgress = null;
		}
		mProgress = new ProgressDialog(ctx);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setMessage(msg);
		mProgress.setCancelable(cancelable);
		mProgress.show();
	}

	public static void dismissProgress() {
		if (mProgress != null && mProgress.isShowing()) {
			mProgress.dismiss();
		}
	}
  
	public static final int MILLION = 1000000;

	public static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.8) Gecko/20100723 Ubuntu/9.10 (karmic) Firefox/3.6.8";
	public static final int CONN_TIME_OUT = 20 * 1000;
	public static final int LOAD_PICTURE_TIME_OUT = 10 * 1000;
	public static final int BUFFER_SIZE = 8192;
  
	public static boolean SDCardExists() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else {
			return false;
		}
	}
	 
	public static boolean SDCardHaveEnoughSpace(){
		if(!SDCardExists()){
			return false;
		}
		File path =Environment.getExternalStorageDirectory();
		StatFs statFs = new StatFs(path.getAbsolutePath());
		long blockSize = statFs.getBlockSize();
		long blockCount = statFs.getAvailableBlocks();
		if(blockSize * blockCount < MINI_SDCARD_LEFT_BYTE){
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean SDCardHaveEnoughSpace(long wholeSize){
		if(!SDCardExists()){
			return false;
		}
		File path =Environment.getExternalStorageDirectory();
		StatFs statFs = new StatFs(path.getAbsolutePath());
		long blockSize = statFs.getBlockSize();
		long blockCount = statFs.getAvailableBlocks();
		if(blockSize * blockCount < wholeSize){
			return false;
		} else {
			return true;
		}
	}

	public static boolean hasInternet(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	public static HttpParams getHttpParams(Context context) {
		// 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）
		HttpParams httpParams = new BasicHttpParams();

		// 设置连接超时和 Socket 超时，以及 Socket 缓存大小
		HttpConnectionParams.setConnectionTimeout(httpParams, CONN_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, READ_TIME_OUT);
		HttpConnectionParams.setSocketBufferSize(httpParams, BUFFER_SIZE);

		// 设置重定向，缺省为 true
		HttpClientParams.setRedirecting(httpParams, true);

		// 检测代理设置
		String proxyHost = Proxy.getHost(context);
		int proxyPort = Proxy.getPort(context);

		LogUtil.LOGD(TAG, "[createHttpClient] proxyHost = " + proxyHost);
		LogUtil.LOGD(TAG, "[createHttpClient] proxyPort = " + proxyPort);

		boolean isWifiConnected = isWifiConnected(context);
		boolean isMobileConnected = isMobileConnected(context);

		LogUtil.LOGD(TAG, "[createHttpClient] isWifiConnected = " + isWifiConnected);
		LogUtil.LOGD(TAG, "[createHttpClient] isMobileConnected = " + isMobileConnected);

		if (!isWifiConnected && !TextUtils.isEmpty(proxyHost)
				&& proxyPort != -1) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			LogUtil.LOGD(TAG, "[createHttpClient] Set proxy: host: " + proxyHost + " port:" + proxyPort);
			httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		// 设置 user agent
		String userAgent = USER_AGENT;
		HttpProtocolParams.setUserAgent(httpParams, userAgent);

		return httpParams;
	}

	public static String getEntityString(Context ctx, URI uri)
			throws ParseException, IOException {
		LogUtil.LOGD(TAG, "url == " + uri.toString());

		HttpGet get = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient(getHttpParams(ctx));
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			get.abort();
			throw new IOException("[getData]not http ok, url -- " + uri);
		}
		HttpEntity entity = response.getEntity();
		String str = EntityUtils.toString(entity, "utf8");
		if (TextUtils.isEmpty(str)) {
			return null;
		} else {
			return str;
		}
	}

	public static ArrayList<GregorianCalendar> getBetweenDate(String d1, String d2)
			throws ParseException {
		ArrayList<GregorianCalendar> v = new ArrayList<GregorianCalendar>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc1 = new GregorianCalendar(), gc2 = new GregorianCalendar();
		try {
			gc1.setTime(sdf.parse(d1));
			gc2.setTime(sdf.parse(d2));
			
			if(gc1.after(gc2)){
				GregorianCalendar swap = gc1;
				gc1 = gc2;
				gc2 = swap;
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		do {
			GregorianCalendar gc3 = (GregorianCalendar) gc1.clone();
			v.add(gc3);
			gc1.add(Calendar.DAY_OF_MONTH, 1);
		} while (!gc1.after(gc2));
		return v;
	}
	
	public static boolean downloadUseWifiCheck(Context context) {
		boolean onlyUseWIFI = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.only_use_wifi_to_download_key), false);
		if (onlyUseWIFI && !Tools.isWiFiActive(context)) {
			Toast.makeText(context, R.string.notify_no_wifi_download_error, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	public static void startDownloadService(Context context, DownLoadInfoStructure info, Class<?> serviceClass) {
		boolean isDownload = downloadUseWifiCheck(context);
		if (isDownload) {
			Intent i = new Intent(context, serviceClass);
			i.putExtra(DownLoadService.ITEM_KEY, info.key);
			i.putExtra(DownLoadService.ITEM_PACKAGENAME, info.packageName);
			i.putExtra(DownLoadService.ITEM_NAME, info.name);
			context.startService(i);
		}
	}
	
}
