package com.soulter.goastjforandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.soulter.goastjforandroid.R;
import com.soulter.goastjforandroid.util.SocketManager;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {


    static String INTENT_EXTRA = "login_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        final EditText ipEditText = findViewById(R.id.userip);
        final EditText portEditText = findViewById(R.id.userport);
        final EditText passwordEditText = findViewById(R.id.password);

        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final TextView loginStatus = findViewById(R.id.login_status);


        //initial loading
        loadingProgressBar.setVisibility(View.GONE);


        final SharedPreferences spfs = this.getSharedPreferences("spfs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = this.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();

        ipEditText.setText(spfs.getString("ip",""));
        portEditText.setText(spfs.getString("port",""));
        passwordEditText.setText(spfs.getString("psw",""));

        String loginStatusStr = getIntent().getStringExtra(INTENT_EXTRA);
        if (loginStatusStr != null){
            loginStatus.setText(loginStatusStr);
        }



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                final String ip = ipEditText.getText().toString();
                final String port = portEditText.getText().toString();
                final String psw = passwordEditText.getText().toString();

                editor.putString("ip",ip);
                editor.apply();
                editor.putString("port",port);
                editor.apply();
                editor.putString("psw",psw);
                editor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.v("TAG", "link start" + ip + port + psw);

                            SocketManager.socket = new Socket(ip, Integer.parseInt(port));
                            SocketManager.inputStreamReader = new InputStreamReader(SocketManager.socket.getInputStream(), "GBK");
                            SocketManager.bufferedWriter = new BufferedWriter(new OutputStreamWriter(SocketManager.socket.getOutputStream(), "GBK"));

                            Log.v("TAG", "link success" + ip + port + psw);

                            loginStatus.post(new Runnable() {
                                @Override
                                public void run() {
                                    loginStatus.setText("连接成功");
                                }
                            });
                            //发送密码
                            SocketManager.bufferedWriter.write("#pw "+psw);
                            SocketManager.bufferedWriter.newLine();
                            SocketManager.bufferedWriter.flush();
                            Thread.sleep(350);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }).start();


            }
        });

    }
}
