package com.nodejs.comic;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.nodejs.comicapi.API;
import com.nodejs.comicapi.model.Classify;
public class ClassifyActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actclassifylist);
	}
}
