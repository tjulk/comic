/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.nodejs.comic.ComicApplication;

public class RequestTask extends AsyncTask<HashMap<String, String>, Void, String> {

	private OnRequestResult onRequestResult;
	private Context mContext;

	public static final String NET_ERROR = "net_error";
	private static final String TAG = "RequestTask";

	public RequestTask(Context context, OnRequestResult onRequestSuccess) {
		this.mContext = context;
		this.onRequestResult = onRequestSuccess;
	}

	public HttpClient createHttpClient(Context context) {
		HttpParams httpParams = Tools.getHttpParams(context);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		return httpClient;
	}

	private List<BasicNameValuePair> createHttpParams(HashMap<String, String> params) {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		if (params == null) {
			return pairs;
		}
		params.remove("url");
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			LogUtil.LOGD(TAG, "key--" + entry.getKey().toString() + "--value--" + entry.getValue().toString());
			pairs.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
		}
		return pairs;
	}

	public boolean isNetworkEnable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (!wifiInfo.isConnected() && !mobInfo.isConnected()) {
			return false;
		}
		return true;
	}

	public void cancel() {
		this.cancel(true);
	}

	protected String doInBackground(HashMap<String, String>... params) {

		if (!isNetworkEnable()) {
			return NET_ERROR;
		}
		if (params == null) {
			return NET_ERROR;
		}
		String url = params[0].get("url");
		if (TextUtils.isEmpty(url)) {
			return NET_ERROR;
		}
		ComicApplication application = (ComicApplication) mContext;
		params[0].put("model", application.getmModel());
		params[0].put("company", application.getmCompany());
		params[0].put("locale", application.getmLocale());
		params[0].put("osversion", application.getmOSVersion() + "");
		LogUtil.LOGD(TAG, "requestUrl" + url);
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(createHttpParams(params[0]), HTTP.UTF_8));
			HttpClient mHttpClient = createHttpClient(mContext);
			HttpResponse response = mHttpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LogUtil.LOGE(TAG, "status code --- " + response.getStatusLine().getStatusCode() + "---" + url);
				return NET_ERROR;
			} else {
				return EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			LogUtil.LOGE(TAG, "ClientProtocolException---" + e.getMessage());
		} catch (IOException e) {
			LogUtil.LOGE(TAG, "IOException---" + e.getMessage());
		} catch (Exception e) {
			LogUtil.LOGE(TAG, "Exception---" + e.getMessage());
		}
		return NET_ERROR;
	}

	protected void onPostExecute(String result) {
		if (onRequestResult != null) {
			onRequestResult.onRequestResult(result);
		}
	}

	public interface OnRequestResult {
		public void onRequestResult(String result);
	}

}