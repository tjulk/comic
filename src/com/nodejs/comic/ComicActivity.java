package com.nodejs.comic;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TabHost;

public class ComicActivity extends TabActivity implements OnClickListener {
	
	
	private LinearLayout tabBtnHome;
	private LinearLayout tabBtnRecommend;
	private LinearLayout tabBtnClassify;
	private LinearLayout tabBtnSearch;
	private LinearLayout tabBtnMore;
	
	private TabHost tabHost;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabactivity);
		tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("书架",
				getResources().getDrawable(R.drawable.tab_icon_favor))
				.setContent(new Intent(this, FavorActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("推荐",
				getResources().getDrawable(R.drawable.tab_icon_recommend))
				.setContent(new Intent(this, RecommentActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("分类",
				getResources().getDrawable(R.drawable.tab_icon_classify))
				.setContent(new Intent(this, ClassifyActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator("搜索",
				getResources().getDrawable(R.drawable.tab_icon_search))
				.setContent(new Intent(this, SearchActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab5").setIndicator("更多",
				getResources().getDrawable(R.drawable.tab_icon_more))
				.setContent(new Intent(this, MoreActivity.class)));
		initSimulationTab();
	}
	
	private void initSimulationTab(){
		tabBtnHome = (LinearLayout)findViewById(R.id.tabBtnHome);
		tabBtnRecommend = (LinearLayout)findViewById(R.id.tabBtnRecommend);
		tabBtnClassify = (LinearLayout)findViewById(R.id.tabBtnClassify);
		tabBtnSearch = (LinearLayout)findViewById(R.id.tabBtnSearch);
		tabBtnMore = (LinearLayout)findViewById(R.id.tabBtnMore);
		tabBtnHome.setOnClickListener(this);
		tabBtnRecommend.setOnClickListener(this);
		tabBtnClassify.setOnClickListener(this);
		tabBtnSearch.setOnClickListener(this);
		tabBtnMore.setOnClickListener(this);
		
	}


	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tabBtnHome:
			tabHost.setCurrentTab(0);
			break;
		case R.id.tabBtnRecommend:
			tabHost.setCurrentTab(1);
			break;
		case R.id.tabBtnClassify:
			tabHost.setCurrentTab(2);
			break;
		case R.id.tabBtnSearch:
			tabHost.setCurrentTab(3);
			break;
		case R.id.tabBtnMore:
			tabHost.setCurrentTab(4);
			break;

		default:
			break;
		}
		
	}
	
	
}
