package com.nodejs.comic.views;

import java.util.List;

import com.nodejs.comicapi.API;
import com.nodejs.comicapi.model.Classify;

import android.content.Context;
import android.util.Log;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ClassifyView extends ScrollView{
	private static String TAG = "ClassifyView";
	List<Classify> classifies;
	public ClassifyView(Context context) {
		super(context);
		classifies = API.getClassifies(context);
		Log.d(TAG, "classifies:"+classifies.toString());
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		for (Classify classifie : classifies) {
			LinearLayout classifyTypeItem = new LinearLayout(context);
			classifyTypeItem.setLayoutParams(params);
			
			TextView typeText = new TextView(context);
			typeText.setLayoutParams(params);
			typeText.setText(classifie.type);
			classifyTypeItem.addView(typeText);
			
			GridView classifyNameGrid = new GridView(context);
			classifyNameGrid.setLayoutParams(params);
			
			
		}
		
	}
	

}
