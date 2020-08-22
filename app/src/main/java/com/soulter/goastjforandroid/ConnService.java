package com.soulter.goastjforandroid;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import android.app.AlertDialog;


import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnService extends Service {

    public static final String COUNTER = "msg";
    public static final String CODE = "codeID";
    public static final String ACTION_NAME = " com.soulter.goastjforandroid.ConnService.COUNTER_ACTION";

    InputStream inputStream = null;


    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                int c;
                try {
                    int loopagain = 0;
                    char[] chs = new char[1024];
                    while (loopagain == 0) {
                        try{
                            c=SocketManager.inputStreamReader.read(chs);

                            String s = new String(chs,0,c);  //得到原data
//                            s = s.replaceAll("\r|\n","&enter&"); //去除换行
                            Log.v("tag",s);
                            String result = s;
                            Pattern pattern = Pattern.compile("\\!.*\\!",Pattern.MULTILINE|Pattern.DOTALL);
                            Matcher macher = pattern.matcher(s);
                            if (macher.find()){
                                Log.v("tag1",macher.group());
                                switch (macher.group()){
                                    case "!alivem!":{
                                        SocketManager.bufferedWriter.write("#alivem#");
                                        SocketManager.bufferedWriter.newLine();
                                        SocketManager.bufferedWriter.flush();
                                        result = macher.replaceAll("");  // REPLACE
                                        break;
                                    }
                                    case "!passErr!":{
                                        result = macher.replaceAll("");  // REPLACE
                                        loopagain = 1;
                                        bridgeForActivity("密码错误",1);
                                    }
                                    case "!relogin!":{
                                        result = macher.replaceAll("");  // REPLACE
                                        loopagain = 1;
                                        bridgeForActivity("被强制下线",1);
                                    }

                                    case "!finish!":{
                                        result = macher.replaceAll("");  // REPLACE

                                    }
                                    default:{
                                        result = macher.replaceAll("");  // REPLACE
                                        break;
                                    }

                                }

                            }

                            if (!result.equals("") || result.length() > 1){
                                bridgeForActivity(result,0);
                            }


                            Pattern patternUrl = Pattern.compile("http://[^\\s]*",Pattern.MULTILINE|Pattern.DOTALL);
                            final Matcher macherUrl = patternUrl.matcher(s);
                            while (macherUrl.find()){
                                Log.v("abc",macherUrl.group());
                                bridgeForActivity(macherUrl.group(),2);
                            }


                        }catch (Exception e){
//                            Out.say("HandleConn","接受数据出错，连接正在重置");
                            e.printStackTrace();
                            bridgeForActivity("连接已断开",1);
                            loopagain = 1;
                            Thread.interrupted();
                        }

                        if (loopagain == 1){
                            Log.v("TAG","nomoreagain");
                            break;
                        }
                    }
                }catch (Exception e){
//                    Out.say("HandleConn","处理连接数据时出错 非致命错误");
                    e.printStackTrace();
                }
            }
        }).start();


        return super.onStartCommand(intent, flags, startId);

    }

    public void bridgeForActivity(String msg,int code){
        Log.v("TAG","bfa:"+msg);
        Intent intent = new Intent();
        intent.setAction(ACTION_NAME);
        intent.putExtra(COUNTER,msg);
        intent.putExtra(CODE,code);
        sendBroadcast(intent);
    }




}
