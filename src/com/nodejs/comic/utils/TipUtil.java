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
import android.widget.Toast;

import com.nodejs.comic.ComicApplication;
 
public class TipUtil {
	private static Context mContext = ComicApplication.getContext();

	public static void showMessageByShort(String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
	}
	public static void showMessageByShort(int strId) {
		Toast.makeText(mContext, strId, Toast.LENGTH_SHORT).show();
	}
	
	public static void showMessageByLong(String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
	}
	public static void showMessageByLong(int strId) {
		Toast.makeText(mContext, strId, Toast.LENGTH_LONG).show();
	}
}
