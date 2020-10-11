package com.soulter.goastjforandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenshotActivity extends AppCompatActivity {


    private MyReceiver3 myReceiver;
    private IntentFilter intentFilter;
//    ArrayList<String> clientsName = new ArrayList<>();

    sendOrder sendOrder = new sendOrder();
    SharedPreferences prefs;
    String scrLink = "";
    String focusingclient = "";
    ImageView imageView;
    ProgressBar progressBar;
    TextView scrName;
    CardView scrCardview;
    CardView scrSettingCv;
    TextView scrLoadingStatus;
    Timer timer;
    int count = 0;
    int scrGetTag = 0;

    String scrArgument = "";
    int scrPeriodTime = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot);

//        clientsName = getIntent().getStringArrayListExtra(MainActivity.CLIENT_DATA_NAME_SEC);
//
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.screenshot_recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        //新增
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//
//        recyclerView.setLayoutManager(layoutManager);
////        FruitAdapter adapter = new FruitAdapter(fruitList);
////        recyclerView.setAdapter(adapter);


        prefs = PreferenceManager.getDefaultSharedPreferences(ScreenshotActivity.this);
        scrArgument = prefs.getString("scr_argument","!!scr lc.png 0.5 0.05");
        try {
            scrPeriodTime = Integer.parseInt(prefs.getString("period_time","3000"));
        }catch (Exception e){
            e.printStackTrace();
        }


        focusingclient = getIntent().getStringExtra(ConnService.COUNTER_FOCUSING);
        Log.v("focus", focusingclient);
        //注册广播
        myReceiver = new MyReceiver3();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        registerReceiver(myReceiver, intentFilter);

        imageView = (ImageView)findViewById(R.id.screenshot_image);
        progressBar = (ProgressBar)findViewById(R.id.screenshot_loading);
        scrName = (TextView)findViewById(R.id.screenshot_tv);
        scrCardview = (CardView)findViewById(R.id.screenshot_refresh);
        scrSettingCv = (CardView)findViewById(R.id.scr_setting_entry);
        scrLoadingStatus = (TextView)findViewById(R.id.tv_status_loading);

        progressBar.setVisibility(View.VISIBLE);

        scrCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scrGetTag == 0){

                    scrArgument = prefs.getString("scr_argument","!!scr lc.png 0.5 0.05");
                    try {
                        scrPeriodTime = Integer.parseInt(prefs.getString("period_time","3000"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    scrGetTag = 1;
                    timer = new Timer();
                    timer.schedule(new ScrTask(),100,scrPeriodTime);
                    scrLoadingStatus.setText("轻触停止循环截图");
                }else{
                    scrGetTag = 0;
                    progressBar.setVisibility(View.INVISIBLE);
                    try {
                        timer.cancel();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    scrLoadingStatus.setText("轻触启动循环截图");
                }

            }
        });

        scrSettingCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScreenshotActivity.this,ScrSettingActivity.class);
                startActivity(intent);
            }
        });



        scrName.setText(focusingclient);

        sendOrder.send(scrArgument);
        scrGetTag = 1;



    }

    class ScrTask extends TimerTask{
        @Override
        public void run() {
            sendOrder.send(scrArgument);
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    @SuppressWarnings("unchecked")
    class MyReceiver3 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    int codeid = intent.getIntExtra(ConnService.CODE,0);
                    final String msg = intent.getStringExtra(ConnService.COUNTER);

                    if (codeid == 2 && progressBar.getVisibility() == View.VISIBLE){
                        Log.v("screenLink", msg);
                        scrLink = msg;
                        new Thread(getScreenshot).start();

                    }
                }
            });
        }
    }

    private Runnable getScreenshot = new Runnable() {
        Bitmap bitmap;
        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(scrLink)) { //网络图片
                    // 对资源链接
                    URL url = new URL(scrLink);
                    //打开输入流
                    InputStream inputStream = url.openStream();
                    //对网上资源进行下载转换位图图片
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    count+=1;
                    if (count<=1){
                        scrGetTag = 0;
                    }
                }

                imageView.post(new Runnable() {
                    @Override
                    public void run() {

                        imageView.setImageBitmap(bitmap);
                    }
                });
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                scrLoadingStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        //第一次截取不需要显示这个。
                        if (count > 1 ){
                            scrLoadingStatus.setText("轻触停止循环截图"+"(" +count+")") ;
                        }

                    }
                });

//                saveFile(mBitmap);
//                mSaveMessage = "图片保存成功！";
            } catch (IOException e) {
//                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };



    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        try {
            timer.cancel();
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onDestroy();
    }

}
