package com.nodejs.comic.handler;

import android.content.ContentValues;
import android.net.Uri;

import com.nodejs.comic.models.DownLoadInfoStructure;

public abstract class ComicContent {
	public static final String RECORD_ID = "_id";
	public static final int CONTENT_ID_COLUMN = 0;
	public static final String AUTHORITY = "com.nodejs.comic.ApkProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public interface DownLoadInfoColumn {
		public static final String ITEM_KEY = "itemKey";
		public static final String ITEM_PACKAGENAME = "itemPackageName";
		public static final String ITEM_CUR_SIZE = "itemCurSize";
		public static final String ITEM_WHOLE_SIZE = "itemWholeSize";
		public static final String ITEM_NAME = "itemName";
		public static final String ITEM_LASTMODIFIEDDATE = "itemLastModifiedDate";
		public static final String ITEM_ICON = "itemIcon";
	}

	public interface DownloadStatusInfoColumn {
		public static final String ITEM_PACKAGENAME = "itemPackageName";
	}

	public interface SearchInfoColumn {
		public static final String ITEM_KEYWORD = "keyword";
	}

	public static final class DownLoadInfo extends ComicContent implements DownLoadInfoColumn {
		public static final String TABLE_NAME_DOWNLOADINFO = "downLoadInfo";
		public static final Uri CONTENT_URI = Uri.parse(ComicContent.CONTENT_URI + "/downLoadInfo");
		public static final int CONTENT_ITEM_KEY_COLUMN = 1;
		public static final int CONTENT_ITEM_PACKAGENAME_COLUMN = 2;
		public static final int CONTENT_ITEM_CUR_SIZE_COLUMN = 3;
		public static final int CONTENT_ITEM_WHOLE_SIZE_COLUMN = 4;
		public static final int CONTENT_ITEM_NAME_COLUMN = 5;
		public static final int CONTENT_ITEM_LASTMODIFIEDDATE_COLUMN = 6;
		public static final int CONTENT_ITEM_ICON_COLUMN = 7;

		public static final String[] CONTENT_PROJECTION = new String[] { RECORD_ID, DownLoadInfoColumn.ITEM_KEY,
				DownLoadInfoColumn.ITEM_PACKAGENAME, DownLoadInfoColumn.ITEM_CUR_SIZE, DownLoadInfoColumn.ITEM_WHOLE_SIZE,
				DownLoadInfoColumn.ITEM_NAME, DownLoadInfoColumn.ITEM_LASTMODIFIEDDATE ,DownLoadInfoColumn.ITEM_ICON};

		public static ContentValues toContentValues(DownLoadInfoStructure info) {
			ContentValues values = new ContentValues();
			values.clear();
			values.put(DownLoadInfoColumn.ITEM_KEY, info.key);
			values.put(DownLoadInfoColumn.ITEM_PACKAGENAME, info.packageName);
			values.put(DownLoadInfoColumn.ITEM_CUR_SIZE, info.curSize);
			values.put(DownLoadInfoColumn.ITEM_WHOLE_SIZE, info.wholeSize);
			values.put(DownLoadInfoColumn.ITEM_NAME, info.name);
			values.put(DownLoadInfoColumn.ITEM_LASTMODIFIEDDATE, info.lastModifiedDate);
			values.put(DownLoadInfoColumn.ITEM_ICON, info.icon);
			return values;
		}

	}

	public static class DownloadStatusInfo extends ComicContent implements DownloadStatusInfoColumn {
		public static final String TABLE_NAME_DOWNLOADSTATUSINFO = "downloadStatusInfo";
		public static final Uri CONTENT_URI = Uri
				.parse(ComicContent.CONTENT_URI + "/" + TABLE_NAME_DOWNLOADSTATUSINFO);
		public static final Uri CONTENT_URI_ID = Uri.parse(ComicContent.CONTENT_URI + "/"
				+ TABLE_NAME_DOWNLOADSTATUSINFO + "/#");
		public static final int CONTENT_ITEM_PACKAGENAME_COLUMN = 1;

		public static final String[] CONTENT_PROJECTION = new String[] { RECORD_ID,
				DownloadStatusInfoColumn.ITEM_PACKAGENAME };

		public static ContentValues toContentValues(String packageName) {
			ContentValues values = new ContentValues();
			values.clear();
			values.put(DownloadStatusInfoColumn.ITEM_PACKAGENAME, packageName);
			return values;
		}
	}

	public static class SearchInfo extends ComicContent implements SearchInfoColumn {
		public static final String TABLE_NAME_SEARCHINFO = "search";
		public static final Uri CONTENT_URI = Uri.parse(ComicContent.CONTENT_URI + "/" + TABLE_NAME_SEARCHINFO);
		public static final Uri CONTENT_URI_ID = Uri.parse(ComicContent.CONTENT_URI + "/" + TABLE_NAME_SEARCHINFO
				+ "/#");

		public static final String[] CONTENT_PROJECTION = new String[] { RECORD_ID, SearchInfoColumn.ITEM_KEYWORD };

		public static ContentValues toContentValues(String keyword) {
			ContentValues values = new ContentValues();
			values.clear();
			values.put(SearchInfoColumn.ITEM_KEYWORD, keyword);
			return values;
		}
	}

}
