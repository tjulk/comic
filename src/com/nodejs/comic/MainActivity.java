package com.nodejs.comic;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends TabActivity implements OnClickListener {
	
	private LinearLayout tabBtnHome;
	private LinearLayout tabBtnRecommend;
	private LinearLayout tabBtnClassify;
	private LinearLayout tabBtnSearch;
	private LinearLayout tabBtnMore;
	private TabHost tabHost;
	
	private ImageView tab_img_home;
	private ImageView tab_img_recommend;
	private ImageView tab_img_classify;
	private ImageView tab_img_search;
	private ImageView tab_img_more;
	
	private TextView tab_text_home;
	private TextView tab_text_recommend;
	private TextView tab_text_classify;
	private TextView tab_text_search;
	private TextView tab_text_more;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actmain);
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
		
		tab_img_home = (ImageView)findViewById(R.id.tab_img_home);
		tab_img_recommend = (ImageView)findViewById(R.id.tab_img_recommend);
		tab_img_classify = (ImageView)findViewById(R.id.tab_img_classify);
		tab_img_search = (ImageView)findViewById(R.id.tab_img_search);
		tab_img_more = (ImageView)findViewById(R.id.tab_img_more);
		
		tab_text_home = (TextView) findViewById(R.id.tab_text_home);
		tab_text_recommend = (TextView) findViewById(R.id.tab_text_recommend);
		tab_text_classify = (TextView) findViewById(R.id.tab_text_classify);
		tab_text_search = (TextView) findViewById(R.id.tab_text_search);
		tab_text_more = (TextView) findViewById(R.id.tab_text_more);
		
		tab_img_home.setImageResource(R.drawable.tab_icon_favor_s);
	}


	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tabBtnHome:
			tabHost.setCurrentTab(0);
			tab_img_home.setImageResource(R.drawable.tab_icon_favor_s);
			tab_img_recommend.setImageResource(R.drawable.tab_icon_recommend);
			tab_img_classify.setImageResource(R.drawable.tab_icon_classify);
			tab_img_search.setImageResource(R.drawable.tab_icon_search);
			tab_img_more.setImageResource(R.drawable.tab_icon_more);
			break;
		case R.id.tabBtnRecommend:
			tabHost.setCurrentTab(1);
			tab_img_home.setImageResource(R.drawable.tab_icon_favor);
			tab_img_recommend.setImageResource(R.drawable.tab_icon_recommend_s);
			tab_img_classify.setImageResource(R.drawable.tab_icon_classify);
			tab_img_search.setImageResource(R.drawable.tab_icon_search);
			tab_img_more.setImageResource(R.drawable.tab_icon_more);
			break;
		case R.id.tabBtnClassify:
			tabHost.setCurrentTab(2);
			tab_img_home.setImageResource(R.drawable.tab_icon_favor);
			tab_img_recommend.setImageResource(R.drawable.tab_icon_recommend);
			tab_img_classify.setImageResource(R.drawable.tab_icon_classify_s);
			tab_img_search.setImageResource(R.drawable.tab_icon_search);
			tab_img_more.setImageResource(R.drawable.tab_icon_more);
			break;
		case R.id.tabBtnSearch:
			tabHost.setCurrentTab(3);
			tab_img_home.setImageResource(R.drawable.tab_icon_favor);
			tab_img_recommend.setImageResource(R.drawable.tab_icon_recommend);
			tab_img_classify.setImageResource(R.drawable.tab_icon_classify);
			tab_img_search.setImageResource(R.drawable.tab_icon_search_s);
			tab_img_more.setImageResource(R.drawable.tab_icon_more);
			break;
		case R.id.tabBtnMore:
			tabHost.setCurrentTab(4);
			tab_img_home.setImageResource(R.drawable.tab_icon_favor);
			tab_img_recommend.setImageResource(R.drawable.tab_icon_recommend);
			tab_img_classify.setImageResource(R.drawable.tab_icon_classify);
			tab_img_search.setImageResource(R.drawable.tab_icon_search);
			tab_img_more.setImageResource(R.drawable.tab_icon_more_s);
			break;
		default:
			break;
		}
		
	}
	
	
}
