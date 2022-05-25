package com.soulter.goastjforandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.soulter.goastjforandroid.R;
import com.wuzy.photoviewex.PhotoView;

import java.util.Objects;

public class ScreenshotViewActivity extends AppCompatActivity {

    private byte[] appIcon;
    private Bitmap icon;
    private PhotoView scrView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        scrView = findViewById(R.id.scv_image);
        appIcon = getIntent().getByteArrayExtra("scr");
        icon = BitmapFactory.decodeByteArray(appIcon,0,appIcon.length);
        scrView.setImageBitmap(icon);
    }
}