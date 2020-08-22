package com.soulter.goastjforandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private MyReceiver myReceiver;
    private IntentFilter intentFilter;
    private TextView okok;
    private EditText inputOrder;
    private Button sendOrder;
    private Button eIWow;
    private Button eIWowDb;
    private Button eIUp;
    private Button eIDown;
    private ScrollView mScrollView;

    private ArrayList<String> orderList = new ArrayList<>();
    private int orderIndex = 0;
    private int downTag = 0;

    public static String ORDER_DIALOG_SHOW = "order_dialog_show";


    Intent mIntent;

    String msgAll = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        myReceiver = new MyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        //注册广播
        registerReceiver(myReceiver, intentFilter);

        okok = findViewById(R.id.okok);
        inputOrder = findViewById(R.id.input_order);
        sendOrder = findViewById(R.id.send_order);

        eIWow = findViewById(R.id.easy_input_wow);
        eIWowDb = findViewById(R.id.easy_input_wow_db);
        eIUp = findViewById(R.id.easy_input_up);
        eIDown = findViewById(R.id.easy_input_down);

        mScrollView = findViewById(R.id.scroll_view);

        mIntent = new Intent(MainActivity.this,ConnService.class);
        startService(mIntent);

        sendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String order = inputOrder.getText().toString();
                            if (!order.equals("")){
                                orderList.add(order);
                                orderIndex = orderList.size();
                                Log.v("easyinput",String.valueOf(orderIndex)+"  "+String.valueOf(orderList.size()));
                                SocketManager.bufferedWriter.write(order);
                                SocketManager.bufferedWriter.newLine();
                                SocketManager.bufferedWriter.flush();
                                Log.v("TAG","writeOK:"+inputOrder.getText().toString());

                            }


                        }catch (Exception e){
                            e.getStackTrace();
                        }
                    }
                }).start();


            }
        });

        //快捷输入感叹号而已
        eIWow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOrder.setText("!");
                inputOrder.setSelection(1);
            }
        });

        eIWowDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOrder.setText("!!");
                inputOrder.setSelection(2);
            }
        });

        eIUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (orderList.size() != 0 && orderIndex > 0){
                    orderIndex -= 1;
                    inputOrder.setText(orderList.get(orderIndex));
                    inputOrder.setSelection(orderList.get(orderIndex).length());
                    downTag = 0;
                    Log.v("easyinput",String.valueOf(orderIndex)+"  "+String.valueOf(orderList.size()));
                }else {
                    Toast.makeText(MainActivity.this,"到顶啦",Toast.LENGTH_SHORT).show();
             }

            }
        });

        eIDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (orderList.size() != 0 && orderIndex < orderList.size()-1){
                    orderIndex += 1;
                    Log.v("easyinput",String.valueOf(orderIndex)+"  "+String.valueOf(orderList.size()));
                    inputOrder.setText(orderList.get(orderIndex));
                    inputOrder.setSelection(orderList.get(orderIndex).length());

                }else {
                    if (orderIndex != orderList.size() && downTag == 0){
                        inputOrder.setText("");
                        if (orderList.size()!=0){
                            orderIndex += 1;
                        }
                        downTag = 1;
                        Log.v("easyinput",String.valueOf(orderIndex)+"  "+String.valueOf(orderList.size()));
                    }

                }
            }
        });




        okok.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                diyOrderDialog();
                return false;
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

//


    }


    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    final String msg = intent.getStringExtra(ConnService.COUNTER);
                    int codeid = intent.getIntExtra(ConnService.CODE,0);

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
                    }
                    if (codeid == 2)
                    {
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("新截图")
                                .setMessage("是否预览？")
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        showScrDialog(msg);

                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).create().show();
                    }


                    msgAll += msg;
                    okok.setText("" + msgAll);
                    inputOrder.setText("");

                }
            });
        }
    }

    public void showScrDialog(String url){
//        android.app.AlertDialog.Builder webDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
//        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
//        View webview = inflater.inflate(R.layout.web_message,null);
//        WebView webMessage = (WebView)webview.findViewById(R.id.web_message);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            webMessage.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }
//        webMessage.getSettings().setBlockNetworkImage(false);
//        webMessage.loadUrl(url);
//        webDialogBuilder.setView(webview);
//        webDialogBuilder.show();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);    }

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



    @Override
    protected void onResume() {
        SharedPreferences spfs = this.getSharedPreferences("spfs",Context.MODE_PRIVATE);

        Log.v("tag",String.valueOf(spfs.getInt("showOrder",0)));
        if (spfs.getInt("showOrder",0) == 1){
            diyOrderDialog();
            SharedPreferences.Editor editor = this.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();
            editor.putInt("showOrder",0);
            editor.apply();
        }
        super.onResume();
    }


    public void diyOrderDialog(){


        //spfs->String ,Sring to Json to Hashmap to String[]
        SharedPreferences spfs = this.getSharedPreferences("spfs",Context.MODE_PRIVATE);
        final String json = spfs.getString("orderData","");

        final String[] orderNameStrings;

        int defOrderCount = 2;

        if (json.equals("")){
            orderNameStrings = new String[defOrderCount];
            orderNameStrings[0] = "自定义命令";
            orderNameStrings[1] = "让Client说话";

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("自定义命令：")
                    .setItems(orderNameStrings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                Intent intent = new Intent(MainActivity.this,DiyOrderActivity.class);
                                startActivity(intent);
                            }
                            if (i == 1){
                                inputOrder.setText("mshta vbscript:createobject(\"sapi.spvoice\").speak(\"Hello\")(window.close)");
                            }
                        }
                    }).create().show();
        }else{

            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            Gson gson = new Gson();
            final HashMap<String,String> map = gson.fromJson(json, type);
            orderNameStrings= new String[map.size() + defOrderCount];
            orderNameStrings[0] = "自定义命令";
            orderNameStrings[1] = "让Client说话";

            Log.v("mapSize",String.valueOf(map.size()));
//            Log.v("map", map.toString());
//            Log.v("keySet", map.keySet().toString());
            int index = defOrderCount;
            for(String entry : map.keySet()) {
                orderNameStrings[index]=entry;
                index++;
                Log.v("entry",entry);
            }
//            for(String entry1 : map.values()) {
//                Log.v("entry1",entry1);
//            }

            Log.v("najs",""+orderNameStrings[2]);
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("自定义命令：")
                    .setItems(orderNameStrings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                Intent intent = new Intent(MainActivity.this,DiyOrderActivity.class);
                                startActivity(intent);
                            }else if (i == 1){
                                inputOrder.setText("mshta vbscript:createobject(\"sapi.spvoice\").speak(\"Hello\")(window.close)");
                            }else
                                inputOrder.setText(""+map.get(orderNameStrings[i]));
                        }
                    }).create().show();
        }


    }





}

