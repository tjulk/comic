package com.nodejs.comic.utils;

public class RequestConstant {

	public static final String BASE_URL = "http://app.pekall.com/appmarket/store/market/";
	// public static final String BASE_URL =
	// "http://192.168.10.224:8080/appmarket/store/market/";

	public static final String LOCALE = "locale";
	public static final String PARAM_STARTNUM = "startNum";
	public static final String PARAM_COUNTNUM = "countNum";
	public static final String PARAM_MODELNUM = "modelnum";
	public static final String PARAM_COMPANY = "company";
	public static final String PARAM_OSVERSION = "osversion";

	public final static class CATALOG_REQ {
		public static final String URL = BASE_URL + "findCategory.json";
		public static final String PARAM_CATALOGCODE = "catalog";
	}

	public final static class SOFTWARE_LIST_REQ {
		public static final String URL = BASE_URL + "findAppList.json";
		public static final String PARAM_CATALOGID = "catalogId";
	}

	public final static class SEARCH_SOFTWARE_LIST_REQ {
		public static final String URL = BASE_URL + "findAppListByKeyWord.json";
		public static final String PARAM_KEYWORD = "keywords";
	}

	public final static class SOFTWARE_DETAIL_REQ {
		public static final String URL = BASE_URL + "findDetailByAppId.json";
		public static final String PARAM_APPID = "appId";
	}

	public final static class SPEED_INSTALL_REQ {
		public static final String URL = BASE_URL + "findOneInstallApps.json";
	}

	public final static class CHEKC_UPDATE_REQ {
		public static final String URL = BASE_URL + "findUpdatedApps.json";
		public static final String PARAM_PACKAGES = "packages";
	}

};
