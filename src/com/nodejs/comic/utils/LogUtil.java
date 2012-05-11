/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                      LeiKang    tju.leikang@gmail.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.utils;

import android.util.Log;

public class LogUtil {
	private static final boolean DEBUG  = true;
    private static final boolean LOG_ENABLED = DEBUG;
    public static void LOGD(String log_tag,String message) {
        if (LOG_ENABLED) {
            Log.d(log_tag, message);
        }
    }
    public static void LOGV(String log_tag,String message) {
        if (LOG_ENABLED) {
            Log.v(log_tag, message);
        }
    }
    public static void LOGW(String log_tag,String message) {
    	if (LOG_ENABLED) {
    		Log.w(log_tag, message);
    	}
    }
    public static void LOGE(String log_tag,String message) {
    	if (LOG_ENABLED) {
    		Log.e(log_tag, message);
    	}
    }
}
	