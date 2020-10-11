package com.soulter.goastjforandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnService extends Service {

    public static final String COUNTER = "msg";
    public static final String CODE = "codeID";
    public static final String COUNTER_CLIENT_NAME = "count_client_name";
    public static final String COUNTER_CLIENT_NUM = "count_client_num";
    public static final String COUNTER_FOCUSING = "count_focusing";
    public static final String ACTION_NAME = " com.soulter.goastjforandroid.ConnService.COUNTER_ACTION";




    private ArrayList<String> clientsName = new ArrayList<>();
    private ArrayList<String> clientsNum = new ArrayList<>();
    private String focusingClient = "";


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
                    char[] chs;
                    while (loopagain == 0) {
                        chs = new char[12000];


                        try{

                            c=SocketManager.inputStreamReader.read(chs);


                            Log.v("char length:","   "+chs.length +"  c:  "+c);
                            Log.v("char[] display:","   "+ Arrays.toString(chs));

                            String s = new String(chs,0,c);  //得到原data
                            String result = s;


                            //Clients
                            if (result.contains("!clients ")){
                                //检测到Client数据
                                Log.v("clientsData","appear. result:"+result);
                                try{
                                    String clientData = result.substring(result.indexOf("!clients ") + 1, result.lastIndexOf("!"));
                                    Log.v("clientsData-get","get:"+clientData);
                                    clientsName.clear();
                                    clientsNum.clear();

                                    String[] clientList = clientData.split(" "); //未经加工的ClientData
                                    for (int i = 0; i < clientList.length/6 ; i++){
                                        clientsName.add(clientList[i*6+2]);
                                        clientsNum.add(clientList[i*6+1]);
                                        if (clientList[i*6+4].equals("true")){
                                            focusingClient = clientList[i*6+2];
                                            Log.v("clientsData-focusing","get:"+focusingClient);
                                        }

//                                                ClientsField clientsField = new ClientsField(clientList[i*6],clientList[i*6+1]);
//                                                Log.v("testing",clientList[i*6+1]+"  "+clientList[i*6+2] );
//                                                clientsFields.add(clientsField);
                                    }
                                    bridgeForActivity("",3); //3 是发送心跳给Mainactivity，告知它有了新的client变动
                                }catch (Exception e){
                                    e.printStackTrace();
                                }


                            }


                            Pattern pattern = Pattern.compile("\\!.*\\!");
                            Matcher macher = pattern.matcher(s);

//                            Pattern pattern2 = Pattern.compile("\\!client.*\\!");
//                            Matcher macher2 = pattern2.matcher(s);
//                            Log.v("test:",macher2.group());

                            Log.v("result0",result);
                            if (macher.find()){
                                String macherText = macher.group();
                                Log.v("tag1",macherText);
                                switch (macherText){
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
                                        Log.v("defalt","defalt");
                                        result = macher.replaceAll("");  // REPLACE
                                        break;
                                    }

                                }

                            }



                            if(result.contains("!reDir ")){
//                                Log.v("onCheckingRedir","yes： "+result);
                                bridgeForActivity(result,4);
                            }

                            if (!result.equals("") || result.length() > 1){
                                bridgeForActivity(result,0);
                            }


                            Pattern patternUrl = Pattern.compile("http://[^\\s]*",Pattern.MULTILINE|Pattern.DOTALL);
                            final Matcher macherUrl = patternUrl.matcher(s);
                            while (macherUrl.find()){
//                                Log.v("abc",macherUrl.group());
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
                        chs = null;
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
        Intent intent = new Intent();
        intent.setAction(ACTION_NAME);
        if (code == 3){ // clientsInfo
            intent.putExtra(CODE,code);
            intent.putExtra(COUNTER_CLIENT_NAME,clientsName);
            intent.putExtra(COUNTER_CLIENT_NUM,clientsNum);
            intent.putExtra(COUNTER_FOCUSING,focusingClient);
            sendBroadcast(intent);
        }else{
            intent.putExtra(COUNTER,msg);
            intent.putExtra(CODE,code);
            sendBroadcast(intent);
        }

    }




}
