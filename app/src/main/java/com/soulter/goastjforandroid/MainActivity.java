package com.soulter.goastjforandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.MulticastSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private MyReceiver myReceiver;
    private IntentFilter intentFilter;
    private TextView okok;
    private TextView focusNowTv;
    private TextView focusNumNowTv;
    private CardView clientFocus;
    private CardView createModeBtn;
    private CardView screenshotBtn;
    private CardView cmdEntry;
    private ScrollView mScrollView;

    private sendOrder sendOrder = new sendOrder();


    private ArrayList<String> clientsName = new ArrayList<>();
    private ArrayList<String> clientsNum = new ArrayList<>();
    private String focusingClient = "";


//    public static String ORDER_DIALOG_SHOW = "order_dialog_show";
    public static String CLIENT_DATA_NAME_SEC = "client_name_sec";
    public static String CLIENT_DATA_NUM_SEC = "client_num_sec";
    public static String TO_CMD_ACTIVITY_OKOK = "client_num_sec";
    public static String ALL_MESSAGE_TAG = "all_of_the_message";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public static final int REQUEST_FOCUS_CLIENT_CODE = 1;


    Intent mIntent;

    String msgAll = "";
    SpannableStringBuilder spannableStringBuilder =  new SpannableStringBuilder();




    int msgChatTag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        myReceiver = new MyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        //注册广播
        registerReceiver(myReceiver, intentFilter);

        okok = findViewById(R.id.okok_main);
        clientFocus = findViewById(R.id.client_focus);
        createModeBtn = findViewById(R.id.create_mode_entry);
        screenshotBtn = findViewById(R.id.screenshot_entry);
        focusNowTv = findViewById(R.id.main_tv_focus_now);
        focusNumNowTv = findViewById(R.id.main_tv_focus_num_now);
        cmdEntry = findViewById(R.id.command_table_entry);

        mScrollView = findViewById(R.id.scroll_view_main);

        mIntent = new Intent(MainActivity.this,ConnService.class);
        startService(mIntent);

        clientFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientsFieldActivity.class);
                intent.putExtra(CLIENT_DATA_NAME_SEC,clientsName);
                intent.putExtra(CLIENT_DATA_NUM_SEC,clientsNum);
                startActivityForResult(intent,REQUEST_FOCUS_CLIENT_CODE);
            }
        });


        createModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FileExplorerActivity.class);
                startActivity(intent);
            }
        });
        screenshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScreenshotActivity.class);
                intent.putStringArrayListExtra(CLIENT_DATA_NAME_SEC,clientsName);
                intent.putExtra(ConnService.COUNTER_FOCUSING,focusingClient);
                Log.v("scrname","get: "+focusingClient);
                startActivity(intent);
            }
        });
        cmdEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CmdTableActivity.class);
                intent.putExtra(TO_CMD_ACTIVITY_OKOK,msgAll);
                startActivity(intent);
            }
        });


        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScrollView.post(new Runnable() {
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });


        //兼容Android6.0+运行时权限解决方案
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示")
                        .setMessage("请给予本应用读写权限")
                        .setNegativeButton("不给", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setCancelable(false).create().show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT>=18) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("OnAcR",""+requestCode);
        if (requestCode == REQUEST_FOCUS_CLIENT_CODE){
            try {
//                sendOrder("!focus "+data.getStringExtra("client_name_focus"));
                sendOrder.send("!focus "+data.getStringExtra("client_name_focus"));

            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("unchecked")
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    int codeid = intent.getIntExtra(ConnService.CODE,0);
                        final String msg = intent.getStringExtra(ConnService.COUNTER);
                        //处理登录异常  codeid=1 时是登陆异常的情况。
                        if (codeid == 1){
                            try{
                                SocketManager.bufferedWriter.write("#close#");
                                SocketManager.bufferedWriter.newLine();
                                SocketManager.bufferedWriter.flush();

                                SocketManager.socket.close();
                                SocketManager.socket=null;
                                SocketManager.inputStreamReader=null;
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
                            intent2.putExtra(LoginActivity.INTENT_EXTRA,msg);
                            startActivity(intent2);
                            stopService(mIntent);
                            finish();
                        }else if (codeid == 2)
                        {
//                            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
//                            builder.setTitle("新截图")
//                                    .setMessage("是否预览？")
//                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            showScrDialog(msg);
//
//                                        }
//                                    })
//                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                        }
//                                    }).create().show();
                        } else if(codeid == 3)// clientsInfo
                        {
//                        clientsFields = null;

                        clientsName.clear();
                        clientsNum.clear();
                        clientsName = intent.getStringArrayListExtra(ConnService.COUNTER_CLIENT_NAME);
                        clientsNum = intent.getStringArrayListExtra(ConnService.COUNTER_CLIENT_NUM);
                        focusingClient = intent.getStringExtra(ConnService.COUNTER_FOCUSING);
                        focusNumNowTv.setText("聚焦("+clientsNum.size()+")");
                        focusNowTv.setText(""+focusingClient);
//                        clientsFields = Arrays.asList(intent.getStringExtra(ConnService.COUNTER).substring(1,intent.getStringExtra(ConnService.COUNTER).length()-1).split(", "));
                        Log.v("clients2LvData",clientsName.toString()+" "+clientsNum.toString());
                        }else if (codeid == 0){
                            Log.v("tag",codeid+"\n"+msg);

                            msgAll += msg;
                            if (msgChatTag == 1){
                                chatDisplay();
                            }else{
                                okok.setText(msgAll);
                            }
                        }
                }
            });
        }
    }

    public void chatDisplay(){

        String msgChatStr = "";
        String[] msgChats = msgAll.split("\n");
        for (String line : msgChats){
            if (line.indexOf("[TransCmd-echo]") == 0){
                msgChatStr += "\n"+line;
                okok.setText(msgChatStr);
            }

        }
        if (msgChatStr.equals("")){
            okok.setText("           \n\n\n\n此地无银");
        }

    }


    @Override
    protected void onDestroy() {
        try {
            SocketManager.socket.close();
        }catch (Exception e){
            e.getStackTrace();
        }
        unregisterReceiver(myReceiver);

        super.onDestroy();
    }





}

