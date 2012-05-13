package com.nodejs.comic;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.nodejs.comic.utils.KeywordsFlow;

public class SearchActivity extends Activity implements OnClickListener{
	private KeywordsFlow mKeywordsFlow;
	public String[] keywords; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actsearch);
		
		mKeywordsFlow = (KeywordsFlow) findViewById(R.id.keywordsflow);
		keywords = getResources().getStringArray(R.array.key_words);
		mKeywordsFlow.setDuration(800l);
		mKeywordsFlow.setOnItemClickListener(this);
		feedKeywordsFlow(mKeywordsFlow, keywords);
		mKeywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
	}
	@Override
	public void onClick(View v) {
		
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
}
