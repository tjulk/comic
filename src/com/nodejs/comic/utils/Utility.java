/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.pekall.market.MarketApplication;
import com.pekall.market.R;
import com.pekall.market.model.SoftwareInfo;
import com.pekall.market.model.SoftwareSet;

public class Utility {

	public static final String TAG = "Utility";
	public static final int MB = 1024 * 1024;
	public static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

	public static final String LOCAL_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/PekallMarket/";
	public static final String LOCAL_CACHE_PATH = LOCAL_BASE_PATH + ".cache/";

	public static MarketApplication getMarketApplication(Context context) {
		return (MarketApplication) context.getApplicationContext();
	}

	public static ImageCache getImageCache(Context context) {
		return getMarketApplication(context).getImageCache();
	}

	public static HashMap<String, String> getDataCache(Context context) {
		return getMarketApplication(context).getDataCache();
	}

	public static ExecutorService getExecutor(Context context) {
		return getMarketApplication(context).getExecutor();
	}

	public static boolean sdcardStatus() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static String isExistCache(String cacheFolder, String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		File file;
		if (TextUtils.isEmpty(cacheFolder)) {
			file = new File(LOCAL_CACHE_PATH + URLEncoder.encode(url));
		} else {
			file = new File(LOCAL_CACHE_PATH + cacheFolder + "/" + URLEncoder.encode(url));
		}
		if (file.exists()) {
			return file.getPath();
		}
		return null;
	}

	public static void saveBmpToSd(Bitmap bm, String cacheFolder, String url) {

		if (bm == null) {
			return;
		}
		File file = null;
		createBaseFolder();
		if (TextUtils.isEmpty(cacheFolder)) {
			file = new File(Utility.LOCAL_CACHE_PATH + URLEncoder.encode(url));
		} else {
			File folder = new File(LOCAL_CACHE_PATH + cacheFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			file = new File(LOCAL_CACHE_PATH + cacheFolder + "/" + URLEncoder.encode(url));
		}

		if (file.exists()) {
			LogUtil.LOGD(TAG, "image file exist");
			return;
		}
		if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
			LogUtil.LOGD(TAG, "space memory size is too small");
			return;
		}
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file.getPath());
			bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
		} catch (FileNotFoundException e) {
			LogUtil.LOGW(TAG, "FileNotFoundException" + e.getMessage());
			if (file != null) {
				file.delete();
			}
		} catch (IOException e) {
			LogUtil.LOGW(TAG, "IOException" + e.getMessage());
			if (file != null) {
				file.delete();
			}
		} finally {
			try {
				if (outStream != null)
					outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static int freeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
		return (int) sdFreeMB;
	}

	public static String createRequestUrl(HashMap<String, String> params) {
		String url = params.get("url") + "?";
		params.remove("url");
		String key, value;
		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			key = it.next();
			value = params.get(key);
			url += key + "=" + value + "&";
		}
		if (url != null) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static void createBaseFolder() {
		File file = new File(LOCAL_BASE_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(LOCAL_CACHE_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static String getLanguage(Resources res) {
		String language = res.getConfiguration().locale.getLanguage().toLowerCase().trim();
		return language + "-" + res.getConfiguration().locale.getCountry();
	}
	
	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFile(files[i]);
				}
			}
			file.delete();
		}
	}
	
	public static ArrayList<SoftwareInfo> parseResult2SoftwareList(String result) throws JSONException {
		Gson gson = new Gson();
		SoftwareSet softwareSet = gson.fromJson(result, SoftwareSet.class);
		return softwareSet.getSoftlist();
	}
	
	public static void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
		final int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i).findViewById(R.id.overview);
			if (image != null) {
				image.setPaused(pause);
			}
		}
	}
	
	public static void writeCacheObj(Context context, Object obj) {
		try {
			LogUtil.LOGD(TAG, "write cache --- " + obj.getClass().getName());
			deleteCacheObj(context, obj.getClass());
			FileOutputStream outStream = new FileOutputStream(context.getCacheDir().getPath()+ "/" +MD5(obj.getClass().getName()));
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
			objectOutputStream.writeObject(obj);
			objectOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object readCacheObj(Context context, Class<?> clazz) {
		Object obj = null;
		try {
			LogUtil.LOGD(TAG, "read cache --- " + clazz.getName());
			FileInputStream freader = new FileInputStream(context.getCacheDir().getPath()+ "/" + MD5(clazz.getName()));
			ObjectInputStream objectInputStream = new ObjectInputStream(freader);
			obj = objectInputStream.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
		return obj;
	}
	
	public static void deleteCacheObj(Context context, Class<? extends Object> clazz) {
		File cache = new File(context.getCacheDir().getPath(), MD5(clazz.getName()));
		if (cache != null && cache.exists()) {
			LogUtil.LOGD(TAG,"delete cache --- " + clazz.getName());
			cache.delete();
		}
	}
	
	public final static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte b = md[i];
				str[k++] = hexDigits[b >> 4 & 0xf];
				str[k++] = hexDigits[b & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String formatSize(String size) {  
        String suffix = "byte";
        float s = 0;
        try {
        	s = Float.parseFloat(size);
        } catch (NumberFormatException e) {
        	s = 0;
        }
        if (s >= 1024) {  
            suffix = "KB";  
            s /= 1024;  
            if (s >= 1024) {  
                suffix = "M";  
                s /= 1024;  
            }  
        }  
        java.math.BigDecimal b = new java.math.BigDecimal(s);  
        s =  b.setScale(1, java.math.BigDecimal.ROUND_HALF_UP).floatValue();  
        StringBuilder resultBuffer = new StringBuilder(Float.toString(s));  
      
      
        if (suffix != null)  
            resultBuffer.append(suffix);  
        return resultBuffer.toString();  
    }  
	
	public static HashMap<String, String> getCheckUpdateParams(ArrayList<SoftwareInfo> installs, MarketApplication mApplication) {
		HashMap<String, String> params = new HashMap<String, String>();
		StringBuilder packages = new StringBuilder("{\"packages\":[");
		StringBuilder packageInfo = null;
		for (SoftwareInfo softwareInfo : installs) {
			packageInfo = new StringBuilder();
			packageInfo.append("{");
			packageInfo.append("\"packageName\":\"");
			packageInfo.append(softwareInfo.getPackageName());
			packageInfo.append("\",");
			packageInfo.append("\"versionCode\":");
			packageInfo.append(softwareInfo.getVersionCode());
			packageInfo.append("},");
			packages.append(packageInfo);
		}
		packages.deleteCharAt(packages.length() - 1);
		packages.append("]}");
		LogUtil.LOGD(TAG, "package-----"+packages);
		params.put("url", RequestConstant.CHEKC_UPDATE_REQ.URL);
		params.put(RequestConstant.CHEKC_UPDATE_REQ.PARAM_PACKAGES, packages.toString());
		params.put(RequestConstant.PARAM_COMPANY, mApplication.getmCompany());
		params.put(RequestConstant.PARAM_MODELNUM, mApplication.getmModel());
		params.put(RequestConstant.PARAM_OSVERSION, mApplication.getmOSVersion()+"");
		return params;
	}
}
