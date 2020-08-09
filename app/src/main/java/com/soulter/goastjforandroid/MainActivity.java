package com.soulter.goastjforandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;

public class MainActivity extends AppCompatActivity {


    private MyReceiver myReceiver;
    private TextView okok;
    private EditText inputOrder;
    private Button sendOrder;

    Intent mIntent;

    String msgAll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        //注册广播
        registerReceiver(myReceiver, intentFilter);

        okok = findViewById(R.id.okok);
        okok.setMovementMethod(ScrollingMovementMethod.getInstance());//滑动
        inputOrder = findViewById(R.id.input_order);
        sendOrder = findViewById(R.id.send_order);

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
                                okok.setText(msgAll);
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


    }


    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    String msg = intent.getStringExtra(ConnService.COUNTER);
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
                            Log.v("TAG","msgAll:"+msgAll);
                            okok.setText(""+msgAll);
                            Log.v("TAG","ok:msgall");
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

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        try {
            SocketManager.socket.close();
        }catch (Exception e){
            e.getStackTrace();
        }
        super.onPause();
    }
}

