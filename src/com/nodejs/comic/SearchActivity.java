/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.nodejs.comic.handler.ComicContent;
import com.nodejs.comic.models.SoftwareInfo;
import com.nodejs.comic.utils.AsyncImageView;
import com.nodejs.comic.utils.KeywordsFlow;
import com.nodejs.comic.utils.RequestConstant;
import com.nodejs.comic.utils.RequestTask;
import com.nodejs.comic.utils.Utility;

public class SearchActivity extends Activity implements OnClickListener, OnItemClickListener, OnScrollListener,
		RequestTask.OnRequestResult, TextWatcher {

	private AutoCompleteTextView mKeyword;
	private ImageView mSearch;
	private ListView mResult;
	private View mInfoBar;
	private View mLoading;
	private View mNetworkError;
	private View mNoMatch;
	private Button mRetry;
	private TextView mSearchText;
	private View mFooterView;

	private ComicApplication mApplication;
	private ArrayList<SoftwareInfo> mSoftwareList;
	private int mState;
	private int mMoreState;
	private String mKeywordValue;
	private static final int GET_SOFTWARE_LIST_DATA = 0;
	private static final int REQUEST_SOFTWARE_LIST_DATA = 1;
	private static final int NO_MATCHES_FOUND = 2;
	private static final int GET_DATA_ERROR = 3;
	private static final int GET_MORE_SOFTWARE_LIST_DATA = 4;
	private static final int NO_MORE_SOFTWARE_LIST_DATA = 5;
	public String[] keywords;

	private SoftwareListAdapter mAdapter;
	private RequestTask mRequestTask;
	private KeywordsFlow mKeywordsFlow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (ComicApplication) getApplication();
		setContentView(R.layout.actsearch);
		mKeyword = (AutoCompleteTextView) findViewById(R.id.keyword);
		mKeyword.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				showSoftKeyboard();
				return true;
			}
		});
		mSearch = (ImageView) findViewById(R.id.search);
		mResult = (ListView) findViewById(R.id.result);
		mKeywordsFlow = (KeywordsFlow) findViewById(R.id.keywordsflow);
		keywords = getResources().getStringArray(R.array.key_words);

		mKeywordsFlow.setDuration(800l);
		mKeywordsFlow.setOnItemClickListener(this);
		// 添加
		feedKeywordsFlow(mKeywordsFlow, keywords);
		mKeywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);

		mInfoBar = findViewById(R.id.info_bar);
		mLoading = findViewById(R.id.loading_view);
		mSearchText = (TextView) findViewById(R.id.search_text);
		mNetworkError = findViewById(R.id.network_error_view);
		mNoMatch = findViewById(R.id.no_matches_found);
		mRetry = (Button) findViewById(R.id.retry);
		mRetry.setOnClickListener(this);
		mKeyword.addTextChangedListener(this);
		mKeyword.setThreshold(1);
		mResult.setOnItemClickListener(this);
		mResult.setOnScrollListener(this);
		mState = -1;
		updateInfoView(mState);
		mSearch.setOnClickListener(this);
		mSoftwareList = new ArrayList<SoftwareInfo>();
		mFooterView = getLayoutInflater().inflate(R.layout.fragment_software_list_footer, null);
		mFooterView.setOnClickListener(this);
		mResult.addFooterView(mFooterView);
		mAdapter = new SoftwareListAdapter();
		mResult.setAdapter(mAdapter);
		mResult.setVisibility(View.GONE);
		mSearchText.setText(R.string.searching);
	}

	private static void feedKeywordsFlow(KeywordsFlow keywordsFlow, String[] arr) {
		Random random = new Random();
		int size = arr.length;
		int ran = random.nextInt(size);
		String tmp = "";
		for (int i = 0; i < KeywordsFlow.MAX; i++) {
			if (ran + i < size) {
				tmp = arr[ran + i];
			} else {
				tmp = arr[ran + i - size];
			}
			keywordsFlow.feedKeyword(tmp);
		}
	}

	private void updateFooterView(int state) {
		View more = mFooterView.findViewById(R.id.more);
		View loading = mFooterView.findViewById(R.id.load_layout);
		View neterror = mFooterView.findViewById(R.id.net_error);
		View noMoreData = mFooterView.findViewById(R.id.no_more_data);
		more.setVisibility(View.INVISIBLE);
		loading.setVisibility(View.INVISIBLE);
		neterror.setVisibility(View.INVISIBLE);
		noMoreData.setVisibility(View.INVISIBLE);
		switch (state) {
		case REQUEST_SOFTWARE_LIST_DATA:
			loading.setVisibility(View.VISIBLE);
			break;
		case GET_MORE_SOFTWARE_LIST_DATA:
			if (mState == REQUEST_SOFTWARE_LIST_DATA)
				loading.setVisibility(View.VISIBLE);
			else
				more.setVisibility(View.VISIBLE);
			break;
		case GET_DATA_ERROR:
			neterror.setVisibility(View.VISIBLE);
			break;
		case NO_MORE_SOFTWARE_LIST_DATA:
			noMoreData.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			mMoreState = REQUEST_SOFTWARE_LIST_DATA;
			String keyword = mKeyword.getText().toString();
			if (TextUtils.isEmpty(keyword)) {
				showKeyWordsFlow();
				feedKeywordsFlow(mKeywordsFlow, keywords);
				mKeywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
				hideSoftKeyboard();
				return;
			}
			hideSoftKeyboard();
			mHandler.sendEmptyMessage(REQUEST_SOFTWARE_LIST_DATA);
			break;
		case R.id.retry:
			mKeywordValue = "";
			mHandler.sendEmptyMessage(REQUEST_SOFTWARE_LIST_DATA);
			break;
		}
		if (v instanceof TextView) {
			mMoreState = REQUEST_SOFTWARE_LIST_DATA;
			String keyword = ((TextView) v).getText().toString();
			if (TextUtils.isEmpty(keyword) || keyword.equals(mKeywordValue)) {
				return;
			}
			mKeyword.setText(keyword);
			mHandler.sendEmptyMessage(REQUEST_SOFTWARE_LIST_DATA);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mRequestTask != null) {
			mRequestTask.cancel(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		SoftwareInfo info = mSoftwareList.get(position);
		Intent intent = new Intent(this, SoftwareDetailActivity.class);
		intent.putExtra("software", info);
		startActivity(intent);
	}

	@Override
	public void onRequestResult(String result) {
		int what;
		if (RequestTask.NET_ERROR.equals(result)) {
			what = GET_DATA_ERROR;
		} else {
			ArrayList<SoftwareInfo> tmp;
			try {
				tmp = Utility.parseResult2SoftwareList(result);
				if (tmp.size() > 0 && tmp.size() < 10) {
					mKeyword.setText("");
					if (mMoreState != GET_MORE_SOFTWARE_LIST_DATA) {
						mSoftwareList.clear();
					}
					mSoftwareList.addAll(tmp);
					what = GET_SOFTWARE_LIST_DATA;
					mMoreState = NO_MORE_SOFTWARE_LIST_DATA;
				} else if (tmp.size() >= 10) {
					mMoreState = GET_MORE_SOFTWARE_LIST_DATA;
					what = GET_SOFTWARE_LIST_DATA;
					mSoftwareList.addAll(tmp);
				} else {
					what = NO_MATCHES_FOUND;
				}
			} catch (JSONException e) {
				what = NO_MATCHES_FOUND;
				e.printStackTrace();
			}
		}
		mHandler.sendEmptyMessage(what);
	}

	private void hideKeyWordsFlow() {
		if (mKeywordsFlow != null)
			mKeywordsFlow.setVisibility(View.GONE);

	}

	private void showKeyWordsFlow() {
		if (mKeywordsFlow != null)
			mKeywordsFlow.setVisibility(View.VISIBLE);
	}

	private void hideSoftKeyboard() {

		InputMethodManager inputManger = (InputMethodManager) getBaseContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		if (inputManger != null) {

			inputManger.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

		}

	}

	private void showSoftKeyboard() {
		mKeyword.setText("");
		InputMethodManager inputManger = (InputMethodManager) getBaseContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		if (inputManger != null) {
			inputManger.showSoftInput(mKeyword, 0);

		}
	}

	@SuppressWarnings("unchecked")
	private void request(String keyword) {
		mKeywordValue = keyword;
		Cursor c = managedQuery(ComicContent.SearchInfo.CONTENT_URI, null, ComicContent.SearchInfo.ITEM_KEYWORD
				+ " = ?", new String[] { keyword }, null);
		if (c == null || c.getCount() <= 0) {
			ContentValues values = new ContentValues();
			values.put(ComicContent.SearchInfo.ITEM_KEYWORD, keyword);
			getContentResolver().insert(ComicContent.SearchInfo.CONTENT_URI, values);
		}
		mRequestTask = new RequestTask(mApplication, this);
		mRequestTask.execute(getRequestParams(keyword));
	}

	@SuppressWarnings("unchecked")
	private void request() {
		Cursor c = managedQuery(ComicContent.SearchInfo.CONTENT_URI, null, ComicContent.SearchInfo.ITEM_KEYWORD
				+ " = ?", new String[] { mKeywordValue }, null);
		if (c == null || c.getCount() <= 0) {
			ContentValues values = new ContentValues();
			values.put(ComicContent.SearchInfo.ITEM_KEYWORD, mKeywordValue);
			getContentResolver().insert(ComicContent.SearchInfo.CONTENT_URI, values);
		}
		mRequestTask = new RequestTask(mApplication, this);
		mRequestTask.execute(getRequestParams(mKeywordValue));
	}

	private HashMap<String, String> getRequestParams(String keyword) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("url", RequestConstant.SEARCH_SOFTWARE_LIST_REQ.URL);
		params.put(RequestConstant.SEARCH_SOFTWARE_LIST_REQ.PARAM_KEYWORD, keyword);
		params.put(RequestConstant.PARAM_STARTNUM, mSoftwareList.size() + "");
		params.put(RequestConstant.PARAM_COUNTNUM, "10");
		params.put(RequestConstant.PARAM_COMPANY, mApplication.getmCompany());
		params.put(RequestConstant.PARAM_MODELNUM, mApplication.getmModel());
		params.put(RequestConstant.PARAM_OSVERSION, mApplication.getmOSVersion() + "");
		System.out.println(RequestConstant.SEARCH_SOFTWARE_LIST_REQ.URL + "?"
				+ RequestConstant.SEARCH_SOFTWARE_LIST_REQ.PARAM_KEYWORD + "=" + keyword + "&"
				+ RequestConstant.PARAM_COMPANY + "=" + mApplication.getmCompany() + "&"
				+ RequestConstant.PARAM_MODELNUM + "=" + mApplication.getmModel() + "&"
				+ RequestConstant.PARAM_OSVERSION + "=" + mApplication.getmOSVersion());
		return params;
	}

	private void updateInfoView(int state) {
		if (state == GET_SOFTWARE_LIST_DATA) {
			mInfoBar.setVisibility(View.GONE);
			mKeywordsFlow.setVisibility(View.GONE);
		} else {
			mInfoBar.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.GONE);
			mNetworkError.setVisibility(View.GONE);
			mNoMatch.setVisibility(View.GONE);
			switch (state) {
			case REQUEST_SOFTWARE_LIST_DATA:
				mLoading.setVisibility(View.VISIBLE);
				hideKeyWordsFlow();
				break;
			case GET_DATA_ERROR:
				mNetworkError.setVisibility(View.VISIBLE);
				hideKeyWordsFlow();
				break;
			case NO_MATCHES_FOUND:
				mNoMatch.setVisibility(View.VISIBLE);
				hideKeyWordsFlow();
				break;
			}
		}
	}

	private void updateListView() {
		if (mAdapter.getCount() <= 0) {
			mResult.setVisibility(View.GONE);
		} else {
			mResult.setVisibility(View.VISIBLE);
		}
		mAdapter.notifyDataSetChanged();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mState = msg.what;
			switch (mState) {
			case REQUEST_SOFTWARE_LIST_DATA:
				String keyword = mKeyword.getText().toString();
				if (TextUtils.isEmpty(keyword) && mMoreState != GET_MORE_SOFTWARE_LIST_DATA
						|| keyword.equals(mKeywordValue) && mMoreState != GET_MORE_SOFTWARE_LIST_DATA) {
					return;
				}
				if (mMoreState == GET_MORE_SOFTWARE_LIST_DATA)
					request();
				else{
					mSoftwareList.clear();
					request(keyword);
				}
				break;
			case GET_SOFTWARE_LIST_DATA:
				updateListView();
				break;
			case GET_DATA_ERROR:
				break;
			case NO_MATCHES_FOUND:
				break;
			}

			if (mState != REQUEST_SOFTWARE_LIST_DATA)
				updateInfoView(mState);
			updateFooterView(mMoreState);
		};
	};

	public class SoftwareListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public SoftwareListAdapter() {
			mInflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mSoftwareList.size();
		}

		@Override
		public Object getItem(int position) {
			return mSoftwareList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.fragment_software_list_item, null);
				holder.overview = (AsyncImageView) convertView.findViewById(R.id.overview);
				holder.name = (TextView) convertView.findViewById(R.id.software_name);
				holder.size = (TextView) convertView.findViewById(R.id.software_size);
				holder.downloadCount = (TextView) convertView.findViewById(R.id.software_download_count);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.setData((SoftwareInfo) getItem(position));
			return convertView;
		}

		final class ViewHolder {
			AsyncImageView overview;
			TextView name;
			TextView size;
			TextView downloadCount;

			public void setData(SoftwareInfo info) {
				overview.setUrlImage(info.getPackageName(), info.getIcon());
				name.setText(info.getName());
				size.setText(info.getShowSize());
				downloadCount.setText(info.getShowSize());
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mMoreState == GET_MORE_SOFTWARE_LIST_DATA && mState != REQUEST_SOFTWARE_LIST_DATA) {
			if ((firstVisibleItem + visibleItemCount) >= totalItemCount && totalItemCount > 0) {
				mState = REQUEST_SOFTWARE_LIST_DATA;
				if (!mHandler.hasMessages(REQUEST_SOFTWARE_LIST_DATA))
					mHandler.sendEmptyMessage(REQUEST_SOFTWARE_LIST_DATA);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		Utility.searchAsyncImageViews(view, scrollState == OnScrollListener.SCROLL_STATE_FLING);
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String contentStr = s.toString();
		if (!TextUtils.isEmpty(contentStr)) {
			Cursor c = managedQuery(ComicContent.SearchInfo.CONTENT_URI, null, ComicContent.SearchInfo.ITEM_KEYWORD
					+ " like ?", new String[] { contentStr + "%" }, null);
			KeywordAdapter keywordAdapter = new KeywordAdapter(SearchActivity.this, c);
			mKeyword.setAdapter(keywordAdapter);
		}
	}

	class KeywordAdapter extends CursorAdapter {

		public KeywordAdapter(Context context, Cursor c) {
			super(context, c);
		}

		private void setView(View view, Cursor cursor) {
			TextView tvWordItem = (TextView) view;
			tvWordItem.setText(cursor.getString(cursor.getColumnIndex(ComicContent.SearchInfo.ITEM_KEYWORD)));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			setView(view, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.keyword_item, null);
			setView(view, cursor);
			return view;
		}

		@Override
		public CharSequence convertToString(Cursor cursor) {
			return cursor == null ? "" : cursor.getString(cursor.getColumnIndex(ComicContent.SearchInfo.ITEM_KEYWORD));
		}

	}

}
