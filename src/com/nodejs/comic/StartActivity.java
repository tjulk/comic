package com.nodejs.comic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actstart);
        Intent intent = new Intent(this, ComicActivity.class);
        startActivity(intent);
    }
}