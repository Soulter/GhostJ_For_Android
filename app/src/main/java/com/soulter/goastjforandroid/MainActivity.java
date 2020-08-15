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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
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
    private Button easyInput;
    private ScrollView mScrollView;

    public static String ORDER_DIALOG_SHOW = "order_dialog_show";


    Intent mIntent;

    String msgAll = "";


//
//
//    //在handler中更新UI
//    private  Handler mHandler = new Handler(){
//        public void handleMessage(Message msg) {
//            if (!msgAll.equals("")){
//                Log.v("hhh","sssssss");
//
//
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            //更新UI
//                            if (!msgAll.equals(""))
//                                okok.setText(msgAll);
//                            okok.setText(msgAll);
//
//                        }
//                    });
//                sendEmptyMessageDelayed(1,2000);
//            }
//
//        }
//    };






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
        easyInput = findViewById(R.id.easy_input);
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
                            msgAll+= order;
                            if (order.equals("..clear")){
                                Toast.makeText(MainActivity.this,"清空记录",Toast.LENGTH_LONG).show();
                                msgAll = "";
                                okok.setText("  ");
                            }else{
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
        easyInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOrder.setText("!");
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

//        mHandler.sendEmptyMessageDelayed(1,1000);


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


                    msgAll += msg;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //更新UI
                            if (!msgAll.equals(""))
                                okok.setText(""+msgAll);
                            inputOrder.setText("");
                        }
                    });


                }
            });
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

//    @Override
//    protected void onPause() {
//        try {
//            SocketManager.socket.close();
//        }catch (Exception e){
//            e.getStackTrace();
//        }
//        super.onPause();
//    }


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

