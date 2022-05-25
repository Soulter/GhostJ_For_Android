package com.soulter.goastjforandroid.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.soulter.goastjforandroid.service.ConnService;
import com.soulter.goastjforandroid.R;
import com.soulter.goastjforandroid.util.sendOrder;
import com.wuzy.photoviewex.PhotoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenshotActivity extends AppCompatActivity {


    private MyReceiver3 myReceiver;
    private IntentFilter intentFilter;
//    ArrayList<String> clientsName = new ArrayList<>();

    com.soulter.goastjforandroid.util.sendOrder sendOrder = new sendOrder();
    SharedPreferences prefs;
    String scrLink = "";
    String audioLink = "";
    String focusingclient = "";
    ImageView imageView;
    ProgressBar progressBar;
    TextView scrName;
    CardView scrCardview;
    CardView scrSettingCv;
    TextView scrLoadingStatus;
    EditText scrInputOrder;
    Button scrSendOrder;
    Button getAscr;
    Button savePic;
    Button audio;
    Button fullImageViewBtn;
    PhotoView photoView ;
    private Drawable drawable;
    Timer timer;
    int count = 0;
    int scrGetTag = 0;
    int scaleTag = 0;


    int audioOn = 0;
    int audioCount = 0;
    int audioPlayCount = 0;

    String scrArgument = "";
    int scrPeriodTime = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot);


        prefs = PreferenceManager.getDefaultSharedPreferences(ScreenshotActivity.this);
        scrArgument = prefs.getString("scr_argument","!!scr lc.png 0.5 0.05");
        try {
            scrPeriodTime = Integer.parseInt(prefs.getString("period_time","3000"));
            scaleTag =Integer.parseInt(prefs.getString("new_f","0"));
        }catch (Exception e){
            e.printStackTrace();
        }


        focusingclient = getIntent().getStringExtra(ConnService.COUNTER_FOCUSING);
        if (focusingclient.equals("")){
            focusingclient = "当前未聚焦...";
        }
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
        scrInputOrder = (EditText)findViewById(R.id.input_order_scr);
        scrInputOrder.setText("!!audio 5000");
        scrSendOrder =(Button)findViewById(R.id.send_order_scr);
        getAscr = findViewById(R.id.get_a_scr);
        savePic = findViewById(R.id.save_pic);
        audio = findViewById(R.id.audio);
        fullImageViewBtn = findViewById(R.id.full_screen_screenshot_view_btn);
        photoView = (PhotoView) findViewById(R.id.screenshot_image1);
        progressBar.setVisibility(View.VISIBLE);
        savePic.setVisibility(View.INVISIBLE);
        scrArgument = prefs.getString("scr_argument","!!scr lc.png 0.5 0.05");
        scrCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scrGetTag == 0){


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

        scrSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!scrInputOrder.equals("")){
                    sendOrder.send(scrInputOrder.getText().toString());
                    if (scrGetTag == 0){
                        scrGetTag = 2;
                        sendOrder.send(scrArgument);
                    }
                }
            }
        });

        getAscr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOrder.send(scrArgument);
                progressBar.setVisibility(View.VISIBLE);
                scrGetTag = 2;
            }
        });


        savePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawable!=null){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, bmp2Uri(ScreenshotActivity.this,drawableToBitmap(drawable)));
                    intent.setType("image/jpeg");
                    startActivity(Intent.createChooser(intent,"分享图片"));
                }

            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioOn == 0){
                    audioOn = 1;
                    audio.setText("声音开");
                    sendOrder.send("!!audio");

                }else{
                    audioOn = 0;
                    audio.setText("声音关");
                }
            }
        });


        fullImageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawable != null){
                    Intent intent = new Intent(ScreenshotActivity.this, ScreenshotViewActivity.class);
                    intent.putExtra("scr",bitmap2Bytes(drawableToBitamp(drawable)));
                    startActivity(intent);
                }
            }
        });

        verifyStoragePermissions();

        scrName.setText(focusingclient);

        sendOrder.send(scrArgument);
        scrGetTag = 2;



    }

    private void playWav(final String path) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            //todo: play complete.

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


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



                    if (codeid == 6){
                        Log.v("audioGet", msg);
                        audioLink = msg;
                        new Thread(getAudio).start();
                    }

                    if (codeid == 2 && scrGetTag != 0){
                        Log.v("screenLink", msg);
                        scrLink = msg;
                        new Thread(getScreenshot).start();

                    }

                }
            });
        }
    }
    private Runnable getScreenshot = new Runnable() {

        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(scrLink)) { //网络图片
                    // 对资源链接
                    URL url = new URL(scrLink);
                    //打开输入流
                    InputStream inputStream = url.openStream();
                    //对网上资源进行下载转换位图图片
//                    bitmap = BitmapFactory.decodeStream(inputStream);
//                    inputStream.close();
                    drawable = Drawable.createFromStream(inputStream,"scr.jpg");
                    count+=1;

                    //针对只截取一次。
                    if (scrGetTag == 2){
                        scrGetTag = 0;
                    }
                }

                if (scaleTag == 0){
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {

                            imageView.setImageDrawable(drawable);
                            savePic.setVisibility(View.VISIBLE);
                        }
                    });
                }else
                {
                    photoView.post(new Runnable() {
                        @Override
                        public void run() {
                            photoView.setImageDrawable(drawable);
                            savePic.setVisibility(View.VISIBLE);
                        }
                    });
                }


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
                        if (count > 1 && scrGetTag != 0){
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

    /**
     * drawable转化成bitmap的方法
     * @param drawable 需要转换的Drawable
     */
    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap转化成byte数组
     * @param bm 需要转换的Bitmap
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    private Runnable getAudio = new Runnable() {

        @Override
        public void run() {
            audioLink = "http://39.100.5.139/ghost/audio/" + focusingclient + "/audio.wav";
            try {

                URL url = new URL(audioLink);
                    InputStream inputStream = url.openStream();

                    int length;
                    byte[] bytes = new byte[1024 * 10*10];

                    File appFile = new File(Environment.getExternalStorageDirectory()+File.separator+"ghost");
                    if (!appFile.exists()){
                        appFile.mkdirs();
                    }
                    String fileName = "1"+".wav";
                    File path = new File(appFile,fileName);
                    try{
                        OutputStream os = new FileOutputStream(path);
                        while ((length = inputStream.read(bytes)) != -1) {
                            os.write(bytes, 0, length);
                        }
                        os.flush();
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                Log.v("downAudioOK", "downAudioOK");

                if (audioOn == 1){
                    try {
                        sendOrder.send("!!audio 5000");
                        sendOrder.send(scrArgument);
                        audioCount += 1;

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


                playWav(Environment.getExternalStorageDirectory() + File.separator + "ghost/1.wav");


//                saveFile(mBitmap);
//                mSaveMessage = "图片保存成功！";
            } catch (Exception e) {
//                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            }
        }
    };



    public Bitmap drawableToBitmap(Drawable drawable)
    {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ?               Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Uri bmp2Uri(Context c, Bitmap b){
        File appFile = new File(Environment.getExternalStorageDirectory()+File.separator+"GhostJ");
        if (!appFile.exists()){
            appFile.mkdirs();
        }
        String fileName = "scr_"+System.currentTimeMillis()+".jpg";
        File path = new File(appFile,fileName);
        try{
            OutputStream os = new FileOutputStream(path);
            b.compress(Bitmap.CompressFormat.JPEG,100,os);

            os.close();
            return Uri.fromFile(path);
        }catch (Exception e){
            e.printStackTrace();

        }
        return null;
    }



    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        if (timer != null){
            try {
                timer.cancel();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        super.onDestroy();
    }

}
